package av.rest;

import java.lang.reflect.Method;

import org.restexpress.RestExpress;

import av.nado.network.http.NadoHttpController;
import av.rest.preprocessor.LogMessageObserver;
import av.util.exception.AException;

public class NadoRest
{
    private static NadoRest m_pThis;
    private RestExpress     m_restExpress;
    
    public static NadoRest instance()
    {
        if (m_pThis == null)
        {
            m_pThis = new NadoRest();
        }
        
        return m_pThis;
    }
    
    public void loadConfig(NadoRestConfig config) throws AException
    {
        m_restExpress = new RestExpress();
        m_restExpress.setBaseUrl(config.getBaseUrl());
        m_restExpress.setPort(config.getPort());
        m_restExpress.setName(config.getName());
        m_restExpress.setExecutorThreadCount(config.getExecutorThreadCount());
        m_restExpress.setMaxContentSize(Integer.MAX_VALUE);
        
        m_restExpress.addMessageObserver(new LogMessageObserver());
        
        addController(config, new NadoHttpController());
        
        m_restExpress.bind(config.getPort());
        m_restExpress.awaitShutdown();
    }
    
    private void addController(NadoRestConfig config, Object object) throws AException
    {
        Class<?> type = object.getClass();
        Method[] methods = type.getDeclaredMethods();
        for (Method method : methods)
        {
            if (!method.isAnnotationPresent(Rest.class))
            {
                continue;
            }
            
            RestInfo info = new RestInfo();
            
            Rest rest = method.getAnnotation(Rest.class);
            String uri = rest.uri();
            StringBuilder b = new StringBuilder(config.getVersion()).append("/").append(uri);
            
            info.setUri(b.toString());
            info.setType(type);
            info.setMethod(rest.method().toLowerCase());
            info.setController(object);
            info.setMethodName(method.getName());
            info.setRqstType(rest.request());
            
            NadoRestController controller = new NadoRestController();
            controller.addRestInformation(info);
            controller.setInfo(info);
            
            m_restExpress.uri(b.toString(), controller).method(info.getHttpMethod());
        }
    }
    
    public static void main(String[] arg) throws Exception
    {
        NadoRestConfig config = new NadoRestConfig();
        config.setPort(9090);
        NadoRest.instance().loadConfig(config);
    }
}
