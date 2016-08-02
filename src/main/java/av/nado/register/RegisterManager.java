package av.nado.register;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import av.nado.register.redis.RedisRegister;
import av.nado.register.zookeeper.ZookRegister;
import av.nado.remote.NadoProxy;
import av.nado.remote.RemoteIp;
import av.nado.util.Check;
import av.util.exception.AException;

public class RegisterManager
{
    public static final String               KEY_REDIS        = "redis";
    public static final String               KEY_ZOOKEEPER    = "zookeeper";
    
    private static RegisterManager           m_pThis;
    private RegisterType                     m_type;
    private Register                         m_register;
    
    private static Map<String, RegisterType> m_mapRigsterType = new HashMap<String, RegisterType>();
    
    static
    {
        m_mapRigsterType.put(KEY_REDIS, RegisterType.REGISTER_TYPE_REDIS);
        m_mapRigsterType.put(KEY_ZOOKEEPER, RegisterType.REGISTER_TYPE_ZOOKEEPER);
    }
    
    public static RegisterManager instance()
    {
        if (m_pThis == null)
        {
            m_pThis = new RegisterManager();
        }
        
        return m_pThis;
    }
    
    public void setType(String type) throws AException
    {
        m_type = m_mapRigsterType.get(type);
        if (m_type == null)
        {
            throw new AException(AException.ERR_FATAL, "unkown register type: {}", type);
        }
        
        switch (m_type)
        {
            case REGISTER_TYPE_REDIS:
                m_register = new RedisRegister();
                break;
            case REGISTER_TYPE_ZOOKEEPER:
                m_register = new ZookRegister();
                break;
            default:
                throw new AException(AException.ERR_FATAL, "unkown register type: {}", type);
        }
    }
    
    public void setAddress(String ips) throws AException
    {
        if (Check.IfOneEmpty(ips))
        {
            throw new AException(AException.ERR_FATAL, "invalid register ip address");
        }
        
        String[] arrIpName = ips.split(",");
        
        if (arrIpName == null || arrIpName.length < 1)
        {
            throw new AException(AException.ERR_FATAL, "invalid register ip address: {}", ips);
        }
        
        Set<RemoteIp> lstIps = new HashSet<RemoteIp>();
        for (String ipName : arrIpName)
        {
            RemoteIp ip = RemoteIp.getRemoteIp(ipName);
            if (ip == null)
            {
                throw new AException(AException.ERR_FATAL, "invalid register ip address: {}", ipName);
            }
            
            lstIps.add(ip);
        }
        
        m_register.setRemoteIp(lstIps);
    }
    
    public NadoProxy findProxy(String key) throws AException
    {
        return m_register.findProxy(key);
    }
    
    public void registerProxy(String key, String addr, String type) throws AException
    {
        m_register.registerProxy(key, addr, type);
    }
    
    public Map<String, NadoProxy> loadRemoteIps(String clientType) throws AException
    {
        return m_register.loadRemoteIps(clientType);
    }
}
