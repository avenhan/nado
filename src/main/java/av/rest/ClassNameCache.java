package av.rest;

public class ClassNameCache
{
    private Class<?> clazz;
    private String   name;
    
    public ClassNameCache()
    {
        super();
    }
    
    public ClassNameCache(Class<?> clazz, String name)
    {
        super();
        this.clazz = clazz;
        this.name = name;
    }
    
    /**
     * @return the clazz
     */
    public Class<?> getClazz()
    {
        return clazz;
    }
    
    /**
     * @param clazz
     *            the clazz to set
     */
    public void setClazz(Class<?> clazz)
    {
        this.clazz = clazz;
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * @param name
     *            the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ClassNameCache [clazz=" + clazz.getName() + ", name=" + name + "]";
    }
}
