package av.nado.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NadoSetting
{
    public static final String  KEY_REGISTER  = "register";
    public static final String  KEY_PORT      = "port";
    public static final String  KEY_REMOTE    = "remote";
    public static final String  KEY_PROTOCOL  = "protocol";
    public static final String  KEY_ADDRESS   = "address";
    public static final String  KEY_BOOTSTRAP = "bootstrap";
    
    private Map<String, String> register;
    private int                 port;
    private String              bootstrap;
    private List<String>        remote;
    
    @SuppressWarnings("unchecked")
    public static NadoSetting load(Map<String, Object> map)
    {
        NadoSetting setting = new NadoSetting();
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            if (entry.getKey().equals(NadoSetting.KEY_REGISTER))
            {
                setting.setRegister((Map<String, String>) entry.getValue());
                continue;
            }
            
            if (entry.getKey().equals(NadoSetting.KEY_BOOTSTRAP))
            {
                Map<String, String> mapBootstrap = (Map<String, String>) entry.getValue();
                
                if (mapBootstrap.containsKey(KEY_PORT))
                {
                    setting.setPort(Integer.parseInt(mapBootstrap.get(NadoSetting.KEY_PORT)));
                }
                
                setting.setBootstrap(mapBootstrap.get(NadoSetting.KEY_PROTOCOL));
            }
            
            if (entry.getKey().equals(NadoSetting.KEY_REMOTE))
            {
                Object object = entry.getValue();
                if (object instanceof List<?>)
                {
                    setting.setRemote((List<String>) object);
                }
                else
                {
                    List<String> lstRemote = new ArrayList<String>();
                    lstRemote.add((String) object);
                    setting.setRemote(lstRemote);
                }
            }
        }
        
        return setting;
    }
    
    public Map<String, String> getRegister()
    {
        return register;
    }
    
    public void setRegister(Map<String, String> register)
    {
        this.register = register;
    }
    
    public int getPort()
    {
        return port;
    }
    
    public void setPort(int port)
    {
        this.port = port;
    }
    
    public String getBootstrap()
    {
        return bootstrap;
    }
    
    public void setBootstrap(String bootstrap)
    {
        this.bootstrap = bootstrap;
    }
    
    public List<String> getRemote()
    {
        return remote;
    }
    
    public void setRemote(List<String> remote)
    {
        this.remote = remote;
    }
    
}
