package av.nado.test;

import java.io.Serializable;

import av.nado.annotation.Remote;

public class TestRemote implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -4763766765115184945L;
    
    private int               id;
    private String            name;
    
    @Remote
    public int getId()
    {
        return id;
    }
    
    public void setId(int id)
    {
        this.id = id;
    }
    
    @Remote
    public String getName()
    {
        return name;
    }
    
    @Remote
    public void setName(String name)
    {
        this.name = name;
    }
    
    @Remote
    public void print(String... ars)
    {
        System.out.println(ars.toString());
    }
}
