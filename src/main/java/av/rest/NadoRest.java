package av.rest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.restexpress.RestExpress;

import av.nado.network.http.NadoHttpController;
import av.rest.preprocessor.LogMessageObserver;
import av.util.exception.AException;

public class NadoRest
{
    private static NadoRest m_pThis;
    private RestExpress     m_restExpress;
    
    private Map<String, NadoRestController> m_mapRestInfo = new HashMap<String, NadoRestController>();
    
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
        

        initializeMostLike();
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
            
            if (m_mapRestInfo.containsKey(b.toString()))
            {
                RestInfo existedInfo = m_mapRestInfo.get(b.toString()).getInfo();
                if (existedInfo != null)
                {
                    throw new AException(AException.ERR_FATAL, "uri: {} method: {} existed at: {}.{}()", b.toString(), rest.method(),
                            existedInfo.getType().getName(), existedInfo.getMethodName());
                }
            }
            
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
            m_mapRestInfo.put(b.toString(), controller);
        }
    }
    
    private void initializeMostLike()
    {
        for (Map.Entry<String, NadoRestController> entry : m_mapRestInfo.entrySet())
        {
            NadoRestController controller = entry.getValue();
            Map<String, RestInfo> mapMostLike = getMostLikeRestInformations(controller.getInfo());
            controller.addMostLike(mapMostLike);
        }
    }
    
    private Map<String, RestInfo> getMostLikeRestInformations(RestInfo info)
    {
        Map<String, RestInfo> mapRet = new HashMap<String, RestInfo>();
        if (!info.isHasPattern())
        {
            return mapRet;
        }
        
        String[] arrPaths = info.getArrPaths();
        for (Map.Entry<String, NadoRestController> entry : m_mapRestInfo.entrySet())
        {
            RestInfo existed = entry.getValue().getInfo();
            if (existed.isHasPattern())
            {
                continue;
            }
            
            String[] arrOtherPaths = existed.getArrPaths();
            if (arrOtherPaths.length != arrPaths.length)
            {
                continue;
            }
            
            boolean isMostLike = true;
            for (int i = 0; i < arrOtherPaths.length; i++)
            {
                String checkPath = arrPaths[i];
                if (checkPath.equalsIgnoreCase(arrOtherPaths[i]))
                {
                    continue;
                }
                
                if (checkPath.charAt(0) == '{' && checkPath.charAt(checkPath.length() - 1) == '}')
                {
                    continue;
                }
                
                isMostLike = false;
                break;
            }
            
            if (!isMostLike)
            {
                continue;
            }
            
            mapRet.put(entry.getKey(), existed);
        }
        
        return mapRet;
    }
    
    public static void main(String[] arg) throws Exception
    {
        String url = "1.0/test/{hello}/work/{type}/see";
        String[] arrPox = url.split("/");
        System.out.println(arrPox.length);
        for (String pox : arrPox)
        {
            System.out.println(pox);
        }
        
        NadoRestConfig config = new NadoRestConfig();
        config.setPort(9090);
        NadoRest.instance().loadConfig(config);
    }
}
