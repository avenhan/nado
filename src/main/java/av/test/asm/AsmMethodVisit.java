package av.test.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class AsmMethodVisit extends MethodVisitor
{
    
    
    public AsmMethodVisit(MethodVisitor mv)
    {
        super(Opcodes.ASM4, mv);
    }
    
    @Override
    public void visitCode()
    {
        // 此方法在访问方法的头部时被访问到，仅被访问一次
        visitMethodInsn(Opcodes.INVOKESTATIC, Monitor.class.getName(), "start", "()V");
        super.visitCode();
        
    }
    
    @Override
    public void visitInsn(int opcode)
    {
        // 此方法可以获取方法中每一条指令的操作类型，被访问多次
        // 如应在方法结尾处添加新指令，则应判断：
        if (opcode == Opcodes.RETURN)
        {
            visitMethodInsn(Opcodes.INVOKESTATIC, Monitor.class.getName(), "end", "()V");
        }
        super.visitInsn(opcode);
    }
    
}
