package av.netty;

import av.action.Action;
import av.nado.util.Check;
import av.util.exception.AException;

public class ReceiveAction extends Action<NettyChannelInfo>
{
    private NettyChannelInfo info;
    
    public NettyChannelInfo getInfo()
    {
        return info;
    }
    
    public void setInfo(NettyChannelInfo info)
    {
        this.info = info;
    }
    
    @Override
    protected void onAction(NettyChannelInfo info) throws AException
    {
        if (Check.IfOneEmpty(info))
        {
            return;
        }
        
        info.analysisJson();
    }
    
}
