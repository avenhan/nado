package av.netty;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;

import av.nado.remote.RemoteIp;
import av.nado.util.Aggregate;
import av.nado.util.Check;
import av.nado.util.JsonUtil;
import av.util.exception.AException;
import av.util.trace.FunctionTime;
import av.util.trace.Trace;

public class NettyChannelInfo
{
    private static Logger            logger                 = LogManager.getLogger(NettyChannelInfo.class);
    
    public static final int          KEY_TIME_TO_DROP_WORDS = 500000;
    public static final int          KEY_TIME_TO_REDO_SEND  = 300000;
    public static final int          KEY_TIME_TO_RESENT     = 100000;
    public static final int          KEY_TIME_SEND_WATE     = 100;
    
    private static NettyManager      netty                  = NettyManager.instance();
    
    private boolean                  isServer;
    private RemoteIp                 ip;
    private Channel                  channel;
    private Deque<String>            recvs                  = new ConcurrentLinkedDeque<String>();
    private Map<Long, NettySendInfo> mapPost                = new ConcurrentHashMap<Long, NettySendInfo>();
    
    private Map<Long, Boolean>       mapRecvSeq             = new ConcurrentHashMap<Long, Boolean>();
    private AtomicLong               lastBeginSequence      = new AtomicLong(0);
    
    private String                   readBuf                = "";
    private long                     lastCanbeJsonTime      = 0;
    
    public boolean isServer()
    {
        return isServer;
    }
    
    public void setServer(boolean isServer)
    {
        this.isServer = isServer;
    }
    
    public RemoteIp getIp()
    {
        return ip;
    }
    
    public void setIp(RemoteIp ip)
    {
        this.ip = ip;
    }
    
    public Channel getChannel()
    {
        return channel;
    }
    
    public void setChannel(Channel channel)
    {
        this.channel = channel;
    }
    
    public Deque<String> getRecvs()
    {
        return recvs;
    }
    
    public void setRecvs(Deque<String> recvs)
    {
        this.recvs = recvs;
    }
    
    public NettyWrap sendMessage(NettySendInfo info) throws AException
    {
        createSyncObject(info);
        long seq = postMessage(info);
        if (seq < 0)
        {
            return null;
        }
        
        long startListenTime = System.currentTimeMillis();
        while (true)
        {
            if (info.getRecv() != null)
            {
                break;
            }
            
            syncObject(info);
            
            if (info.getRecv() != null)
            {
                break;
            }
            
            if (System.currentTimeMillis() - startListenTime + KEY_TIME_SEND_WATE >= KEY_TIME_TO_RESENT)
            {
                postBaseMessage(info, true);
            }
        }
        
        return info.getRecv();
    }
    
    public long postMessage(NettySendInfo info)
    {
        // Trace.print("ip: {} post command: {} len: {} message: {}", ip,
        // info.getWrap().getCommand(), info.getJson().length(),
        // info.getWrap().getMsg());
        return postBaseMessage(info, false);
    }
    
    public void recieve(String msg)
    {
        if (Check.IfOneEmpty(msg))
        {
            return;
        }
        
        this.recvs.addLast(msg);
        try
        {
            analysisJson();
        }
        catch (AException e)
        {
            logger.catching(e);
        }
    }
    
    protected void analysisJson() throws AException
    {
        if (this.recvs.isEmpty() && Check.IfOneEmpty(readBuf))
        {
            return;
        }
        
        FunctionTime functionTime = new FunctionTime();
        functionTime.add("ip", ip.toString());
        
        try
        {
            while (true)
            {
                if (Check.IfOneEmpty(readBuf))
                {
                    readBuf = this.recvs.pop();
                }
                
                functionTime.addCurrentTime("pop");
                if (Check.IfOneEmpty(readBuf))
                {
                    break;
                }
                
                readBuf = readJson(readBuf);
                functionTime.addCurrentTime("read");
                if (this.recvs.isEmpty())
                {
                    break;
                }
                
                readBuf = new StringBuilder(readBuf).append(this.recvs.pop()).toString();
                functionTime.addCurrentTime("add");
            }
        }
        finally
        {
            functionTime.print();
        }
    }
    
    protected void redoPostMessage() throws AException
    {
        if (isServer)
        {
            return;
        }
        
        if (mapPost.isEmpty())
        {
            return;
        }
        
        if (!channel.isConnected() || !channel.isOpen())
        {
            Trace.debug("channel: {} is closed...", ip);
            return;
        }
        
        for (Map.Entry<Long, NettySendInfo> entry : mapPost.entrySet())
        {
            NettySendInfo sendInfo = entry.getValue();
            if (System.currentTimeMillis() - sendInfo.getSentTime() < KEY_TIME_TO_REDO_SEND)
            {
                continue;
            }
            
            postBaseMessage(sendInfo, true);
        }
    }
    
    protected void removeSafeSequence()
    {
        if (!isServer)
        {
            return;
        }
        
        FunctionTime functionTime = new FunctionTime();
        try
        {
            if (mapRecvSeq.isEmpty())
            {
                return;
            }
            
            while (true)
            {
                long lastSequence = lastBeginSequence.get();
                if (!mapRecvSeq.containsKey(lastSequence++))
                {
                    break;
                }
                
                mapRecvSeq.remove(lastSequence - 1);
                Trace.print("remove safe sequence: {} current time: {}", lastSequence - 1, System.currentTimeMillis());
                lastBeginSequence.getAndIncrement();
            }
        }
        finally
        {
            functionTime.print();
        }
    }
    
