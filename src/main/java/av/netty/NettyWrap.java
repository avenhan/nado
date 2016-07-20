package av.netty;

public class NettyWrap
{
    private long    seq;
    private Command command;
    private String  msg;
    private long    timestamp;
    
    public long getSeq()
    {
        return seq;
    }
    
    public void setSeq(long seq)
    {
        this.seq = seq;
    }
    
    public Command getCommand()
    {
        return command;
    }
    
    public void setCommand(Command command)
    {
        this.command = command;
    }
    
    public String getMsg()
    {
        return msg;
    }
    
    public void setMsg(String msg)
    {
        this.msg = msg;
    }
    
    public long getTimestamp()
    {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }
    
}
