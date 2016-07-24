package av.nado.remote;

import java.net.SocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import av.nado.util.Check;

public class RemoteIp
{
    public static final String KEY_REMOTE_PATH   = "remote";
    private static Logger      logger            = LogManager.getLogger(RemoteIp.class);
    private String             ip;
    private int                port;
    
    private long               lastConnectedTime = 0;
    private long               readCount         = 0;
    private String             url;
    private Object             attachment        = null;
    
    public static RemoteIp getRemoteIp(SocketAddress addr)
    {
        if (addr == null)
        {
            return null;
        }
        
        String ipAddr = addr.toString();
        ipAddr = ipAddr.replace("/", "");
        
        return getRemoteIp(ipAddr);
    }
    
    public static RemoteIp getRemoteIp(String addr)
    {
        if (Check.IfOneEmpty(addr))
        {
            return null;
        }
        
        String[] arrIpInfo = addr.split(":");
        if (arrIpInfo == null || arrIpInfo.length != 2)
        {
            return null;
        }
        
        try
        {
            RemoteIp ret = new RemoteIp();
            ret.setIp(arrIpInfo[0].replace(" ", ""));
            ret.setPort(Integer.parseInt(arrIpInfo[1].replace(" ", "")));
            
            return ret;
        }
        catch (Exception e)
        {
            logger.catching(e);
            return null;
        }
    }
    
    public String getIp()
    {
        return ip;
    }
    
    public void setIp(String ip)
    {
        this.ip = ip;
    }
    
    public int getPort()
    {
        return port;
    }
    
    public void setPort(int port)
    {
        this.port = port;
    }
    
    public long getLastConnectedTime()
    {
        return lastConnectedTime;
    }
    
    public void setLastConnectedTime(long lastConnectedTime)
    {
        this.lastConnectedTime = lastConnectedTime;
    }
    
    public long getReadCount()
    {
        return readCount;
    }
    
    public void setReadCount(long readCount)
    {
        this.readCount = readCount;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + port;
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RemoteIp other = (RemoteIp) obj;
        if (ip == null)
        {
            if (other.ip != null)
                return false;
        }
        else if (!ip.equals(other.ip))
            return false;
        if (port != other.port)
            return false;
        return true;
    }
    
    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder();
        return b.append(ip).append(":").append(port).toString();
    }
    
    public String getUrl()
    {
        if (Check.IfOneEmpty(url))
        {
            url = new StringBuilder("http://").append(ip).append(":").append(port).append("/1.0/").append(KEY_REMOTE_PATH).toString();
        }
        
        return url;
    }
    
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public Object getAttachment()
    {
        return attachment;
    }
    
    public void setAttachment(Object attachment)
    {
        this.attachment = attachment;
    }
}
