package av.netty;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;

import av.action.ActionPool;
import av.nado.remote.RemoteIp;
import av.nado.util.Check;
import av.nado.util.JsonUtil;
import av.util.exception.AException;
import av.util.trace.Trace;

public class NettyChannelInfo
{
    private static Logger            logger                 = LogManager.getLogger(NettyChannelInfo.class);
    
    public static final int          KEY_TIME_TO_DROP_WORDS = 5000;
    public static final int          KEY_TIME_TO_REDO_SEND  = 30000;
    public static final int          KEY_TIME_TO_RESENT     = 10000;
    public static final int          KEY_TIME_SEND_WATE     = 100;
    
    private static NettyManager      netty                  = NettyManager.instance();
    
    private boolean                  isServer;
    private RemoteIp                 ip;
    private Channel                  channel;
    private Deque<String>            recvs                  = new ConcurrentLinkedDeque<String>();
    private Map<Long, NettySendInfo> mapPost                = new ConcurrentHashMap<Long, NettySendInfo>();
    private Map<Long, NettySendInfo> mapSent                = new ConcurrentHashMap<Long, NettySendInfo>();
    
    private String                   readBuf                = "";
    private long                     lastCanbeJsonTime      = 0;
    
    private ReceiveAction            action                 = new ReceiveAction();
    
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
    
    public NettyWrap sendMessage(NettySendInfo info)
    {
        info.setPost(false);
        long seq = postMessage(info);
        if (seq < 0)
        {
            return null;
        }
        
        NettySendInfo sndInfo = null;
        long startListenTime = System.currentTimeMillis();
        
        while (true)
        {
            synchronized (mapSent)
            {
                try
                {
                    mapSent.wait(KEY_TIME_SEND_WATE);
                }
                catch (InterruptedException e)
                {
                    logger.catching(e);
                }
                
                sndInfo = mapSent.get(seq);
                if (sndInfo == null)
                {
                    Trace.debug("do not found send seq: {} info", seq);
                    return null;
                }
                
                if (sndInfo.getRecv() == null)
                {
                    if (System.currentTimeMillis() - startListenTime + KEY_TIME_SEND_WATE >= KEY_TIME_TO_RESENT)
                    {
                        postBaseMessage(sndInfo, true);
                    }
                    
                    continue;
                }
                
                mapSent.remove(seq);
                break;
            }
        }
        
        return sndInfo.getRecv();
    }
    
    public long postMessage(NettySendInfo info)
    {
        Trace.print("ip: {} post command: {} message: {}", ip, info.getWrap().getCommand(), info.getWrap().getMsg());
        return postBaseMessage(info, false);
    }
    
    public void recieve(String msg)
    {
        if (Check.IfOneEmpty(msg))
        {
            return;
        }
        
        this.recvs.addLast(msg);
        synchronized (action)
        {
            if (!action.isFinished())
            {
                return;
            }
            
            try
            {
                ActionPool.instance().addAction(action, this);
            }
            catch (AException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    protected void analysisJson() throws AException
    {
        if (this.recvs.isEmpty() && Check.IfOneEmpty(readBuf))
        {
            return;
        }
        
        while (true)
        {
            if (Check.IfOneEmpty(readBuf))
            {
                readBuf = this.recvs.pop();
            }
            
            if (Check.IfOneEmpty(readBuf))
            {
                break;
            }
            
            readBuf = readJson(readBuf);
            if (this.recvs.isEmpty())
            {
                break;
            }
            
            readBuf = new StringBuilder(readBuf).append(this.recvs.pop()).toString();
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
    
    public long postBaseMessage(NettySendInfo info, boolean redo)
    {
        if (channel == null)
        {
            return -1;
        }
        
        long seq = info.getWrap().getSeq();
        // Trace.print("ip: {} post msg seq: {}", ip, seq);
        
        if (!channel.isConnected() || !channel.isOpen())
        {
            Trace.debug("channel: {} is closed...", ip);
            return -1;
        }
        
        info.setSentTime(System.currentTimeMillis());
        info.setSendCount(info.getSendCount() + 1);
        
        channel.write(info.getJson());
        
        if (redo || isServer)
        {
            return seq;
        }
        
        mapPost.put(seq, info);
        if (!info.isPost())
        {
            synchronized (mapSent)
            {
                mapSent.put(seq, info);
            }
        }
        return seq;
    }
    
    private String readJson(String in) throws AException
    {
        String buffer = in;
        while (true)
        {
            if (Check.IfOneEmpty(buffer))
            {
                break;
            }
            
            String json = JsonUtil.readJsonString(buffer);
            if (!Check.IfOneEmpty(json))
            {
                // can explain to json
                lastCanbeJsonTime = System.currentTimeMillis();
                buffer = buffer.substring(json.length());
                onReceiveMessage(json);
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
                continue;
            }
            
            return readJson(buffer.substring(1));
        }
        
        return buffer;
    }
    
    private void onReceiveMessage(String json) throws AException
    {
        if (Check.IfOneEmpty(json))
        {
            return;
        }
        
        Trace.print("receive json: {}", json);
        
        NettyWrap wrap = JsonUtil.toObject(NettyWrap.class, json);
        if (wrap == null)
        {
            return;
        }
        
        if (isServer)
        {
            netty.onReceiveMessage(this, wrap, null);
            return;
        }
        
        NettySendInfo info = onReceiveAck(wrap);
        if (info != null && !info.isPost())
        {
            // is send routine, do not on receive message
            return;
        }
        
        // post routine
        netty.onReceiveMessage(this, wrap, info);
    }
    
    private NettySendInfo onReceiveAck(NettyWrap wrap)
    {
        long seq = wrap.getSeq();
        NettySendInfo info = mapPost.remove(seq);
        if (info == null)
        {
            return null;
        }
        // Trace.print("client rcv ack seq: {}, left size: {}", seq,
        // mapPost.size());
        
        if (info.isPost())
        {
            return info;
        }
        
        info.setRecv(wrap);
        synchronized (mapSent)
        {
            mapSent.notifyAll();
        }
        return info;
    }
}
