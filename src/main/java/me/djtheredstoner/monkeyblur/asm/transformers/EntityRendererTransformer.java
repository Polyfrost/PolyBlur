package me.djtheredstoner.monkeyblur.asm.transformers;

import me.djtheredstoner.asmdsl.InsnListBuilder;
import me.djtheredstoner.monkeyblur.asm.ITransformer;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

public class EntityRendererTransformer implements ITransformer {
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.renderer.EntityRenderer"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode method : classNode.methods) {
            String methodName = mapMethodName(classNode, method);
            if (methodName.equals("renderWorldPass") || methodName.equals("func_175068_a")) {
                method.instructions.insert(renderWorldPassStart());

                final ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();

                while (iterator.hasNext()) {
                    final AbstractInsnNode next = iterator.next();
                    if (next.getOpcode() == LDC) {
                        LdcInsnNode ldc = (LdcInsnNode) next;
                        if ("hand".equals(ldc.cst)) {
                            method.instructions.insertBefore(ldc, renderWorldPassEnd());
                        } else if ("frustum".equals(ldc.cst)) {
                            method.instructions.insertBefore(ldc, setupCamera());
                        }
                    }
                }
            }

        }
    }

    private InsnList renderWorldPassStart() {
        return new InsnListBuilder() {{
            invokestatic(
                "me/djtheredstoner/monkeyblur/Hooks",
                "startFrame",
                "()V"
            );
        }}.l();
    }

    private InsnList renderWorldPassEnd() {
        return new InsnListBuilder() {{
            invokestatic(
                "me/djtheredstoner/monkeyblur/Hooks",
                "endFrame",
                "()V"
            );
        }}.l();
    }

    private InsnList setupCamera() {
        return new InsnListBuilder() {{
            fload(2);
            invokestatic(
                "me/djtheredstoner/monkeyblur/Hooks",
                "setupCamera",
                "(F)V"
            );
        }}.l();
    }
}
