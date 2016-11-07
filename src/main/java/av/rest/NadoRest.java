package av.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.RestExpress;

import av.nado.network.http.NadoHttpController;
import av.nado.util.Aggregate;
import av.nado.util.Check;
import av.nado.util.CompareKey;
import av.nado.util.CompareType;
import av.nado.util.JsonUtil;
import av.nado.util.ParameterNameUtils;
import av.rest.preprocessor.LogMessageObserver;
import av.rest.preprocessor.NadoExceptionMapping;
import av.rest.preprocessor.RestPreprocessor;
import av.util.exception.AException;
import av.util.trace.FunctionTime;
import io.netty.channel.ChannelHandler;

public class NadoRest
{
    private static Logger                   logger        = LogManager.getLogger(NadoRest.class);
    
    private static NadoRest                 m_pThis;
    private RestExpress                     m_restExpress;
    
    private Map<String, NadoRestController> m_mapRestInfo = new HashMap<String, NadoRestController>();
    
    public static NadoRest instance()
    {
        if (m_pThis == null)
        {
            m_pThis = new NadoRest();
        }
        
        return m_pThis;
    }
    
    public void loadConfig(NadoRestConfig config, Object... controllers) throws AException
    {
        FunctionTime functionTime = new FunctionTime();
        Set<Object> setControllers = new HashSet<Object>();
        
        for (Object controller : controllers)
        {
            if (controller == null)
            {
                throw new AException(AException.ERR_SERVER, "invalid parameters");
            }
            
            setControllers.add(controller);
        }
        
        loadConfig(config, setControllers);
        functionTime.print();
    }
    
    public void loadConfig(NadoRestConfig config, Collection<?> lstObjects) throws AException
    {
        if (Check.IfOneEmpty(config, lstObjects))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        m_restExpress = new RestExpress();
        m_restExpress.setBaseUrl(config.getBaseUrl());
        m_restExpress.setPort(config.getPort());
        m_restExpress.setName(config.getName());
        m_restExpress.setExecutorThreadCount(config.getExecutorThreadCount());
        m_restExpress.setMaxContentSize(Integer.MAX_VALUE);
        
        m_restExpress.addMessageObserver(new LogMessageObserver());
        m_restExpress.setExceptionMap(new NadoExceptionMapping());
        
        m_restExpress.addFinallyProcessor(new RestPreprocessor());
        
        for (Object object : lstObjects)
        {
            if (object == null)
            {
                throw new AException(AException.ERR_SERVER, "invalid parameters");
            }
            
            try
            {
                addController(config, object);
            }
            catch (Exception e)
            {
                throw new AException(AException.ERR_FATAL, e);
            }
        }
        
        initializeMostLike();
        
        // test add new channel channdler
        try
        {
            ChannelHandler handler = m_restExpress.buildRequestHandler();
            handler.handlerAdded(new TestChannelHandle());
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_FATAL, e);
        }
        
        m_restExpress.bind(config.getPort());
        // m_restExpress.awaitShutdown();
        
