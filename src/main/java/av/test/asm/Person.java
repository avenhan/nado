package av.test.asm;

public class Person
{
    private String name = "zoop";
    
    public String getName()
    {
        Monitor.start();
        Monitor.end();
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void print()
    {
        System.out.println("print on the screen...");
    }
}
