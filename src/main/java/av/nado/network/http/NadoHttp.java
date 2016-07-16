package av.nado.network.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

import av.nado.network.BaseNetwork;
import av.nado.network.NetworkStatus;
import av.nado.remote.RemoteIp;
import av.nado.util.Aggregate;
import av.nado.util.Check;
import av.nado.util.JsonUtil;
import av.util.exception.AException;
import av.util.trace.Trace;

public class NadoHttp implements BaseNetwork
{
    private Map<String, String> mapHeader = new HashMap<String, String>();
    
    public void startServer(int port)
    {
        // TODO Auto-generated method stub
        
    }
    
    public void startClient(RemoteIp ip)
    {
        // TODO Auto-generated method stub
        
    }
    
    public <R> Aggregate<NetworkStatus, R> send(Class<R> type, RemoteIp ip, Object obj) throws AException
    {
        if (Check.IfOneEmpty(type, ip, obj))
        {
            throw new AException(AException.ERR_INVALID_PARAMETER, "invalid parameter");
        }
        
        String url = ip.getUrl();
        String json = JsonUtil.toJson(obj);
        // header information
        
        Trace.debug("nado url: {} post: {}", url, json);
        
        Aggregate<Integer, String> aggregate = HttpHelper.post(url, json, this.mapHeader);
        if (aggregate == null)
        {
            throw new AException(AException.ERR_SERVER, "http return failed...");
        }
        
        Aggregate<NetworkStatus, R> ret = new Aggregate<NetworkStatus, R>();
        if (aggregate.getFirst() == HttpStatus.SC_OK)
        {
            ret.putFirst(NetworkStatus.NETWORK_STATUS_SUCCESS);
        }
        else
        {
            ret.putFirst(NetworkStatus.NETWORK_STATUS_FAILED);
        }
        
        String retJson = aggregate.getSecond();
        if (Check.IfOneEmpty(retJson) || retJson.length() < 2)
        {
            throw new AException(AException.ERR_SERVER, "http return failed...not json: {}", retJson);
        }
        
        R r = JsonUtil.toObject(type, retJson);
        ret.putSecond(r);
        
        return ret;
    }
    
}
