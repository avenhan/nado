package av.util.exception;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import av.nado.util.Check;
import av.util.trace.Trace;

public class AException extends Exception
{
    /**
     * 
     */
    private static final long           serialVersionUID             = -1757404543965572726L;
    
    public static final int             ERR_UNKNOWN                  = 0;
    public static final int             ERR_FATAL                    = 1;
    public static final int             ERR_INVALID_TOKEN            = 401;
    public static final int             ERR_NOT_FOUND                = 404;
    public static final int             ERR_JSON_CAST                = 4;
    public static final int             ERR_INVALID_SERVER_PARAMETER = 5;
    
    public static final int             ERR_SUCCESS                  = 200;
    public static final int             ERR_NOT_MOIDIFIED            = 304;
    public static final int             ERR_INVALID_PARAMETER        = 400;
    public static final int             ERR_USER_FORBIDDEN           = 403;
    public static final int             ERR_SERVER                   = 500;
    public static final int             ERR_CONFLICT                 = 409;
    
    private static Logger               logger                       = LogManager.getLogger(AException.class);
    
    private boolean                     isException                  = false;
    private int                         changedCode                  = -1;
    private String                      clientMsg                    = null;
    private static Map<Integer, String> m_mapCodeInfo                = new HashMap<Integer, String>();
    private static String               idPrefix                     = getLocalHost();
    private String                      reason                       = "";
    
    public static void initialize(String idPrefix, Map<Integer, String> mapIdError)
    {
        if (Check.IfOneEmpty(AException.idPrefix))
        {
            AException.idPrefix = idPrefix;
        }
        
        m_mapCodeInfo.put(ERR_UNKNOWN, "unknown");
        m_mapCodeInfo.put(ERR_INVALID_TOKEN, "invalid token");
        m_mapCodeInfo.put(ERR_NOT_FOUND, "not found");
        m_mapCodeInfo.put(ERR_SERVER, "server error");
        m_mapCodeInfo.put(ERR_JSON_CAST, "json cast error");
        m_mapCodeInfo.put(ERR_INVALID_PARAMETER, "invalid parameter");
        m_mapCodeInfo.put(ERR_INVALID_SERVER_PARAMETER, "invalid parameter in server function");
        
        if (mapIdError != null)
        {
            m_mapCodeInfo.putAll(mapIdError);
        }
    }
    
    private static String getLocalHost()
    {
        try
        {
            InetAddress address = InetAddress.getLocalHost();
            String host = address.getHostName();
            String ip = address.getHostAddress();
            logger.error("local host={}, ip={}.", host, ip);
            return host;
        }
        catch (Exception ignore)
        {
        }
        return "";
    }
    
    public AException(int code)
    {
        super(getExceptionMessage(null, code));
        setTrackTraceLastLine(((new Exception()).getStackTrace()));
        outputLog();
    }
    
    public AException(String id, int code)
    {
        super(getExceptionMessage(id, code));
        setTrackTraceLastLine(((new Exception()).getStackTrace()));
        outputLog();
    }
    
    public AException(int code, String reasonFormat, Object... objs)
    {
        super(getExceptionMessage(null, code, reasonFormat, objs));
        setTrackTraceLastLine(((new Exception()).getStackTrace()));
        outputLog();
    }
    
    public AException(String id, int code, String reasonFormat, Object... objs)
    {
        super(getExceptionMessage(id, code, reasonFormat, objs));
        setTrackTraceLastLine(((new Exception()).getStackTrace()));
        outputLog();
    }
    
    public AException(int code, Throwable e)
    {
        super(getExceptionMessage(null, code, e, null));
        setStackTrace(e.getStackTrace());
        isException = true;
        
        outputLog();
    }
    
    public AException(String id, int code, Throwable e)
    {
        super(getExceptionMessage(id, code, e, null));
        setStackTrace(e.getStackTrace());
        isException = true;
        outputLog();
    }
    
    public AException(int code, Throwable e, String reasonFormat, Object... objs)
    {
        super(getExceptionMessage(null, code, e, reasonFormat, objs));
        setStackTrace(e.getStackTrace());
        isException = true;
        this.clientMsg = Trace.print(reasonFormat, objs);
        outputLog();
    }
    
