package av.nado.network.netty;

import av.nado.network.BaseNetwork;
import av.nado.network.NetworkStatus;
import av.nado.remote.NadoWrap;
import av.nado.remote.RemoteIp;
import av.nado.util.Aggregate;
import av.nado.util.Check;
import av.netty.NettyManager;
import av.util.exception.AException;
import av.util.trace.FunctionTime;

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
    
    public <R> Aggregate<NetworkStatus, R> send(Class<R> type, RemoteIp ip, Object obj) throws AException
    {
        FunctionTime time = new FunctionTime();
        
        try
        {
            Object ret = nettyManager.send(ip, obj);
            time.addCurrentTime("send");
            
            R r = null;
            Aggregate<NetworkStatus, R> aggregate = new Aggregate<NetworkStatus, R>();
            if (Check.IfOneEmpty(ret))
            {
                aggregate.put(NetworkStatus.NETWORK_STATUS_FAILED, null);
            }
            else
            {
                r = type.cast(ret);
                aggregate.put(NetworkStatus.NETWORK_STATUS_SUCCESS, r);
            }
            
            return aggregate;
        }
        finally
        {
            time.print();
        }
    }
    
    public boolean isValidClient(RemoteIp ip) throws AException
    {
        return NettyManager.instance().isValid(ip);
    }
    
}
