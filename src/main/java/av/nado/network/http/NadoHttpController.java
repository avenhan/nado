package av.nado.network.http;

import java.util.List;

import org.restexpress.Response;

import av.nado.base.NadoManager;
import av.nado.param.NadoParam;
import av.nado.remote.NadoResponse;
import av.nado.remote.NadoWrap;
import av.nado.util.Check;
import av.rest.Rest;
import av.util.exception.AException;
import av.util.trace.FunctionTime;

public class NadoHttpController
{
    @Rest(uri = "remote", request = NadoFunctionRequest.class, method = "post")
    public Object nadoFunctionPost(NadoFunctionRequest rqst, Response response) throws Exception
    {
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
            
            NadoResponse rspd = new NadoResponse();
            rspd.setBody(NadoParam.toExplain(ret));
            
            functionTime.addCurrentTime("ret");
            return rspd;
        }
        finally
        {
            functionTime.print();
        }
    }
    
    @Rest(uri = "test", request = TestGetRequest.class)
    public Object testGet(TestGetRequest rqst, Response response) throws Exception
    {
        throw new AException(AException.ERR_NOT_MOIDIFIED, "fuck exception...");
        // return "test get from api-02\n";
    }
    
    @Rest(uri = "test", request = TestGetRequest.class, method = "head")
    public Object testGetHead(TestGetRequest rqst, Response response) throws Exception
    {
        return "test head";
    }
    
    @Rest(uri = "test/{type}", method = "post", request = TestPostRequest.class)
    public Object testPost(TestPostRequest rqst, Response response) throws Exception
    {
        return "test post";
    }
    
    @Rest(uri = "test/id", method = "post", request = TestPostRequest.class)
    public Object testPostId(TestPostRequest rqst, Response response) throws Exception
    {
        return "test post Id";
    }
    
}
