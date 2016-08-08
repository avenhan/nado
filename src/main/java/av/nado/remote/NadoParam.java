package av.nado.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import av.nado.util.Check;
import av.nado.util.CompareKey;
import av.nado.util.CompareType;
import av.nado.util.JsonUtil;
import av.nado.util.MDHelper;
import av.netty.Command;
import av.netty.NettyWrap;
import av.test.nado.TestRemote;
import av.util.exception.AException;
import av.util.trace.FunctionTime;
import av.util.trace.Trace;

public class NadoParam
{
    private static Logger logger = LogManager.getLogger(NadoParam.class);
    
    public static String toExplain(Object param) throws AException
    {
        FunctionTime fuTime = new FunctionTime();
        try
        {
            StringBuilder b = new StringBuilder();
            if (param == null)
            {
                b.append("null:null");
            }
            else if (param instanceof Throwable)
            {
                fuTime.addCurrentTime("type.{}", param.getClass().getName());
                b.append(Throwable.class.getName()).append(":").append(hessianEncode(param));
            }
            else if (param instanceof String)
            {
                fuTime.addCurrentTime("type.{}", param.getClass().getName());
                b.append(String.class.getName()).append(":{").append(param).append("}");
            }
            else if (param instanceof Integer || param instanceof Long || param instanceof Double || param instanceof Boolean
                    || param instanceof BigDecimal)
            {
                fuTime.addCurrentTime("type.{}", param.getClass().getName());
                b.append(param.getClass().getName()).append(":").append(param.toString());
            }
            else if (param instanceof Collection<?>)
            {
                fuTime.addCurrentTime("type.{}", param.getClass().getName());
                return createCollectionExplain(param);
            }
            else if (param instanceof Map<?, ?>)
            {
                fuTime.addCurrentTime("type.{}", param.getClass().getName());
                return createMapExplain(param);
            }
            else
            {
                fuTime.addCurrentTime("type.{}", param.getClass().getName());
                b.append(param.getClass().getName()).append(":{").append(JsonUtil.toJson(param)).append("}");
            }
            
            return b.toString();
        }
        finally
        {
            // fuTime.print();
        }
    }
    
