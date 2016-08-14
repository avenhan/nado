package av.nado.network.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

import av.nado.network.BaseNetwork;
import av.nado.network.NetworkStatus;
import av.nado.param.NadoParam;
import av.nado.remote.NadoResponse;
import av.nado.remote.RemoteIp;
import av.nado.util.Aggregate;
import av.nado.util.Check;
import av.nado.util.JsonUtil;
import av.rest.NadoRest;
import av.rest.NadoRestConfig;
import av.util.exception.AException;
import av.util.trace.Trace;

public class NadoHttp implements BaseNetwork
{
    private Map<String, String> mapHeader = new HashMap<String, String>();
    
    public void startServer(int port) throws AException
    {
        NadoRestConfig config = new NadoRestConfig();
        config.setPort(port);
        NadoRest.instance().loadConfig(config, new NadoHttpController());
    }
    
    public void startClient(RemoteIp ip) throws AException
    {
        // TODO Auto-generated method stub
    }
    
    public <R> Aggregate<NetworkStatus, Object> send(RemoteIp ip, Object obj) throws AException
    {
        if (Check.IfOneEmpty(ip, obj))
        {
            throw new AException(AException.ERR_INVALID_PARAMETER, "invalid parameter");
        }
        
        String url = ip.getUrl();
        String json = JsonUtil.toJson(obj);
        // header information
        
        Trace.print("nado url: {} post: {}", url, json);
        Aggregate<Integer, String> aggregate = HttpHelper.post(url, json, this.mapHeader);
        if (aggregate == null)
        {
            throw new AException(AException.ERR_SERVER, "http return failed...");
        }
        
        Aggregate<NetworkStatus, Object> ret = new Aggregate<NetworkStatus, Object>();
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
        
        NadoResponse rspd = JsonUtil.toObject(NadoResponse.class, retJson);
        Object retObject = NadoParam.fromExplain(rspd.getBody());
        
        ret.putSecond(retObject);
        return ret;
    }
    
    public boolean isValidClient(RemoteIp ip) throws AException
    {
        // TODO Auto-generated method stub
        return true;
    }
    
}
