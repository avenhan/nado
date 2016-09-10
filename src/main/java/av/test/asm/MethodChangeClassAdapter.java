package av.test.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class MethodChangeClassAdapter extends ClassVisitor implements Opcodes
{
    public MethodChangeClassAdapter(final ClassVisitor cv)
    {
        super(Opcodes.ASM4, cv);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
    {
        if ("print".equals(name)) // 此处的execute即为需要修改的方法 ，修改方法內容
        {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);// 先得到原始的方法
            MethodVisitor newMethod = null;
            newMethod = new AsmMethodVisit(mv); // 访问需要修改的方法
            return newMethod;
        }
        
        return null;
    }

}
