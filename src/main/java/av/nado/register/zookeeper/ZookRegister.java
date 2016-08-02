package av.nado.register.zookeeper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.zookeeper.CreateMode;

import av.nado.register.Register;
import av.nado.remote.NadoProxy;
import av.nado.remote.RemoteIp;
import av.nado.util.Check;
import av.util.exception.AException;
import av.util.trace.Trace;
import av.zookeeper.Zookeeper;
import av.zookeeper.ZookeeperSetting;

public class ZookRegister implements Register
{
    public static final String KEY_NADO_ROOT    = "/nado";
    public static final int    KEY_TIME_TIMEOUT = 10000;
    
    private Set<RemoteIp>      m_setIps;
    
    public NadoProxy findProxy(String key) throws AException
    {
        StringBuilder b = new StringBuilder(KEY_NADO_ROOT).append("/").append(key);
        List<String> lstIps = Zookeeper.instance().children(b.toString());
        
        NadoProxy proxy = new NadoProxy();
        proxy.setName(key);
        
        for (String ip : lstIps)
        {
            proxy.addIp(ip);
        }
        
        return proxy;
    }
    
    public void registerProxy(String key, String addr, String type) throws AException
    {
        if (Check.IfOneEmpty(key, addr))
        {
            throw new AException(AException.ERR_SERVER, "invalid paramete");
        }
        
        StringBuilder b = new StringBuilder(KEY_NADO_ROOT).append("/").append(key);
        
        Zookeeper.instance().create(b.toString(), "");
        
        b.append("/").append(addr);
        Zookeeper.instance().create(b.toString(), type, CreateMode.EPHEMERAL);
    }
    
    public void setRemoteIp(Set<RemoteIp> lstIps) throws AException
    {
        m_setIps = lstIps;
        StringBuilder b = new StringBuilder();
        for (RemoteIp remoteIp : lstIps)
        {
            if (b.length() > 1)
            {
                b.append(",");
            }
            b.append(remoteIp.toString());
        }
        
        Trace.print("zk register: {}", b.toString());
        ZookeeperSetting setting = new ZookeeperSetting();
        setting.setIpAddr(b.toString());
        setting.setTimeOut(KEY_TIME_TIMEOUT);
        
        Zookeeper.instance().loadConfig(setting);
        Zookeeper.instance().create(KEY_NADO_ROOT, "");
    }
    
    public Map<String, NadoProxy> loadRemoteIps(String clientType) throws AException
    {
        Map<String, NadoProxy> mapRet = new HashMap<String, NadoProxy>();
        
        List<String> lstKeys = Zookeeper.instance().children(KEY_NADO_ROOT);
        for (String key : lstKeys)
        {
            StringBuilder b = new StringBuilder(KEY_NADO_ROOT).append("/").append(key);
            List<String> lstIps = Zookeeper.instance().children(b.toString());
            
            NadoProxy proxy = new NadoProxy();
            proxy.setName(key);
            
            for (String ip : lstIps)
            {
                StringBuilder ipBuilder = new StringBuilder(b).append("/").append(ip);
                String value = Zookeeper.instance().toString(ipBuilder.toString());
                
                if (!value.equalsIgnoreCase(clientType))
                {
                    continue;
                }
                
                proxy.addIp(ip, value);
            }
            
            if (proxy.getLstRemoteIps().isEmpty())
            {
                continue;
            }
            
            mapRet.put(key, proxy);
        }
        
        return mapRet;
    }
    
    public static void main(String[] args) throws Exception
    {
        ZookeeperSetting setting = new ZookeeperSetting();
        setting.setIpAddr("127.0.0.1:2181");
        setting.setTimeOut(KEY_TIME_TIMEOUT);
        
        Zookeeper.instance().loadConfig(setting);
        Zookeeper.instance().create("/nado", "");
        
        Zookeeper.instance().create("/nado/test", "diff", CreateMode.EPHEMERAL);
        
        String ret = Zookeeper.instance().toString("/nado/test");
        
        List<String> lstChild = Zookeeper.instance().children("/nado");
        
        Trace.print(lstChild.toString());
    }
}
