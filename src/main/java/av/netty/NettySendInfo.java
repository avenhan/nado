package av.netty;

public class NettySendInfo
{
    private NettyWrap wrap;
    private long      createTime;
    private long      sentTime;
    private int       sendCount;
    private String    json;
    private NettyWrap recv;
    private boolean   isPost = true;
    
    private Object    objFire;
    
    public NettyWrap getWrap()
    {
        return wrap;
    }
    
    public void setWrap(NettyWrap wrap)
    {
        this.wrap = wrap;
    }
    
    public long getCreateTime()
    {
        return createTime;
    }
    
    public void setCreateTime(long createTime)
    {
        this.createTime = createTime;
    }
    
    public long getSentTime()
    {
        return sentTime;
    }
    
    public void setSentTime(long sentTime)
    {
        this.sentTime = sentTime;
    }
    
    public int getSendCount()
    {
        return sendCount;
    }
    
    public void setSendCount(int sendCount)
    {
        this.sendCount = sendCount;
    }
    
    public String getJson()
    {
        return json;
    }
    
    public void setJson(String json)
    {
        this.json = json;
    }
    
    public NettyWrap getRecv()
    {
        return recv;
    }
    
    public void setRecv(NettyWrap recv)
    {
        this.recv = recv;
    }
    
    public boolean isPost()
    {
        return isPost;
    }
    
    public void setPost(boolean isPost)
    {
        this.isPost = isPost;
    }
    
    public Object getObjFire()
    {
        return objFire;
    }
    
    public void setObjFire(Object objFire)
    {
        this.objFire = objFire;
    }
}
