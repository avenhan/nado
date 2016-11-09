package org.restexpress;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import av.nado.util.Check;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RestExpressUtil
{
    public static void setHeader(Request rqst, String key, String value)
    {
        if (Check.IfOneEmpty(rqst, key, value))
        {
            return;
        }
        
        Map<String, String> mapHeaders = new HashMap<String, String>();
        Set<String> setNames = rqst.getHeaderNames();
        if (setNames.contains(key))
        {
            for (String name : setNames)
            {
                mapHeaders.put(name, rqst.getHeader(name));
            }
            
            rqst.clearHeaders();
        }
        
        mapHeaders.put(key, value);
        for (Map.Entry<String, String> entry : mapHeaders.entrySet())
        {
            rqst.addHeader(entry.getKey(), entry.getValue());
        }
    }
    
    public static ByteBuf emptyBuf()
    {
        return Unpooled.buffer(8);
    }
}