        Runtime runtime = Runtime.getRuntime();
        Thread thread = new Thread(new Runnable()
        {
            
            public void run()
            {
                System.out.println("server is shutdown...");
            }
        });
        runtime.addShutdownHook(thread);
    }
    
    private void addController(NadoRestConfig config, Object object) throws Exception
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
            StringBuilder b = new StringBuilder(uri);
            
            if (m_mapRestInfo.containsKey(b.toString()))
            {
                RestInfo existedInfo = m_mapRestInfo.get(b.toString()).getInfo();
                if (existedInfo != null && existedInfo.getMethod().equals(rest.method().toLowerCase()))
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
            info.setParamInfos(getMethodParamInfos(type, method));
            
            NadoRestController controller = new NadoRestController();
            controller.addRestInformation(info);
            controller.setInfo(info);
            
            if (info.isUpstream())
            {
                m_restExpress.uploadUri(b.toString(), controller);
            }
            else if (info.isDownstream())
            {
                m_restExpress.downloadUri(b.toString(), controller);
            }
            else
            {
                m_restExpress.uri(b.toString(), controller).method(info.getHttpMethod());
            }
            
            m_mapRestInfo.put(b.toString(), controller);
        }
    }
    
    private RestParamInfo[] getMethodParamInfos(Class<?> type, Method method) throws Exception
    {
        if (method.getName().equals("testPostIdx"))
        {
            System.out.println("meet up");
        }
        
        List<String> paramNames = ParameterNameUtils.getMethodParameterNamesByAsm4(type, method);
        Parameter[] params = method.getParameters();
        RestParamInfo[] paramInfos = new RestParamInfo[params.length];
        
        logger.debug("fun: {} params: {}", method.getName(), paramNames.toString());
        
        for (int i = 0; i < params.length; i++)
        {
            RestParamInfo paramInfo = new RestParamInfo();
            Parameter param = params[i];
            Class<?> paramType = param.getType();
            Type implType = param.getParameterizedType();
            
            paramInfo.setContains(getParamImplTypes(paramType, implType));
            
            paramInfo.setParam(param);
            paramInfo.setType(paramType);
            RestParam restParam = param.getAnnotation(RestParam.class);
            if (restParam != null)
            {
                if (Check.IfOneEmpty(restParam.key()))
                {
                    String key = paramNames.get(i);
                    paramInfo.setName(key);
                    paramInfo.setKey(makeRestParam(key, restParam));
                }
                else
                {
                    paramInfo.setName(restParam.key());
                    paramInfo.setKey(restParam);
                }
                
                paramInfos[i] = paramInfo;
                continue;
            }
            
            if (paramType.equals(Response.class) || paramType.equals(Request.class))
            {
                paramInfo.setKey(null);
                paramInfos[i] = paramInfo;
                continue;
            }
            
            if (!paramType.isPrimitive() && !paramType.equals(Collection.class) && !paramType.equals(Map.class))
            {
                try
                {
                    Object objType = paramType.newInstance();
                    if (objType instanceof ApiRequest || objType instanceof Response || objType instanceof Request)
                    {
                        paramInfo.setKey(null);
                        paramInfos[i] = paramInfo;
                        continue;
                    }
                }
                catch (Exception e)
                {
                    // TODO: handle exception
                    logger.catching(e);
                }
            }
            
            String key = paramNames.get(i);
            restParam = makeRestParam(key, null);
            paramInfo.setName(key);
            paramInfo.setKey(restParam);
            paramInfos[i] = paramInfo;
        }
        
        return paramInfos;
    }
    
    private List<Class<?>> getParamImplTypes(Class<?> paramType, Type implType) throws Exception
    {
        List<Class<?>> lstRet = new ArrayList<Class<?>>();
        if (paramType.isPrimitive())
        {
            return lstRet;
        }
        
        if (paramType.isArray())
        {
            return lstRet;
        }
        
        String implName = implType.getTypeName();
        int indFrom = implName.indexOf('<');
        int indTo = implName.indexOf('>');
        if (indFrom < 0 || indTo < 0 || indFrom >= indTo)
        {
            return lstRet;
        }
        
        String left = implName.substring(indFrom + 1, indTo);
        List<String> lstTypeNames = Check.getAsList(left);
        for (String typeName : lstTypeNames)
        {
            lstRet.add(Class.forName(typeName));
        }
        
        return lstRet;
    }
    
    private RestParam makeRestParam(final String key, final RestParam orgParam)
    {
        RestParam ret = new RestParam()
        {
            public Class<? extends Annotation> annotationType()
            {
                if (orgParam != null)
                {
                    return orgParam.annotationType();
                }
                return null;
            }
            
            public boolean required()
            {
                if (orgParam != null)
                {
                    return orgParam.required();
                }
                
                return true;
            }
            
            public String key()
            {
                return key;
            }
            
            public String defaultValue()
            {
                if (orgParam != null)
                {
                    return orgParam.defaultValue();
                }
                
                return "";
            }
        };
        
        return ret;
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
    
    // curl -i localhost:9009/test/idx -X POST
    // -d'{"hello":"aven","saves":[{"id":100,"value":"world_0"},{"id":101,"value":"world_1"},{"id":102,"value":"world_2"}],"maps":{{"key":"key_i","key2":"","type":"CT_BIGGER"}:{"id":1001,"value":"map_1"}}}'
    // -H "Authorization:Bear 000-xxx-bbb-aaa"
    public static void main(String[] arg) throws Exception
    {
        Test test = new Test();
        test.setId(9090);
        test.setValue("hello world");
        Aggregate<CompareKey, Test> aggregate = new Aggregate<CompareKey, Test>();
        aggregate.put(new CompareKey("max", CompareType.CT_NOTBIGGER), test);
        
        String json = JsonUtil.toJson(aggregate);
        System.out.println(json);
        
        // Aggregate<CompareKey, Test> tt =
        // JsonUtil.toObject(aggregate.getClass(), json, CompareKey.class,
        // Test.class);
        
        ClassFileScanner scan = new ClassFileScanner("av.nado.network.http");
        scan.getFullyQualifiedClassNameList();
        
        NadoRestConfig config = new NadoRestConfig();
        config.setPort(9009);
        NadoRest.instance().loadConfig(config, new NadoHttpController());
        
        System.out.println("is working...");
    }
}
