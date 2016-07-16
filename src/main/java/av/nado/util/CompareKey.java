package av.nado.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import av.util.exception.AException;

public class CompareKey implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String            key;
    private CompareType       type;
    private String            key2             = "";
    
    public CompareKey()
    {
    }
    
    public CompareKey(String in) throws AException
    {
        if (Check.IfOneEmpty(in))
        {
            return;
        }
        
        List<String> lst = new ArrayList<String>();
        Pattern p = Pattern.compile("([_a-zA-Z0-9]*[^!=><,;\t\r\n\\s*])|([!=><]*[^,;\t\r\n\\s*])");
        Matcher m = p.matcher(in);
        
        while (m.find())
        {
            String get = m.group();
            if (Check.IfOneEmpty(get))
            {
                continue;
            }
            
            lst.add(get);
        }
        
        if (lst.isEmpty() || lst.size() == 1)
        {
            return;
        }
        
        this.key = lst.get(0);
        StringBuilder b = new StringBuilder();
        StringBuilder b2 = new StringBuilder();
        for (int i = 1; i < lst.size(); i++)
        {
            String at = lst.get(i);
            if (at.startsWith(">") || at.startsWith("<") || at.startsWith("=") || at.startsWith("!"))
            {
                b.append(at);
            }
            else
            {
                b2.append(lst.get(i));
            }
        }
        
        this.type = CompareType.fromString(b.toString());
        this.key2 = b2.toString();
        if (this.type == null)
        {
            throw new AException(AException.ERR_SERVER, "invalid compare key: {}", in);
        }
    }
    
    public CompareKey(String key, CompareType type)
    {
        this.key = key;
        this.type = type;
    }
    
    public CompareKey(String key, String type) throws AException
    {
        this.key = key;
        this.type = CompareType.fromString(type);
        if (this.type == null)
        {
            throw new AException(AException.ERR_SERVER, "invalid compare key");
        }
    }
    
    public CompareKey(String key, CompareType type, String key2)
    {
        this.key = key;
        this.type = type;
        this.key2 = key2;
    }
    
    public String getKey()
    {
        return key;
    }
    
    public void setKey(String key)
    {
        this.key = key;
    }
    
    public CompareType getType()
    {
        return type;
    }
    
    public void setType(CompareType type)
    {
        this.type = type;
    }
    
    public String getKey2()
    {
        return key2;
    }
    
    public void setKey2(String key2)
    {
        this.key2 = key2;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((key2 == null) ? 0 : key2.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompareKey other = (CompareKey) obj;
        if (key == null)
        {
            if (other.key != null)
                return false;
        }
        else if (!key.equals(other.key))
            return false;
        if (key2 == null)
        {
            if (other.key2 != null)
                return false;
        }
        else if (!key2.equals(other.key2))
            return false;
        if (type != other.type)
            return false;
        return true;
    }
    
    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder(key).append(type.toString());
        if (!Check.IfOneEmpty(key2))
        {
            b.append(key2);
        }
        
        return b.toString();
    }
}
