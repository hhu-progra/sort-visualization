package instrumentation;

import org.objectweb.asm.*;

import static instrumentation.ArrayAccessAgent.log;

class ArrayAccessClassVisitor extends ClassVisitor {
    public ArrayAccessClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
        log("ArrayAccessClassVisitor created");
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        log("Visiting method: " + name);
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (!isSetupMethod(name)) {
            // We want to instrument all methods the students write, but we want to skip the boring array initialization methods.
            log("Instrumenting method: " + name);
            return new ArrayAccessMethodVisitor(mv, name);
        }
        return mv;
    }

    private static boolean isSetupMethod(String name) {
        return name.toLowerCase().contains("toints")
                || name.toLowerCase().contains("random")
                || name.toLowerCase().contains("heightsfrom")
                || name.toLowerCase().contains("main")
                || name.toLowerCase().contains("printbars")
                || name.toLowerCase().contains("init");
    }
}