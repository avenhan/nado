package av.nado.base;

import java.util.ArrayList;
import java.util.HashMap;
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
    public static final String   KEY_CLIENT    = "client";
    
    private Map<String, String> register;
    private Map<String, Integer> boostrap      = new HashMap<String, Integer>();
    private List<String>        remote;
    
    private String               client;
    
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
            
            if (entry.getKey().equals(NadoSetting.KEY_CLIENT))
            {
                setting.setClient((String) entry.getValue());
                continue;
            }
            
            if (entry.getKey().equals(NadoSetting.KEY_BOOTSTRAP))
            {
                Object object = entry.getValue();
                if (object instanceof List<?>)
                {
                    List<String> lstBoostrap = (List<String>) object;
                    for (String boostrap : lstBoostrap)
                    {
                        String[] arrBoostrap = boostrap.split(":");
                        arrBoostrap[0].replaceAll(" ", "");
                        arrBoostrap[0].replaceAll("\t", "");
                        arrBoostrap[0].replaceAll("\n", "");
                        
                        arrBoostrap[1].replaceAll(" ", "");
                        arrBoostrap[1].replaceAll("\t", "");
                        arrBoostrap[1].replaceAll("\n", "");
                        
                        setting.boostrap.put(arrBoostrap[0], Integer.parseInt(arrBoostrap[1]));
                    }
                }
                else
                {
                    String boostrap = (String) object;
                    
                    String[] arrBoostrap = boostrap.split(":");
                    arrBoostrap[0].replaceAll(" ", "");
                    arrBoostrap[0].replaceAll("\t", "");
                    arrBoostrap[0].replaceAll("\n", "");
                    
                    arrBoostrap[1].replaceAll(" ", "");
                    arrBoostrap[1].replaceAll("\t", "");
                    arrBoostrap[1].replaceAll("\n", "");
                    
                    setting.boostrap.put(arrBoostrap[0], Integer.parseInt(arrBoostrap[1]));
                }
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
    
    
    public Map<String, Integer> getBoostrap()
    {
        return boostrap;
    }
    
    public void setBoostrap(Map<String, Integer> boostrap)
    {
        this.boostrap = boostrap;
    }
    
    public List<String> getRemote()
    {
        return remote;
    }
    
    public void setRemote(List<String> remote)
    {
        this.remote = remote;
    }
    
    public String getClient()
    {
        return client;
    }
    
    public void setClient(String client)
    {
        this.client = client;
    }
}
