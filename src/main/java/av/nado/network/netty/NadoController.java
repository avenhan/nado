package av.nado.network.netty;

import java.util.List;

import av.nado.base.NadoManager;
import av.nado.remote.NadoParam;
import av.nado.remote.NadoWrap;
import av.nado.util.Check;
import av.netty.NettyController;
import av.util.exception.AException;
import av.util.trace.FunctionTime;

public class NadoController implements NettyController<NadoWrap>
{
    
    public Object receive(NadoWrap rqst) throws Exception
    {
        if (true)
        {
            return "null";
        }
        
        FunctionTime functionTime = new FunctionTime();
        
        try
        {
            
            String type = rqst.getType();
            String method = rqst.getMethod();
            functionTime.addCurrentTime("{}.{}", type, method);
            
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
            
            functionTime.addCurrentTime("params");
            
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
            
            functionTime.addCurrentTime("invoke");
            return ret;
        }
        finally
        {
            functionTime.print();
        }
    }
    
}
