//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.ccbluex.liquidbounce.memoryfix;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;
import java.util.function.BiConsumer;

public class ClassTransformer implements IClassTransformer {
    public ClassTransformer() {
    }

    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (name.equals("CapeUtils")) {
            return this.transformCapeUtils(bytes);
        } else if (name.equals("io.prplz.net.ccbluex.liquidbounce.memoryfix.CapeImageBuffer")) {
            return this.transformMethods(bytes, this::transformCapeImageBuffer);
        } else if (transformedName.equals("net.minecraft.client.resources.AbstractResourcePack")) {
            return this.transformMethods(bytes, this::transformAbstractResourcePack);
        } else {
            return transformedName.equals("net.minecraft.client.Minecraft") ? this.transformMethods(bytes, this::transformMinecraft) : bytes;
        }
    }

    private byte[] transformMethods(byte[] bytes, BiConsumer<ClassNode, MethodNode> transformer) {
        ClassReader classReader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        classNode.methods.forEach((m) -> {
            transformer.accept(classNode, m);
        });
        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private byte[] transformCapeUtils(byte[] bytes) {
        ClassWriter classWriter = new ClassWriter(2);
        RemappingClassAdapter adapter = new RemappingClassAdapter(classWriter, new Remapper() {
            public String map(String typeName) {
                return typeName.equals("CapeUtils$1") ? "io.prplz.net.ccbluex.liquidbounce.memoryfix.CapeImageBuffer".replace('.', '/') : typeName;
            }
        });
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(adapter, 8);
        return classWriter.toByteArray();
    }

    private void transformCapeImageBuffer(ClassNode clazz, MethodNode method) {
        ListIterator iter = method.instructions.iterator();

        while(iter.hasNext()) {
            AbstractInsnNode insn = (AbstractInsnNode)iter.next();
            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode)insn;
                if (methodInsn.name.equals("parseCape")) {
                    methodInsn.owner = "CapeUtils";
                } else if (methodInsn.name.equals("setLocationOfCape")) {
                    methodInsn.setOpcode(182);
                    methodInsn.owner = "net/minecraft/client/entity/AbstractClientPlayer";
                    methodInsn.desc = "(Lnet/minecraft/util/ResourceLocation;)V";
                }
            }
        }

    }

    private void transformAbstractResourcePack(ClassNode clazz, MethodNode method) {
        if ((method.name.equals("getPackImage") || method.name.equals("func_110586_a")) && method.desc.equals("()Ljava/awt/image/BufferedImage;")) {
            ListIterator iter = method.instructions.iterator();

            while(iter.hasNext()) {
                AbstractInsnNode insn = (AbstractInsnNode)iter.next();
                if (insn.getOpcode() == 176) {
                    method.instructions.insertBefore(insn, new MethodInsnNode(184, "io.prplz.net.ccbluex.liquidbounce.memoryfix.ResourcePackImageScaler".replace('.', '/'), "scalePackImage", "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false));
                }
            }
        }

    }

    private void transformMinecraft(ClassNode clazz, MethodNode method) {
        ListIterator iter = method.instructions.iterator();

        while(iter.hasNext()) {
            AbstractInsnNode insn = (AbstractInsnNode)iter.next();
            if (insn.getOpcode() == 184) {
                MethodInsnNode methodInsn = (MethodInsnNode)insn;
                if (methodInsn.owner.equals("java/lang/System") && methodInsn.name.equals("gc")) {
                    iter.remove();
                }
            }
        }

    }
}
