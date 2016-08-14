package av.netty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import av.nado.param.NadoParam;
import av.nado.remote.NadoResponse;
import av.nado.util.Aggregate;
import av.nado.util.JsonUtil;
import av.util.exception.AException;
import av.util.trace.Trace;

public class NettySignature
{
    public static final String KEY_AV_SIGNATURE_START = "<AV:";
    public static final String KEY_AV_SIGNATURE_END   = ">";
    
    public static String signature(String value)
    {
        StringBuilder b = new StringBuilder(KEY_AV_SIGNATURE_START).append(Integer.toHexString(value.length())).append(KEY_AV_SIGNATURE_END)
                .append(value);
        return b.toString();
    }
    
    /***
     * 
     * 1.如果没有签名则返回 -1，剩余字符串为入参
     * 
     * 2.如果有签名，则返回签名位置，
     * 
     * @param signature
     * @return
     */
    public static Aggregate<Integer, String> getValue(String signature)
    {
        // FunctionTime functionTime = new FunctionTime();
        try
        {
            Aggregate<Integer, String> aggregate = new Aggregate<Integer, String>();
            int index = signature.indexOf(KEY_AV_SIGNATURE_START);
            if (index < 0)
            {
                aggregate.put(-1, null);
                return aggregate;
            }
            
            String temp = signature.substring(index + KEY_AV_SIGNATURE_START.length());
            
            Pattern pattern = Pattern.compile("^([a-zA-Z_0-9`]+)>");
            Matcher matcher = pattern.matcher(temp);
            if (!matcher.find())
            {
                aggregate.put(-1, null);
                return aggregate;
            }
            
            String find = matcher.group();
            find = find.substring(0, find.length() - 1);
            int length = Integer.parseInt(find, 16);
            
            int pos = index + KEY_AV_SIGNATURE_START.length() + find.length() + 1;
            
            if (find.length() + 1 + length > temp.length())
            {
                aggregate.put(pos, null);
                return aggregate;
            }
            
            String json = temp.substring(find.length() + 1, find.length() + 1 + length);
            
            aggregate.put(pos, json);
            return aggregate;
        }
        finally
        {
            // functionTime.print();
        }
    }
    
    public static void main(String[] arg) throws Exception
    {
        AException e = new AException(AException.ERR_SERVER, "fuck it");
        NadoResponse rspNadoResponse = new NadoResponse();
        rspNadoResponse.setBody(NadoParam.toExplain(e));
        NettyWrap wrap = new NettyWrap();
        wrap.setCommand(Command.NC_USER);
        wrap.setSeq(17);
        wrap.setMsg(NadoParam.toExplain(rspNadoResponse));
        // wrap.setMsg("hello, world");
        
        String signature = signature(JsonUtil.toJson(wrap)) + "| from another.";
        Trace.print(signature);
        
        Aggregate<Integer, String> aggregate = getValue(signature);
        if (aggregate.getFirst() < 0)
        {
            Trace.print("not found...");
            return;
        }
        
        String json = aggregate.getSecond();
        String left = signature.substring(aggregate.getFirst() + json.length());
        Trace.print("json: {} left: {}", json, left);
        NettyWrap wrap2 = JsonUtil.toObject(NettyWrap.class, json);
        Object object = NadoParam.fromExplain(wrap2.getMsg());
        
    }
}
