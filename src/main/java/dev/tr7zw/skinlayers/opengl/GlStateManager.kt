package dev.tr7zw.skinlayers.opengl

import org.lwjgl.opengl.GL11
import java.nio.ByteBuffer

object GlStateManager {
    fun _getTexImage(i: Int, j: Int, k: Int, l: Int, m: ByteBuffer?) {
        GL11.glGetTexImage(i, j, k, l, m)
    }

    fun _pixelStore(i: Int, j: Int) {
        GL11.glPixelStorei(i, j)
    }
}
