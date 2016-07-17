package av.nado.util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class MDHelper
{
    public static final String                   MD5         = "MD5";
    public static final String                   SHA1        = "SHA-1";
    public static final String                   SHA256      = "SHA-256";
    public static final String                   SHA384      = "SHA-384";
    public static final String                   SHA512      = "SHA-512";
    public static final String                   RIPEMD128   = "RIPEMD128";
    public static final String                   RIPEMD160   = "RIPEMD160";
    
    private static final char[]                  HEX_DIGITS  = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private static final Map<Character, Integer> mapHexIndex = new HashMap<Character, Integer>();
    
    static
    {
        int index = 0;
        for (char c : HEX_DIGITS)
        {
            mapHexIndex.put(c, index);
            index++;
        }
    }
    
    private static final ThreadLocal<MessageDigest> localMD5 = new ThreadLocal<MessageDigest>()
    {
        @Override
        protected synchronized MessageDigest initialValue()
        {
            try
            {
                return MessageDigest.getInstance(MD5);
            }
            catch (Exception e)
            {
            }
            return null;
        }
    };
    
    private static MessageDigest getMD5()
    {
        return localMD5.get();
    }
    
    public static String md5(String input)
    {
        return md5(input.getBytes());
    }
    
    public static String md5(byte[] input)
    {
        return hex(getMD5().digest(input));
    }
    
    public static String md5(ByteBuffer buf)
    {
        MessageDigest md = getMD5();
        md.update(buf);
        return hex(md.digest());
    }
    
    public static byte[] md5(ByteBuffer[] bufs)
    {
        MessageDigest md = getMD5();
        for (int i = 0; i < bufs.length; i++)
        {
            md.update(bufs[i]);
        }
        return md.digest();
    }
    
    public static String hex(int value)
    {
        return Integer.toHexString(value);
    }
    
    public static String hex(long value)
    {
        return "";
    }
    
    public static String hex(double value)
    {
        return "";
    }
    
    public static String hex(byte[] bytes)
    {
        int len = bytes.length;
        StringBuilder builder = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++)
        {
            builder.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            builder.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return builder.toString();
    }
    
    public static byte[] fromHex(String value)
    {
        int length = value.length();
        byte[] ret = new byte[length / 2];
        for (int i = 0; i < length; i += 2)
        {
            char first = value.charAt(i);
            char second = value.charAt(i + 1);
            
            int byteValue = (mapHexIndex.get(first) << 4) | (mapHexIndex.get(second));
            ret[i / 2] = (byte) byteValue;
        }
        
        return ret;
    }
    
    public static void main(String[] arg)
    {
        byte[] arr = { 10, (byte) 234, (byte) 240, 123, 5 };
        for (byte b : arr)
        {
            System.out.print(b + ", ");
        }
        System.out.println(" ");
        String ret = hex(arr);
        System.out.println(ret);
        
        byte[] arr2 = fromHex(ret);
        for (byte b : arr2)
        {
            System.out.print(b + ", ");
        }
    }
}
