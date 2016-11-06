package av.rest.preprocessor;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.pipeline.Postprocessor;
import org.restexpress.pipeline.Preprocessor;

import av.nado.util.JsonUtil;
import av.util.exception.AException;

public class RestPreprocessor implements Preprocessor, Postprocessor
{

    public void process(Request paramRequest)
    {
        // TODO Auto-generated method stub
        System.out.println("prepare request ....");
    }
    
    public void process(Request paramRequest, Response paramResponse)
    {
        if (paramResponse.getException() != null)
        {
            Throwable exception = paramResponse.getException();
            AException e = new AException(paramResponse.getResponseStatus().code(), exception);
            paramResponse.setException(e);
            
            NadoError err = new NadoError();
            err.setCode(e.getCode());
            err.setId(e.getId());
            err.setMessage(e.getClientMessage());
            
            paramResponse.setResponseCode(e.getCode());
            try
            {
                paramResponse.setBody(JsonUtil.toJson(err));
            }
            catch (AException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }
    
}
