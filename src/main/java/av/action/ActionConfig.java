package av.action;

public class ActionConfig
{
    private int minThreadCount  = 20;
    private int maxThreadCount  = 20;
    private int keepAliveTimeMs = 3000;
    private int cachedSize      = 20;
    
    public int getMinThreadCount()
    {
        return minThreadCount;
    }
    
    public void setMinThreadCount(int minThreadCount)
    {
        if (minThreadCount < 1)
        {
            return;
        }

        this.minThreadCount = minThreadCount;
    }
    
    public int getMaxThreadCount()
    {
        return maxThreadCount;
    }
    
    public void setMaxThreadCount(int maxThreadCount)
    {
        if (maxThreadCount < 1)
        {
            return;
        }

        this.maxThreadCount = maxThreadCount;
    }
    
    public int getKeepAliveTimeMs()
    {
        return keepAliveTimeMs;
    }
    
    public void setKeepAliveTimeMs(int keepAliveTimeMs)
    {
        this.keepAliveTimeMs = keepAliveTimeMs;
    }
    
    public int getCachedSize()
    {
        return cachedSize;
    }
    
    public void setCachedSize(int cachedSize)
    {
        this.cachedSize = cachedSize;
    }
}
