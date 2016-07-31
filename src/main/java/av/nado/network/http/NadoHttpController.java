package av.nado.network.http;

import org.restexpress.Response;

import av.rest.Rest;

public class NadoHttpController
{
    @Rest(uri = "test", request = TestGetRequest.class)
    public Object testGet(TestGetRequest rqst, Response response) throws Exception
    {
        return "test get";
    }
    
    @Rest(uri = "test/{type}", method = "post", request = TestPostRequest.class)
    public Object testPost(TestPostRequest rqst, Response response) throws Exception
    {
        return "test post";
    }
}
