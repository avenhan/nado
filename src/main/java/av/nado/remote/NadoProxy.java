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
    
    public void addIp(String ip)
    {
        RemoteIp remoteIp = RemoteIp.getRemoteIp(ip);
        if (remoteIp == null)
        {
            return;
        }
        
        lstRemoteIps.add(remoteIp);
    }
}
