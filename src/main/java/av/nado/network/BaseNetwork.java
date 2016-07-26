package av.nado.network;

import av.nado.remote.RemoteIp;
import av.nado.util.Aggregate;
import av.util.exception.AException;

public interface BaseNetwork
{
    public void startServer(int port) throws AException;
    
    public void startClient(RemoteIp ip) throws AException;
    
    public boolean isValidClient(RemoteIp ip) throws AException;
    
    public <R> Aggregate<NetworkStatus, Object> send(RemoteIp ip, Object obj) throws AException;
}
