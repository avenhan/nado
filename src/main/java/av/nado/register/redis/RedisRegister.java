package av.nado.register.redis;

import java.util.Map;
import java.util.Set;

import av.nado.register.Register;
import av.nado.remote.NadoProxy;
import av.nado.remote.RemoteIp;
import av.util.exception.AException;

public class RedisRegister implements Register
{
    private Set<RemoteIp> m_setIps;
    
    public NadoProxy findProxy(String key) throws AException
    {
        NadoProxy proxy = new NadoProxy();
        proxy.setName(key);
        
        // RedisRegister.instance().addCacheType(key);
        // RedisList<String> redisList = new RedisList<>(key, String.class);
        // try
        // {
        // List<String> lstRemoteIps = redisList.sub(0, -1);
        // for (String ip : lstRemoteIps)
        // {
        // proxy.addIp(ip);
        // }
        //
        // if (proxy.getLstRemoteIps().isEmpty())
        // {
        // return null;
        // }
        //
        // return proxy;
        // }
        // catch (AException e)
        // {
        //
        // }
        
        return null;
    }
    
    public void registerProxy(String key, String addr) throws AException
    {
        // RedisRegister.instance().addCacheType(bKey.toString());
        // RedisList<String> redisList = new RedisList<>(bKey.toString(),
        // String.class);
        //
        // String lock = "";
        // try
        // {
        // lock = RedisRegister.instance().lock(KEY_LOCK_NADO, type, method);
        // boolean isFind = false;
        // List<String> lstExisted = redisList.sub(0, -1);
        // for (String existed : lstExisted)
        // {
        // if (existed.equals(addressInfo))
        // {
        // isFind = true;
        // break;
        // }
        // }
        //
        // if (isFind)
        // {
        // return;
        // }
        //
        // redisList.add(b.toString());
        // }
        // finally
        // {
        // RedisRegister.instance().unlock(KEY_LOCK_NADO, lock);
        // }
    }
    
    public void setRemoteIp(Set<RemoteIp> lstIps) throws AException
    {
        m_setIps = lstIps;
    }
    
    public Map<String, NadoProxy> loadRemoteIps() throws AException
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}
