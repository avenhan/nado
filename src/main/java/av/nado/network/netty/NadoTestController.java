package av.nado.network.netty;

import av.netty.NettyController;
import av.util.trace.Trace;

public class NadoTestController implements NettyController<String>
{
    
    public Object receive(String request)
    {
        Trace.print("nado test controller receive: {}", request);
        return getString(10000);
    }
    
    private static String getString(long times)
    {
        StringBuilder b = new StringBuilder("I am server....start: ");
        for (int i = 0; i < times; i++)
        {
            b.append(i).append("-");
        }
        
        return b.toString();
    }
}
