package av.nado.remote;

import java.util.ArrayList;
import java.util.List;

public class NadoProxy
{
    private String         name;
    private List<RemoteIp> lstRemoteIps = new ArrayList<RemoteIp>();
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public List<RemoteIp> getLstRemoteIps()
    {
        return lstRemoteIps;
    }
    
    public void setLstRemoteIps(List<RemoteIp> lstRemoteIps)
    {
        this.lstRemoteIps = lstRemoteIps;
    }
    
    public void addIp(RemoteIp remoteIp)
    {
        if (remoteIp == null)
        {
            return;
        }
        
        lstRemoteIps.add(remoteIp);
    }
    
    public void addIp(String ip)
    {
        RemoteIp remoteIp = RemoteIp.getRemoteIp(ip);
        if (remoteIp == null)
        {
            return;
        }
        
        lstRemoteIps.add(remoteIp);
    }
    
    public void addIp(String ip, String type)
    {
        RemoteIp remoteIp = RemoteIp.getRemoteIp(ip);
        if (remoteIp == null)
        {
            return;
        }
        remoteIp.setType(type);
        lstRemoteIps.add(remoteIp);
    }
    
    public boolean contain(RemoteIp ip)
    {
        for (RemoteIp remoteIp : lstRemoteIps)
        {
            if (remoteIp.equals(ip) && remoteIp.getType().equals(ip.getType()))
            {
                return true;
            }
        }
        
        return false;
    }
}
