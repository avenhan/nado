package av.nado.test;

import av.nado.base.NadoManager;

public class TestServer
{
    public static void main(String[] args) throws Exception
    {
        startServer();
        
        NadoManager.instance().invoke(TestRemote.class.getName(), "setName", "avenhan");
        String name = (String) NadoManager.instance().invoke(TestRemote.class.getName(), "getName");
        
        int id = (Integer) NadoManager.instance().invoke(TestRemote.class.getName(), "getId");
        
        System.out.println(name + " id: " + id);
    }
    
    public static void startServer() throws Exception
    {
        NadoManager.instance().loadConfig("conf/nado.xml");
    }
}