    public AException(String id, int code, Throwable e, String reasonFormat, Object... objs)
    {
        super(getExceptionMessage(id, code, e, reasonFormat, objs));
        setStackTrace(e.getStackTrace());
        isException = true;
        this.clientMsg = Trace.print(reasonFormat, objs);
        outputLog();
    }
    
    public String getId()
    {
        String info = super.getMessage();
        if (info == null)
        {
            return "";
        }
        
        String[] arrInfo = info.split(":", 3);
        if (arrInfo.length != 3)
        {
            return "";
        }
        
        return arrInfo[0];
    }
    
    public int getCode()
    {
        if (changedCode != -1)
        {
            return changedCode;
        }
        
        String info = super.getMessage();
        if (info == null)
        {
            return 0;
        }
        
        String[] arrInfo = info.split(":", 3);
        if (arrInfo.length != 3)
        {
            return 0;
        }
        
        try
        {
            return Integer.parseInt(arrInfo[1]);
        }
        catch (Exception e)
        {
            return 0;
        }
    }
    
    public void setCode(int code)
    {
        this.changedCode = code;
    }
    
    public String getRealMessage()
    {
        String info = super.getMessage();
        if (info == null)
        {
            return "";
        }
        
        String[] arrInfo = info.split(":", 3);
        if (arrInfo.length != 3)
        {
            return "";
        }
        
        if (Check.IfOneEmpty(clientMsg))
        {
            return arrInfo[2];
        }
        
        info = arrInfo[2];
        return info.substring(clientMsg.length());
    }
    
    public String getClientMessage()
    {
        if (!Check.IfOneEmpty(clientMsg))
        {
            return clientMsg;
        }
        
        String info = super.getMessage();
        if (info == null)
        {
            return "";
        }
        
        String[] arrInfo = info.split(":", 3);
        if (arrInfo.length != 3)
        {
            return "";
        }
        
        if (!this.isException)
        {
            return arrInfo[2];
        }
        
        try
        {
            int code = Integer.parseInt(arrInfo[1]);
            return getCodeAsString(code);
        }
        catch (Exception e)
        {
            return arrInfo[2];
        }
    }
    
    private static String getExceptionMessage(String id, int code)
    {
        return getExceptionMessage(id, code, "");
    }
    
    private static String getExceptionMessage(String id, int code, String reasonFormat, Object... objs)
    {
        String uuid = id;
        if (Check.IfOneEmpty(uuid))
        {
            uuid = UUID.randomUUID().toString();
        }
        StringBuilder ret = new StringBuilder(idPrefix).append(uuid).append(":").append(code).append(":").append(Trace.print(reasonFormat, objs));
        return ret.toString();
    }
    
    private static String getExceptionMessage(String id, int code, Throwable e, String reasonFormat, Object... objs)
    {
        String uuid = id;
        if (Check.IfOneEmpty(uuid))
        {
            uuid = UUID.randomUUID().toString();
        }
        
        String clientMsg = Trace.print(reasonFormat, objs);
        StringBuilder ret = new StringBuilder(idPrefix).append(uuid).append(":").append(code).append(":");
        if (!Check.IfOneEmpty(clientMsg))
        {
            ret.append(clientMsg).append(":");
        }
        ret.append(e.getClass().getName()).append(" ");
        ret.append(e.getMessage());
        return ret.toString();
    }
    
    private static String getCodeAsString(int code)
    {
        String info = m_mapCodeInfo.get(code);
        if (info == null)
        {
            info = "unknown error type";
        }
        
        return info;
    }
    
    private void setTrackTraceLastLine(StackTraceElement[] arrTrace)
    {
        StackTraceElement[] ret = new StackTraceElement[arrTrace.length - 1];
        for (int i = 1; i < arrTrace.length; i++)
        {
            ret[i - 1] = arrTrace[i];
        }
        
        setStackTrace(ret);
    }
    
    public String getReason()
    {
        return reason;
    }
    
    private void outputLog()
    {
        logger.catching(this);
    }
}
