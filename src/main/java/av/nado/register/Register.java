package av.nado.register;

import java.util.Map;
import java.util.Set;

import av.nado.remote.NadoProxy;
import av.nado.remote.RemoteIp;
import av.util.exception.AException;

public interface Register
{
    public NadoProxy findProxy(String key, String clientType) throws AException;
    
    public void registerProxy(String key, String addr, String type) throws AException;
    
    public void setRemoteIp(Set<RemoteIp> lstIps) throws AException;
    
    public Map<String, NadoProxy> loadRemoteIps(String clientType) throws AException;
    
    public void setNotify(RegisterNotify notify) throws AException;
}
