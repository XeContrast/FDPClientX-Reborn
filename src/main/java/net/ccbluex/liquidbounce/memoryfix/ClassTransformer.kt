//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package net.ccbluex.liquidbounce.memoryfix

import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.commons.RemappingClassAdapter
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.util.function.BiConsumer
import java.util.function.Consumer

class ClassTransformer : IClassTransformer {
    override fun transform(name: String, transformedName: String, bytes: ByteArray): ByteArray {
        return if (name == "CapeUtils") {
            transformCapeUtils(bytes)
        } else if (name == "io.prplz.net.ccbluex.liquidbounce.memoryfix.CapeImageBuffer") {
            transformMethods(bytes) { clazz: ClassNode, method: MethodNode ->
                this.transformCapeImageBuffer(
                    clazz,
                    method
                )
            }
        } else if (transformedName == "net.minecraft.client.resources.AbstractResourcePack") {
            transformMethods(bytes) { clazz: ClassNode, method: MethodNode ->
                this.transformAbstractResourcePack(
                    clazz,
                    method
                )
            }
        } else {
            if (transformedName == "net.minecraft.client.Minecraft") this.transformMethods(bytes) { clazz: ClassNode, method: MethodNode ->
                this.transformMinecraft(
                    clazz,
                    method
                )
            } else bytes
        }
    }

    private fun transformMethods(bytes: ByteArray, transformer: BiConsumer<ClassNode, MethodNode>): ByteArray {
        val classReader = ClassReader(bytes)
        val classNode = ClassNode()
        classReader.accept(classNode, 0)
        classNode.methods.forEach(Consumer { m: MethodNode ->
            transformer.accept(classNode, m)
        })
        val classWriter = ClassWriter(0)
        classNode.accept(classWriter)
        return classWriter.toByteArray()
    }

    private fun transformCapeUtils(bytes: ByteArray): ByteArray {
        val classWriter = ClassWriter(2)
        val adapter = RemappingClassAdapter(classWriter, object : Remapper() {
            override fun map(typeName: String): String {
                return if (typeName == "CapeUtils$1") "io.prplz.net.ccbluex.liquidbounce.memoryfix.CapeImageBuffer".replace(
                    '.',
                    '/'
                ) else typeName
            }
        })
        val classReader = ClassReader(bytes)
        classReader.accept(adapter, 8)
        return classWriter.toByteArray()
    }

    private fun transformCapeImageBuffer(clazz: ClassNode, method: MethodNode) {
        val iter: ListIterator<*> = method.instructions.iterator()

        while (iter.hasNext()) {
            val insn = iter.next() as AbstractInsnNode
            if (insn is MethodInsnNode) {
                if (insn.name == "parseCape") {
                    insn.owner = "CapeUtils"
                } else if (insn.name == "setLocationOfCape") {
                    insn.opcode = 182
                    insn.owner = "net/minecraft/client/entity/AbstractClientPlayer"
                    insn.desc = "(Lnet/minecraft/util/ResourceLocation;)V"
                }
            }
        }
    }

    private fun transformAbstractResourcePack(clazz: ClassNode, method: MethodNode) {
        if ((method.name == "getPackImage" || method.name == "func_110586_a") && (method.desc == "()Ljava/awt/image/BufferedImage;")) {
            val iter: ListIterator<*> = method.instructions.iterator()

            while (iter.hasNext()) {
                val insn = iter.next() as AbstractInsnNode
                if (insn.opcode == 176) {
                    method.instructions.insertBefore(
                        insn,
                        MethodInsnNode(
                            184,
                            "io.prplz.net.ccbluex.liquidbounce.memoryfix.ResourcePackImageScaler".replace('.', '/'),
                            "scalePackImage",
                            "(Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;",
                            false
                        )
                    )
                }
            }
        }
    }

    private fun transformMinecraft(clazz: ClassNode, method: MethodNode) {
        val iter: MutableListIterator<*> = method.instructions.iterator()

        while (iter.hasNext()) {
            val insn = iter.next() as AbstractInsnNode
            if (insn.opcode == 184) {
                val methodInsn = insn as MethodInsnNode
                if (methodInsn.owner == "java/lang/System" && methodInsn.name == "gc") {
                    iter.remove()
                }
            }
        }
    }
}
