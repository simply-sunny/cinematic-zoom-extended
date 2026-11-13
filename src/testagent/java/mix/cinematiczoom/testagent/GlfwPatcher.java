package mix.cinematiczoom.testagent;

import java.util.function.UnaryOperator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** The GLFW class rewrite for BackgroundWindowAgent. Loaded apart from the game, with its own ASM. */
public final class GlfwPatcher implements UnaryOperator<byte[]> {
    private static final String GLFW = "org/lwjgl/glfw/GLFW";
    private static final int GLFW_FOCUSED = 0x00020001;
    private static final int GLFW_FOCUS_ON_SHOW = 0x0002000C;

    @Override
    public byte[] apply(byte[] bytes) {
        return patch(bytes);
    }

    private static byte[] patch(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor method = super.visitMethod(access, name, descriptor, signature, exceptions);
                // Both public glfwCreateWindow overloads end up here, after the game has set its hints.
                if (name.equals("nglfwCreateWindow")) {
                    return new MethodVisitor(Opcodes.ASM9, method) {
                        @Override
                        public void visitCode() {
                            super.visitCode();
                            disableHint(this, GLFW_FOCUSED);
                            disableHint(this, GLFW_FOCUS_ON_SHOW);
                        }
                    };
                }
                if (name.equals("glfwFocusWindow") && descriptor.equals("(J)V")) {
                    method.visitCode();
                    method.visitInsn(Opcodes.RETURN);
                    method.visitMaxs(0, 2);
                    method.visitEnd();
                    return null;
                }
                return method;
            }
        }, 0);
        return writer.toByteArray();
    }

    private static void disableHint(MethodVisitor method, int hint) {
        method.visitLdcInsn(hint);
        method.visitInsn(Opcodes.ICONST_0);
        method.visitMethodInsn(Opcodes.INVOKESTATIC, GLFW, "glfwWindowHint", "(II)V", false);
    }
}