    public static Object fromExplain(String explain) throws AException
    {
        if (Check.IfOneEmpty(explain))
        {
            throw new AException(AException.ERR_SERVER, "invalid explain param");
        }
        
        FunctionTime functionTime = new FunctionTime();
        
        int index = explain.indexOf(':');
        if (index == -1)
        {
            throw new AException(AException.ERR_SERVER, "invalid explain param");
        }
        
        String typeName = explain.substring(0, index);
        String value = explain.substring(index + 1);
        
        try
        {
            if (value.equals("null"))
            {
                return null;
            }
            
            Class<?> type = Class.forName(typeName);
            functionTime.add("type", typeName);
            
            if (value.charAt(0) == '{')
            {
                if (value.charAt(value.length() - 1) == '}')
                {
                    value = value.substring(1, value.length() - 1);
                }
                else
                {
                    throw new AException(AException.ERR_SERVER, "invalid explain param");
                }
                
                if (type.equals(String.class))
                {
                    return value;
                }
                
                if (type.equals(Collection.class))
                {
                    return explainCollection(value);
                }
                
                if (type.equals(Map.class))
                {
                    return explainMap(value);
                }
                
                Object obj = JsonUtil.toObject(type, value);
                return obj;
            }
            
            if (type.equals(Integer.class))
            {
                return Integer.parseInt(value);
            }
            
            if (type.equals(Long.class))
            {
                return Long.parseLong(value);
            }
            
            if (type.equals(Boolean.class))
            {
                return Boolean.parseBoolean(value);
            }
            
            if (type.equals(Double.class))
            {
                return Double.parseDouble(value);
            }
            
            if (type.equals(BigDecimal.class))
            {
                return new BigDecimal(value);
            }
            
            if (type.equals(Collection.class))
            {
                return hessionDecode(value);
            }
            
            if (type.equals(Map.class))
            {
                return hessionDecode(value);
            }
            
            if (type.equals(Throwable.class))
            {
                return hessionDecode(value);
            }
            
            throw new AException(AException.ERR_SERVER, "unknown explain type: {} value: {}", typeName, value);
        }
        catch (ClassNotFoundException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
        finally
        {
            // functionTime.print();
        }
    }
    
    public static String serialized(Object obj) throws AException
    {
        FunctionTime functionTime = new FunctionTime();
        if (obj == null)
        {
            return "null";
        }
        
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream os = null;
        try
        {
            os = new ObjectOutputStream(bo);
            os.writeObject(obj);
            byte[] objBytes = bo.toByteArray();
            return MDHelper.hex(objBytes);
        }
        catch (IOException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
        finally
        {
            try
            {
                if (os != null)
                {
                    os.close();
                }
                
                if (bo != null)
                {
                    bo.close();
                }
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.catching(e);
            }
            
            functionTime.print();
        }
    }
    
    public static Object deserialized(String explain) throws AException
    {
        FunctionTime functionTime = new FunctionTime();
        ByteArrayInputStream bi = null;
        ObjectInputStream oi = null;
        
        try
        {
            int length = explain.length();
            if (length == 4 && explain.equals("null"))
            {
                return null;
            }
            
            if (length % 2 == 1)
            {
                throw new AException(AException.ERR_SERVER, "invalid explain: {}", explain);
            }
            
            byte[] in = MDHelper.fromHex(explain);
            bi = new ByteArrayInputStream(in);
            
            oi = new ObjectInputStream(bi);
            Object ret = oi.readObject();
            return ret;
        }
        catch (IOException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
        catch (ClassNotFoundException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
        finally
        {
            try
            {
                if (oi != null)
                {
                    oi.close();
                }
                
                if (bi != null)
                {
                    bi.close();
                }
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.catching(e);
            }
            
            functionTime.print();
        }
    }
    
    public static String hessianEncode(Object obj) throws AException
    {
        FunctionTime functionTime = new FunctionTime();
        if (obj == null)
        {
            return "null";
        }
        
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        HessianOutput os = null;
        try
        {
            os = new HessianOutput(bo);
            os.writeObject(obj);
            byte[] objBytes = bo.toByteArray();
            return MDHelper.hex(objBytes);
        }
        catch (IOException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
        finally
        {
            try
            {
                if (os != null)
                {
                    os.close();
                }
                
                if (bo != null)
                {
                    bo.close();
                }
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.catching(e);
            }
            
            functionTime.print();
        }
    }
    
    public static Object hessionDecode(String explain) throws AException
    {
        FunctionTime functionTime = new FunctionTime();
        ByteArrayInputStream bi = null;
        HessianInput oi = null;
        
        try
        {
            int length = explain.length();
            if (length == 4 && explain.equals("null"))
            {
                return null;
            }
            
            if (length % 2 == 1)
            {
                throw new AException(AException.ERR_SERVER, "invalid explain: {}", explain);
            }
            
            byte[] in = MDHelper.fromHex(explain);
            bi = new ByteArrayInputStream(in);
            
            oi = new HessianInput(bi);
            Object ret = oi.readObject();
            return ret;
        }
        catch (IOException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
        finally
        {
            try
            {
                if (oi != null)
                {
                    oi.close();
                }
                
                if (bi != null)
                {
                    bi.close();
                }
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logger.catching(e);
            }
            
            functionTime.print();
        }
    }
    
    private static String createCollectionExplain(Object param) throws AException
    {
        StringBuilder b = new StringBuilder();
        Collection<Object> lst = (Collection<Object>) param;
        
        // to check all objects in collect has same type
        Object obj = null;
        for (Object object : lst)
        {
            if (object == null)
            {
                continue;
            }
            
            if (obj == null)
            {
                obj = object;
                continue;
            }
            
            if (!obj.getClass().equals(object.getClass()))
            {
                obj = null;
                break;
            }
        }
        
        if (obj == null)
        {
            b.append(Collection.class.getName()).append(":").append(hessianEncode(param));
        }
        else
        {
            b.append(Collection.class.getName()).append(":{");
            b.append(param.getClass().getName()).append(":");
            b.append(obj.getClass().getName()).append(":").append(JsonUtil.toJson(param));
            b.append("}");
        }
        
        return b.toString();
    }
    
    // java.util.Collection:{av.nado.test.TestRemote:[{"id":123,"name":"rmd
    // ... from"}]}
    @SuppressWarnings("unchecked")
    private static Object explainCollection(String explain) throws AException
    {
        int index = explain.indexOf(':');
        if (index == -1)
        {
            throw new AException(AException.ERR_SERVER, "invalid explain: {}", explain);
        }
        
        String typeName = explain.substring(0, index);
        String value = explain.substring(index + 1);
        
        index = value.indexOf(':');
        if (index == -1)
        {
            throw new AException(AException.ERR_SERVER, "invalid explain: {}", explain);
        }
        
        String cellTypeName = value.substring(0, index);
        value = value.substring(index + 1);
        
        try
        {
            Class<?> type = Class.forName(typeName);
            Class<?> cellType = Class.forName(cellTypeName);
            Object objCollect = type.newInstance();
            if (objCollect instanceof List<?>)
            {
                return JsonUtil.toList(cellType, value);
            }
            else if (objCollect instanceof Set<?>)
            {
                return JsonUtil.toSet(cellType, value, (Collection<Object>) objCollect);
            }
            else
            {
                throw new AException(AException.ERR_SERVER, "nado unknow collection: {}, cell type: {}", typeName, cellTypeName);
            }
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_SERVER, e);
        }
        catch (InstantiationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_SERVER, e);
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_SERVER, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static String createMapExplain(Object param) throws AException
    {
        StringBuilder b = new StringBuilder();
        Map<Object, Object> map = (Map<Object, Object>) param;
        
        // to check all objects in collect has same type
        Object objKey = null;
        Object objValue = null;
        for (Map.Entry<Object, Object> entry : map.entrySet())
        {
            Object tempKey = entry.getKey();
            Object tempV = entry.getValue();
            if (objKey == null)
            {
                objKey = tempKey;
            }
            else
            {
                if (tempKey != null && objKey != null && !objKey.getClass().equals(tempKey.getClass()))
                {
                    break;
                }
            }
            
            if (tempV == null)
            {
                continue;
            }
            
            if (objValue == null)
            {
                objValue = tempV;
                continue;
            }
            
            if (!objValue.getClass().equals(tempV.getClass()))
            {
                objValue = null;
                break;
            }
        }
        
        if (objKey == null || objValue == null)
        {
            b.append(Map.class.getName()).append(":").append(hessianEncode(param));
        }
        else
        {
            b.append(Map.class.getName()).append(":{");
            b.append(param.getClass().getName()).append(":");
            b.append(objKey.getClass().getName()).append(":");
            b.append(objValue.getClass().getName()).append(":").append(JsonUtil.toJson(param));
            b.append("}");
        }
        
        return b.toString();
    }
    
    @SuppressWarnings("unchecked")
    private static Object explainMap(String explain) throws AException
    {
        int index = explain.indexOf(':');
        if (index == -1)
        {
            throw new AException(AException.ERR_SERVER, "invalid explain: {}", explain);
        }
        
        String typeName = explain.substring(0, index);
        String value = explain.substring(index + 1);
        
        index = value.indexOf(':');
        if (index == -1)
        {
            throw new AException(AException.ERR_SERVER, "invalid explain: {}", explain);
        }
        
        String keyTypeName = value.substring(0, index);
        value = value.substring(index + 1);
        
        index = value.indexOf(':');
        if (index == -1)
        {
            throw new AException(AException.ERR_SERVER, "invalid explain: {}", explain);
        }
        
        String valueTypeName = value.substring(0, index);
        value = value.substring(index + 1);
        
        try
        {
            Class<?> type = Class.forName(typeName);
            Class<?> keyType = Class.forName(keyTypeName);
            Class<?> valueType = Class.forName(valueTypeName);
            Object objMap = type.newInstance();
            
            return JsonUtil.toMap(keyType, valueType, value, (Map<Object, Object>) objMap);
        }
        catch (ClassNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_SERVER, e);
        }
        catch (InstantiationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_SERVER, e);
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_SERVER, e);
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        Map<CompareKey, Object> mapCondition2 = new HashMap<CompareKey, Object>();
        mapCondition2.put(new CompareKey("id", CompareType.CT_BIGGER), 200);
        
        String jsonString = JsonUtil.toJson(mapCondition2);
        
        String testJson = "{{\"key\":\"id\",\"key2\":\"\",\"type\":\"CT_BIGGER\"}:200}";
        Map<Object, Object> mapDD = JsonUtil.toMap(CompareKey.class, Integer.class, jsonString, null);
        for (Object obj : mapDD.keySet())
        {
            Object objV = mapDD.get(obj);
            System.out.println(objV.toString());
        }
        
        String explain = toExplain(0);
        Object object = fromExplain(explain);
        Trace.print("explain: {} value: {}", explain, object.toString());
        
        // test map
        Map<CompareKey, Object> mapCondition = new HashMap<CompareKey, Object>();
        explain = toExplain(mapCondition);
        Object retObject = fromExplain(explain);
        Trace.print("explain: {} value: {}", explain, JsonUtil.toJson(retObject));
        
        mapCondition.put(new CompareKey("id", CompareType.CT_BIGGER), 200);
        
        explain = toExplain(mapCondition);
        System.out.println(explain);
        object = fromExplain(explain);
        if (object instanceof Map<?, ?>)
        {
            Map<Object, Object> map = (Map<Object, Object>) object;
            for (Object key : map.keySet())
            {
                Object valObject = map.get(key);
                System.out.println(key.getClass().getName() + "   " + valObject.getClass().getName());
            }
        }
        
        // test hessian
        String javaEncode = serialized(mapCondition);
        String hessian = hessianEncode(mapCondition);
        Trace.print("java len: {}  hessian len: {}", javaEncode.length(), hessian.length());
        object = hessionDecode(hessian);
        if (object instanceof Map<?, ?>)
        {
            Map<Object, Object> map = (Map<Object, Object>) object;
            for (Object key : map.keySet())
            {
                Object valObject = map.get(key);
                System.out.println(key.getClass().getName() + "   " + valObject.getClass().getName());
            }
        }
        
        // test list
        Set<TestRemote> lstTestRemotes = new HashSet<TestRemote>();
        
        explain = toExplain(lstTestRemotes);
        object = fromExplain(explain);
        Trace.print("explain: {} value: {}", explain, JsonUtil.toJson(object));
        
        TestRemote rm = new TestRemote();
        rm.setId(123);
        rm.setName("rmd ... from");
        lstTestRemotes.add(rm);
        
        explain = toExplain(lstTestRemotes);
        object = fromExplain(explain);
        if (object instanceof Collection<?>)
        {
            Collection<Object> lst = (Collection<Object>) object;
            for (Object object2 : lst)
            {
                System.out.println(object2.toString());
            }
        }
        Trace.print("explain: {} value: {}", explain, JsonUtil.toJson(object));
        
        // test others
        explain = toExplain(3.2);
        object = fromExplain(explain);
        Trace.print("explain: {} value: {}", explain, object.toString());
        
        explain = toExplain("he:llo");
        object = fromExplain(explain);
        Trace.print("explain: {} value: {}", explain, object.toString());
        
        explain = toExplain(true);
        object = fromExplain(explain);
        Trace.print("explain: {} value: {}", explain, object.toString());
        
        explain = toExplain(1234L);
        object = fromExplain(explain);
        Trace.print("explain: {} value: {}", explain, object.toString());
        
        BigDecimal decimal = new BigDecimal(120.123);
        decimal.setScale(3, BigDecimal.ROUND_HALF_UP);
        
        explain = toExplain(decimal);
        object = fromExplain(explain);
        Trace.print("explain: {} value: {}", explain, object.toString());
        
        TestRemote testRemote = new TestRemote();
        testRemote.setName("aven");
        testRemote.setId(1002);
        explain = toExplain(testRemote);
        object = fromExplain(explain);
        Trace.print("explain: {} value: {}", explain, JsonUtil.toJson(object));
        
        explain = toExplain(null);
        object = fromExplain(explain);
        Trace.print("explain: {} value: {}", explain, object);
        
        // test exception
        AException e = new AException(AException.ERR_SERVER, "fuck it");
        explain = toExplain(e);
        object = fromExplain(explain);
        Trace.print("explain: {} len: {} value: {}", explain, explain.length(), JsonUtil.toJson(object));
        
        // String jsonException = JsonUtil.toJson(e);
        // AException exception = JsonUtil.toObject(AException.class,
        // jsonException);
        // Trace.print("json exception: {}", jsonException);
        // exception.fillInStackTrace();
        
        String testJJ = "{\"command\":\"NC_USER\",\"msg\":\"av.nado.remote.NadoResponse:{{\"body\":\"java.lang.Throwable:4d74001c61762e7574696c2e657863657074696f6e2e41457863657074696f6e53000b6973457863657074696f6e4653000b6368616e676564436f646549ffffffff530009636c69656e744d73674e530006726561736f6e53000053000d64657461696c4d6573736167655300576176656e64654d6163426f6f6b2d4169722e6c6f63616c31393330383365612d633766302d343464312d613332642d3036303739343862303863353a3530303a77616e7420746f207468726f7720657863657074696f6e5300056361757365520000000053000a737461636b54726163655674001c5b6a6176612e6c616e672e537461636b5472616365456c656d656e746c0000000b4d74001b6a6176612e6c616e672e537461636b5472616365456c656d656e7453000e6465636c6172696e67436c61737353001761762e6e61646f2e746573742e546573744f757470757453000a6d6574686f644e616d65530009746573745468726f7753000866696c654e616d6553000f546573744f75747075742e6a61766153000a6c696e654e756d626572490000004b7a4d74001b6a6176612e6c616e672e537461636b5472616365456c656d656e7453000e6465636c6172696e67436c61737353002361762e6e61646f2e746573742e546573744f75747075744d6574686f6441636365737353000a6d6574686f644e616d65530006696e766f6b6553000866696c654e616d654e53000a6c696e654e756d62657249ffffffff7a4d74001b6a6176612e6c616e672e537461636b5472616365456c656d656e7453000e6465636c6172696e67436c61737353002c636f6d2e65736f7465726963736f6674776172652e7265666c65637461736d2e4d6574686f6441636365737353000a6d6574686f644e616d65530006696e766f6b6553000866696c654e616d655300114d6574686f644163636573732e6a61766153000a6c696e654e756d626572490000002c7a4d74001b6a6176612e6c616e672e537461636b5472616365456c656d656e7453000e6465636c6172696e67436c61737353001861762e6e61646f2e626173652e4e61646f4d616e6167657253000a6d6574686f644e616d6553000a696e766f6b654261736553000866696c654e616d655300104e61646f4d616e616765722e6a61766153000a6c696e654e756d626572490000007e7a4d74001b6a6176612e6c616e672e537461636b5472616365456c656d656e7453000e6465636c6172696e67436c61737353001861762e6e61646f2e626173652e4e61646f4d616e6167657253000a6d6574686f644e616d65530006696e766f6b6553000866696c654e616d655300104e61646f4d616e616765722e6a61766153000a6c696e654e756d62657249000000607a4d74001b6a6176612e6c616e672e537461636b5472616365456c656d656e7453000e6465636c6172696e67436c61737353002461762e6e61646f2e6e6574776f726b2e6e657474792e4e61646f436f6e74726f6c6c657253000a6d6574686f644e616d655300077265636569766553000866696c654e616d655300134e61646f436f6e74726f6c6c65722e6a61766153000a6c696e654e756d62657249000000457a4d74001b6a6176612e6c616e672e537461636b5472616365456c656d656e7453000e6465636c6172696e67436c61737353002461762e6e61646f2e6e6574776f726b2e6e657474792e4e61646f436f6e74726f6c6c657253000a6d6574686f644e616d655300077265636569766553000866696c654e616d655300134e61646f436f6e74726f6c6c65722e6a61766153000a6c696e654e756d62657249000000017a4d74001b6a6176612e6c616e672e537461636b5472616365456c656d656e7453000e6465636c6172696e67436c61737353001461762e6e657474792e4e65747479416374696f6e53000a6d6574686f644e616d6553000372756e53000866696c654e616d655300104e65747479416374696f6e2e6a61766153000a6c696e654e756d62657249000000467a4d74001b6a6176612e6c616e672e537461636b5472616365456c656d656e7453000e6465636c6172696e67436c6173735300276a6176612e7574696c2e636f6e63757272656e742e546872656164506f6f6c4578656375746f7253000a6d6574686f644e616d6553000972756e576f726b657253000866696c654e616d65530017546872656164506f6f6c4578656375746f722e6a61766153000a6c696e654e756d62657249000004767a4d74001b6a6176612e6c616e672e537461636b5472616365456c656d656e7453000e6465636c6172696e67436c61737353002e6a6176612e7574696c2e636f6e63757272656e742e546872656164506f6f6c4578656375746f7224576f726b657253000a6d6574686f644e616d6553000372756e53000866696c654e616d65530017546872656164506f6f6c4578656375746f722e6a61766153000a6c696e654e756d62657249000002697a4d74001b6a6176612e6c616e672e537461636b5472616365456c656d656e7453000e6465636c6172696e67436c6173735300106a6176612e6c616e672e54687265616453000a6d6574686f644e616d6553000372756e53000866696c654e616d6553000b5468726561642e6a61766153000a6c696e654e756d62657249000002e97a7a53001473757070726573736564457863657074696f6e73567400266a6176612e7574696c2e436f6c6c656374696f6e7324556e6d6f6469666961626c654c6973746c000000007a7a\"}}\",\"seq\":17}";
        
        jsonString = "{\"command\":\"NC_USER\",\"msg\":\"av.nado.remote.NadoResponse:{{\"body\":\"java.lang.Throwable:4d74001c61762e7574696c2e657863657074696f6e2e41457863657074696f6e53000b6973457863657074696f6e4653000b6368616e676564436f646549ffffffff530009636c69656e744d73674e530006726561736f6e53000053000d64657461696c4d6573736167655300476176656e64654d6163426f6f6b2d4169722e6c6f63616c64393661343661662d343065632d343166352d623865652d3039373737323034343734333a3530303a6675636b2069745300056361757365520000000053000a737461636b54726163655674001c5b6a6176612e6c616e672e537461636b5472616365456c656d656e746c000000014d74001b6a6176612e6c616e672e537461636b5472616365456c656d656e7453000e6465636c6172696e67436c61737353001861762e6e61646f2e72656d6f74652e4e61646f506172616d53000a6d6574686f644e616d655300046d61696e53000866696c654e616d6553000e4e61646f506172616d2e6a61766153000a6c696e654e756d62657249000002e37a7a53001473757070726573736564457863657074696f6e73567400266a6176612e7574696c2e436f6c6c656374696f6e7324556e6d6f6469666961626c654c6973746c000000007a7a/\"}}\",\"seq\":17}";
        jsonString = JsonUtil.readJsonString(jsonString);
        
        jsonString = JsonUtil.readJsonString(testJJ);
        Trace.print(jsonString);
        NadoResponse rspNadoResponse = new NadoResponse();
        rspNadoResponse.setBody(toExplain(e));
        NettyWrap wrap = new NettyWrap();
        wrap.setCommand(Command.NC_USER);
        wrap.setSeq(17);
        wrap.setMsg(toExplain(rspNadoResponse));
        
        jsonString = JsonUtil.toJson(wrap);
        Trace.print("json: {}", jsonString);
        jsonString = JsonUtil.readJsonString(jsonString);
        
        if (jsonString.length() < 1)
        {
            jsonString = testJJ;
        }
        
        try
        {
            wrap = JsonUtil.toObject(NettyWrap.class, jsonString);
            Trace.print("command: {}, msg: {}", wrap.getCommand(), wrap.getMsg());
        }
        catch (Exception e2)
        {
            // TODO: handle exception
            jsonString = "{\"command\":\"NC_USER\",\"msg\":\"av.nado.remote.NadoResponse:{{\"body\":\"java.lang.Throwable:4d74001c000000007a7a\"}}\",\"seq\":17}";
            jsonString = JsonUtil.readJsonString(jsonString);
        }
        
        object = fromExplain(wrap.getMsg());
        NadoResponse rspd = (NadoResponse) object;
        object = fromExplain(rspd.getBody());
        
        Trace.print("fuck on...");
    }
    
}
