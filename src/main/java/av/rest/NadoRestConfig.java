package av.rest;

public class NadoRestConfig
{
    public static final int    KEY_MAX_EXECUTOR_THREAD = 100;
    public static final String KEY_NADO_NAME           = "NadoRest";
    public static final String KEY_BASE_URL            = "http://localhost/";
    
    private int                port;
    
    private String             name                    = KEY_NADO_NAME;
    private String             baseUrl;
    private int                executorThreadCount     = KEY_MAX_EXECUTOR_THREAD;
    
    public int getPort()
    {
        return port;
    }
    
    public void setPort(int port)
    {
        this.port = port;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public String getBaseUrl()
    {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }
    
    public int getExecutorThreadCount()
    {
        return executorThreadCount;
    }
    
    public void setExecutorThreadCount(int executorThreadCount)
    {
        this.executorThreadCount = executorThreadCount;
    }
    
}
