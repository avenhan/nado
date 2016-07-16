package av.netty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import av.nado.remote.RemoteIp;
import av.nado.util.Check;

public class NettySendRedo implements Runnable
{
    private long              timeoutTime;
    private static AtomicLong lastRunTime = new AtomicLong(0);
    
    public long getTimeoutTime()
    {
        return timeoutTime;
    }
    
    public void setTimeoutTime(long timeoutTime)
    {
        this.timeoutTime = timeoutTime;
    }
    
    public void run()
    {
        try
        {
            if (!canRun())
            {
                return;
            }
            
            lastRunTime.set(System.currentTimeMillis());
            doResendRoutine();
        }
        catch (Throwable e)
        {
            // TODO: handle exception
        }
    }
    
    private synchronized boolean canRun()
    {
        if (lastRunTime.get() + timeoutTime > System.currentTimeMillis())
        {
            return false;
        }
        
        return true;
    }
    
    private void doResendRoutine() throws Throwable
    {
        Map<RemoteIp, NettyChannelInfo> mapChannel = NettyManager.instance().getChannelMap();
        if (Check.IfOneEmpty(mapChannel))
        {
            return;
        }
        
        List<NettyChannelInfo> lstClientChannel = new ArrayList<NettyChannelInfo>();
        for (Map.Entry<RemoteIp, NettyChannelInfo> entry : mapChannel.entrySet())
        {
            NettyChannelInfo info = entry.getValue();
            if (info == null || info.isServer())
            {
                continue;
            }
            
            lstClientChannel.add(info);
        }
        
        for (NettyChannelInfo nettyChannelInfo : lstClientChannel)
        {
            nettyChannelInfo.redoPostMessage();
        }
    }
}
