/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & opZywl & lucas]
 */
package net.ccbluex.liquidbounce.utils

import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import java.nio.FloatBuffer
import java.nio.IntBuffer

object GLUtils {
    private val windowPosition: FloatBuffer = GLAllocation.createDirectFloatBuffer(4)
    private val viewport: IntBuffer = GLAllocation.createDirectIntBuffer(16)
    private val modelMatrix: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val projectionMatrix: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)
    private val BUFFER = FloatArray(3)

    fun enableDepth() {
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
    }

    fun disableDepth() {
        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)
    }

    var enabledCaps: IntArray = IntArray(32)

    fun enableCaps(vararg caps: Int) {
        for (cap in caps) GL11.glEnable(cap)
        enabledCaps = caps
    }

    fun disableCaps() {
        for (cap in enabledCaps) GL11.glDisable(cap)
    }

    fun startBlend() {
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    }


    fun endBlend() {
        GlStateManager.disableBlend()
    }

    @JvmOverloads
    fun setup2DRendering(blend: Boolean = true) {
        if (blend) {
            startBlend()
        }
        GlStateManager.disableTexture2D()
    }

    fun end2DRendering() {
        GlStateManager.enableTexture2D()
        endBlend()
    }

    fun startRotate(x: Float, y: Float, rotate: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
        GlStateManager.rotate(rotate, 0f, 0f, -1f)
        GlStateManager.translate(-x, -y, 0f)
    }

    fun endRotate() {
        GlStateManager.popMatrix()
    }

    @JvmStatic
    fun project2D(
        x: Float,
        y: Float,
        z: Float,
        scaleFactor: Int
    ): FloatArray? {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix)
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix)
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport)

        if (GLU.gluProject(x, y, z, modelMatrix, projectionMatrix, viewport, windowPosition)) {
            BUFFER[0] = windowPosition[0] / scaleFactor
            BUFFER[1] = (Display.getHeight() - windowPosition[1]) / scaleFactor
            BUFFER[2] = windowPosition[2]
            return BUFFER
        }

        return null
    }
}
