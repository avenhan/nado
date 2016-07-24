package av.netty;

import av.nado.remote.NadoParam;
import av.util.exception.AException;
import av.util.trace.Trace;

public class NettyAction<T> implements Runnable
{
    private NettyChannelInfo   info;
    private NettyWrap          wrap;
    private NettySendInfo      sendInfo;
    private NettyController<T> controller;
    
    private T                  objParam;
    
    public NettyChannelInfo getInfo()
    {
        return info;
    }
    
    public void setInfo(NettyChannelInfo info)
    {
        this.info = info;
    }
    
    public NettyWrap getWrap()
    {
        return wrap;
    }
    
    public void setWrap(NettyWrap wrap)
    {
        this.wrap = wrap;
    }
    
    public NettySendInfo getSendInfo()
    {
        return sendInfo;
    }
    
    public void setSendInfo(NettySendInfo sendInfo)
    {
        this.sendInfo = sendInfo;
    }
    
    public NettyController<T> getController()
    {
        return controller;
    }
    
    public void setController(NettyController<T> controller)
    {
        this.controller = controller;
    }
    
    public T getObjParam()
    {
        return objParam;
    }
    
    public void setObjParam(T objParam)
    {
        this.objParam = objParam;
    }
    
    public void run()
    {
        Trace.print("seq: {} action begin at: {}ms", wrap.getSeq(), System.currentTimeMillis() - wrap.getTimestamp());
        
        Object retObject = null;
        try
        {
            retObject = controller.receive(objParam);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
            retObject = e1;
        }
        
        try
        {
            wrap.setMsg(NadoParam.toExplain(retObject));
            
            if (info.isServer())
            {
                NettyManager.instance().post(info, wrap);
            }
        }
        catch (AException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Trace.print("seq: {} action finished at: {}ms", wrap.getSeq(), System.currentTimeMillis() - wrap.getTimestamp());
    }
    
}
