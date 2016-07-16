package av.util.exception;

/***
 * 
 * @author av
 *
 * error code will be catch by exception handler
 *
 */
public enum ErrorCode
{
    // parameter
    // system
    ERR_FATAL(0, "err_fatal"),
    
    // parameter
    INVALID_PARAMETER(400, "invalid_parameters"),
    INVALID_USER_PASSOWRD(400, "invalid_user_password"),
    
    // unknown error
    ERR_SERVER(500, "err_server");
    
    
    private int code;
    private String explain;
    
    ErrorCode(int code, String explain)
    {
        this.code = code;
        this.explain = explain;
    }
}
