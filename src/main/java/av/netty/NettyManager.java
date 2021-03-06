package av.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import av.nado.network.netty.NadoTestController;
import av.nado.remote.NadoParam;
import av.nado.remote.RemoteIp;
import av.nado.util.Check;
import av.nado.util.JsonUtil;
import av.sequence.Sequence;
import av.util.exception.AException;
import av.util.trace.FunctionTime;
import av.util.trace.Trace;

public class NettyManager
{
    public static final int                   MAX_THREAD_COUNT = 100;
    public static final int                   MAX_CACHED_SIZE  = 1024;
    
    private static NettyManager               m_pThis;
    private ScheduledExecutorService          m_schedule;
    private Map<RemoteIp, NettyChannelInfo>   m_mapChannels    = new ConcurrentHashMap<RemoteIp, NettyChannelInfo>();
    private NettySendRedo                     m_sendRedo       = new NettySendRedo();
    private int                               m_maxThreadCount = MAX_THREAD_COUNT;
    
    private Map<Class<?>, NettyController<?>> m_mapController  = new HashMap<Class<?>, NettyController<?>>();
    
    /**
     * ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long
     * keepAliveTime, TimeUnit unit, BlockingQueue workQueue,
     * RejectedExecutionHandler handler) corePoolSize： 线程池维护线程的最少数量
     * maximumPoolSize：线程池维护线程的最大数量 keepAliveTime： 线程池维护线程所允许的空闲时间 unit：
     * 线程池维护线程所允许的空闲时间的单位 workQueue： 线程池所使用的缓冲队列 handler： 线程池对拒绝任务的处理策略
     * 
     * 当一个任务通过execute(Runnable)方法欲添加到线程池时：
     * 
     * 如果此时线程池中的数量小于corePoolSize，即使线程池中的线程都处于空闲状态，也要创建新的线程来处理被添加的任务。
     * 如果此时线程池中的数量等于 corePoolSize，但是缓冲队列 workQueue未满，那么任务被放入缓冲队列。
     * 如果此时线程池中的数量大于corePoolSize，缓冲队列workQueue满，并且线程池中的数量小于maximumPoolSize，
     * 建新的线程来处理被添加的任务。
     * 如果此时线程池中的数量大于corePoolSize，缓冲队列workQueue满，并且线程池中的数量等于maximumPoolSize，那么通过
     * handler所指定的策略来处理此任务。
     * 
     */
    private ThreadPoolExecutor                m_threadPool     = null;
    
    private NettyManager()
    {
        m_schedule = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        
        m_sendRedo.setTimeoutTime(NettyChannelInfo.KEY_TIME_TO_REDO_SEND);
        m_schedule.scheduleWithFixedDelay(m_sendRedo, 100, m_sendRedo.getTimeoutTime(), TimeUnit.MILLISECONDS);
    }
    
    public static NettyManager instance()
    {
        if (m_pThis == null)
        {
            m_pThis = new NettyManager();
        }
        
        return m_pThis;
    }
    
