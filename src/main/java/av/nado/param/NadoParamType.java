package av.nado.param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public class NadoParamType
{
    public static final int PARAM_TYPE_OBJECT  = 0;
    public static final int PARAM_TYPE_NULL    = 1;
    public static final int PARAM_TYPE_INT     = PARAM_TYPE_NULL + 1;
    public static final int PARAM_TYPE_LONG    = PARAM_TYPE_NULL + 2;
    public static final int PARAM_TYPE_BOOLEAN = PARAM_TYPE_NULL + 3;
    public static final int PARAM_TYPE_DOUBLE  = PARAM_TYPE_NULL + 4;
    public static final int PARAM_TYPE_STRING  = PARAM_TYPE_NULL + 5;
    
    // BigDecimal
    public static final int PARAM_TYPE_DECIMAL = PARAM_TYPE_NULL + 6;
    
    // complex type
    public static final int PARAM_TYPE_LIST    = 100;
    public static final int PARAM_TYPE_SET     = PARAM_TYPE_LIST + 1;
    public static final int PARAM_TYPE_MAP     = PARAM_TYPE_LIST + 2;
    
    public int getType(Object obj)
    {
        if (obj == null)
        {
            return PARAM_TYPE_NULL;
        }
        else if (obj instanceof Integer)
        {
            return PARAM_TYPE_INT;
        }
        else if (obj instanceof Long)
        {
            return PARAM_TYPE_LONG;
        }
        else if (obj instanceof Boolean)
        {
            return PARAM_TYPE_BOOLEAN;
        }
        else if (obj instanceof Double)
        {
            return PARAM_TYPE_DOUBLE;
        }
        else if (obj instanceof String)
        {
            return PARAM_TYPE_STRING;
        }
        else if (obj instanceof BigDecimal)
        {
            return PARAM_TYPE_DECIMAL;
        }
        else if (obj instanceof Collection)
        {
            return PARAM_TYPE_LIST;
        }
        else if (obj instanceof Map)
        {
            return PARAM_TYPE_MAP;
        }
        else
        {
            return PARAM_TYPE_OBJECT;
        }
    }
}
