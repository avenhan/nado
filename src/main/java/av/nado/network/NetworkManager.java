package av.nado.network;

import java.util.HashMap;
import java.util.Map;

import av.nado.network.http.NadoHttp;
import av.nado.network.netty.NadoNetty;
import av.nado.remote.RemoteIp;
import av.nado.util.Aggregate;
import av.util.exception.AException;

public class NetworkManager
{
    public static final String              KEY_HTTP          = "http";
    public static final String              KEY_NETTY         = "tcp";
    
    private static NetworkManager           m_pThis;
    private Map<String, BaseNetwork>        m_mapNetwork      = new HashMap<String, BaseNetwork>();
    private Map<String, RemoteIp>           m_mapConnectedIps = new HashMap<String, RemoteIp>();
    
    private static Map<String, NetworkType> m_mapType         = new HashMap<String, NetworkType>();
    
    static
    {
        m_mapType.put(KEY_HTTP, NetworkType.NETWORK_TYPE_HTTP);
        m_mapType.put(KEY_NETTY, NetworkType.NETWORK_TYPE_NETTY);
    }
    
    private NetworkManager()
    {
        // TODO Auto-generated constructor stub
    }
    
    public static NetworkManager instance()
    {
        if (m_pThis == null)
        {
            m_pThis = new NetworkManager();
        }
        
        return m_pThis;
    }
    
    public void setNetworkType(String type) throws AException
    {
        NetworkType networkType = m_mapType.get(type);
        if (networkType == null)
        {
            throw new AException(AException.ERR_FATAL, "invalid bootstrap procotol: {}", type);
        }
        
        BaseNetwork network = null;
        switch (networkType)
        {
            case NETWORK_TYPE_HTTP:
                network = new NadoHttp();
                break;
            case NETWORK_TYPE_NETTY:
                network = new NadoNetty();
                break;
            default:
                throw new AException(AException.ERR_SERVER, "unknown network type: {}", networkType);
        }
        
        m_mapNetwork.put(type, network);
    }
    
    public void startServer(String type, int port) throws AException
    {
        BaseNetwork network = m_mapNetwork.get(type);
        if (network == null)
        {
            throw new AException(AException.ERR_SERVER, "unknow network type: {}", type);
        }
        
        network.startServer(port);
    }
    
    public RemoteIp startClient(RemoteIp ip) throws AException
    {
        RemoteIp ipExisted = m_mapConnectedIps.get(ip.toString());
        if (ipExisted != null)
        {
            return ipExisted;
        }
        
        BaseNetwork network = m_mapNetwork.get(ip.getType());
        if (network == null)
        {
            throw new AException(AException.ERR_SERVER, "unknow network type: {}", ip.getType());
        }
        
        network.startClient(ip);
        
        int count = 0;
        while (!network.isValidClient(ip))
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            count++;
            if (count > 10)
            {
                break;
            }
        }
        
        ip.setLastConnectedTime(System.currentTimeMillis());
        m_mapConnectedIps.put(ip.toString(), ip);
        return ip;
    }
    
    public <R> Aggregate<NetworkStatus, Object> send(RemoteIp ip, Object obj) throws AException
    {
        BaseNetwork network = m_mapNetwork.get(ip.getType());
        if (network == null)
        {
            throw new AException(AException.ERR_SERVER, "unknow network type: {}", ip.getType());
        }
        
        return network.send(ip, obj);
    }
    
    public boolean isValidClient(RemoteIp ip) throws AException
    {
        BaseNetwork network = m_mapNetwork.get(ip.getType());
        if (network == null)
        {
            throw new AException(AException.ERR_SERVER, "unknow network type: {}", ip.getType());
        }
        
        return network.isValidClient(ip);
    }
}
