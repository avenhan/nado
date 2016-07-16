package av.nado.util;

public enum CompareType
{
    CT_EQUAL,
    
    CT_NOTEQUAL,
    
    CT_BIGGER,
    
    CT_NOTBIGGER,
    
    CT_SMALLER,
    
    CT_NOTSMALLER;
    
    @Override
    public String toString()
    {
        String ret = "";
        switch (this)
        {
            case CT_BIGGER:
                ret = ">";
                break;
            case CT_EQUAL:
                ret = "==";
                break;
            case CT_SMALLER:
                ret = "<";
                break;
            case CT_NOTBIGGER:
                ret = "<=";
                break;
            case CT_NOTSMALLER:
                ret = ">=";
                break;
            case CT_NOTEQUAL:
                ret = "!=";
                break;
            default:
                break;
        }
        
        return ret;
    }
    
    public static CompareType fromString(String type)
    {
        if (type.equals("=="))
        {
            return CompareType.CT_EQUAL;
        }
        
        if (type.equals(">"))
        {
            return CT_BIGGER;
        }
        
        if (type.equals("<"))
        {
            return CT_SMALLER;
        }
        
        if (type.equals("!="))
        {
            return CT_NOTEQUAL;
        }
        
        if (type.equals(">="))
        {
            return CompareType.CT_NOTSMALLER;
        }
        
        if (type.equals("<="))
        {
            return CompareType.CT_NOTBIGGER;
        }
        
        return null;
    }
}
