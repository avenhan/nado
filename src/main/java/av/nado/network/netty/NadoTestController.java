package av.nado.network.netty;

import av.netty.NettyController;
import av.util.trace.Trace;

public class NadoTestController implements NettyController<String>
{
    
    public Object receive(String request)
    {
        Trace.print("nado controller receive: {}", request);
        return "I am server....";
    }
    
}
