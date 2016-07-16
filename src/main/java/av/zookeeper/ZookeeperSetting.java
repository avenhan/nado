package av.zookeeper;

public class ZookeeperSetting
{
    private String ipAddr;
    private int    timeOut;
    
    public String getIpAddr()
    {
        return ipAddr;
    }
    
    public void setIpAddr(String ipAddr)
    {
        this.ipAddr = ipAddr;
    }
    
    public int getTimeOut()
    {
        return timeOut;
    }
    
    public void setTimeOut(int timeOut)
    {
        this.timeOut = timeOut;
    }
    
}
