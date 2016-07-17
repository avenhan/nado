package av.nado.network.netty;

import av.nado.network.BaseNetwork;
import av.nado.network.NetworkStatus;
import av.nado.remote.NadoWrap;
import av.nado.remote.RemoteIp;
import av.nado.util.Aggregate;
import av.nado.util.Check;
import av.netty.NettyManager;
import av.util.exception.AException;

public class NadoNetty implements BaseNetwork
{
    public void startServer(int port) throws AException
    {
        NettyManager.instance().startServer(port, 0);
        NettyManager.instance().bind(NadoWrap.class, NadoController.class);
    }
    
    public void startClient(RemoteIp ip) throws AException
    {
        NettyManager.instance().startClient(ip);
    }
    
    public <R> Aggregate<NetworkStatus, R> send(Class<R> type, RemoteIp ip, Object obj) throws AException
    {
        Object ret = NettyManager.instance().send(ip, obj);
        
        Aggregate<NetworkStatus, R> aggregate = new Aggregate<NetworkStatus, R>();
        if (Check.IfOneEmpty(ret))
        {
            aggregate.put(NetworkStatus.NETWORK_STATUS_FAILED, null);
        }
        
        R r = null; // JsonUtil.toObject(type, ret);
        if (ret != null)
        {
            r = type.cast(ret);
        }
        aggregate.put(NetworkStatus.NETWORK_STATUS_SUCCESS, r);
        
        return aggregate;
    }
    
    public boolean isValidClient(RemoteIp ip) throws AException
    {
        return NettyManager.instance().isValid(ip);
    }
    
}
