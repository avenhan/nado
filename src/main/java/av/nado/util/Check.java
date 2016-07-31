package av.nado.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import av.util.exception.AException;

public class Check
{
    public static final String  AUTHORIZATION_TYPE  = "Bearer";
    private static final String AUTHORIZATION_REGEX = "^Bearer\\s+[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    private static final String UUID_REGEX          = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    private static final String TOKEN_REGEX         = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    private static final String EMAIL_REGEX         = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    private static final String HTTP_REGEX          = "(http://|https://){1}[\\w\\.\\-/:]+";
    
    private static final String IP_REGEX            = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\." + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
    
    public static final String  KEY_TIME_FORMAT     = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    
    public static class MapKeyComparator implements Comparator<Object>
    {
        public int compare(Object a, Object b)
        {
            return a.toString().compareTo(b.toString());
        }
    }
    
    private static Logger logger = LogManager.getLogger(Check.class);
    
    public static boolean isUUID(String uuid)
    {
        if (!patternMatch(uuid, UUID_REGEX, true))
        {
            return false;
        }
        
        return true;
    }
    
    public static boolean isEmail(String email)
    {
        if (!patternMatch(email, EMAIL_REGEX, true))
        {
            return false;
        }
        
        return true;
    }
    
    public static boolean isHttp(String http)
    {
        if (!patternMatch(http, HTTP_REGEX, true))
        {
            return false;
        }
        
        return true;
    }
    
    public static boolean isIpAddr(String ip)
    {
        if (!patternMatch(ip, IP_REGEX, true))
        {
            return false;
        }
        
        return true;
    }
    
    public static String getToken(String auth)
    {
        if (!patternMatch(auth, AUTHORIZATION_REGEX, true))
        {
            return "";
        }
        
        String token = "";
        try
        {
            Pattern p = Pattern.compile(TOKEN_REGEX, Pattern.CASE_INSENSITIVE);
            Matcher matcher = p.matcher(auth);
            while (matcher.find())
            {
                token = matcher.group();
            }
        }
        catch (Exception e)
        {
            logger.error("Regex match failed. More info: {}", e.toString());
        }
        
        return token;
    }
    
    public static boolean patternMatch(String str, String pattern, boolean caseInsensitive)
    {
        if (IfOneEmpty(str, pattern))
        {
            return false;
        }
        
        try
        {
            int flags = 0;
            if (caseInsensitive)
            {
                flags = Pattern.CASE_INSENSITIVE;
            }
            Pattern p = Pattern.compile(pattern, flags);
            Matcher matcher = p.matcher(str);
            
            return matcher.find();
        }
        catch (Throwable e)
        {
            logger.error("Regex match failed. More info: {}", e.toString());
            return false;
        }
    }
    
    public static boolean onlyOneNotEmpty(Object... objs)
    {
        int notEmptyCount = 0;
        for (Object obj : objs)
        {
            if (!IfOneEmpty(obj))
            {
                notEmptyCount++;
            }
        }
        
        return (notEmptyCount == 1);
    }
    
    public static boolean IfOneEmpty(Object... objs)
    {
        for (Object key : objs)
        {
            if (key == null)
            {
                return true;
            }
            
            if (key instanceof String)
            {
                String keyString = (String) key;
                if (keyString.isEmpty())
                {
                    return true;
                }
                continue;
            }
            
            if (key instanceof Collection<?>)
            {
                Collection<?> type = (Collection<?>) key;
                if (type.isEmpty())
                {
                    return true;
                }
                continue;
            }
            
            if (key instanceof Map<?, ?>)
            {
                Map<?, ?> type = (Map<?, ?>) key;
                if (type.isEmpty())
                {
                    return true;
                }
                continue;
            }
            
            if (key instanceof Object[])
            {
                Object[] arrKey = (Object[]) key;
                if (arrKey.length < 1)
                {
                    return true;
                }
                continue;
            }
        }
        
        return false;
    }
    
    public static void conditionThrow(boolean ifCond, StackTraceElement line, int err, String info) throws AException
    {
        if (info == null || info.equals(""))
        {
            throw new AException(AException.ERR_SERVER, "invalid condition throw parameter info");
        }
        
        if (ifCond)
        {
            throw new AException(err, info);
        }
    }
    
    public static boolean IfOneNull(Object... objs)
    {
        for (Object key : objs)
        {
            if (key == null)
            {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean IfAllEmpty(Object... objs)
    {
        for (Object key : objs)
        {
            if (key == null)
            {
                continue;
            }
            
            if (key instanceof String)
            {
                String keyString = (String) key;
                if (!keyString.isEmpty())
                {
                    return false;
                }
                continue;
            }
            
            if (key instanceof Collection<?>)
            {
                Collection<?> type = (Collection<?>) key;
                if (!type.isEmpty())
                {
                    return false;
                }
                continue;
            }
            
            if (key instanceof Map<?, ?>)
            {
                Map<?, ?> type = (Map<?, ?>) key;
                if (!type.isEmpty())
                {
                    return false;
                }
                continue;
            }
            
            if (key instanceof Object[])
            {
                Object[] arrKey = (Object[]) key;
                if (arrKey.length > 0)
                {
                    return false;
                }
                continue;
            }
        }
        
        return true;
    }
    
    public static boolean ifIn(Object obj, Collection<?> objs)
    {
        for (Object obj2 : objs)
        {
            if (obj == null && obj2 == null)
            {
                return true;
            }
            
            if (obj == null || obj2 == null)
            {
                continue;
            }
            
            if (!obj.getClass().equals(obj2.getClass()))
            {
                continue;
            }
            
            if (obj == obj2)
            {
                return true;
            }
            
            if (obj.toString().equals(obj2.toString()))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean ifIn(Object obj, Object... objs)
    {
        for (Object obj2 : objs)
        {
            if (obj == null && obj2 == null)
            {
                return true;
            }
            
            if (obj == null || obj2 == null)
            {
                continue;
            }
            
            if (obj2 instanceof Collection<?>)
            {
                if (ifIn(obj, (Collection<?>) obj2))
                {
                    return true;
                }
            }
            
            if (!obj.getClass().equals(obj2.getClass()))
            {
                continue;
            }
            
            if (obj == obj2)
            {
                return true;
            }
            
            if (obj.toString().equals(obj2.toString()))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean toBoolean(String value)
    {
        if (Check.IfOneEmpty(value))
        {
            return false;
        }
        
        if (value.equalsIgnoreCase("1"))
        {
            return true;
        }
        else if (value.equalsIgnoreCase("0"))
        {
            return false;
        }
        
        return Boolean.parseBoolean(value);
    }
    
    public static void getAsCollects(Collection<String> collect, String value)
    {
        if (Check.IfOneEmpty(value) || collect == null)
        {
            return;
        }
        
        Pattern p = Pattern.compile("[a-zA-Z0-9_.-]*[^,;\t\r\n\\s*]");
        Matcher m = p.matcher(value);
        
        while (m.find())
        {
            String get = m.group();
            if (Check.IfOneEmpty(get))
            {
                continue;
            }
            
            collect.add(get);
        }
    }
    
    public static List<String> getAsList(String value)
    {
        List<String> lstRet = new ArrayList<String>();
        getAsCollects(lstRet, value);
        
        return lstRet;
    }
    
    public static <K, V> Map<K, V> sortMapByKey(Map<K, V> map)
    {
        if (map == null || map.isEmpty())
        {
            return map;
        }
        
        Map<K, V> sortMap = new TreeMap<K, V>(new MapKeyComparator());
        sortMap.putAll(map);
        return sortMap;
    }
    
    public static Set<String> sortSet(Set<String> set)
    {
        if (set == null || set.isEmpty() || set.size() == 1)
        {
            return set;
        }
        
        Set<String> sortMap = new TreeSet<String>(new MapKeyComparator());
        sortMap.addAll(set);
        
        return sortMap;
    }
    
    public boolean isChineseChar(String str)
    {
        char[] chars = str.toCharArray();
        boolean isGB2312 = false;
        for (int i = 0; i < chars.length; i++)
        {
            byte[] bytes = ("" + chars[i]).getBytes();
            if (bytes.length == 2)
            {
                int[] ints = new int[2];
                ints[0] = bytes[0] & 0xff;
                ints[1] = bytes[1] & 0xff;
                
                if (ints[0] >= 0x81 && ints[0] <= 0xFE && ints[1] >= 0x40 && ints[1] <= 0xFE)
                {
                    isGB2312 = true;
                    break;
                }
            }
        }
        
        return isGB2312;
    }
    
    public int toInt(Object obj) throws AException
    {
        if (obj == null)
        {
            return 0;
        }
        
        if (obj instanceof Integer)
        {
            return (Integer) obj;
        }
        
        if (obj instanceof Boolean)
        {
            if ((Boolean) obj)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        
        if (obj instanceof String)
        {
            String vv = (String) obj;
            if (vv.equalsIgnoreCase("true"))
            {
                return 1;
            }
            
            if (vv.equalsIgnoreCase("false"))
            {
                return 0;
            }
            
            long lv = Long.parseLong(vv);
            return (int) lv;
        }
        
        return 0;
    }
    
    public static Object add(Object obj, double increase) throws AException
    {
        if (obj == null)
        {
            obj = increase;
            return obj;
        }
        
        if (obj instanceof Integer)
        {
            int ret = (Integer) obj;
            return (ret + increase);
        }
        
        if (obj instanceof Long)
        {
            long ret = (Long) obj;
            return (ret + increase);
        }
        
        if (obj instanceof Double)
        {
            double ret = (Double) obj;
            return (ret + increase);
        }
        
        throw new AException(AException.ERR_SERVER, "invalid value: add ");
    }
    
    public static long toTime(String value)
    {
        if (value == null || value.length() < 1)
        {
            return 0;
        }
        
        value = value.toLowerCase();
        int index = value.indexOf("second");
        if (index >= 0)
        {
            String s = value.substring(0, index);
            List<String> lst = getAsList(s);
            if (lst == null || lst.isEmpty())
            {
                return 0;
            }
            
            return Long.parseLong(lst.get(0)) * 1000;
        }
        
        index = value.indexOf("minute");
        if (index >= 0)
        {
            String s = value.substring(0, index);
            List<String> lst = getAsList(s);
            if (lst == null || lst.isEmpty())
            {
                return 0;
            }
            
            return Long.parseLong(lst.get(0)) * 60 * 1000;
        }
        
        index = value.indexOf("hour");
        if (index >= 0)
        {
            String s = value.substring(0, index);
            List<String> lst = getAsList(s);
            if (lst == null || lst.isEmpty())
            {
                return 0;
            }
            
            return Long.parseLong(lst.get(0)) * 60 * 60 * 1000;
        }
        
        index = value.indexOf("day");
        if (index >= 0)
        {
            String s = value.substring(0, index);
            List<String> lst = getAsList(s);
            if (lst == null || lst.isEmpty())
            {
                return 0;
            }
            
            return Long.parseLong(lst.get(0)) * 24 * 60 * 60 * 1000;
        }
        
        long ret = 0;
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(value.toLowerCase());
        while (m.find())
        {
            String get = m.group();
            if (Check.IfOneEmpty(get))
            {
                continue;
            }
            
            ret = Long.parseLong(get) * 1000;
            break;
        }
        
        return ret;
    }
    
    public static boolean compare(Object a, CompareType t, Object b)
    {
        if (a == null || b == null || t == null)
        {
            return false;
        }
        
        switch (t)
        {
            case CT_BIGGER:
                if (a instanceof Integer)
                {
                    int vv = Integer.parseInt(b.toString());
                    return ((Integer) a > vv);
                }
                else if (a instanceof Long)
                {
                    Long vv = Long.parseLong(b.toString());
                    return ((Long) a > vv);
                }
                else if (a instanceof Boolean)
                {
                    return false;
                }
                else if (a instanceof Double)
                {
                    Double vv = Double.parseDouble(b.toString());
                    return ((Double) a > vv);
                }
                else if (a instanceof String)
                {
                    String vv = (String) a;
                    return vv.compareTo(b.toString()) > 0;
                }
                break;
            
            case CT_EQUAL:
                if (a instanceof Integer)
                {
                    int vv = Integer.parseInt(b.toString());
                    return ((Integer) a == vv);
                }
                else if (a instanceof Long)
                {
                    Long vv = Long.parseLong(b.toString());
                    return ((Long) a == vv);
                }
                else if (a instanceof Boolean)
                {
                    Boolean vv = Boolean.parseBoolean(b.toString());
                    return ((Boolean) a == vv);
                }
                else if (a instanceof Double)
                {
                    Double vv = Double.parseDouble(b.toString());
                    return ((Double) a == vv);
                }
                else if (a instanceof String)
                {
                    String vv = (String) a;
                    return vv.compareTo(b.toString()) == 0;
                }
                break;
            
            case CT_SMALLER:
                if (a instanceof Integer)
                {
                    int vv = Integer.parseInt(b.toString());
                    return ((Integer) a < vv);
                }
                else if (a instanceof Long)
                {
                    Long vv = Long.parseLong(b.toString());
                    return ((Long) a < vv);
                }
                else if (a instanceof Boolean)
                {
                    return false;
                }
                else if (a instanceof Double)
                {
                    Double vv = Double.parseDouble(b.toString());
                    return ((Double) a < vv);
                }
                else if (a instanceof String)
                {
                    String vv = (String) a;
                    return vv.compareTo(b.toString()) < 0;
                }
                break;
            
            case CT_NOTSMALLER:
                if (a instanceof Integer)
                {
                    int vv = Integer.parseInt(b.toString());
                    return ((Integer) a >= vv);
                }
                else if (a instanceof Long)
                {
                    Long vv = Long.parseLong(b.toString());
                    return ((Long) a >= vv);
                }
                else if (a instanceof Boolean)
                {
                    return false;
                }
                else if (a instanceof Double)
                {
                    Double vv = Double.parseDouble(b.toString());
                    return ((Double) a >= vv);
                }
                else if (a instanceof String)
                {
                    String vv = (String) a;
                    return vv.compareTo(b.toString()) >= 0;
                }
                break;
            
            case CT_NOTBIGGER:
                if (a instanceof Integer)
                {
                    int vv = Integer.parseInt(b.toString());
                    return ((Integer) a <= vv);
                }
                else if (a instanceof Long)
                {
                    Long vv = Long.parseLong(b.toString());
                    return ((Long) a <= vv);
                }
                else if (a instanceof Boolean)
                {
                    return false;
                }
                else if (a instanceof Double)
                {
                    Double vv = Double.parseDouble(b.toString());
                    return ((Double) a <= vv);
                }
                else if (a instanceof String)
                {
                    String vv = (String) a;
                    return vv.compareTo(b.toString()) <= 0;
                }
                break;
            
            case CT_NOTEQUAL:
                if (a instanceof Integer)
                {
                    int vv = Integer.parseInt(b.toString());
                    return ((Integer) a != vv);
                }
                else if (a instanceof Long)
                {
                    Long vv = Long.parseLong(b.toString());
                    return ((Long) a != vv);
                }
                else if (a instanceof Boolean)
                {
                    Boolean vv = Boolean.parseBoolean(b.toString());
                    return ((Boolean) a != vv);
                }
                else if (a instanceof Double)
                {
                    Double vv = Double.parseDouble(b.toString());
                    return ((Double) a != vv);
                }
                else if (a instanceof String)
                {
                    String vv = (String) a;
                    return vv.compareTo(b.toString()) != 0;
                }
                break;
            
            default:
                break;
        }
        
        return false;
    }
    
    public static <T> T as(Class<T> a, Object b)
    {
        if (a == null || b == null)
        {
            return null;
        }
        
        if (a.equals(Integer.class))
        {
            int vv = Integer.parseInt(b.toString());
            return a.cast(vv);
        }
        else if (a.equals(Long.class))
        {
            Long vv = Long.parseLong(b.toString());
            return a.cast(vv);
        }
        else if (a.equals(Boolean.class))
        {
            Boolean vv = Boolean.parseBoolean(b.toString());
            return a.cast(vv);
        }
        else if (a.equals(Double.class))
        {
            Double vv = Double.parseDouble(b.toString());
            return a.cast(vv);
        }
        else if (a.equals(String.class))
        {
            String vv = b.toString();
            return a.cast(vv);
        }
        
        return a.cast(b);
    }
    
    public static String toUTCString(long time)
    {
        java.util.Date date = new Date(time);
        
        DateFormat dateFormat = new SimpleDateFormat(KEY_TIME_FORMAT);
        String date_time = dateFormat.format(date);
        
        return date_time;
    }
}
