package av.nado.network.netty;

import av.nado.network.BaseNetwork;
import av.nado.network.NetworkStatus;
import av.nado.remote.NadoWrap;
import av.nado.remote.RemoteIp;
import av.nado.util.Aggregate;
import av.netty.NettyManager;
import av.util.exception.AException;

public class NadoNetty implements BaseNetwork
{
    private static NettyManager nettyManager = NettyManager.instance();
    
    public void startServer(int port) throws AException
    {
        NettyManager.instance().startServer(port, 0);
        NettyManager.instance().bind(NadoWrap.class, NadoController.class);
    }
    
    public void startClient(RemoteIp ip) throws AException
    {
        NettyManager.instance().startClient(ip);
    }
    
    public <R> Aggregate<NetworkStatus, Object> send(RemoteIp ip, Object obj) throws AException
    {
        try
        {
            Object ret = nettyManager.send(ip, obj);
            Aggregate<NetworkStatus, Object> aggregate = new Aggregate<NetworkStatus, Object>();
            aggregate.put(NetworkStatus.NETWORK_STATUS_SUCCESS, ret);
            
            return aggregate;
        }
        finally
        {
        }
    }
    
    public boolean isValidClient(RemoteIp ip) throws AException
    {
        return NettyManager.instance().isValid(ip);
    }
    
}
