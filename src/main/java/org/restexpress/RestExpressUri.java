package org.restexpress;

import io.netty.handler.codec.http.HttpMethod;

public class RestExpressUri
{
    public static Object getContainUri(String uri, HttpMethod method, boolean isUpload)
    {
        if (isUpload)
        {
            return RestExpress.getUploadUriController(uri);
        }
        
        return RestExpress.getDownloadUriController(uri);
    }
}
