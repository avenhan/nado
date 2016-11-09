package org.restexpress;

public interface StreamNotify
{
    public Object upload(Request request, Response response) throws Exception;
    
    public Object download(Request request, Response response) throws Exception;
}
