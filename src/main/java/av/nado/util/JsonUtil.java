package av.nado.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import av.util.exception.AException;

public class JsonUtil
{
    private static ObjectMapper objectMapper = new ObjectMapper();
    
    /*
     *  object to json string
     */
    public static String toJson(Object obj) throws AException
    {
        if (obj == null)
        {
            return "";
        }
        
        return JSON.toJSONString(obj);
    }
    
    /*
     *  json string to object as T
     */
    public static <T> T toObject(Class<T> type, String json) throws AException
    {
        if (type == null || json == null || json.isEmpty())
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        return JSON.parseObject(json, type);
    }
    
    /*
     *  json string to list of T
     */
    public static <T> List<T> toList(Class<T> type, String json) throws AException
    {
        if (type == null || json == null || json.isEmpty())
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        return JSON.parseArray(json, type);
    }
    
    public static <T> Collection<Object> toSet(Class<T> type, String json, Collection<Object> collections) throws AException
    {
        try
        {
            JSONArray jArray = JSONArray.parseArray(json);
            for (Object object : jArray)
            {
                String cellJson = toJson(object);
                T t = toTypeObject(type, cellJson);
                collections.add(t);
            }
            
            return collections;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new AException(AException.ERR_SERVER, e);
        }
    }
    
    public static <K, V> Map<Object, Object> toMap(Class<K> typeK, Class<V> typeV, String json, Map<Object, Object> map) throws AException
    {
        if (map == null)
        {
            map = new HashMap<Object, Object>();
        }
        
        JSONObject jObject = JSONObject.parseObject(json);
        for (Entry<String, Object> entry : jObject.entrySet())
        {
            K k = toTypeObject(typeK, entry.getKey());
            V v = toTypeObject(typeV, entry.getValue());
            map.put(k, v);
        }
        
        return map;
        
    }
    
    private static <T> T toTypeObject(Class<T> type, Object obj) throws AException
    {
        if (obj == null)
        {
            return null;
        }
        if (obj.getClass().equals(type))
        {
            return type.cast(obj);
        }
        
        if (type.equals(Integer.class))
        {
            return type.cast(Integer.parseInt(obj.toString()));
        }
        
        if (type.equals(Long.class))
        {
            return type.cast(Long.parseLong(obj.toString()));
        }
        
        if (type.equals(Double.class))
        {
            return type.cast(Double.parseDouble(obj.toString()));
        }
        
        if (type.equals(Boolean.class))
        {
            return type.cast(Boolean.parseBoolean(obj.toString()));
        }
        
        if (type.equals(String.class))
        {
            return type.cast(obj.toString());
        }
        
        return toObject(type, toJson(obj));
    }
    
    public static String readJsonString(String buff) throws AException
    {
        // FunctionTime fun = new FunctionTime();
        try
        {
            if (objectMapper == null)
            {
                throw new AException(AException.ERR_SERVER, "invalid json util");
            }
            
            if (buff == null || buff.isEmpty() || buff.length() < 2)
            {
                return "";
            }
            
            try
            {
                JsonNode node = objectMapper.readTree(buff);
                if (node == null)
                {
                    return "";
                }
                
                return node.toString();
            }
            catch (JsonProcessingException e)
            {
                return "";
            }
            catch (IOException e)
            {
                throw new AException(AException.ERR_SERVER, e);
            }
        }
        finally
        {
            // fun.print();
        }
    }
    
    static
    {
        objectMapper
                // .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                
                // Ignore additional/unknown properties in a payload.
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                
                // Only serialize populated properties (do no serialize nulls)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                
                // Use fields directly.
                .setVisibility(PropertyAccessor.FIELD, com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY)
                
                // Ignore accessor and mutator methods (use fields per above).
                .setVisibility(PropertyAccessor.GETTER, com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.SETTER, com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.IS_GETTER, com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE);
        
    }
    
    public static void main(String[] arg) throws Exception
    {
        String json = "[{\"address\": \"address2\",\"name\":\"haha2\",\"id\":2,\"email\":\"email2\"},"
                + "{\"address\":\"address\",\"name\":\"haha\",\"id\":1,\"email\":\"email\"}]";
        
        json += "\n adfad";
        
        String readJson = readJsonString(json);
        String left = json.substring(readJson.length() + 1);
        
        String testJson = "{{\"key\":\"id\",\"key2\":\"\",\"type\":\"CT_BIGGER\"}:200}";
        
        readJson = readJsonString(testJson);
        
        json = "{\"command\":500,\"data\":\"wo shi fa xian bu liao de\",\"lst\":[\"index = 0\",\"index = 1\",\"2\"]}";
        
        readJson = readJsonString(json);
        
        json = "[{\"address\": \"address2\",:\"haha2\",\"id\":2,\"email\":\"email2\"}," + "{\"address\":\"address\",\"namehaha\",\"id\":1,email\"}]";
        
        readJson = readJsonString(json);
        left = json.substring(readJson.length());
    }
}
