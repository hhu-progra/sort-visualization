package instrumentation;

import org.objectweb.asm.*;

import java.util.HashMap;
import java.util.Map;

import static instrumentation.ArrayAccessAgent.log;

public class ArrayAccessMethodVisitor extends MethodVisitor {
    private static final Map<String, String> localVariableOfBarArrayType = new HashMap<>();
    private final String methodName;

    public ArrayAccessMethodVisitor(MethodVisitor methodVisitor, String name) {
        super(Opcodes.ASM9, methodVisitor);
        this.methodName = name;
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        // Note that this method is called when the local variables area is being visited, which is after visitVarInsn
        // is called. I.e. we cannot use the localVariableOfBarArrayType map in visitVarInsn, because it is not filled yet.
        // We postpone the lookup of the real name of the variable until the code is actually executed.
        log("visitLocalVariable " + name + " " + descriptor + " " + signature + " " + start + " " + end + " " + index);
        if(descriptor.equals("[LBar;")) {
            localVariableOfBarArrayType.put(localVariableKey(index), localVariableName(name));
        }
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    private String localVariableName(String name) {
        return methodName + "." + name;
    }

    private String localVariableKey(int variableIndex) {
        return methodName + ".#" + variableIndex;
    }

    public static String getLocalVariableName(String methodAndVariableIndex) {
        return localVariableOfBarArrayType.getOrDefault(methodAndVariableIndex, methodAndVariableIndex);
    }

    @Override
    public void visitVarInsn(int opcode, int variableIndex) {
        // This method is called when a local variable is loaded onto the stack. We are only interested in Bar arrays.
        // The method newBarsArray will make sure we ignore objects with the wrong type.
        log("Local Variable Instruction: " + opcode + " " + variableIndex);

        if (opcode == Opcodes.ALOAD) {
            log("ALOAD " + variableIndex);
            // push object reference
            mv.visitVarInsn(opcode, variableIndex);
            // put reference to methodname+variableIndex on the stack
            // note that localVariableOfBarArrayType is filled _after_ visitVarInsn is called, so we have to defer getting
            // the real name until the code is executed
            mv.visitLdcInsn(localVariableKey(variableIndex));
            // This static call does not take place immediately, but will be run whenever ALOAD is executed. By then,
            // the localVariableOfBarArrayType map is filled, and we can get the real name of the variable.
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "DrawCalls", "newBarsArray", "(Ljava/lang/Object;Ljava/lang/String;)V", false);
        }
        super.visitVarInsn(opcode, variableIndex);
    }

    @Override
    public void visitInsn(int opcode) {
        log("Zero Operand Instruction: " + opcode);
        switch (opcode) {
            case Opcodes.AASTORE -> instrumentWrite();
            case Opcodes.AALOAD -> instrumentRead();
            default -> super.visitInsn(opcode);
        }
    }

    private void instrumentWrite() {
        // AASTORE: an array element is stored. We are only interested in Bar arrays, drawBars will
        // make sure we ignore objects with the wrong type.
        // As we want to visualize which array index is written, we have to copy the arrayref and the index
        // and pass it to drawBars, AFTER the actual write operation has taken place.
        log("Instrumenting AASTORE");
        // ..., arrayref, index, value →
        mv.visitInsn(Opcodes.DUP_X2);
        // ..., value, arrayref, index, value →
        mv.visitInsn(Opcodes.POP);
        // ..., value, arrayref, index →
        mv.visitInsn(Opcodes.DUP_X2);
        // ..., index, value, arrayref, index →
        mv.visitInsn(Opcodes.DUP_X2);
        // ..., index, index, value, arrayref, index →
        mv.visitInsn(Opcodes.POP);
        // ..., index, index, value, arrayref →
        mv.visitInsn(Opcodes.DUP_X2);
        // ..., index, arrayref, index, value, arrayref →
        mv.visitInsn(Opcodes.DUP_X2);
        // ..., index, arrayref, arrayref, index, value, arrayref →
        mv.visitInsn(Opcodes.POP);
        // ..., index, arrayref, arrayref, index, value →
        super.visitInsn(Opcodes.AASTORE); // do actual write operation
        // ..., index, arrayref →
        mv.visitInsn(Opcodes.SWAP);
        // ..., arrayref, index →
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "DrawCalls", "drawBarsWrite", "(Ljava/lang/Object;I)V", false);
    }

    private void instrumentRead() {
        // AALOAD: an array element is loaded. We are only interested in Bar arrays, drawBars will
        // make sure we ignore objects with the wrong type.
        // As we want to visualize which array index is read, we have to copy the arrayref and the index
        // and pass it to drawBars.
        // ..., arrayref, index →
        mv.visitInsn(Opcodes.DUP_X1);
        // ..., index, arrayref, index →
        mv.visitInsn(Opcodes.SWAP);
        // ..., index, index, arrayref →
        mv.visitInsn(Opcodes.DUP_X2);
        // ..., arrayref, index, index, arrayref →
        mv.visitInsn(Opcodes.SWAP);
        // ..., arrayref, index, arrayref, index →
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "DrawCalls", "drawBarsRead", "(Ljava/lang/Object;I)V", false);
        super.visitInsn(Opcodes.AALOAD);
    }
}