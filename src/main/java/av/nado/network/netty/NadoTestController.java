package av.nado.network.netty;

import av.netty.NettyController;

public class NadoTestController implements NettyController<String>
{
    
    public Object receive(String request)
    {
        return getString(0);
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
