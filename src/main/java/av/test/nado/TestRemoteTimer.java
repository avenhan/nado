package av.test.nado;

import av.nado.annotation.Remote;
import av.util.trace.Trace;

public class TestRemoteTimer
{
    @Remote
    void testTimer(int count)
    {
        Trace.print("call timer .... count: {}", count);
    }
}
