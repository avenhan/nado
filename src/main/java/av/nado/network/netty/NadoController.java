package av.nado.network.netty;

import java.util.List;

import av.nado.base.NadoManager;
import av.nado.remote.NadoParam;
import av.nado.remote.NadoResponse;
import av.nado.remote.NadoWrap;
import av.nado.util.Check;
import av.nado.util.JsonUtil;
import av.netty.NettyController;
import av.util.exception.AException;
import av.util.trace.Trace;

public class NadoController implements NettyController<NadoWrap>
{
    
    public Object receive(NadoWrap rqst) throws Exception
    {
        try
        {
            Trace.print("nado controller receive: {}", JsonUtil.toJson(rqst));
        }
        catch (AException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        String type = rqst.getType();
        String method = rqst.getMethod();
        Object[] arrParam = null;
        List<String> lstParamExplain = rqst.getParams();
        if (!Check.IfOneEmpty(lstParamExplain))
        {
            arrParam = new Object[lstParamExplain.size()];
            for (int i = 0; i < lstParamExplain.size(); i++)
            {
                Object param = null;
                switch (rqst.getParamType())
                {
                    case NadoWrap.KEY_PARAM_TYPE_JSON:
                        param = NadoParam.fromExplain(lstParamExplain.get(i));
                        break;
                    case NadoWrap.KEY_PARAM_TYPE_SERIALIZE:
                        param = NadoParam.deserialized(lstParamExplain.get(i));
                        break;
                    
                    case NadoWrap.KEY_PARAM_TYPE_HESSIAN:
                        param = NadoParam.hessionDecode(lstParamExplain.get(i));
                        break;
                    
                    default:
                        throw new AException(AException.ERR_SERVER, "unkown param type: {}", rqst.getParamType());
                }
                arrParam[i] = param;
            }
        }
        
        Object ret = null;
        try
        {
            if (arrParam == null)
            {
                ret = NadoManager.instance().invoke(type, method);
            }
            else
            {
                ret = NadoManager.instance().invoke(type, method, arrParam);
            }
        }
        catch (Exception e)
        {
            ret = e;
        }
        
        String retExplain = NadoParam.toExplain(ret);
        NadoResponse rspd = new NadoResponse();
        rspd.setBody(retExplain);
        
        return rspd;
    }
    
}
