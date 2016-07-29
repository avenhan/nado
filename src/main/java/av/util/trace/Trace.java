package av.util.trace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Trace
{
    private static Logger  logger       = LogManager.getLogger(Trace.class);
    private static boolean m_isGetTrace = true;
    private static boolean m_isLog      = false;
    
    public static void initialize(boolean isGetTrace)
    {
        m_isGetTrace = isGetTrace;
    }
    
    public static void setLog(boolean isLog)
    {
        m_isLog = isLog;
    }
    
    public static boolean isLog()
    {
        return m_isLog;
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
    
    public static String print(String text)
    {
        return print(true, text);
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
            b = new StringBuilder(trace.getClassName()).append(".");
            b.append(trace.getMethodName()).append("().").append(trace.getLineNumber());
            b.append(" - ");
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
        
        if (m_isLog)
        {
            logger.debug(ret);
        }
        else
        {
            System.out.println(ret);
        }
        
        return ret;
    }
    
    public static void debug(String text)
    {
        if (!m_isGetTrace)
        {
            return;
        }
        String info = print(text);
        logger.debug(info);
    }
    
    public static void debug(String format, Object... objs)
    {
        if (!m_isGetTrace)
        {
            return;
        }
        
        String info = print(format, objs);
        logger.debug(info);
    }
}