    public void startServer(int port, int maxThreadCount)
    {
        int coreCount = Runtime.getRuntime().availableProcessors() * 2;
        if (maxThreadCount < 1)
        {
            m_maxThreadCount = MAX_THREAD_COUNT;
        }
        else if (maxThreadCount > 0 && maxThreadCount < coreCount)
        {
            m_maxThreadCount = coreCount;
        }
        else
        {
            m_maxThreadCount = maxThreadCount;
        }
        
        m_threadPool = new ThreadPoolExecutor(coreCount, m_maxThreadCount, 1000, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(MAX_CACHED_SIZE), new ThreadPoolExecutor.CallerRunsPolicy());
        
        ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), coreCount);
        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        
        bootstrap.setPipelineFactory(new ChannelPipelineFactory()
        {
            public ChannelPipeline getPipeline()
            {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("encode", new StringEncoder());
                pipeline.addLast("decode", new StringDecoder());
                pipeline.addLast("handler", new NadoServerHandler());
                return pipeline;
            }
        });
        
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("reuseAddress", true);
        
        bootstrap.bind(new InetSocketAddress(port));
    }
    
    public void startClient(final RemoteIp ip)
    {
        if (m_threadPool == null)
        {
            int coreCount = Runtime.getRuntime().availableProcessors() + 1;
            m_threadPool = new ThreadPoolExecutor(coreCount, coreCount, 1000, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<Runnable>(MAX_CACHED_SIZE), new ThreadPoolExecutor.CallerRunsPolicy());
        }
        
        ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory()
        {
            public ChannelPipeline getPipeline()
            {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("encode", new StringEncoder());
                pipeline.addLast("decode", new StringDecoder());
                pipeline.addLast("handler", new NadoClientHandler(ip));
                return pipeline;
            }
        });
        
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(4096));
        
        bootstrap.connect(new InetSocketAddress(ip.getIp(), ip.getPort()));
    }
    
    public <T, C extends NettyController<T>> void bind(Class<T> type, Class<C> controllerType) throws AException
    {
        try
        {
            NettyController<T> controller = controllerType.newInstance();
            m_mapController.put(type, controller);
        }
        catch (InstantiationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_SERVER, e);
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_SERVER, e);
        }
    }
    
    public long post(RemoteIp ip, Object msg) throws AException
    {
        if (Check.IfOneEmpty(ip, msg))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter to send from netty");
        }
        
        NettyChannelInfo info = m_mapChannels.get(ip);
        return post(info, msg);
    }
    
    protected long post(NettyChannelInfo info, Object msg) throws AException
    {
        NettyWrap wrap = new NettyWrap();
        wrap.setSeq(Sequence.getSequence());
        wrap.setMsg(NadoParam.toExplain(msg));
        wrap.setCommand(Command.NC_USER);
        wrap.setTimestamp(Trace.getCurrentTime());
        
        return post(info, wrap);
    }
    
    public long post(NettyChannelInfo info, NettyWrap wrap) throws AException
    {
        if (Check.IfOneEmpty(info, wrap))
        {
            throw new AException(AException.ERR_SERVER, "channel info or wrap is null");
        }
        
        try
        {
            String json = JsonUtil.toJson(wrap);
            
            NettySendInfo sent = new NettySendInfo();
            sent.setCreateTime(System.currentTimeMillis());
            sent.setJson(NettySignature.signature(json));
            sent.setSendCount(0);
            sent.setSentTime(0);
            sent.setWrap(wrap);
            
            long seq = info.postMessage(sent);
            return seq;
        }
        finally
        {
        }
    }
    
    public Object send(RemoteIp ip, Object msg) throws AException
    {
        if (Check.IfOneEmpty(ip, msg))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter to send from netty");
        }
        
        NettyChannelInfo info = m_mapChannels.get(ip);
        return send(info, msg);
    }
    
    protected Object send(NettyChannelInfo info, Object msg) throws AException
    {
        NettyWrap wrap = new NettyWrap();
        wrap.setSeq(Sequence.getSequence());
        wrap.setMsg(NadoParam.toExplain(msg));
        wrap.setCommand(Command.NC_USER);
        wrap.setTimestamp(Trace.getCurrentTime());
        
        NettyWrap ret = send(info, wrap);
        if (ret == null)
        {
            return null;
        }
        
        String retBodyExplain = ret.getMsg();
        Object retObject = NadoParam.fromExplain(retBodyExplain);
        return retObject;
    }
    
    public NettyWrap send(NettyChannelInfo info, NettyWrap wrap) throws AException
    {
        if (Check.IfOneEmpty(info, wrap))
        {
            throw new AException(AException.ERR_SERVER, "channel info or wrap is null");
        }
        
        FunctionTime time = new FunctionTime(false);
        time.add("ip", info.getIp().toString());
        time.add("seq", wrap.getSeq());
        
        try
        {
            String json = JsonUtil.toJson(wrap);
            time.addCurrentTime("json");
            
            NettySendInfo sent = new NettySendInfo();
            sent.setCreateTime(Trace.getCurrentTime());
            sent.setJson(NettySignature.signature(json));
            sent.setSendCount(0);
            sent.setSentTime(0);
            sent.setWrap(wrap);
            time.addCurrentTime("sign");
            
            NettyWrap ret = info.sendMessage(sent);
            time.addCurrentTime("send");
            if (ret == null)
            {
                return null;
            }
            
            // Trace.print(".........send seq: {} receive seq: {} time waste:
            // {}ms", wrap.getSeq(), ret.getSeq(),
            // Trace.getWaste(sent.getCreateTime()));
            return ret;
        }
        finally
        {
            time.print();
        }
    }
    
    public boolean isValid(RemoteIp addr)
    {
        NettyChannelInfo info = m_mapChannels.get(addr);
        if (info == null)
        {
            return false;
        }
        
        Channel channel = info.getChannel();
        if (channel == null)
        {
            return false;
        }
        
        if (channel.isConnected())
        {
            return true;
        }
        
        if (channel.isOpen())
        {
            return true;
        }
        
        return false;
    }
    
    protected Map<RemoteIp, NettyChannelInfo> getChannelMap()
    {
        return m_mapChannels;
    }
    
    /***
     * 
     * @param ip
     * @param channel
     * @param isServer:
     *            判断是否是server方，客户的链接到远端服务，则 false， 服务器接收到客户段链接，则是true
     * @return
     */
    protected NettyChannelInfo addConnected(RemoteIp ip, Channel channel, boolean isServer)
    {
        NettyChannelInfo info = new NettyChannelInfo();
        info.setIp(ip);
        info.setChannel(channel);
        
        m_mapChannels.put(ip, info);
        return info;
    }
    
    protected NettyChannelInfo addConnected(SocketAddress addr, Channel channel, boolean isServer)
    {
        RemoteIp ip = RemoteIp.getRemoteIp(addr);
        NettyChannelInfo info = new NettyChannelInfo();
        info.setIp(ip);
        info.setChannel(channel);
        info.setServer(isServer);
        
        m_mapChannels.put(RemoteIp.getRemoteIp(addr), info);
        return info;
    }
    
    protected void removeServer(RemoteIp ip)
    {
        m_mapChannels.remove(ip);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void onReceiveMessage(NettyChannelInfo info, NettyWrap wrap, NettySendInfo sendInfo)
    {
        FunctionTime functionTime = new FunctionTime(false);
        functionTime.add("seq", wrap.getSeq());
        try
        {
            Object objParam = NadoParam.fromExplain(wrap.getMsg());
            functionTime.addCurrentTime("param");
            NettyController<?> controller = m_mapController.get(objParam.getClass());
            functionTime.addCurrentTime("controller");
            
            if (controller == null)
            {
                boolean isPost = true;
                if (sendInfo != null && sendInfo.isPost())
                {
                    isPost = sendInfo.isPost();
                }
                
                throw new AException(AException.ERR_NOT_FOUND, "ip: {} not found controller type: {} seq:{} is post: {}", info.getIp(),
                        objParam.getClass().getName(), wrap.getSeq(), isPost);
            }
            
            // NettyAction action = new NettyAction();
            // action.setInfo(info);
            // action.setSendInfo(sendInfo);
            // action.setWrap(wrap);
            // action.setController(controller);
            // action.setObjParam(objParam);
            //
            // functionTime.addCurrentTime("action");
            // Trace.print("seq: {} receive and will do action at time: {}ms",
            // wrap.getSeq(), System.currentTimeMillis() - wrap.getTimestamp());
            // m_threadPool.execute(action);
            // action.run();
            
            doAction(controller, objParam.getClass(), info, wrap);
            functionTime.addCurrentTime("do action");
        }
        catch (Exception e)
        {
            sendErrorToClient(info, wrap, e);
        }
        finally
        {
            functionTime.print();
        }
    }
    
    private <T> void doAction(NettyController<?> controller, Object objParam, NettyChannelInfo info, NettyWrap wrap)
    {
        FunctionTime time = new FunctionTime(false);
        time.add("ip", info.getIp().toString());
        time.add("seq", wrap.getSeq());
        
        Object retObject = null;
        try
        {
            retObject = controller.receive(null);
            time.addCurrentTime("invoke");
        }
        catch (Exception e)
        {
            retObject = e;
        }
        
        try
        {
            wrap.setMsg(NadoParam.toExplain(retObject));
            time.addCurrentTime("to msg");
            if (info.isServer())
            {
                post(info, wrap);
                time.addCurrentTime("post");
            }
        }
        catch (AException e)
        {
        }
        
        time.print();
    }
    
    private void sendErrorToClient(NettyChannelInfo info, NettyWrap wrap, Exception e)
    {
        if (!info.isServer())
        {
            return;
        }
        
        try
        {
            wrap.setCommand(Command.NC_ERROR);
            wrap.setMsg(NadoParam.toExplain(e));
        }
        catch (AException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        try
        {
            post(info, wrap);
        }
        catch (AException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    
    public static void main(String[] arg) throws Exception
    {
        final int port = 9090;
        NettyManager.instance().startServer(port, 0);
        NettyManager.instance().bind(String.class, NadoTestController.class);
        NettyManager.instance().startClient(RemoteIp.getRemoteIp("127.0.0.1:" + port));
        
        ScheduledExecutorService schedule = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        
        schedule.scheduleWithFixedDelay(new Runnable()
        {
            private long times = 0;
            
            public void run()
            {
                try
                {
                    times = testSendMsg(times, port);
                }
                catch (Throwable e)
                {
                    // TODO: handle exception
                }
            }
        }, 100, 5000, TimeUnit.MILLISECONDS);
        
        while (true)
        {
            Thread.sleep(10000);
            System.out.println("is end .sleep......................########");
        }
    }
    
    private static long testSendMsg(long times, int port) throws Throwable
    {
        System.out.println("\n\n\n需要定时执行的任务...");
        
        while (!NettyManager.instance().isValid(RemoteIp.getRemoteIp("127.0.0.1:" + port)))
        {
            try
            {
                Thread.sleep(200);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        NettyManager.instance().send(RemoteIp.getRemoteIp("127.0.0.1:" + port), getString(times));
        times++;
        
        NettyManager.instance().send(RemoteIp.getRemoteIp("127.0.0.1:" + port), getString(times));
        times++;
        
        NettyManager.instance().send(RemoteIp.getRemoteIp("127.0.0.1:" + port), getString(times));
        times++;
        
        return times;
    }
    
    private static String getString(long times)
    {
        StringBuilder b = new StringBuilder("start: ");
        for (int i = 0; i < 1; i++)
        {
            b.append(i).append("-");
        }
        
        return b.toString();
    }
}
