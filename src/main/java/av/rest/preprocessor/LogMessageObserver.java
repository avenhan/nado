/*****************************************************************************************
 * 
 * Copyright (C) 2015, Yunio Inc. All rights reserved. http://yunio.com
 * 
 *****************************************************************************************/
package av.rest.preprocessor;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restexpress.ContentType;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.pipeline.MessageObserver;

import io.netty.buffer.ByteBuf;

public class LogMessageObserver extends MessageObserver
{
    private static Logger                     logger = LogManager.getLogger(LogMessageObserver.class);
    private final Map<String, MessageLogInfo> timers = new ConcurrentHashMap<String, MessageLogInfo>();
    
    private static List<String>               FILTERED_URLS = new ArrayList<String>();
    static
    {
        FILTERED_URLS.add("/album/list");
        FILTERED_URLS.add("/album/items");
        FILTERED_URLS.add("/event");
    }
    
    public Map<String, MessageLogInfo> getCopyInformation()
    {
        Map<String, MessageLogInfo> mapRet = new HashMap<String, MessageLogInfo>();
        mapRet.putAll(timers);
        return mapRet;
    }
    
    @Override
    protected void onReceived(Request request, Response response)
    {
        MessageLogInfo info = new MessageLogInfo();
        info.setId(request.getCorrelationId());
        info.setUrl(request.getUrl());
        info.setMethod(request.getEffectiveHttpMethod().toString());
        info.setToken(request.getHeader("Authorization"));
        
        timers.put(request.getCorrelationId(), info);
        LogMessageListener.instance().initialize(this);
    }
    
