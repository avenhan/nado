package av.nado.network.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import av.nado.util.Aggregate;
import av.nado.util.Check;
import av.util.exception.AException;

public class HttpHelper
{
    private static int connectTimeout = 30000;
    private static int readTimeout    = 120000;
    
    public static int getConnectTimeout()
    {
        return connectTimeout;
    }
    
    public static void setConnectTimeout(int connectTimeout)
    {
        HttpHelper.connectTimeout = connectTimeout;
    }
    
    public static int getReadTimeout()
    {
        return readTimeout;
    }
    
    public static void setReadTimeout(int readTimeout)
    {
        HttpHelper.readTimeout = readTimeout;
    }
    
    public static Aggregate<Integer, String> post(String url, String jsonData) throws AException
    {
        return post(url, jsonData, null);
    }
    
    public static Aggregate<Integer, String> post(String url, String json, Map<String, String> mapHeader) throws AException
    {
        if (Check.IfOneEmpty(url))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        DefaultHttpClient httpclient = new DefaultHttpClient();
        initSSL(httpclient, url);
        HttpPost method = new HttpPost(url);
        
        try
        {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, connectTimeout);
            HttpConnectionParams.setSoTimeout(httpParams, readTimeout);
            
            if (!Check.IfOneEmpty(mapHeader))
            {
                for (String key : mapHeader.keySet())
                {
                    String value = mapHeader.get(key);
                    
                    if (Check.IfOneEmpty(key, value))
                    {
                        continue;
                    }
                    method.setHeader(key, value);
                }
            }
            
            method.setParams(httpParams);
            
            if (!Check.IfOneEmpty(json))
            {
                StringEntity entity = new StringEntity(json);
                method.setEntity(entity);
            }
            
            HttpResponse response = httpclient.execute(method);
            int err = response.getStatusLine().getStatusCode();
            if (err != HttpStatus.SC_OK)
            {
            }
            
            String res = EntityUtils.toString(response.getEntity());
            
            Aggregate<Integer, String> retAggregate = new Aggregate<Integer, String>();
            retAggregate.put(err, res);
            
            return retAggregate;
        }
        catch (Exception e)
        {
            method.abort();
            throw new AException(AException.ERR_SERVER, e);
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }
    }
    
    public static Aggregate<Integer, String> post(String url, Map<String, String> mapPostValue, Map<String, String> mapHeader) throws AException
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        initSSL(httpclient, url);
        HttpPost method = new HttpPost(url);
        
        try
        {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, connectTimeout);
            HttpConnectionParams.setSoTimeout(httpParams, readTimeout);
            
            if (!Check.IfOneEmpty(mapHeader))
            {
                for (String key : mapHeader.keySet())
                {
                    String value = mapHeader.get(key);
                    
                    if (Check.IfOneEmpty(key, value))
                    {
                        continue;
                    }
                    method.setHeader(key, value);
                }
            }
            
            method.setParams(httpParams);
            
            if (!Check.IfOneEmpty(mapPostValue))
            {
                List<NameValuePair> lstParam = new ArrayList<NameValuePair>();
                for (String key : mapPostValue.keySet())
                {
                    String value = mapPostValue.get(key);
                    if (Check.IfOneEmpty(key, value))
                    {
                        continue;
                    }
                    
                    lstParam.add(new BasicNameValuePair(key, value));
                }
                
                method.setEntity(new UrlEncodedFormEntity(lstParam, HTTP.UTF_8));
            }
            
            HttpResponse response = httpclient.execute(method);
            int err = response.getStatusLine().getStatusCode();
            if (err != HttpStatus.SC_OK)
            {
            }
            
            String res = EntityUtils.toString(response.getEntity());
            Aggregate<Integer, String> retAggregate = new Aggregate<Integer, String>();
            retAggregate.put(err, res);
            return retAggregate;
        }
        catch (Exception e)
        {
            method.abort();
            throw new AException(AException.ERR_SERVER, e);
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }
    }
    
    public static Aggregate<Integer, String> get(String url) throws AException
    {
        return get(url, null);
    }
    
    public static Aggregate<Integer, String> get(String url, Map<String, String> mapHeader) throws AException
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        initSSL(httpclient, url);
        HttpGet method = new HttpGet(url);
        HttpResponse response = null;
        
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, connectTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, readTimeout);
        
        if (!Check.IfOneEmpty(mapHeader))
        {
            for (String key : mapHeader.keySet())
            {
                String value = mapHeader.get(key);
                
                if (Check.IfOneEmpty(key, value))
                {
                    continue;
                }
                
                method.setHeader(key, value);
            }
        }
        
        method.setParams(httpParams);
        
        try
        {
            response = httpclient.execute(method);
            int err = response.getStatusLine().getStatusCode();
            if (err != HttpStatus.SC_OK)
            {
            }
            
            String res = EntityUtils.toString(response.getEntity());
            
            Aggregate<Integer, String> retAggregate = new Aggregate<Integer, String>();
            retAggregate.put(err, res);
            return retAggregate;
        }
        catch (Exception e)
        {
            method.abort();
            throw new AException(AException.ERR_SERVER, e);
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }
    }
    
    public static Aggregate<Integer, String> delete(String url, Map<String, String> mapHeader) throws AException
    {
        if (Check.IfOneEmpty(url))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        DefaultHttpClient httpclient = new DefaultHttpClient();
        initSSL(httpclient, url);
        HttpDelete method = new HttpDelete(url);
        
        try
        {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, connectTimeout);
            HttpConnectionParams.setSoTimeout(httpParams, readTimeout);
            
            if (!Check.IfOneEmpty(mapHeader))
            {
                for (String key : mapHeader.keySet())
                {
                    String value = mapHeader.get(key);
                    
                    if (Check.IfOneEmpty(key, value))
                    {
                        continue;
                    }
                    method.setHeader(key, value);
                }
            }
            
            method.setParams(httpParams);
            
            HttpResponse response = httpclient.execute(method);
            int err = response.getStatusLine().getStatusCode();
            if (err != HttpStatus.SC_OK)
            {
            }
            
            String res = EntityUtils.toString(response.getEntity());
            
            Aggregate<Integer, String> retAggregate = new Aggregate<Integer, String>();
            retAggregate.put(err, res);
            
            return retAggregate;
        }
        catch (Exception e)
        {
            method.abort();
            throw new AException(AException.ERR_SERVER, e);
        }
        finally
        {
            httpclient.getConnectionManager().shutdown();
        }
    }
    
    public static void initSSL(HttpClient httpclient, String url) throws AException
    {
        String urlTemp = url.toLowerCase();
        if (!urlTemp.startsWith("https:"))
        {
            return;
        }
        
        try
        {
            // Secure Protocol implementation.
            SSLContext ctx = SSLContext.getInstance("SSL");
            // Implementation of a trust manager for X509 certificates
            X509TrustManager tm = new X509TrustManager()
            {
                
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException
                {
                    
                }
                
                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException
                {
                }
                
                public X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[] { tm }, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            
            ClientConnectionManager ccm = httpclient.getConnectionManager();
            // register https protocol in httpclient's scheme registry
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", 443, ssf));
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
    }
    
    public static void main(String args[]) throws Exception
    {
        Aggregate<Integer, String> ret = HttpHelper.get("https://itunes.apple.com/search?term=2333&media=software");
        System.out.println(ret.getSecond());
    }
}
