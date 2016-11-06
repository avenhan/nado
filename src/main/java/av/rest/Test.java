package av.rest;

import java.util.Map;

import av.nado.util.CompareKey;

public class Test
{
    private int                   id;
    private String                value;
    private Map<CompareKey, Test> maps;
    
    public int getId()
    {
        return id;
    }
    
    public void setId(int id)
    {
        this.id = id;
    }
    
    public String getValue()
    {
        return value;
    }
    
    public void setValue(String value)
    {
        this.value = value;
    }
    
    public Map<CompareKey, Test> getMaps()
    {
        return maps;
    }
    
    public void setMaps(Map<CompareKey, Test> maps)
    {
        this.maps = maps;
    }
    
}
