package av.nado.register.redis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import av.nado.register.Register;
import av.nado.remote.NadoProxy;
import av.nado.remote.RemoteIp;
import av.nado.util.Check;
import av.redis.Redis;
import av.util.exception.AException;

public class RedisRegister implements Register
{
    public static final long   KEY_REDIS_TIME_OUT = 10000;
    public static final String KEY_REDIS = "nado";
    public static final String KEY_REDIS_LOCK  = "nado:lock";
    public static final String KEY_REDIS_PROXY = "nado:proxy";
    
    private Set<RemoteIp> m_setIps;
    
    public static void main(String[] arg) throws Exception
    {
        Set<RemoteIp> lstIps = new HashSet<RemoteIp>();
        lstIps.add(RemoteIp.getRemoteIp("127.0.0.1:6379"));
        
        RedisRegister register = new RedisRegister();
        register.setRemoteIp(lstIps);
        
        register.registerProxy(RedisRegister.class.getName(), "127.0.0.1:3402", "tcp");
        register.registerProxy(RedisRegister.class.getName(), "127.0.0.2:2322", "tcp");
        register.registerProxy(RedisRegister.class.getName(), "127.0.1.2:2322", "tcp");
        
        Map<String, NadoProxy> map = register.loadRemoteIps("tcp");
        System.out.println("map size: " + map.size());
    }
    
    public void setRemoteIp(Set<RemoteIp> lstIps) throws AException
    {
        m_setIps = lstIps;
        Redis.initialize(KEY_REDIS, Redis.KEY_CLUSTER_TYPE_IMAGE, lstIps);
        Redis.initialize(KEY_REDIS_LOCK, Redis.KEY_CLUSTER_TYPE_IMAGE, lstIps);
        Redis.initialize(KEY_REDIS_PROXY, Redis.KEY_CLUSTER_TYPE_IMAGE, lstIps);
    }
    
    public NadoProxy findProxy(String key, String clientType) throws AException
    {
        NadoProxy nadoProxy = new NadoProxy();
        nadoProxy.setName(key);
        
        String proxys = Redis.get(KEY_REDIS_PROXY, key);
        Set<String> setProxy = getAsProxys(proxys);
        for (String proxy : setProxy)
        {
            String[] arrInfo = proxy.split(":");
            if (!Check.IfOneEmpty(clientType) && !clientType.equalsIgnoreCase(arrInfo[2]))
            {
                continue;
            }
            
            StringBuilder b = new StringBuilder(arrInfo[0]).append(":").append(arrInfo[1]);
            
            nadoProxy.addIp(b.toString(), arrInfo[2]);
        }
        
        return nadoProxy;
    }
    
    public void registerProxy(String key, String addr, String type) throws AException
    {
        String value = new StringBuilder(addr).append(":").append(type).toString();
        Redis.lock(KEY_REDIS_LOCK, key);
        
        try
        {
            String existedProxy = Redis.get(KEY_REDIS_PROXY, key);
            Set<String> setExisted = getAsProxys(existedProxy);
            if (setExisted.contains(value))
            {
                return;
            }
            
            setExisted.add(value);
            Redis.set(KEY_REDIS_PROXY, key, proxyToString(setExisted), KEY_REDIS_TIME_OUT);
        }
        finally
        {
            Redis.unlock(KEY_REDIS_LOCK, key);
        }
    }
    
    public Map<String, NadoProxy> loadRemoteIps(String clientType) throws AException
    {
        Map<String, NadoProxy> mapRet = new HashMap<String, NadoProxy>();
        
        Set<String> keys = Redis.keys(KEY_REDIS_PROXY, "*");
        for (String key : keys)
        {
            String remoteKey = key.substring(KEY_REDIS_PROXY.length() + 1);
            String proxys = Redis.get(KEY_REDIS_PROXY, remoteKey);
            Set<String> setProxy = getAsProxys(proxys);
            NadoProxy nadoProxy = new NadoProxy();
            nadoProxy.setName(remoteKey);
            
            for (String proxy : setProxy)
            {
                String[] arrInfo = proxy.split(":");
                if (!Check.IfOneEmpty(clientType) && !clientType.equalsIgnoreCase(arrInfo[2]))
                {
                    continue;
                }
                
                StringBuilder b = new StringBuilder(arrInfo[0]).append(":").append(arrInfo[1]);
                
                nadoProxy.addIp(b.toString(), arrInfo[2]);
            }
            
            mapRet.put(nadoProxy.getName(), nadoProxy);
        }
        
        return mapRet;
    }
    
    private Set<String> getAsProxys(String value)
    {
        Set<String> setRet = new HashSet<String>();
        if (Check.IfOneEmpty(value))
        {
            return setRet;
        }
        
        Pattern p = Pattern.compile("[a-zA-Z0-9.:]*[^,;\t\r\n\\s*]");
        Matcher m = p.matcher(value);
        
        while (m.find())
        {
            String get = m.group();
            if (Check.IfOneEmpty(get))
            {
                continue;
            }
            
            setRet.add(get);
        }
        
        return setRet;
    }
    
    private String proxyToString(Collection<String> lst)
    {
        boolean isAdded = false;
        StringBuilder b = new StringBuilder();
        for (String proxy : lst)
        {
            if (!isAdded)
            {
                b.append(proxy);
                isAdded = true;
            }
            else
            {
                b.append(",").append(proxy);
            }
        }
        
        return b.toString();
    }
}
