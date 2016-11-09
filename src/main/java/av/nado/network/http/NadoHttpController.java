package av.nado.network.http;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.restexpress.HttpHeaderExt;
import org.restexpress.Request;
import org.restexpress.Response;

import av.nado.base.NadoManager;
import av.nado.param.NadoParam;
import av.nado.remote.NadoResponse;
import av.nado.remote.NadoWrap;
import av.nado.util.Check;
import av.nado.util.CompareKey;
import av.rest.Rest;
import av.rest.RestParam;
import av.rest.Test;
import av.util.exception.AException;
import av.util.trace.FunctionTime;
import io.netty.buffer.ByteBuf;

public class NadoHttpController
{
    @Rest(uri = "remote", method = "post")
    public Object nadoFunctionPost(NadoFunctionRequest rqst) throws Exception
    {
        FunctionTime functionTime = new FunctionTime();
        try
        {
            String type = rqst.getType();
            String method = rqst.getMethod();
            functionTime.addCurrentTime("{}.{}", type, method);
            
            Object[] arrParam = null;
            List<String> lstParamExplain = rqst.getParams();
            if (!Check.IfOneEmpty(lstParamExplain))
            {
                arrParam = new Object[lstParamExplain.size()];
                for (int i = 0; i < lstParamExplain.size(); i++)
                {
                    Object param = null;
                    switch (rqst.getParamType())
                    {
                        case NadoWrap.KEY_PARAM_TYPE_JSON:
                            param = NadoParam.fromExplain(lstParamExplain.get(i));
                            break;
                        case NadoWrap.KEY_PARAM_TYPE_SERIALIZE:
                            param = NadoParam.deserialized(lstParamExplain.get(i));
                            break;
                        
                        case NadoWrap.KEY_PARAM_TYPE_HESSIAN:
                            param = NadoParam.hessionDecode(lstParamExplain.get(i));
                            break;
                        
                        default:
                            throw new AException(AException.ERR_SERVER, "unkown param type: {}", rqst.getParamType());
                    }
                    arrParam[i] = param;
                }
            }
            
            functionTime.addCurrentTime("params");
            Object ret = null;
            try
            {
                if (arrParam == null)
                {
                    ret = NadoManager.instance().invoke(type, method);
                }
                else
                {
                    ret = NadoManager.instance().invoke(type, method, arrParam);
                }
            }
            catch (Exception e)
            {
                ret = e;
            }
            
            functionTime.addCurrentTime("invoke");
            
            NadoResponse rspd = new NadoResponse();
            rspd.setBody(NadoParam.toExplain(ret));
            
            functionTime.addCurrentTime("ret");
            return rspd;
        }
        finally
        {
            functionTime.print();
        }
    }
    
    @Rest(uri = "test")
    public Object testGet(TestGetRequest rqst, Response response) throws Exception
    {
        throw new AException(AException.ERR_NOT_MOIDIFIED, "fuck exception...");
        // return "test get from api-02\n";
    }
    
    @Rest(uri = "test", method = "head")
    public Object testGetHead(TestGetRequest rqst, Response response) throws Exception
    {
        return "test head";
    }
    
    @Rest(uri = "test/{type}", method = "post")
    public Object testPost(String type, @RestParam(key = "Content-Length") int contentLength, Response response) throws Exception
    {
        return "test post";
    }
    
    @Rest(uri = "test/id", method = "post")
    public Object testPostId(Request rqst, Response rspd) throws Exception
    {
        return "test post Id";
    }
    
    @Rest(uri = "test/idx", method = "post")
    public Object testPostIdx(@RestParam(required = false) List<Test> saves, @RestParam(required = false) Collection<Test> sets,
            @RestParam(required = false) Map<CompareKey, Test> maps, @RestParam(required = false) Test[] arr) throws Exception
    {
        for (Test test : saves)
        {
            System.out.println("id: " + test.getId() + " value: " + test.getValue());
        }
        return "test post Idx";
    }
    
    @Rest(uri = "/test/idxs", method = "post")
    public void stFun(@RestParam(required = false) int a, @RestParam(required = false) double b, @RestParam(required = false) boolean c,
            @RestParam(required = false) short d, @RestParam(required = false) byte e, @RestParam(required = false) char f) throws Exception
    {
        System.out.println("a: " + a + " d: " + d + " c: " + c);
    }
    
    @Rest(uri = "test/upload/{file-name}", method = "upload")
    public Object testUpload(@RestParam(key = "file-name") String fileName, @RestParam(key = "Range") String range, Request request,
            @RestParam(key = HttpHeaderExt.HEADER_BYTE_CONTINUE) boolean isContinue) throws Exception
    {
        ByteBuf buf = request.getBody();
        if (buf == null && isContinue)
        {
            File file = new File(fileName);
            if (file.exists())
            {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            request.putAttachment("fos", fos);
            System.out.println("upload file: " + fileName + " is begining...");
            
            throw new AException(AException.ERR_NOT_FOUND, "not found the uploaded file id");
            
            // return null;
        }
        
        if (buf != null && isContinue)
        {
            FileOutputStream fos = (FileOutputStream) request.getAttachment("fos");
            if (fos == null)
            {
                throw new AException(AException.ERR_SERVER, "file output stream is null");
            }
            
            if (buf.isReadable())
            {
                buf.retain();
                ByteBuffer tempBuffer = buf.nioBuffer();
                fos.getChannel().write(tempBuffer);
            }
            
            // System.out.println("upload file: " + fileName + " is uploading...
            // size: " + buf.readableBytes());
            return null;
        }
        
        if (!isContinue)
        {
            System.out.println("upload file: " + fileName + " is finished...last size: " + buf.readableBytes());
            FileOutputStream fos = (FileOutputStream) request.getAttachment("fos");
            if (fos == null)
            {
                throw new AException(AException.ERR_SERVER, "file output stream is null");
            }
            
            fos.close();
            fos = null;
            
            return "upload file: " + fileName + " is finished...";
        }
        
        return null;
    }
    
    @Rest(uri = "test/download/{file-name}", method = "download")
    public void testDownload(@RestParam(key = "file-name") String fileName)
    {
        System.out.println("download file: " + fileName);
    }
}
