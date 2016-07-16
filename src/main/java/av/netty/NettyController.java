package av.netty;

public interface NettyController<R>
{
    public Object receive(R request) throws Exception;
}