    @Override
    protected void onException(Throwable exception, Request request, Response response)
    {
        MessageLogInfo info = timers.remove(request.getCorrelationId());
        if (info != null)
        {
            info.stop();
        }
        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder();
            prepBuf(request, response, exception, info, sb);
            logger.debug(sb.toString());
            logger.debug("  ", exception);
        }
    }
    
    @Override
    protected void onSuccess(Request request, Response response)
    {
    }
    
    private String appendHeaders(Request req, StringBuilder sb)
    {
        String token = null;
        Set<String> headerNames = req.getHeaderNames();
        Iterator<String> it = headerNames.iterator();
        String version = req.isHttpVersion1_0() ? "HTTP/1.0" : "HTTP/1.1";
        sb.append("  Version : " + version + "\n");
        sb.append("  Headers : \n");
        while (it.hasNext())
        {
            String header = it.next();
            sb.append("    " + header + " : " + req.getHeader(header) + "\n");
        }
        sb.append("IP: ").append(req.getRemoteAddress());
        
        return token;
    }
    
    private void appendHeaders(Response resp, StringBuilder sb)
    {
        Set<String> headerNames = resp.getHeaderNames();
        Iterator<String> it = headerNames.iterator();
        sb.append("  Headers : \n");
        while (it.hasNext())
        {
            String header = it.next();
            sb.append("    " + header + " : " + resp.getHeader(header) + "\n");
        }
    }
    
    /**
     * Replace password with "********" in a json string
     * 
     * @param str
     *            Json string
     * @return
     */
    private String replacePasswd(String str)
    {
        // Dummied out for now, as there are some requests that can cause
        // infinite loop here.
        // Have not figured out which one yet.
        final String pattern = "password";
        if (str != null && str.contains(pattern))
        {
            return "[password replaced here]";
        }
        else
        {
            return str;
        }
        
        /*      
                final String pattern = "password";
                if (str.contains(pattern))
                {
                    String replaced = str;
                    int beginning = 0;
                    int beginNdx = 0;
                    int index = replaced.indexOf(pattern, beginNdx);
                    while (index > -1)
                    {
                        int n = index + pattern.length() + 1;
                        int ndx1;
                        if (replaced.charAt(n+1) == '"')
                            ndx1 = replaced.indexOf("\"", n);         // First double-quote after the password pattern
                        else
                        {
                            int tmp = replaced.indexOf("\"", n);
                            ndx1 = replaced.indexOf("\"", tmp + 1);
                        }
                        int ndx2 = replaced.indexOf("\"", ndx1 + 1);  // The double-quote that ends the password
                        StringBuilder buf = new StringBuilder();
                        buf.append(replaced.substring(beginning, ndx1 + 1)); // Take the substring from beginning to password string
                        buf.append("********"); // Replace the password
                        buf.append(replaced.substring(ndx2));  // Take the rest of the string
                        replaced = buf.toString();
                        beginNdx = ndx2 + 1;
                        index = replaced.indexOf(pattern, beginNdx);
                    }
                    return replaced;
                }
                else
                {
                    return str;
                }
        */
    }
    
    private String getBody(Request req)
    {
        String body = null;
        ByteBuf bb = req.getBody();
        bb.readerIndex(0); // always set the reader index back to the beginning
        body = req.getBody().toString(ContentType.CHARSET);
        return replacePasswd(body);
    }
    
    private void prepBuf(Request req, Response resp, Throwable exception, MessageLogInfo timer, StringBuilder sb)
    {
        sb.append("\nClient Request ---------------------------\n");
        sb.append("  Method : " + req.getEffectiveHttpMethod().toString() + "\n");
        sb.append("  URL : " + req.getUrl() + "\n");
        sb.append("  Req-Id : " + req.getCorrelationId() + "\n");
        String token = appendHeaders(req, sb);
        
        SocketAddress addr = req.getRemoteAddress();
        String ra = addr.toString();
        sb.append("  Client Address :" + ra + "\n");
        
        String url = req.getUrl();
        if (url.contains("/user"))
        {
            String method = req.getEffectiveHttpMethod().toString();
            if (method.equals("POST") || method.equals("PUT"))
            {
                if (url.contains("avatar"))
                {
                    sb.append("  Body : [avatar content here]");
                }
                else
                {
                    String body = getBody(req);
                    sb.append("  Body : " + body + "\n");
                }
            }
        }
        else if (url.contains("/file/content/"))
        {
            sb.append("  Body : [file content here]");
        }
        else if (url.contains("/content/"))
        {
            sb.append("  Body : [file content here]");
        }
        else
        {
            String body = getBody(req);
            sb.append("  Body : " + body + "\n");
        }
        
        if (exception != null)
            sb.append("  Exception: " + exception.getClass().getSimpleName() + "\n");
        
        sb.append("Client Request ---------------------------\n");
        
        sb.append("Server Response ---------------------------\n");
        sb.append("  Status : " + resp.getResponseStatus().toString() + "\n");
        if (timer != null && timer.getTimer() != null)
        {
            sb.append("  Time : " + timer.getTimer().toString() + "\n");
        }
        else
        {
            sb.append("  Time : (no timer)\n");
        }
        appendHeaders(resp, sb);
        if (url.contains("/file/content/") || url.contains("avatar") || url.contains("thumb"))
        {
            sb.append("  Body : [file content here]");
        }
        else
        {
            if (resp.hasBody())
                sb.append("  Body : " + resp.getBody().toString() + "\n");
            else
                sb.append("  Body : \n");
        }
        sb.append("Server Response ---------------------------\n");
    }
    
    @Override
    protected void onComplete(Request request, Response response)
    {
        MessageLogInfo info = timers.remove(request.getCorrelationId());
        if (info != null)
        {
            info.stop();
        }
        
        if (logger.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder();
            prepBuf(request, response, null, info, sb);
            logger.debug(sb.toString());
        }
    }
    
    protected boolean filtered(String url)
    {
        boolean filtered = false;
        for (String urlPrefix : FILTERED_URLS)
        {
            if (url.contains(urlPrefix))
            {
                filtered = true;
                break;
            }
        }
        return filtered;
    }
    
    public static class Timer
    {
        private long startMillis = 0;
        private long stopMillis  = 0;
        
        public Timer()
        {
            super();
            this.startMillis = System.currentTimeMillis();
        }
        
        public long getStartMillis()
        {
            return startMillis;
        }
        
        public void setStartMillis(long startMillis)
        {
            this.startMillis = startMillis;
        }
        
        public long getStopMillis()
        {
            return stopMillis;
        }
        
        public void setStopMillis(long stopMillis)
        {
            this.stopMillis = stopMillis;
        }
        
        public void stop()
        {
            this.stopMillis = System.currentTimeMillis();
        }
        
        @Override
        public String toString()
        {
            long stopTime = (stopMillis == 0 ? System.currentTimeMillis() : stopMillis);
            
            return String.valueOf(stopTime - startMillis) + "ms";
        }
    }
}
