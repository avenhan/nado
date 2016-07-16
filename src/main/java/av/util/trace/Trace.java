package av.util.trace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Trace
{
    private static Logger  logger       = LogManager.getLogger(Trace.class);
    private static boolean m_isGetTrace = true;
    
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
    
    public static String print(String text)
    {
        if (!m_isGetTrace)
        {
            return "";
        }
        
        System.out.println(text);
        return text;
    }
    
    public static String print(String format, Object... objs)
    {
        if (!m_isGetTrace)
        {
            return "";
        }
        
        if (format == null || format.length() < 1)
        {
            return "";
        }
        
        String[] arr = format.split("\\{\\}");
        StringBuilder b = new StringBuilder();
        
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
        System.out.println(ret);
        
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
