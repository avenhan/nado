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
    public static final String              KEY_HTTP  = "http";
    public static final String              KEY_NETTY = "netty";
    
    private static NetworkManager           m_pThis;
    private NetworkType                     m_networkType;
    private BaseNetwork                     m_network;
    
    private static Map<String, NetworkType> m_mapType = new HashMap<String, NetworkType>();
    
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
        this.m_networkType = m_mapType.get(type);
        if (this.m_networkType == null)
        {
            throw new AException(AException.ERR_FATAL, "invalid bootstrap procotol: {}", type);
        }
        
        switch (this.m_networkType)
        {
            case NETWORK_TYPE_HTTP:
                m_network = new NadoHttp();
                break;
            case NETWORK_TYPE_NETTY:
                m_network = new NadoNetty();
                break;
            default:
                throw new AException(AException.ERR_SERVER, "unknown network type: {}", m_networkType);
        }
    }
    
    public void startServer(int port) throws AException
    {
        m_network.startServer(port);
    }
    
    public void startClient(RemoteIp ip) throws AException
    {
        m_network.startClient(ip);
    }
    
    public <R> Aggregate<NetworkStatus, R> send(Class<R> type, RemoteIp ip, Object obj) throws AException
    {
        return m_network.send(type, ip, obj);
    }
}
