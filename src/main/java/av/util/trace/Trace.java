package av.util.trace;

import java.io.FileInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationFactory.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

public class Trace
{
    private static Logger                  logger       = LogManager.getLogger(Trace.class);
    private static boolean                 m_isGetTrace = true;
    private static java.text.DecimalFormat df           = new java.text.DecimalFormat("#.000");
    private static double                  KEY_MILL     = 1000000.0;
    
    static
    {
        try
        {
            System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
            ConfigurationSource source = new ConfigurationSource(new FileInputStream("conf/log4j2.xml"));
            Configurator.initialize(null, source);
        }
        catch (Exception e)
        {
            logger.catching(e);
        }
    }
    
    public static void initialize(boolean isGetTrace)
    {
        m_isGetTrace = isGetTrace;
    }
    
    public static boolean isGetTrace()
    {
        return m_isGetTrace;
    }
    
    public static String _FILE_()
    {
        if (m_isGetTrace)
        {
            StackTraceElement el = ((new Exception()).getStackTrace())[1];
            return el.getFileName();
        }
        
        return "";
    }
    
    public static String _CLASS_()
    {
        if (m_isGetTrace)
        {
            StackTraceElement el = ((new Exception()).getStackTrace())[1];
            return el.getClassName();
        }
        
        return "";
    }
    
    public static String _FUN_()
    {
        if (m_isGetTrace)
        {
            StackTraceElement el = ((new Exception()).getStackTrace())[1];
            return el.getMethodName();
        }
        
        return "";
    }
    
    public static int _LINE_()
    {
        if (m_isGetTrace)
        {
            StackTraceElement el = ((new Exception()).getStackTrace())[1];
            return el.getLineNumber();
        }
        
        return 0;
    }
    
    public static String _DETAIL_()
    {
        if (m_isGetTrace)
        {
            StackTraceElement el = ((new Exception()).getStackTrace())[1];
            StringBuffer ret = new StringBuffer("[").append(el.getFileName()).append(" | ").append(el.getLineNumber()).append(" | ")
                    .append(el.getMethodName()).append("]");
            return ret.toString();
        }
        
        return "";
    }
    
    public static long getCurrentTime()
    {
        return System.nanoTime();
    }
    
    public static String getWaste(long lastTime)
    {
        long current = getCurrentTime();
        return getAsMs(current - lastTime);
    }
    
    public static String getAsMs(long wasteTime)
    {
        if (wasteTime < KEY_MILL)
        {
            StringBuilder b = new StringBuilder("0").append(df.format(wasteTime / KEY_MILL));
            return b.toString();
        }
        return df.format(wasteTime / KEY_MILL);
    }
    
    public static String print(Object text)
    {
        if (text == null)
        {
            return print(true, "null");
        }
        
        return print(true, text.toString());
    }
    
    public static String print(String format, Object... objs)
    {
        return print(true, format, objs);
    }
    
    /**
     * 
     * @param isTrace
     *            need to record
     * @param format
     * @param objs
     * @return
     */
    protected static String print(boolean isTrace, String format, Object... objs)
    {
        if (!m_isGetTrace)
        {
            return "";
        }
        
        if (format == null || format.length() < 1)
        {
            return "";
        }
        
        StackTraceElement trace = ((new Exception()).getStackTrace())[2];
        String[] arr = format.split("\\{\\}");
        StringBuilder b = null;
        if (isTrace)
        {
            b = new StringBuilder(trace.getClassName()).append(" ");
            b.append(trace.getLineNumber()).append(" ");
            b.append(trace.getMethodName()).append(" - ");
        }
        else
        {
            b = new StringBuilder();
        }
        
        int index = 0;
        for (Object obj : objs)
        {
            if (index < arr.length)
            {
                b.append(arr[index]);
            }
            
            index++;
            if (obj == null)
            {
                b.append("null");
            }
            else
            {
                b.append(obj);
            }
        }
        
        for (int i = index; i < arr.length; i++)
        {
            b.append(arr[i]);
        }
        
        String ret = b.toString();
        if (!isTrace)
        {
            return ret;
        }
        
        logger.debug(ret);
        return ret;
    }
}
