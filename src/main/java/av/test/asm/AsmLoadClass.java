package av.test.asm;

import org.objectweb.asm.Opcodes;

public class AsmLoadClass extends ClassLoader implements Opcodes
{
    public Class defineClassFromBytes(String className, byte[] data) throws ClassFormatError
    {
        return defineClass(className, data, 0, data.length);
    }
}
