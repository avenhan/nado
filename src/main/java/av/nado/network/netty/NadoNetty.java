package av.nado.network.netty;

import av.nado.network.BaseNetwork;
import av.nado.network.NetworkStatus;
import av.nado.remote.NadoWrap;
import av.nado.remote.RemoteIp;
import av.nado.util.Aggregate;
import av.netty.NettyManager;
import av.util.exception.AException;
import av.util.trace.FunctionTime;

public class NadoNetty implements BaseNetwork
{
    private static NettyManager nettyManager = NettyManager.instance();
    
    @Override
    public void startServer(int port) throws AException
    {
        NettyManager.instance().startServer(port, 0);
        NettyManager.instance().bind(NadoWrap.class, NadoController.class);
    }
    
    @Override
    public void startClient(RemoteIp ip) throws AException
    {
        NettyManager.instance().startClient(ip);
    }
    
    @Override
    public <R> Aggregate<NetworkStatus, Object> send(RemoteIp ip, Object obj) throws AException
    {
        FunctionTime time = new FunctionTime();
        
        try
        {
            Object ret = nettyManager.send(ip, obj);
            time.addCurrentTime("send");
            
            Aggregate<NetworkStatus, Object> aggregate = new Aggregate<NetworkStatus, Object>();
            aggregate.put(NetworkStatus.NETWORK_STATUS_SUCCESS, ret);
            
            return aggregate;
        }
        finally
        {
            time.print();
        }
    }
    
    @Override
    public boolean isValidClient(RemoteIp ip) throws AException
    {
        return NettyManager.instance().isValid(ip);
    }
    
}