    private long postBaseMessage(NettySendInfo info, boolean redo)
    {
        if (channel == null)
        {
            return -1;
        }
        
        long seq = info.getWrap().getSeq();
        Trace.print("ip: {} post msg seq: {} is post: {} time waste: {}ms current time: {}", ip, seq, info.isPost(),
                System.currentTimeMillis() - info.getWrap().getTimestamp(), System.currentTimeMillis());
        
        if (!channel.isConnected() || !channel.isOpen())
        {
            Trace.debug("channel: {} is closed...", ip);
            return -1;
        }
        
        info.setSentTime(System.currentTimeMillis());
        info.setSendCount(info.getSendCount() + 1);
        
        if (redo || isServer)
        {
            channel.write(info.getJson());
            return seq;
        }
        
        mapPost.put(seq, info);
        channel.write(info.getJson());
        return seq;
    }
    
    private String readJson(String in) throws AException
    {
        // logger.debug("####### ip: {} in: {}, current time: {}", ip, in,
        // System.currentTimeMillis());
        
        FunctionTime functionTime = new FunctionTime();
        functionTime.add("ip", ip.toString());
        try
        {
            String buffer = in;
            while (true)
            {
                if (Check.IfOneEmpty(buffer))
                {
                    break;
                }
                
                Aggregate<Integer, String> aggregate = NettySignature.getValue(buffer);
                String json = aggregate.getSecond();
                functionTime.addCurrentTime("get json");
                if (aggregate.getFirst() > -1 && !Check.IfOneEmpty(json))
                {
                    // can explain to json
                    lastCanbeJsonTime = System.currentTimeMillis();
                    buffer = buffer.substring(aggregate.getFirst() + json.length());
                    // Trace.print("ip: {} get one json: [{}] left: [{}] current
                    // time: {}", ip, json, buffer, System.currentTimeMillis());
                    onReceiveMessage(json);
                    functionTime.addCurrentTime("recv json");
                    continue;
                }
                
                if (lastCanbeJsonTime == 0)
                {
                    // first not can explain time
                    lastCanbeJsonTime = System.currentTimeMillis();
                    continue;
                }
                
                if (System.currentTimeMillis() - lastCanbeJsonTime < KEY_TIME_TO_DROP_WORDS)
                {
                    // can not read json and do not trigger the drop words
                    // routine,
                    // so break
                    break;
                }
                
                Trace.print("unkown json check....{}", buffer);
                return readJson(buffer.substring(1));
            }
            
            return buffer;
        }
        finally
        {
            functionTime.print();
        }
    }
    
    private void onReceiveMessage(String json) throws AException
    {
        if (Check.IfOneEmpty(json))
        {
            return;
        }
        
        FunctionTime time = new FunctionTime();
        time.add("ip", ip.toString());
        
        try
        {
            NettyWrap wrap = JsonUtil.toObject(NettyWrap.class, json);
            if (wrap == null)
            {
                return;
            }
            time.addCurrentTime("seq[{}] from json", wrap.getSeq());
            Trace.print("seq: {} receive time: {}ms", wrap.getSeq(), System.currentTimeMillis() - wrap.getTimestamp());
            
            if (isServer)
            {
                time.addCurrentTime("is valid seq0");
                if (!isValidServerSequence(wrap))
                {
                    return;
                }
                
                time.addCurrentTime("is valid seq1");
                netty.onReceiveMessage(this, wrap, null);
                time.addCurrentTime("netty");
                return;
            }
            
            NettySendInfo info = onReceiveAck(wrap);
            time.addCurrentTime("check ack");
            if (info != null && !info.isPost())
            {
                time.addCurrentTime("send ack");
                // is send routine, do not on receive message
                return;
            }
            
            // post routine
            netty.onReceiveMessage(this, wrap, info);
            time.addCurrentTime("netty");
        }
        finally
        {
            time.print();
        }
    }
    
    private NettySendInfo onReceiveAck(NettyWrap wrap)
    {
        FunctionTime functionTime = new FunctionTime();
        try
        {
            long seq = wrap.getSeq();
            functionTime.addCurrentTime("seq[{}] time[{}]ms", wrap.getSeq(), System.currentTimeMillis() - wrap.getTimestamp());
            
            NettySendInfo info = mapPost.remove(seq);
            functionTime.addCurrentTime("remove seq");
            if (info == null)
            {
                return null;
            }
            
            if (info.isPost())
            {
                return info;
            }
            
            info.setRecv(wrap);
            functionTime.addCurrentTime("before notify");
            
            notifyObject(info);
            return info;
        }
        finally
        {
            functionTime.print();
        }
    }
    
    private void createSyncObject(NettySendInfo info)
    {
        CountDownLatch objFire = new CountDownLatch(1);
        info.setObjFire(objFire);
    }
    
    private void syncObject(NettySendInfo info)
    {
        Object objFire = info.getObjFire();
        if (objFire == null)
        {
            return;
        }
        
        CountDownLatch countDownLatch = (CountDownLatch) objFire;
        try
        {
            countDownLatch.await(KEY_TIME_SEND_WATE, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            logger.catching(e);
        }
    }
    
    private void notifyObject(NettySendInfo info)
    {
        Object objFire = info.getObjFire();
        if (objFire == null)
        {
            return;
        }
        
        CountDownLatch countDownLatch = (CountDownLatch) objFire;
        countDownLatch.countDown();
    }
    
    private boolean isValidServerSequence(NettyWrap wrap)
    {
        FunctionTime functionTime = new FunctionTime();
        try
        {
            long recvSeq = wrap.getSeq();
            if (recvSeq < lastBeginSequence.get())
            {
                // small than first sequence
                return false;
            }
            
            if (mapRecvSeq.containsKey(recvSeq))
            {
                // sequence is already worked
                return false;
            }
            
            mapRecvSeq.put(recvSeq, true);
            return true;
        }
        finally
        {
            functionTime.print();
        }
    }
}
