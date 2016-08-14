package av.nado.register.redis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import av.nado.register.Register;
import av.nado.register.RegisterNotify;
import av.nado.remote.NadoProxy;
import av.nado.remote.RemoteIp;
import av.nado.util.Check;
import av.redis.Redis;
import av.timer.QuartzManager;
import av.timer.Timer;
import av.util.exception.AException;

public class RedisRegister implements Register
{
    public static final long       KEY_REDIS_TIME_OUT = 10000;
    public static final String     KEY_REDIS          = "nado";
    public static final String     KEY_REDIS_LOCK     = "nado:lock";
    public static final String     KEY_REDIS_PROXY    = "nado:proxy";
    
    private Set<RemoteIp>          m_setIps;
    private RegisterNotify         m_notify;
    private Map<String, String>    m_mapRegister      = new HashMap<String, String>();
    private String                 lastClientType;
    private Map<String, NadoProxy> m_mapLoadProxy     = new HashMap<String, NadoProxy>();
    
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
        
        while (true)
        {
            Thread.sleep(10000);
        }
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
        registerPoxy(key, value);
        m_mapRegister.put(key, value);
    }
    
    public Map<String, NadoProxy> loadRemoteIps(String clientType) throws AException
    {
        Map<String, NadoProxy> mapRet = new HashMap<String, NadoProxy>();
        lastClientType = clientType;
        
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
    
    private void registerPoxy(String key, String value) throws AException
    {
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
    
    @Timer(time = 3000, exclusive = true)
    protected void onTimerRegister() throws AException
    {
        for (Map.Entry<String, String> entry : m_mapRegister.entrySet())
        {
            registerPoxy(entry.getKey(), entry.getValue());
        }
    }
    
    @Timer(time = 5000, exclusive = true)
    protected void onTimerLoadProxy() throws AException
    {
        if (Check.IfOneEmpty(lastClientType))
        {
            return;
        }
        
        if (m_notify == null)
        {
            QuartzManager.instance().delete();
            return;
        }
        
        Map<String, NadoProxy> mapNotifyProxy = new HashMap<String, NadoProxy>();
        
        Map<String, NadoProxy> mapProxy = loadRemoteIps(lastClientType);
        for (Map.Entry<String, NadoProxy> entry : mapProxy.entrySet())
        {
            NadoProxy proxy = entry.getValue();
            NadoProxy existProxy = m_mapLoadProxy.get(entry.getKey());
            if (existProxy == null)
            {
                mapNotifyProxy.put(entry.getKey(), proxy);
                m_mapLoadProxy.put(entry.getKey(), proxy);
                continue;
            }
            
            NadoProxy addProxy = new NadoProxy();
            addProxy.setName(entry.getKey());
            
            for (RemoteIp remoteIp : proxy.getLstRemoteIps())
            {
                if (!existProxy.contain(remoteIp))
                {
                    addProxy.addIp(remoteIp);
                    existProxy.addIp(remoteIp);
                }
            }
            
            if (Check.IfOneEmpty(addProxy.getLstRemoteIps()))
            {
                continue;
            }
            
            mapNotifyProxy.put(entry.getKey(), addProxy);
        }
        
        if (Check.IfOneEmpty(mapNotifyProxy))
        {
            return;
        }
        
        m_notify.onRegisterNotify(mapNotifyProxy);
    }
    
    public void setNotify(RegisterNotify notify) throws AException
    {
        m_notify = notify;
        if (m_notify == null)
        {
            QuartzManager.instance().addJob(this, "onTimerRegister");
        }
        else
        {
            QuartzManager.instance().addJob(this, "onTimerLoadProxy");
        }
    }
}
