/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.injection.access.StaticStorage
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getBlock
import net.ccbluex.liquidbounce.utils.misc.Animation
import net.ccbluex.liquidbounce.utils.render.ColorUtils.setColour
import net.ccbluex.liquidbounce.utils.render.shader.Shader
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.glu.GLU
import java.awt.Color
import kotlin.math.*

object RenderUtils : MinecraftInstance() {
    private val glCapMap: MutableMap<String, MutableMap<Int, Boolean>> = HashMap()
    private var startTime: Long = 0
    private val fakePlayers: MutableMap<Int, EntityOtherPlayerMP> = HashMap()

    fun deltaTimeNormalized(ticks: Int = 50) = (deltaTime / ticks.toDouble()).coerceAtMost(1.0)

    @JvmField
    var deltaTime: Int = 0

    private val DISPLAY_LISTS_2D = IntArray(4)

    init {
        for (i in DISPLAY_LISTS_2D.indices) {
            DISPLAY_LISTS_2D[i] = glGenLists(1)
        }

        glNewList(DISPLAY_LISTS_2D[0], GL11.GL_COMPILE)

        quickDrawRect(-7f, 2f, -4f, 3f)
        quickDrawRect(4f, 2f, 7f, 3f)
        quickDrawRect(-7f, 0.5f, -6f, 3f)
        quickDrawRect(6f, 0.5f, 7f, 3f)

        glEndList()

        glNewList(DISPLAY_LISTS_2D[1], GL11.GL_COMPILE)

        quickDrawRect(-7f, 3f, -4f, 3.3f)
        quickDrawRect(4f, 3f, 7f, 3.3f)
        quickDrawRect(-7.3f, 0.5f, -7f, 3.3f)
        quickDrawRect(7f, 0.5f, 7.3f, 3.3f)

        glEndList()

        glNewList(DISPLAY_LISTS_2D[2], GL11.GL_COMPILE)

        quickDrawRect(4f, -20f, 7f, -19f)
        quickDrawRect(-7f, -20f, -4f, -19f)
        quickDrawRect(6f, -20f, 7f, -17.5f)
        quickDrawRect(-7f, -20f, -6f, -17.5f)

        glEndList()

        glNewList(DISPLAY_LISTS_2D[3], GL11.GL_COMPILE)

        quickDrawRect(7f, -20f, 7.3f, -17.5f)
        quickDrawRect(-7.3f, -20f, -7f, -17.5f)
        quickDrawRect(4f, -20.3f, 7.3f, -20f)
        quickDrawRect(-7.3f, -20.3f, -4f, -20f)

        glEndList()
    }

    @JvmStatic
    fun doGlScissor(x: Int, y: Int, width: Int, height: Int) {
        val mc = Minecraft.getMinecraft()
        var scaleFactor = 1
        var k = mc.gameSettings.guiScale
        if (k == 0) {
            k = 1000
        }
        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor
        }
        GL11.glScissor(
            x * scaleFactor,
            mc.displayHeight - (y + height) * scaleFactor,
            width * scaleFactor,
            height * scaleFactor
        )
    }

    @JvmStatic
    fun drawArc(x1: Float, y1: Float, r: Double, color: Int, startPoint: Int, arc: Double, linewidth: Int) {
        var x1 = x1
        var y1 = y1
        var r = r
        r *= 2.0
        x1 *= 2f
        y1 *= 2f
        val f = (color shr 24 and 0xFF) / 255.0f
        val f1 = (color shr 16 and 0xFF) / 255.0f
        val f2 = (color shr 8 and 0xFF) / 255.0f
        val f3 = (color and 0xFF) / 255.0f
        glDisable(2929)
        glEnable(3042)
        glDisable(3553)
        glBlendFunc(770, 771)
        glDepthMask(true)
        glEnable(2848)
        glHint(3154, 4354)
        glHint(3155, 4354)
        glScalef(0.5f, 0.5f, 0.5f)
        glLineWidth(linewidth.toFloat())
        glEnable(GL11.GL_LINE_SMOOTH)
        glColor4f(f1, f2, f3, f)
        glBegin(GL11.GL_LINE_STRIP)
        var i = startPoint
        while (i <= arc) {
            val x = sin(i * Math.PI / 180.0) * r
            val y = cos(i * Math.PI / 180.0) * r
            glVertex2d(x1 + x, y1 + y)
            i += 1
        }
        glEnd()
        glDisable(GL11.GL_LINE_SMOOTH)
        glScalef(2.0f, 2.0f, 2.0f)
        glEnable(3553)
        glDisable(3042)
        glEnable(2929)
        glDisable(2848)
        glHint(3154, 4352)
        glHint(3155, 4352)
    }

    fun drawImage2(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int) {
        glDisable(GL11.GL_DEPTH_TEST)
        glEnable(GL11.GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        mc.textureManager.bindTexture(image)
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        glDepthMask(true)
        glDisable(GL11.GL_BLEND)
        glEnable(GL11.GL_DEPTH_TEST)
    }

    @JvmStatic
    fun getAnimationStateSmooth(target: Double, current: Double, speed: Double): Double {
        var current = current
        var speed = speed
        val larger = target > current
        val bl = larger
        if (speed < 0.0) {
            speed = 0.0
        } else if (speed > 1.0) {
            speed = 1.0
        }
        if (target == current) {
            return target
        }
        val dif = max(target, current) - min(target, current)
        var factor = max((dif * speed), 1.0)
        if (factor < 0.1) {
            factor = 0.1
        }
        if (larger) {
            if (current + factor > target) {
                current = target
            } else {
                current += factor
            }
        } else {
            if (current - factor < target) {
                current = target
            } else {
                current -= factor
            }
        }
        return current
    }

    fun drawRoundedGradientRectCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        radius: Float,
        color: Int,
        color2: Int,
        color3: Int,
        color4: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        setColour(-1)
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)

        GL11.glPushAttrib(0)
        GL11.glScaled(0.5, 0.5, 0.5)
        x *= 2.0f
        y *= 2.0f
        x1 *= 2.0f
        y1 *= 2.0f
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glBegin(6)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color3)
        i = 0
        while (i <= 90) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y1 - radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        setColour(color4)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        glEnd()
        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
        glDisable(GL11.GL_BLEND)
        glEnable(GL11.GL_TEXTURE_2D)
        GL11.glScaled(2.0, 2.0, 2.0)
        GL11.glPopAttrib()


        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL11.GL_FLAT)
        setColour(-1)
    }

    fun scaleStart(x: Float, y: Float, scale: Float) {
        GL11.glTranslatef(x, y, 0f)
        glScalef(scale, scale, 1f)
        GL11.glTranslatef(-x, -y, 0f)
    }

    fun connectPoints(xOne: Float, yOne: Float, xTwo: Float, yTwo: Float) {
        glPushMatrix()
        glEnable(GL11.GL_LINE_SMOOTH)
        glColor4f(1.0f, 1.0f, 1.0f, 0.8f)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_BLEND)
        glLineWidth(0.5f)
        glBegin(GL11.GL_LINES)
        GL11.glVertex2f(xOne, yOne)
        GL11.glVertex2f(xTwo, yTwo)
        glEnd()
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        glDisable(GL11.GL_LINE_SMOOTH)
        glEnable(GL11.GL_TEXTURE_2D)
        GL11.glPopMatrix()
    }


    private const val zLevel = 0f

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height.
     */
    fun drawTexturedModalRect(x: Int, y: Int, textureX: Int, textureY: Int, width: Int, height: Int, zLevel: Float) {
        val f = 0.00390625f
        val f1 = 0.00390625f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), zLevel.toDouble())
            .tex((textureX.toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), zLevel.toDouble())
            .tex(((textureX + width).toFloat() * f).toDouble(), ((textureY + height).toFloat() * f1).toDouble())
            .endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), zLevel.toDouble())
            .tex(((textureX + width).toFloat() * f).toDouble(), (textureY.toFloat() * f1).toDouble()).endVertex()
        worldrenderer.pos(
            x.toDouble(),
            y.toDouble(), zLevel.toDouble()
        ).tex((textureX.toFloat() * f).toDouble(), (textureY.toFloat() * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    fun drawCircle(x: Float, y: Float, radius: Float, color: Int) {
        glColor(color)
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)
        glPushMatrix()
        glLineWidth(1f)
        glBegin(GL11.GL_POLYGON)
        for (i in 0..360) glVertex2d(x + sin(i * Math.PI / 180.0) * radius, y + cos(i * Math.PI / 180.0) * radius)
        glEnd()
        GL11.glPopMatrix()
        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_LINE_SMOOTH)
        glColor4f(1f, 1f, 1f, 1f)
    }

    @JvmStatic
    fun drawCheck(x: Double, y: Double, lineWidth: Int, color: Int) {
        start2D()
        glPushMatrix()
        glLineWidth(lineWidth.toFloat())
        setColor(Color(color))
        glBegin(GL11.GL_LINE_STRIP)
        glVertex2d(x, y)
        glVertex2d(x + 2, y + 3)
        glVertex2d(x + 6, y - 2)
        glEnd()
        GL11.glPopMatrix()
        stop2D()
    }

    @JvmStatic
    fun setColor(color: Color) {
        val alpha = (color.rgb shr 24 and 0xFF) / 255.0f
        val red = (color.rgb shr 16 and 0xFF) / 255.0f
        val green = (color.rgb shr 8 and 0xFF) / 255.0f
        val blue = (color.rgb and 0xFF) / 255.0f
        glColor4f(red, green, blue, alpha)
    }

    private fun start2D() {
        glEnable(3042)
        glDisable(3553)
        glBlendFunc(770, 771)
        glEnable(2848)
    }

    private fun stop2D() {
        glEnable(3553)
        glDisable(3042)
        glDisable(2848)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        glColor4f(1f, 1f, 1f, 1f)
    }

    private fun quickPolygonCircle(x: Float, y: Float, xRadius: Float, yRadius: Float, start: Int, end: Int) {
        var i = end
        while (i >= start) {
            glVertex2d(x + sin(i * Math.PI / 180.0) * xRadius, y + cos(i * Math.PI / 180.0) * yRadius)
            i -= 4
        }
    }

    fun getRainbowOpaque(seconds: Int, saturation: Float, brightness: Float, index: Int): Int {
        val hue = ((System.currentTimeMillis() + index) % (seconds * 1000)) / (seconds * 1000).toFloat()
        return Color.HSBtoRGB(hue, saturation, brightness)
    }

    @JvmStatic
    fun smoothAnimation(ani: Float, finalState: Float, speed: Float, scale: Float): Float {
        return getAnimationState(
            ani.toDouble(),
            finalState.toDouble(),
            (max(10.0, (abs((ani - finalState).toDouble()) * speed)) * scale)
        ).toFloat()
    }

    @JvmStatic
    fun isHovering(mouseX: Int, mouseY: Int, xLeft: Float, yUp: Float, xRight: Float, yBottom: Float): Boolean {
        return mouseX.toFloat() > xLeft && mouseX.toFloat() < xRight && mouseY.toFloat() > yUp && mouseY.toFloat() < yBottom
    }

    @JvmStatic
    fun scissor(x: Double, y: Double, width: Double, height: Double) {
        var scaleFactor = ScaledResolution(Minecraft.getMinecraft()).scaleFactor
        while ((scaleFactor < 2 && Minecraft.getMinecraft().displayWidth / (scaleFactor + 1) >= 320) && Minecraft.getMinecraft().displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor
        }
        GL11.glScissor(
            (x * scaleFactor).toInt(),
            (Minecraft.getMinecraft().displayHeight - (y + height) * scaleFactor).toInt(),
            (width * scaleFactor).toInt(),
            (height * scaleFactor).toInt()
        )
    }

    @JvmStatic
    fun drawRoundedCornerRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float, color: Int) {
        glEnable(GL11.GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL11.GL_TEXTURE_2D)
        val hasCull = GL11.glIsEnabled(GL11.GL_CULL_FACE)
        glDisable(GL11.GL_CULL_FACE)

        glColor(color)
        drawRoundedCornerRect(x, y, x1, y1, radius)

        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        setGlState(GL11.GL_CULL_FACE, hasCull)
    }

    @JvmStatic
    fun drawGradientRect(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        sideways: Boolean,
        startColor: Int,
        endColor: Int
    ) {
        glDisable(3553)
        glEnable(3042)
        glBlendFunc(770, 771)
        glShadeModel(7425)
        glBegin(7)
        color(startColor)
        if (sideways) {
            glVertex2d(left, top)
            glVertex2d(left, bottom)
            color(endColor)
            glVertex2d(right, bottom)
            glVertex2d(right, top)
        } else {
            glVertex2d(left, top)
            color(endColor)
            glVertex2d(left, bottom)
            glVertex2d(right, bottom)
            color(startColor)
            glVertex2d(right, top)
        }
        glEnd()
        glDisable(3042)
        glShadeModel(7424)
        glEnable(3553)
    }

    @JvmStatic
    fun drawGradientRect(left: Float, top: Float, right: Float, bottom: Float, startColor: Int, endColor: Int) {
        val f = (startColor shr 24 and 0xFF) / 255.0f
        val f2 = (startColor shr 16 and 0xFF) / 255.0f
        val f3 = (startColor shr 8 and 0xFF) / 255.0f
        val f4 = (startColor and 0xFF) / 255.0f
        val f5 = (endColor shr 24 and 0xFF) / 255.0f
        val f6 = (endColor shr 16 and 0xFF) / 255.0f
        val f7 = (endColor shr 8 and 0xFF) / 255.0f
        val f8 = (endColor and 0xFF) / 255.0f
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0).color(f2, f3, f4, f).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).color(f2, f3, f4, f).endVertex()
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).color(f6, f7, f8, f5).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).color(f6, f7, f8, f5).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
    }

    @JvmStatic
    fun getAnimationState(animation: Double, finalState: Double, speed: Double): Double {
        var animation = animation
        val add = (0.01 * speed).toFloat()

        animation =
            if (animation < finalState) (min(animation + add, finalState)) else (max(animation - add, finalState))
        return animation
    }

    @JvmStatic
    fun drawClickGuiArrow(x: Float, y: Float, size: Float, animation: Animation, color: Int) {
        GL11.glTranslatef(x, y, 0.0f)
        val interpolation = DoubleArray(1)
        setup2DRendering {
            render(5) {
                color(color)
                interpolation[0] = interpolate(0.0, size / 2.0, animation.output)
                if (animation.output >= 0.48) {
                    glVertex2d((size / 2.0f).toDouble(), interpolate(size / 2.0, 0.0, animation.output))
                }
                glVertex2d(0.0, interpolation[0])
                if (animation.output < 0.48) {
                    glVertex2d((size / 2.0f).toDouble(), interpolate(size / 2.0, 0.0, animation.output))
                }
                glVertex2d(size.toDouble(), interpolation[0])
            }
        }
        GL11.glTranslatef(-x, -y, 0.0f)
    }

    fun render(mode: Int, render: Runnable) {
        glBegin(mode)
        render.run()
        glEnd()
    }

    private fun setup2DRendering(f: Runnable) {
        glEnable(3042)
        glBlendFunc(770, 771)
        glDisable(3553)
        f.run()
        glEnable(3553)
        GlStateManager.disableBlend()
    }

    @JvmStatic
    fun drawGradientRect(left: Int, top: Int, right: Int, bottom: Int, startColor: Int, endColor: Int) {
        val f = (startColor shr 24 and 255).toFloat() / 255.0f
        val f1 = (startColor shr 16 and 255).toFloat() / 255.0f
        val f2 = (startColor shr 8 and 255).toFloat() / 255.0f
        val f3 = (startColor and 255).toFloat() / 255.0f
        val f4 = (endColor shr 24 and 255).toFloat() / 255.0f
        val f5 = (endColor shr 16 and 255).toFloat() / 255.0f
        val f6 = (endColor shr 8 and 255).toFloat() / 255.0f
        val f7 = (endColor and 255).toFloat() / 255.0f
        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(right.toDouble(), top.toDouble(), zLevel.toDouble()).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), zLevel.toDouble()).color(f1, f2, f3, f).endVertex()
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), zLevel.toDouble()).color(f5, f6, f7, f4).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), zLevel.toDouble()).color(f5, f6, f7, f4).endVertex()
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    private fun drawRDRect(left: Float, top: Float, width: Float, height: Float, color: Int) {
        val f3 = (color shr 24 and 0xFF) / 255.0f
        val f4 = (color shr 16 and 0xFF) / 255.0f
        val f5 = (color shr 8 and 0xFF) / 255.0f
        val f6 = (color and 0xFF) / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f4, f5, f6, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left.toDouble(), (top + height).toDouble(), 0.0).endVertex()
        worldrenderer.pos((left + width).toDouble(), (top + height).toDouble(), 0.0).endVertex()
        worldrenderer.pos((left + width).toDouble(), top.toDouble(), 0.0).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    @JvmStatic
    fun drawGradientRoundedRect(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        radius: Int,
        startColor: Int,
        endColor: Int
    ) {
        Stencil.write(false)
        glDisable(GL11.GL_TEXTURE_2D)
        glEnable(GL11.GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        fastRoundedRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), radius.toFloat())
        glDisable(GL11.GL_BLEND)
        glEnable(GL11.GL_TEXTURE_2D)
        Stencil.erase(true)
        drawGradientRect(left, top, right, bottom, startColor, endColor)
        Stencil.dispose()
    }

    @JvmStatic
    fun drawRoundedRect(x: Float, y: Float, x1: Float, y1: Float, borderC: Int, insideC: Int) {
        drawRect(x + 0.5f, y, x1 - 0.5f, y + 0.5f, insideC)
        drawRect(x + 0.5f, y1 - 0.5f, x1 - 0.5f, y1, insideC)
        drawRect(x, y + 0.5f, x1, y1 - 0.5f, insideC)
    }

    fun drawRoundedRect3(x: Float, y: Float, x2: Float, y2: Float, round: Float, color: Int, mode: Int) {
        var x = x
        var y = y
        var x2 = x2
        var y2 = y2
        val rectX = x
        val rectY = y
        val rectX2 = x2
        val rectY2 = y2
        x += (round / 2.0f + 0.5).toFloat()
        y += (round / 2.0f + 0.5).toFloat()
        x2 -= (round / 2.0f + 0.5).toFloat()
        y2 -= (round / 2.0f + 0.5).toFloat()
        if (mode == 1) drawRect(x, rectY, rectX2, rectY2, color)
        else drawRect(rectX, rectY, x2, rectY2, color)
        circle(x2 - round / 2.0f, y + round / 2.0f, round, color)
        circle(x + round / 2.0f, y2 - round / 2.0f, round, color)
        circle(x + round / 2.0f, y + round / 2.0f, round, color)
        circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color)
        drawRect(
            (x - round / 2.0f - 0.5f).toInt().toFloat(),
            (y + round / 2.0f).toInt().toFloat(), x2.toInt().toFloat(),
            (y2 - round / 2.0f).toInt().toFloat(),
            color
        )
        drawRect(
            x.toInt().toFloat(),
            (y + round / 2.0f).toInt().toFloat(),
            (x2 + round / 2.0f + 0.5f).toInt().toFloat(),
            (y2 - round / 2.0f).toInt().toFloat(),
            color
        )
        drawRect(
            (x + round / 2.0f).toInt().toFloat(),
            (y - round / 2.0f - 0.5f).toInt().toFloat(),
            (x2 - round / 2.0f).toInt().toFloat(),
            (y2 - round / 2.0f).toInt().toFloat(), color
        )
        drawRect(
            (x + round / 2.0f).toInt().toFloat(), y.toInt().toFloat(),
            (x2 - round / 2.0f).toInt().toFloat(),
            (y2 + round / 2.0f + 0.5f).toInt().toFloat(),
            color
        )
    }

    private fun enableRender2D() {
        glEnable(3042)
        glDisable(2884)
        glDisable(3553)
        glEnable(2848)
        glBlendFunc(770, 771)
        glLineWidth(1.0f)
    }

    @JvmOverloads
    fun color(color: Int, alpha: Float = (color shr 24 and 0xFF) / 255.0f) {
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        GlStateManager.color(r, g, b, alpha)
    }

    @JvmStatic
    fun drawRoundedRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        edgeRadius: Float,
        color: Int,
        borderWidth: Float,
        borderColor: Int
    ) {
        var edgeRadius = edgeRadius
        var color = color
        var borderColor = borderColor
        if (color == 16777215) {
            color = -65794
        }
        if (borderColor == 16777215) {
            borderColor = -65794
        }
        if (edgeRadius < 0.0f) {
            edgeRadius = 0.0f
        }
        if (edgeRadius > width / 2.0f) {
            edgeRadius = width / 2.0f
        }
        if (edgeRadius > height / 2.0f) {
            edgeRadius = height / 2.0f
        }
        drawRDRect(x + edgeRadius, y + edgeRadius, width - edgeRadius * 2.0f, height - edgeRadius * 2.0f, color)
        drawRDRect(x + edgeRadius, y, width - edgeRadius * 2.0f, edgeRadius, color)
        drawRDRect(x + edgeRadius, y + height - edgeRadius, width - edgeRadius * 2.0f, edgeRadius, color)
        drawRDRect(x, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0f, color)
        drawRDRect(x + width - edgeRadius, y + edgeRadius, edgeRadius, height - edgeRadius * 2.0f, color)
        enableRender2D()
        color(color)
        glBegin(6)
        var centerX = x + edgeRadius
        var centerY = y + edgeRadius
        glVertex2d(centerX.toDouble(), centerY.toDouble())
        run {
            val vertices = min(max(edgeRadius.toDouble(), 10.0), 90.0).toInt()
            var i = 0
            while (i < vertices + 1) {
                val angleRadians = 6.283185307179586 * (i + 180) / (vertices * 4)
                glVertex2d(centerX + sin(angleRadians) * edgeRadius, centerY + cos(angleRadians) * edgeRadius)
                ++i
            }
        }
        glEnd()
        glBegin(6)
        centerX = x + width - edgeRadius
        centerY = y + edgeRadius
        glVertex2d(centerX.toDouble(), centerY.toDouble())
        run {
            val vertices = min(max(edgeRadius.toDouble(), 10.0), 90.0).toInt()
            var i = 0
            while (i < vertices + 1) {
                val angleRadians = 6.283185307179586 * (i + 90) / (vertices * 4)
                glVertex2d(centerX + sin(angleRadians) * edgeRadius, centerY + cos(angleRadians) * edgeRadius)
                ++i
            }
        }
        glEnd()
        glBegin(6)
        centerX = x + edgeRadius
        centerY = y + height - edgeRadius
        glVertex2d(centerX.toDouble(), centerY.toDouble())
        run {
            val vertices = min(max(edgeRadius.toDouble(), 10.0), 90.0).toInt()
            var i = 0
            while (i < vertices + 1) {
                val angleRadians = 6.283185307179586 * (i + 270) / (vertices * 4)
                glVertex2d(centerX + sin(angleRadians) * edgeRadius, centerY + cos(angleRadians) * edgeRadius)
                ++i
            }
        }
        glEnd()
        glBegin(6)
        centerX = x + width - edgeRadius
        centerY = y + height - edgeRadius
        glVertex2d(centerX.toDouble(), centerY.toDouble())
        run {
            val vertices = min(max(edgeRadius.toDouble(), 10.0), 90.0).toInt()
            var i = 0
            while (i < vertices + 1) {
                val angleRadians = 6.283185307179586 * i / (vertices * 4)
                glVertex2d(centerX + sin(angleRadians) * edgeRadius, centerY + cos(angleRadians) * edgeRadius)
                ++i
            }
        }
        glEnd()
        color(borderColor)
        glLineWidth(borderWidth)
        glBegin(3)
        centerX = x + edgeRadius
        centerY = y + edgeRadius
        val vertices: Int
        var i: Int
        vertices = (min(max(edgeRadius.toDouble(), 10.0), 90.0).toInt()
            .also { i = it })
        while (i >= 0) {
            val angleRadians = 6.283185307179586 * (i + 180) / (vertices * 4)
            glVertex2d(centerX + sin(angleRadians) * edgeRadius, centerY + cos(angleRadians) * edgeRadius)
            --i
        }
        glVertex2d((x + edgeRadius).toDouble(), y.toDouble())
        glVertex2d((x + width - edgeRadius).toDouble(), y.toDouble())
        centerX = x + width - edgeRadius
        centerY = y + edgeRadius
        i = vertices
        while (i >= 0) {
            val angleRadians = 6.283185307179586 * (i + 90) / (vertices * 4)
            glVertex2d(centerX + sin(angleRadians) * edgeRadius, centerY + cos(angleRadians) * edgeRadius)
            --i
        }
        glVertex2d((x + width).toDouble(), (y + edgeRadius).toDouble())
        glVertex2d((x + width).toDouble(), (y + height - edgeRadius).toDouble())
        centerX = x + width - edgeRadius
        centerY = y + height - edgeRadius
        i = vertices
        while (i >= 0) {
            val angleRadians = 6.283185307179586 * i / (vertices * 4)
            glVertex2d(centerX + sin(angleRadians) * edgeRadius, centerY + cos(angleRadians) * edgeRadius)
            --i
        }
        glVertex2d((x + width - edgeRadius).toDouble(), (y + height).toDouble())
        glVertex2d((x + edgeRadius).toDouble(), (y + height).toDouble())
        centerX = x + edgeRadius
        centerY = y + height - edgeRadius
        i = vertices
        while (i >= 0) {
            val angleRadians = 6.283185307179586 * (i + 270) / (vertices * 4)
            glVertex2d(centerX + sin(angleRadians) * edgeRadius, centerY + cos(angleRadians) * edgeRadius)
            --i
        }
        glVertex2d(x.toDouble(), (y + height - edgeRadius).toDouble())
        glVertex2d(x.toDouble(), (y + edgeRadius).toDouble())
        glEnd()
        disableRender2D()
    }

    private fun disableRender2D() {
        glDisable(3042)
        glEnable(2884)
        glEnable(3553)
        glDisable(2848)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.shadeModel(7424)
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
    }

    fun darker(hexColor: Int, factor: Int): Int {
        val alpha = (hexColor shr 24 and 255).toFloat()
        val red = max(
            ((hexColor shr 16 and 255).toFloat() - (hexColor shr 16 and 255).toFloat() / (100.0f / factor.toFloat())).toDouble(),
            0.0
        ).toFloat()
        val green = max(
            ((hexColor shr 8 and 255).toFloat() - (hexColor shr 8 and 255).toFloat() / (100.0f / factor.toFloat())).toDouble(),
            0.0
        ).toFloat()
        val blue = max(
            ((hexColor and 255).toFloat() - (hexColor and 255).toFloat() / (100.0f / factor.toFloat())).toDouble(),
            0.0
        ).toFloat()
        return (((alpha.toInt() shl 24) + (red.toInt() shl 16) + (green.toInt() shl 8)).toFloat() + blue).toInt()
    }

    @JvmStatic
    fun darker(color: Int, factor: Float): Int {
        val r = ((color shr 16 and 0xFF) * factor).toInt()
        val g = ((color shr 8 and 0xFF) * factor).toInt()
        val b = ((color and 0xFF) * factor).toInt()
        val a = color shr 24 and 0xFF
        return (r and 0xFF) shl 16 or ((g and 0xFF) shl 8) or (b and 0xFF) or ((a and 0xFF) shl 24)
    }

    @JvmStatic
    fun drawCheckeredBackground(x: Float, y: Float, x2: Float, y2: Float) {
        var y = y
        drawRect(x, y, x2, y2, getColor(16777215))
        val offset = false
        while (y < y2) {
            var x3 = x + 0
            while (x3 < x2) {
                if (x3 <= x2 - 1.0f) {
                    drawRect(x3, y, x3 + 1.0f, y + 1.0f, getColor(8421504))
                }
                x3 += 2.0f
            }
            ++y
        }
    }

    @JvmStatic
    fun getColor(color: Int): Int {
        val r = color shr 16 and 0xFF
        val g = color shr 8 and 0xFF
        val b = color and 0xFF
        val a = 255
        return (r and 0xFF) shl 16 or ((g and 0xFF) shl 8) or (b and 0xFF) or ((a and 0xFF) shl 24)
    }

    private fun resettColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    fun drawGradientRound(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        bottomLeft: Color?,
        topLeft: Color?,
        bottomRight: Color?,
        topRight: Color?
    ) {
        resettColor()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)
        Shader.drawQuads(x - 1.0f, y - 1.0f, width + 2.0f, height + 2.0f)
        GlStateManager.disableBlend()
    }

    @JvmStatic
    fun customRounded(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        rTL: Float,
        rTR: Float,
        rBR: Float,
        rBL: Float,
        color: Int
    ) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        var z = 0f
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }

        val xTL = (paramXStart + rTL).toDouble()
        val yTL = (paramYStart + rTL).toDouble()

        val xTR = (paramXEnd - rTR).toDouble()
        val yTR = (paramYStart + rTR).toDouble()

        val xBR = (paramXEnd - rBR).toDouble()
        val yBR = (paramYEnd - rBR).toDouble()

        val xBL = (paramXStart + rBL).toDouble()
        val yBL = (paramYEnd - rBL).toDouble()

        glPushMatrix()
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)
        glLineWidth(1f)

        glColor4f(red, green, blue, alpha)
        glBegin(GL11.GL_POLYGON)

        val degree = Math.PI / 180
        if (rBR <= 0) glVertex2d(xBR, yBR)
        else {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(xBR + sin(i * degree) * rBR, yBR + cos(i * degree) * rBR)
                i += 1.0
            }
        }

        if (rTR <= 0) glVertex2d(xTR, yTR)
        else {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(xTR + sin(i * degree) * rTR, yTR + cos(i * degree) * rTR)
                i += 1.0
            }
        }

        if (rTL <= 0) glVertex2d(xTL, yTL)
        else {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(xTL + sin(i * degree) * rTL, yTL + cos(i * degree) * rTL)
                i += 1.0
            }
        }

        if (rBL <= 0) glVertex2d(xBL, yBL)
        else {
            var i = 270.0
            while (i <= 360) {
                glVertex2d(xBL + sin(i * degree) * rBL, yBL + cos(i * degree) * rBL)
                i += 1.0
            }
        }
        glEnd()

        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glPopMatrix()
    }

    @JvmStatic
    fun drawGradientSideways(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        val f = (col1 shr 24 and 255).toFloat() / 255.0f
        val f1 = (col1 shr 16 and 255).toFloat() / 255.0f
        val f2 = (col1 shr 8 and 255).toFloat() / 255.0f
        val f3 = (col1 and 255).toFloat() / 255.0f
        val f4 = (col2 shr 24 and 255).toFloat() / 255.0f
        val f5 = (col2 shr 16 and 255).toFloat() / 255.0f
        val f6 = (col2 shr 8 and 255).toFloat() / 255.0f
        val f7 = (col2 and 255).toFloat() / 255.0f
        glEnable(3042)
        glDisable(3553)
        glBlendFunc(770, 771)
        glEnable(2848)
        glShadeModel(7425)
        glPushMatrix()
        glBegin(7)
        glColor4f(f1, f2, f3, f)
        glVertex2d(left, top)
        glVertex2d(left, bottom)
        glColor4f(f5, f6, f7, f4)
        glVertex2d(right, bottom)
        glVertex2d(right, top)
        glEnd()
        GL11.glPopMatrix()
        glEnable(3553)
        glDisable(3042)
        glDisable(2848)
        glShadeModel(7424)
    }

    @JvmStatic
    fun drawShadow(x: Float, y: Float, width: Float, height: Float) {
        drawTexturedRect(x - 9, y - 9, 9f, 9f, "paneltopleft")
        drawTexturedRect(x - 9, y + height, 9f, 9f, "panelbottomleft")
        drawTexturedRect(x + width, y + height, 9f, 9f, "panelbottomright")
        drawTexturedRect(x + width, y - 9, 9f, 9f, "paneltopright")
        drawTexturedRect(x - 9, y, 9f, height, "panelleft")
        drawTexturedRect(x + width, y, 9f, height, "panelright")
        drawTexturedRect(x, y - 9, width, 9f, "paneltop")
        drawTexturedRect(x, y + height, width, 9f, "panelbottom")
    }

    fun drawTexturedRect(x: Float, y: Float, width: Float, height: Float, image: String) {
        glPushMatrix()
        val enableBlend = GL11.glIsEnabled(GL11.GL_BLEND)
        val disableAlpha = !GL11.glIsEnabled(GL11.GL_ALPHA_TEST)
        if (!enableBlend) glEnable(GL11.GL_BLEND)
        if (!disableAlpha) glDisable(GL11.GL_ALPHA_TEST)
        mc.textureManager.bindTexture(ResourceLocation("fdpclient/ui/shadow/$image.png"))
        GlStateManager.color(1f, 1f, 1f, 1f)
        drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width, height)
        if (!enableBlend) glDisable(GL11.GL_BLEND)
        if (!disableAlpha) glEnable(GL11.GL_ALPHA_TEST)
        GL11.glPopMatrix()
    }


    private fun drawModalRectWithCustomSizedTexture(
        x: Float,
        y: Float,
        u: Float,
        v: Float,
        width: Float,
        height: Float,
        textureWidth: Float,
        textureHeight: Float
    ) {
        val f = 1.0f / textureWidth
        val f1 = 1.0f / textureHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + width) * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + width) * f).toDouble(), (v * f1).toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    fun drawExhiEnchants(stack: ItemStack, x: Float, y: Float) {
        var y = y
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()
        val darkBorder = -0x1000000
        if (stack.item is ItemArmor) {
            val prot = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            val thorn = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack)
            if (prot > 0) {
                drawExhiOutlined(
                    prot.toString() + "",
                    drawExhiOutlined("P", x, y, darkBorder, -1),
                    y,
                    getBorderColor(prot),
                    getMainColor(prot)
                )
                y += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(
                    unb.toString() + "",
                    drawExhiOutlined("U", x, y, darkBorder, -1),
                    y,
                    getBorderColor(unb),
                    getMainColor(unb)
                )
                y += 4f
            }
            if (thorn > 0) {
                drawExhiOutlined(
                    thorn.toString() + "",
                    drawExhiOutlined("T", x, y, darkBorder, -1),
                    y,
                    getBorderColor(thorn),
                    getMainColor(thorn)
                )
                y += 4f
            }
        }
        if (stack.item is ItemBow) {
            val power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack)
            val punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack)
            val flame = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (power > 0) {
                drawExhiOutlined(
                    power.toString() + "",
                    drawExhiOutlined("Pow", x, y, darkBorder, -1),
                    y,
                    getBorderColor(power),
                    getMainColor(power)
                )
                y += 4f
            }
            if (punch > 0) {
                drawExhiOutlined(
                    punch.toString() + "",
                    drawExhiOutlined("Pun", x, y, darkBorder, -1),
                    y,
                    getBorderColor(punch),
                    getMainColor(punch)
                )
                y += 4f
            }
            if (flame > 0) {
                drawExhiOutlined(
                    flame.toString() + "",
                    drawExhiOutlined("F", x, y, darkBorder, -1),
                    y,
                    getBorderColor(flame),
                    getMainColor(flame)
                )
                y += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(
                    unb.toString() + "",
                    drawExhiOutlined("U", x, y, darkBorder, -1),
                    y,
                    getBorderColor(unb),
                    getMainColor(unb)
                )
                y += 4f
            }
        }
        if (stack.item is ItemSword) {
            val sharp = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack)
            val kb = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack)
            val fire = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (sharp > 0) {
                drawExhiOutlined(
                    sharp.toString() + "",
                    drawExhiOutlined("S", x, y, darkBorder, -1),
                    y,
                    getBorderColor(sharp),
                    getMainColor(sharp)
                )
                y += 4f
            }
            if (kb > 0) {
                drawExhiOutlined(
                    kb.toString() + "",
                    drawExhiOutlined("K", x, y, darkBorder, -1),
                    y,
                    getBorderColor(kb),
                    getMainColor(kb)
                )
                y += 4f
            }
            if (fire > 0) {
                drawExhiOutlined(
                    fire.toString() + "",
                    drawExhiOutlined("F", x, y, darkBorder, -1),
                    y,
                    getBorderColor(fire),
                    getMainColor(fire)
                )
                y += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(
                    unb.toString() + "",
                    drawExhiOutlined("U", x, y, darkBorder, -1),
                    y,
                    getBorderColor(unb),
                    getMainColor(unb)
                )
            }
        }
        GlStateManager.enableDepth()
        RenderHelper.enableGUIStandardItemLighting()
    }

    private fun drawExhiOutlined(text: String, x: Float, y: Float, borderColor: Int, mainColor: Int): Float {
        Fonts.fontTahomaSmall.drawString(text, x, y - 0.35.toFloat(), borderColor)
        Fonts.fontTahomaSmall.drawString(text, x, y + 0.35.toFloat(), borderColor)
        Fonts.fontTahomaSmall.drawString(text, x - 0.35.toFloat(), y, borderColor)
        Fonts.fontTahomaSmall.drawString(text, x + 0.35.toFloat(), y, borderColor)
        Fonts.fontTahomaSmall.drawString(text, x, y, mainColor)
        return x + Fonts.fontTahomaSmall.getWidth(text) - 2f
    }

    fun drawFilledCircle(xx: Int, yy: Int, radius: Float, col: Int) {
        val f = (col shr 24 and 255).toFloat() / 255.0f
        val f1 = (col shr 16 and 255).toFloat() / 255.0f
        val f2 = (col shr 8 and 255).toFloat() / 255.0f
        val f3 = (col and 255).toFloat() / 255.0f
        val sections = 50
        val dAngle = 6.283185307179586 / sections
        glPushMatrix()
        glEnable(3042)
        glDisable(3553)
        glEnable(2848)
        glBlendFunc(770, 771)
        glBegin(6)
        var i = 0
        while (i < sections) {
            val x = (radius * sin(i * dAngle)).toFloat()
            val y = (radius * cos(i * dAngle)).toFloat()
            glColor4f(f1, f2, f3, f)
            GL11.glVertex2f(xx.toFloat() + x, yy.toFloat() + y)
            ++i
        }
        GlStateManager.color(0.0f, 0.0f, 0.0f)
        glEnd()
        glEnable(3553)
        glDisable(3042)
        glDisable(2848)
        GL11.glPopMatrix()
    }

    private fun getMainColor(level: Int): Int {
        if (level == 4) return -0x560000
        return -1
    }

    fun drawSquareTriangle(cx: Float, cy: Float, dirX: Float, dirY: Float, color: Color, filled: Boolean) {
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.resetColor()
        glColor(color)
        worldrenderer.begin(if (filled) 5 else 2, DefaultVertexFormats.POSITION)
        worldrenderer.pos((cx + dirX).toDouble(), cy.toDouble(), 0.0).endVertex()
        worldrenderer.pos(cx.toDouble(), cy.toDouble(), 0.0).endVertex()
        worldrenderer.pos(cx.toDouble(), (cy + dirY).toDouble(), 0.0).endVertex()
        worldrenderer.pos((cx + dirX).toDouble(), cy.toDouble(), 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    private fun getBorderColor(level: Int): Int {
        if (level == 2) return 0x7055FF55
        if (level == 3) return 0x7000AAAA
        if (level == 4) return 0x70AA0000
        if (level >= 5) return 0x70FFAA00
        return 0x70FFFFFF
    }

    fun drawRoundedCornerRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float) {
        glBegin(GL11.GL_POLYGON)

        val xRadius = min((x1 - x) * 0.5, radius.toDouble()).toFloat()
        val yRadius = min((y1 - y) * 0.5, radius.toDouble()).toFloat()
        quickPolygonCircle(x + xRadius, y + yRadius, xRadius, yRadius, 180, 270)
        quickPolygonCircle(x1 - xRadius, y + yRadius, xRadius, yRadius, 90, 180)
        quickPolygonCircle(x1 - xRadius, y1 - yRadius, xRadius, yRadius, 0, 90)
        quickPolygonCircle(x + xRadius, y1 - yRadius, xRadius, yRadius, 270, 360)

        glEnd()
    }

    @JvmStatic
    fun drawRoundedRect2(x: Float, y: Float, x2: Float, y2: Float, round: Float, color: Int) {
        var x = x
        var y = y
        var x2 = x2
        var y2 = y2
        x += (round / 2.0f + 0.5).toFloat()
        y += (round / 2.0f + 0.5).toFloat()
        x2 -= (round / 2.0f + 0.5).toFloat()
        y2 -= (round / 2.0f + 0.5).toFloat()
        drawRect(x.toInt().toFloat(), y.toInt().toFloat(), x2.toInt().toFloat(), y2.toInt().toFloat(), color)
        circle(x2 - round / 2.0f, y + round / 2.0f, round, color)
        circle(x + round / 2.0f, y2 - round / 2.0f, round, color)
        circle(x + round / 2.0f, y + round / 2.0f, round, color)
        circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color)
        drawRect(
            (x - round / 2.0f - 0.5f).toInt().toFloat(),
            (y + round / 2.0f).toInt().toFloat(), x2.toInt().toFloat(),
            (y2 - round / 2.0f).toInt().toFloat(),
            color
        )
        drawRect(
            x.toInt().toFloat(),
            (y + round / 2.0f).toInt().toFloat(),
            (x2 + round / 2.0f + 0.5f).toInt().toFloat(),
            (y2 - round / 2.0f).toInt().toFloat(),
            color
        )
        drawRect(
            (x + round / 2.0f).toInt().toFloat(),
            (y - round / 2.0f - 0.5f).toInt().toFloat(),
            (x2 - round / 2.0f).toInt().toFloat(),
            (y2 - round / 2.0f).toInt().toFloat(), color
        )
        drawRect(
            (x + round / 2.0f).toInt().toFloat(), y.toInt().toFloat(),
            (x2 - round / 2.0f).toInt().toFloat(),
            (y2 + round / 2.0f + 0.5f).toInt().toFloat(),
            color
        )
    }

    @JvmStatic
    fun circle(x: Float, y: Float, radius: Float, fill: Int) {
        arc(x, y, 0.0f, 360.0f, radius, fill)
    } // 1

    @JvmStatic
    fun circle(x: Float, y: Float, radius: Float, fill: Color) {
        arc(x, y, 0.0f, 360.0f, radius, fill)
    } // 2

    private fun arc(
        x: Float, y: Float, start: Float, end: Float, radius: Float,
        color: Int
    ) {
        arcEllipse(x, y, start, end, radius, radius, color)
    } // 1

    private fun arc(
        x: Float, y: Float, start: Float, end: Float, radius: Float,
        color: Color
    ) {
        arcEllipse(x, y, start, end, radius, radius, color)
    } // 2


    private fun arcEllipse(
        x: Float, y: Float, start: Float, end: Float, w: Float, h: Float,
        color: Int
    ) { // 1
        var start = start
        var end = end
        GlStateManager.color(0.0f, 0.0f, 0.0f)
        glColor4f(0.0f, 0.0f, 0.0f, 0.0f)
        val temp: Float
        if (start > end) {
            temp = end
            end = start
            start = temp
        }
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        val tessellator = Tessellator.getInstance()
        tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(red, green, blue, alpha)
        if (alpha > 0.5f) {
            glEnable(GL11.GL_POLYGON_SMOOTH)
            glEnable(2848)
            glLineWidth(2.0f)
            glBegin(3)
            var i = end
            while (i >= start) {
                val ldx = cos(i * Math.PI / 180.0).toFloat() * w * 1.001f
                val ldy = sin(i * Math.PI / 180.0).toFloat() * h * 1.001f
                GL11.glVertex2f(x + ldx, y + ldy)
                i -= 4.0f
            }
            glEnd()
            glDisable(2848)
            glDisable(GL11.GL_POLYGON_SMOOTH)
        }
        glBegin(6)
        var i = end
        while (i >= start) {
            val ldx = cos(i * Math.PI / 180.0).toFloat() * w
            val ldy = sin(i * Math.PI / 180.0).toFloat() * h
            GL11.glVertex2f(x + ldx, y + ldy)
            i -= 4.0f
        }
        glEnd()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    private fun arcEllipse(
        x: Float, y: Float, start: Float, end: Float, w: Float, h: Float,
        color: Color
    ) { //2
        var start = start
        var end = end
        GlStateManager.color(0.0f, 0.0f, 0.0f)
        glColor4f(0.0f, 0.0f, 0.0f, 0.0f)
        val temp: Float
        if (start > end) {
            temp = end
            end = start
            start = temp
        }
        val var9 = Tessellator.getInstance()
        var9.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(
            color.red / 255.0f, color.green / 255.0f, color.blue / 255.0f,
            color.alpha / 255.0f
        )
        if (color.alpha > 0.5f) {
            glEnable(2848)
            glLineWidth(2.0f)
            glBegin(3)
            var i = end
            while (i >= start) {
                val ldx = cos(i * Math.PI / 180.0).toFloat() * w * 1.001f
                val ldy = sin(i * Math.PI / 180.0).toFloat() * h * 1.001f
                GL11.glVertex2f(x + ldx, y + ldy)
                i -= 4.0f
            }
            glEnd()
            glDisable(2848)
        }
        glBegin(6)
        var i = end
        while (i >= start) {
            val ldx = cos(i * Math.PI / 180.0).toFloat() * w
            val ldy = sin(i * Math.PI / 180.0).toFloat() * h
            GL11.glVertex2f(x + ldx, y + ldy)
            i -= 4.0f
        }
        glEnd()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun fastRoundedRect(paramXStart: Float, paramYStart: Float, paramXEnd: Float, paramYEnd: Float, radius: Float) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        var z: Float
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }

        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()

        glEnable(GL11.GL_LINE_SMOOTH)
        glLineWidth(1f)

        glBegin(GL11.GL_POLYGON)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }
        glEnd()
        glDisable(GL11.GL_LINE_SMOOTH)
    }

    fun interpolate(current: Double, old: Double, scale: Double): Double {
        return old + (current - old) * scale
    }

    fun isInViewFrustrum(entity: Entity): Boolean {
        return isInViewFrustrum(entity.entityBoundingBox) || entity.ignoreFrustumCheck
    }

    private val frustrum = Frustum()
    private fun isInViewFrustrum(bb: AxisAlignedBB): Boolean {
        val current = mc.renderViewEntity
        frustrum.setPosition(current.posX, current.posY, current.posZ)
        return frustrum.isBoundingBoxInFrustum(bb)
    }

    fun drawFilledCircleNoGL(x: Int, y: Int, r: Double, c: Int, quality: Int) {
        val f = ((c shr 24) and 0xff) / 255f
        val f1 = ((c shr 16) and 0xff) / 255f
        val f2 = ((c shr 8) and 0xff) / 255f
        val f3 = (c and 0xff) / 255f

        glColor4f(f1, f2, f3, f)
        glBegin(GL11.GL_TRIANGLE_FAN)

        for (i in 0..360 / quality) {
            val x2 = sin(((i * quality * Math.PI) / 180)) * r
            val y2 = cos(((i * quality * Math.PI) / 180)) * r
            glVertex2d(x + x2, y + y2)
        }

        glEnd()
    }

    @JvmStatic
    fun createFrameBuffer(framebuffer: Framebuffer?): Framebuffer {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            framebuffer?.deleteFramebuffer()
            return Framebuffer(mc.displayWidth, mc.displayHeight, true)
        }
        return framebuffer
    }

    @JvmStatic
    fun bindTexture(texture: Int) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture)
    }

    fun drawFilledCircle(x: Double, y: Double, r: Double, c: Int, id: Int) {
        val f = (c shr 24 and 0xff).toFloat() / 255f
        val f1 = (c shr 16 and 0xff).toFloat() / 255f
        val f2 = (c shr 8 and 0xff).toFloat() / 255f
        val f3 = (c and 0xff).toFloat() / 255f
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glColor4f(f1, f2, f3, f)
        glBegin(GL11.GL_POLYGON)
        when (id) {
            1 -> {
                glVertex2d(x, y)
                for (i in 0..90) {
                    val x2 = sin((i * 3.141526 / 180)) * r
                    val y2 = cos((i * 3.141526 / 180)) * r
                    glVertex2d(x - x2, y - y2)
                }
            }

            2 -> {
                glVertex2d(x, y)
                for (i in 90..180) {
                    val x2 = sin((i * 3.141526 / 180)) * r
                    val y2 = cos((i * 3.141526 / 180)) * r
                    glVertex2d(x - x2, y - y2)
                }
            }

            3 -> {
                glVertex2d(x, y)
                for (i in 270..360) {
                    val x2 = sin((i * 3.141526 / 180)) * r
                    val y2 = cos((i * 3.141526 / 180)) * r
                    glVertex2d(x - x2, y - y2)
                }
            }

            4 -> {
                glVertex2d(x, y)
                for (i in 180..270) {
                    val x2 = sin((i * 3.141526 / 180)) * r
                    val y2 = cos((i * 3.141526 / 180)) * r
                    glVertex2d(x - x2, y - y2)
                }
            }

            else -> {
                for (i in 0..360) {
                    val x2 = sin((i * 3.141526 / 180)) * r
                    val y2 = cos((i * 3.141526 / 180)) * r
                    GL11.glVertex2f((x - x2).toFloat(), (y - y2).toFloat())
                }
            }
        }
        glEnd()
        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
    }

    fun getGradientOffset(color1: Color, color2: Color, offset: Double): Color {
        var offset = offset
        var inverse_percent: Double
        var redPart: Int
        if (offset > 1.0) {
            inverse_percent = offset % 1.0
            redPart = offset.toInt()
            offset = if (redPart % 2 == 0) inverse_percent else 1.0 - inverse_percent
        }

        inverse_percent = 1.0 - offset
        redPart = (color1.red.toDouble() * inverse_percent + color2.red.toDouble() * offset).toInt()
        val greenPart = (color1.green.toDouble() * inverse_percent + color2.green.toDouble() * offset).toInt()
        val bluePart = (color1.blue.toDouble() * inverse_percent + color2.blue.toDouble() * offset).toInt()
        return Color(redPart, greenPart, bluePart)
    }

    @JvmStatic
    fun drawGradientSidewaysH(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)

        quickDrawGradientSidewaysH(left, top, right, bottom, col1, col2)

        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL11.GL_FLAT)
    }

    fun quickDrawGradientSidewaysH(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        glBegin(GL11.GL_QUADS)

        glColor(col1)
        glVertex2d(left, top)
        glVertex2d(left, bottom)
        glColor(col2)
        glVertex2d(right, bottom)
        glVertex2d(right, top)

        glEnd()
    }

    fun drawGradientSidewaysV(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)

        quickDrawGradientSidewaysV(left, top, right, bottom, col1, col2)

        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL11.GL_FLAT)
    }

    fun quickDrawGradientSidewaysV(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        glBegin(GL11.GL_QUADS)

        glColor(col1)
        glVertex2d(right, top)
        glVertex2d(left, top)
        glColor(col2)
        glVertex2d(left, bottom) // TODO: Fix this, this may have been a mistake
        glVertex2d(right, bottom)

        glEnd()
    }

    fun drawBlockBox(blockPos: BlockPos, color: Color, outline: Boolean, box: Boolean, outlineWidth: Float) {
        val renderManager = mc.renderManager
        val timer = mc.timer

        val x = blockPos.x - renderManager.renderPosX
        val y = blockPos.y - renderManager.renderPosY
        val z = blockPos.z - renderManager.renderPosZ

        var axisAlignedBB = AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0)
        val block = getBlock(blockPos)

        if (block != null) {
            val player: EntityPlayer = mc.thePlayer

            val posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * timer.renderPartialTicks.toDouble()
            val posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * timer.renderPartialTicks.toDouble()
            val posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * timer.renderPartialTicks.toDouble()
            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
                .offset(-posX, -posY, -posZ)
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL11.GL_BLEND)
        disableGlCap(GL11.GL_TEXTURE_2D, GL11.GL_DEPTH_TEST)
        glDepthMask(false)

        if (box) {
            glColor(
                color.red,
                color.green,
                color.blue,
                if (color.alpha != 255) color.alpha else if (outline) 26 else 35
            )
            drawFilledBox(axisAlignedBB)
        }

        if (outline) {
            glLineWidth(outlineWidth)
            enableGlCap(GL11.GL_LINE_SMOOTH)
            glColor(color)

            drawSelectionBoundingBox(axisAlignedBB)
        }

        GlStateManager.resetColor()
        glDepthMask(true)
        resetCaps()
    }

    fun drawSelectionBoundingBox(boundingBox: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer

        worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)

        // Lower Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()

        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()

        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()

        tessellator.draw()
    }

    fun drawEntityBox(entity: Entity, color: Color, outline: Boolean) {
        val renderManager = mc.renderManager
        val timer = mc.timer

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL11.GL_BLEND)
        disableGlCap(GL11.GL_TEXTURE_2D, GL11.GL_DEPTH_TEST)
        glDepthMask(false)

        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ)

        val entityBox = entity.entityBoundingBox
        val axisAlignedBB = AxisAlignedBB(
            entityBox.minX - entity.posX + x - 0.05,
            entityBox.minY - entity.posY + y,
            entityBox.minZ - entity.posZ + z - 0.05,
            entityBox.maxX - entity.posX + x + 0.05,
            entityBox.maxY - entity.posY + y + 0.15,
            entityBox.maxZ - entity.posZ + z + 0.05
        )

        if (outline) {
            glLineWidth(1f)
            enableGlCap(GL11.GL_LINE_SMOOTH)
            glColor(color.red, color.green, color.blue, 95)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)
        GlStateManager.resetColor()
        glDepthMask(true)
        resetCaps()
    }

    fun drawEntityBox(entity: Entity, color: Color, outline: Boolean, box: Boolean, outlineWidth: Float) {
        val renderManager = mc.renderManager
        val timer = mc.timer

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL11.GL_BLEND)
        disableGlCap(GL11.GL_TEXTURE_2D, GL11.GL_DEPTH_TEST)
        glDepthMask(false)

        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ)

        val entityBox = entity.entityBoundingBox
        val axisAlignedBB = AxisAlignedBB(
            entityBox.minX - entity.posX + x - 0.05,
            entityBox.minY - entity.posY + y,
            entityBox.minZ - entity.posZ + z - 0.05,
            entityBox.maxX - entity.posX + x + 0.05,
            entityBox.maxY - entity.posY + y + 0.15,
            entityBox.maxZ - entity.posZ + z + 0.05
        )

        if (outline) {
            glLineWidth(outlineWidth)
            enableGlCap(GL11.GL_LINE_SMOOTH)
            glColor(color.red, color.green, color.blue, if (box) 170 else 255)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        if (box) {
            glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
            drawFilledBox(axisAlignedBB)
        }

        GlStateManager.resetColor()
        glDepthMask(true)
        resetCaps()
    }

    fun drawAxisAlignedBB(
        axisAlignedBB: AxisAlignedBB,
        color: Color,
        outline: Boolean,
        box: Boolean,
        outlineWidth: Float
    ) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_BLEND)
        glLineWidth(outlineWidth)
        glDisable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color)

        if (outline) {
            glLineWidth(outlineWidth)
            enableGlCap(GL11.GL_LINE_SMOOTH)
            glColor(color.red, color.green, color.blue, 95)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        if (box) {
            glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
            drawFilledBox(axisAlignedBB)
        }

        GlStateManager.resetColor()
        glEnable(GL11.GL_TEXTURE_2D)
        glEnable(GL11.GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL11.GL_BLEND)
    }

    fun drawPlatform(y: Double, color: Color, size: Double) {
        val renderManager = mc.renderManager
        val renderY = y - renderManager.renderPosY

        drawAxisAlignedBB(
            AxisAlignedBB(size, renderY + 0.02, size, -size, renderY, -size), color,
            outline = false,
            box = true,
            outlineWidth = 2f
        )
    }

    fun drawPlatform(entity: Entity, color: Color) {
        val renderManager = mc.renderManager
        val timer = mc.timer

        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ)

        val axisAlignedBB = entity.entityBoundingBox
            .offset(-entity.posX, -entity.posY, -entity.posZ)
            .offset(x, y, z)

        drawAxisAlignedBB(
            AxisAlignedBB(
                axisAlignedBB.minX,
                axisAlignedBB.maxY + 0.2,
                axisAlignedBB.minZ,
                axisAlignedBB.maxX,
                axisAlignedBB.maxY + 0.26,
                axisAlignedBB.maxZ
            ),
            color
        )
    }

    fun drawFilledBox(axisAlignedBB: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()

        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        tessellator.draw()
    }

    @JvmStatic
    fun drawBlockBox(blockPos: BlockPos, color: Color, outline: Boolean) {
        val renderManager = mc.renderManager
        val timer = mc.timer

        val x = blockPos.x - renderManager.renderPosX
        val y = blockPos.y - renderManager.renderPosY
        val z = blockPos.z - renderManager.renderPosZ

        var axisAlignedBB = AxisAlignedBB(x, y, z, x + 1.0, y + 1, z + 1.0)
        val block = getBlock(blockPos)

        if (block != null) {
            val player: EntityPlayer = mc.thePlayer

            val posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * timer.renderPartialTicks.toDouble()
            val posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * timer.renderPartialTicks.toDouble()
            val posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * timer.renderPartialTicks.toDouble()
            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
                .offset(-posX, -posY, -posZ)
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL11.GL_BLEND)
        disableGlCap(GL11.GL_TEXTURE_2D, GL11.GL_DEPTH_TEST)
        glDepthMask(false)

        glColor(color.red, color.green, color.blue, if (color.alpha != 255) color.alpha else if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)

        if (outline) {
            glLineWidth(1f)
            enableGlCap(GL11.GL_LINE_SMOOTH)
            glColor(color)

            drawSelectionBoundingBox(axisAlignedBB)
        }

        GlStateManager.resetColor()
        glDepthMask(true)
        resetCaps()
    }


    // Astolfo
    fun Astolfo(var2: Int, st: Float, bright: Float): Int {
        var currentColor = ceil((System.currentTimeMillis() + (var2 * 130L)).toDouble()) / 6
        return Color.getHSBColor(
            if (((360.0.also { currentColor %= it }) / 360.0).toFloat()
                    .toDouble() < 0.5
            ) -((currentColor / 360.0).toFloat()) else (currentColor / 360.0).toFloat(), st, bright
        ).rgb
    }

    fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float) {
        glBegin(GL11.GL_QUADS)

        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())

        glEnd()
    }

    fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        glColor(color)
        quickDrawRect(x, y, x2, y2)
    }

    @JvmStatic
    fun drawRect(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        if (left < right) {
            val i = left
            left = right
            right = i
        }

        if (top < bottom) {
            val j = top
            top = bottom
            bottom = j
        }

        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    @JvmStatic
    fun drawRect(x: Double, y: Double, x2: Double, y2: Double, color: Int) {
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)

        glColor(color)
        glBegin(GL11.GL_QUADS)

        glVertex2d(x2, y)
        glVertex2d(x, y)
        glVertex2d(x, y2)
        glVertex2d(x2, y2)
        glEnd()

        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
    }

    @JvmStatic
    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Color) {
        drawRect(x, y, x2, y2, color.rgb)
    }

    @JvmStatic
    fun drawBorderedRect(x: Double, y: Double, x2: Double, y2: Double, width: Double, color1: Int, color2: Int) {
        drawBorderedRect(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), width.toFloat(), color1, color2)
    }

    @JvmStatic
    fun drawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int) {
        drawRect(x, y, x2, y2, color2)
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)

        glColor(color1)
        glLineWidth(width)
        glBegin(1)
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()

        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
    }

    @JvmStatic
    fun newDrawRect(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        if (left < right) {
            val i = left
            left = right
            right = i
        }

        if (top < bottom) {
            val j = top
            top = bottom
            bottom = j
        }

        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun newDrawRect(left: Double, top: Double, right: Double, bottom: Double, color: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        if (left < right) {
            val i = left
            left = right
            right = i
        }

        if (top < bottom) {
            val j = top
            top = bottom
            bottom = j
        }

        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left, bottom, 0.0).endVertex()
        worldrenderer.pos(right, bottom, 0.0).endVertex()
        worldrenderer.pos(right, top, 0.0).endVertex()
        worldrenderer.pos(left, top, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    @JvmOverloads
    fun drawRoundedRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int,
        popPush: Boolean = true
    ) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        var z: Float
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }

        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()

        if (popPush) glPushMatrix()
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)
        glLineWidth(1f)

        glColor4f(red, green, blue, alpha)
        glBegin(GL11.GL_POLYGON)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }
        glEnd()

        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
        if (popPush) GL11.glPopMatrix()
    }

    @JvmStatic
    fun drawLoadingCircle(x: Float, y: Float) {
        for (i in 0..3) {
            val rot = ((System.nanoTime() / 5000000 * i) % 360).toInt()
            drawCircle(x, y, (i * 10).toFloat(), rot - 180, rot)
        }
    }

    fun drawCircle(x: Float, y: Float, radius: Float, start: Int, end: Int) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor(Color.WHITE)

        glEnable(GL11.GL_LINE_SMOOTH)
        glLineWidth(2f)
        glBegin(GL11.GL_LINE_STRIP)
        var i = end.toFloat()
        while (i >= start) {
            GL11.glVertex2f(
                (x + (cos(i * Math.PI / 180) * (radius * 1.001f))).toFloat(),
                (y + (sin(i * Math.PI / 180) * (radius * 1.001f))).toFloat()
            )
            i -= (360 / 90.0f)
        }
        glEnd()
        glDisable(GL11.GL_LINE_SMOOTH)

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawLimitedCircle(lx: Float, ly: Float, x2: Float, y2: Float, xx: Int, yy: Int, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)

        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)
        glBegin(GL11.GL_TRIANGLE_FAN)

        glColor(color)
        for (i in 0 until sections) {
            x = (radius * sin((i * dAngle))).toFloat()
            y = (radius * cos((i * dAngle))).toFloat()
            GL11.glVertex2f(
                min(x2.toDouble(), max((xx + x).toDouble(), lx.toDouble())).toFloat(),
                min(y2.toDouble(), max((yy + y).toDouble(), ly.toDouble()))
                    .toFloat()
            )
        }

        GlStateManager.color(0f, 0f, 0f)

        glEnd()

        GL11.glPopAttrib()
    }

    @JvmStatic
    fun drawImage(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int, ways: Boolean = false) {
        glDisable(GL11.GL_DEPTH_TEST)
        glEnable(GL11.GL_BLEND)
        glDepthMask(false)
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        if (!ways) {
            mc.textureManager.bindTexture(image)
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        } else {
            GL11.glTranslatef(x.toFloat(), y.toFloat(), x.toFloat())
            mc.textureManager.bindTexture(image)
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, width, height, width.toFloat(), height.toFloat())
            GL11.glTranslatef((-x).toFloat(), (-y).toFloat(), (-x).toFloat())
        }
        glDepthMask(true)
        glDisable(GL11.GL_BLEND)
        glEnable(GL11.GL_DEPTH_TEST)
    }

    fun drawAxisAlignedBB(axisAlignedBB: AxisAlignedBB, color: Color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_BLEND)
        glLineWidth(2f)
        glDisable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color)
        drawFilledBox(axisAlignedBB)
        GlStateManager.resetColor()
        glEnable(GL11.GL_TEXTURE_2D)
        glEnable(GL11.GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL11.GL_BLEND)
    }

    fun drawRectBasedBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int) {
        drawRect(x - width / 2f, y - width / 2f, x2 + width / 2f, y + width / 2f, color1)
        drawRect(x - width / 2f, y + width / 2f, x + width / 2f, y2 + width / 2f, color1)
        drawRect(x2 - width / 2f, y + width / 2f, x2 + width / 2f, y2 + width / 2f, color1)
        drawRect(x + width / 2f, y2 - width / 2f, x2 - width / 2f, y2 + width / 2f, color1)
    }

    fun regFakePlayer(mp: EntityOtherPlayerMP?) {
        if (mp == null) return
        val entityId = mp.entityId
        fakePlayers[entityId] = mp
    }

    fun removeFakePlayer(mp: EntityOtherPlayerMP?) {
        if (mp == null) return
        removeFakePlayer(mp.entityId)
    }

    fun getFakePlayer(id: Int): EntityOtherPlayerMP? {
        return fakePlayers[id]
    }

    fun removeFakePlayer(id: Int) {
        fakePlayers.remove(id)
    }

    fun getFakePlayers(): Collection<EntityOtherPlayerMP> {
        return fakePlayers.values
    }

    fun glColor(red: Int, green: Int, blue: Int, alpha: Int) {
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
    }

    fun glColor(color: Color) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f
        val alpha = color.alpha / 255f

        GlStateManager.color(red, green, blue, alpha)
    }

    fun glColor(color: Color, alpha: Int) {
        glColor(color, alpha / 255f)
    }

    @JvmStatic
    fun glColor(color: Color, alpha: Float) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f

        GlStateManager.color(red, green, blue, alpha)
    }

    @JvmStatic
    fun glColor(hex: Int) {
        val alpha = (hex shr 24 and 0xFF) / 255f
        val red = (hex shr 16 and 0xFF) / 255f
        val green = (hex shr 8 and 0xFF) / 255f
        val blue = (hex and 0xFF) / 255f

        GlStateManager.color(red, green, blue, alpha)
    }

    fun glColor(hex: Int, alpha: Int) {
        val red = (hex shr 16 and 0xFF) / 255f
        val green = (hex shr 8 and 0xFF) / 255f
        val blue = (hex and 0xFF) / 255f

        GlStateManager.color(red, green, blue, alpha / 255f)
    }

    fun glColor(hex: Int, alpha: Float) {
        val red = (hex shr 16 and 0xFF) / 255f
        val green = (hex shr 8 and 0xFF) / 255f
        val blue = (hex and 0xFF) / 255f

        GlStateManager.color(red, green, blue, alpha)
    }

    fun drawTriAngle(cx: Float, cy: Float, r: Float, n: Float, color: Color, polygon: Boolean) {
        var cx = cx
        var cy = cy
        var r = r
        cx *= 2.0f
        cy *= 2.0f
        val b = 6.2831852 / n
        val p = cos(b)
        val s = sin(b)
        r *= 2.0f
        var x = r.toDouble()
        var y = 0.0

        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        glLineWidth(1f)
        enableGlCap(GL11.GL_LINE_SMOOTH)
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.resetColor()
        glColor(color)
        GlStateManager.scale(0.5f, 0.5f, 0.5f)
        worldrenderer.begin(if (polygon) GL11.GL_POLYGON else 2, DefaultVertexFormats.POSITION)
        var ii = 0
        while (ii < n) {
            worldrenderer.pos(x + cx, y + cy, 0.0).endVertex()
            val t = x
            x = p * x - s * y
            y = s * t + p * y
            ii++
        }
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.scale(2f, 2f, 2f)
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, lineWidth: Float, start: Int, end: Int, color: Color) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor(color)

        glEnable(GL11.GL_LINE_SMOOTH)
        glLineWidth(lineWidth)
        glBegin(GL11.GL_LINE_STRIP)
        var i = end.toFloat()
        while (i >= start) {
            GL11.glVertex2f(
                (x + (cos(i * Math.PI / 180) * (radius * 1.001f))).toFloat(),
                (y + (sin(i * Math.PI / 180) * (radius * 1.001f))).toFloat()
            )
            i -= (360 / 90.0f)
        }
        glEnd()
        glDisable(GL11.GL_LINE_SMOOTH)

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    @JvmStatic
    fun drawArrow(x: Double, y: Double, lineWidth: Int, color: Int, length: Double) {
        start2D()
        glPushMatrix()
        glLineWidth(lineWidth.toFloat())
        setColor(Color(color))
        glBegin(GL11.GL_LINE_STRIP)
        glVertex2d(x, y)
        glVertex2d(x + 3, y + length)
        glVertex2d(x + 3 * 2, y)
        glEnd()
        GL11.glPopMatrix()
        stop2D()
    }

    fun draw2D(entity: EntityLivingBase, posX: Double, posY: Double, posZ: Double, color: Int, backgroundColor: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(posX, posY, posZ)
        GlStateManager.rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        GlStateManager.scale(-0.1, -0.1, 0.1)

        glDisable(GL11.GL_DEPTH_TEST)
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        GlStateManager.depthMask(true)

        glColor(color)

        GL11.glCallList(DISPLAY_LISTS_2D[0])

        glColor(backgroundColor)

        GL11.glCallList(DISPLAY_LISTS_2D[1])

        GlStateManager.translate(0.0, 21 + -(entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) * 12, 0.0)

        glColor(color)
        GL11.glCallList(DISPLAY_LISTS_2D[2])

        glColor(backgroundColor)
        GL11.glCallList(DISPLAY_LISTS_2D[3])

        // Stop render
        glEnable(GL11.GL_DEPTH_TEST)
        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)

        GlStateManager.popMatrix()
    }

    fun draw2D(blockPos: BlockPos, color: Int, backgroundColor: Int) {
        val renderManager = mc.renderManager

        val posX = (blockPos.x + 0.5) - renderManager.renderPosX
        val posY = blockPos.y - renderManager.renderPosY
        val posZ = (blockPos.z + 0.5) - renderManager.renderPosZ

        GlStateManager.pushMatrix()
        GlStateManager.translate(posX, posY, posZ)
        GlStateManager.rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        GlStateManager.scale(-0.1, -0.1, 0.1)

        glDisable(GL11.GL_DEPTH_TEST)
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        GlStateManager.depthMask(true)

        glColor(color)

        GL11.glCallList(DISPLAY_LISTS_2D[0])

        glColor(backgroundColor)

        GL11.glCallList(DISPLAY_LISTS_2D[1])

        GlStateManager.translate(0f, 9f, 0f)

        glColor(color)

        GL11.glCallList(DISPLAY_LISTS_2D[2])

        glColor(backgroundColor)

        GL11.glCallList(DISPLAY_LISTS_2D[3])

        // Stop render
        glEnable(GL11.GL_DEPTH_TEST)
        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)

        GlStateManager.popMatrix()
    }

    fun renderNameTag(string: String?, x: Double, y: Double, z: Double) {
        val renderManager = mc.renderManager

        glPushMatrix()
        GL11.glTranslated(x - renderManager.renderPosX, y - renderManager.renderPosY, z - renderManager.renderPosZ)
        GL11.glNormal3f(0f, 1f, 0f)
        GL11.glRotatef(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        GL11.glRotatef(mc.renderManager.playerViewX, 1f, 0f, 0f)
        glScalef(-0.05f, -0.05f, 0.05f)
        setGlCap(GL11.GL_LIGHTING, false)
        setGlCap(GL11.GL_DEPTH_TEST, false)
        setGlCap(GL11.GL_BLEND, true)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val width = Fonts.font35.getStringWidth(string!!) / 2

        Gui.drawRect(-width - 1, -1, width + 1, Fonts.font35.FONT_HEIGHT, Int.MIN_VALUE)
        Fonts.font35.drawString(string, -width.toFloat(), 1.5f, Color.WHITE.rgb, true)

        resetCaps()
        glColor4f(1f, 1f, 1f, 1f)
        GL11.glPopMatrix()
    }

    @JvmOverloads
    fun makeScissorBox(x: Float, y: Float, x2: Float, y2: Float, scaleOffset: Float = 1f) {
        val scaledResolution = StaticStorage.scaledResolution
        val factor = scaledResolution!!.scaleFactor * scaleOffset
        GL11.glScissor(
            (x * factor).toInt(),
            ((scaledResolution.scaledHeight - y2) * factor).toInt(),
            ((x2 - x) * factor).toInt(),
            ((y2 - y) * factor).toInt()
        )
    }

    fun drawUnfilledCircle(x: Double, y: Double, radius: Float, lineWidth: Float, color: Int) {
        GLUtil.setup2DRendering()
        color(color)
        glLineWidth(lineWidth)
        glEnable(GL11.GL_LINE_SMOOTH)
        glBegin(GL11.GL_POINT_BIT)

        var i = 0
        while (i <= 360) {
            glVertex2d(
                x + sin(i.toDouble() * 3.141526 / 180.0) * radius.toDouble(),
                y + cos(i.toDouble() * 3.141526 / 180.0) * radius.toDouble()
            )
            ++i
        }

        glEnd()
        glDisable(GL11.GL_LINE_SMOOTH)
        GLUtil.end2DRendering()
    }

    fun resetCaps(scale: String?) {
        if (!glCapMap.containsKey(scale)) {
            return
        }
        val map = glCapMap[scale]
        map!!.forEach { (cap: Int, state: Boolean) ->
            setGlState(
                cap, state
            )
        }
        map.clear()
    }

    fun resetCaps() {
        resetCaps("COMMON")
    }

    @JvmOverloads
    fun enableGlCap(cap: Int, scale: String = "COMMON") {
        setGlCap(cap, true, scale)
    }


    fun enableGlCap(vararg caps: Int) {
        for (cap in caps) {
            setGlCap(cap, true, "COMMON")
        }
    }

    fun disableGlCap(vararg caps: Int) {
        for (cap in caps) {
            setGlCap(cap, false, "COMMON")
        }
    }

    fun setGlCap(cap: Int, state: Boolean, scale: String) {
        if (!glCapMap.containsKey(scale)) {
            glCapMap[scale] = HashMap()
        }
        glCapMap[scale]!![cap] = GL11.glGetBoolean(cap)
        setGlState(cap, state)
    }

    fun setGlCap(cap: Int, state: Boolean) {
        setGlCap(cap, state, "COMMON")
    }

    fun setGlState(cap: Int, state: Boolean) {
        if (state) glEnable(cap)
        else glDisable(cap)
    }

    fun convertTo2D(x: Double, y: Double, z: Double): DoubleArray? {
        val screenCoords = BufferUtils.createFloatBuffer(3)
        val viewport = BufferUtils.createIntBuffer(16)
        val modelView = BufferUtils.createFloatBuffer(16)
        val projection = BufferUtils.createFloatBuffer(16)
        GL11.glGetFloat(2982, modelView)
        GL11.glGetFloat(2983, projection)
        GL11.glGetInteger(2978, viewport)
        val result = GLU.gluProject(
            x.toFloat(), y.toFloat(), z.toFloat(), modelView, projection, viewport,
            screenCoords
        )
        return if (result
        ) doubleArrayOf(
            screenCoords[0].toDouble(),
            (Display.getHeight() - screenCoords[1]).toDouble(),
            screenCoords[2].toDouble()
        )
        else null
    }

    fun rectangle(left: Double, top: Double, right: Double, bottom: Double, color: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        var var5: Double
        if (left < right) {
            var5 = left
            left = right
            right = var5
        }
        if (top < bottom) {
            var5 = top
            top = bottom
            bottom = var5
        }
        val var11 = (color shr 24 and 255).toFloat() / 255.0f
        val var6 = (color shr 16 and 255).toFloat() / 255.0f
        val var7 = (color shr 8 and 255).toFloat() / 255.0f
        val var8 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(var6, var7, var8, var11)
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(left, bottom, 0.0).endVertex()
        worldRenderer.pos(right, bottom, 0.0).endVertex()
        worldRenderer.pos(right, top, 0.0).endVertex()
        worldRenderer.pos(left, top, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    fun rectangleBordered(
        x: Double, y: Double, x1: Double, y1: Double, width: Double, internalColor: Int,
        borderColor: Int
    ) {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        rectangle(x + width, y, x1 - width, y + width, borderColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        rectangle(x, y, x + width, y1, borderColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        rectangle(x1 - width, y, x1, y1, borderColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        rectangle(x + width, y1 - width, x1 - width, y1, borderColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    fun drawOutlinedBoundingBox(aa: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(3, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(3, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(1, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
        tessellator.draw()
    }

    fun drawBoundingBox(aa: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
        tessellator.draw()
    }

    fun originalRoundedRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int
    ) {
        var paramXStart = paramXStart
        var paramYStart = paramYStart
        var paramXEnd = paramXEnd
        var paramYEnd = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        var z: Float
        if (paramXStart > paramXEnd) {
            z = paramXStart
            paramXStart = paramXEnd
            paramXEnd = z
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart
            paramYStart = paramYEnd
            paramYEnd = z
        }

        val x1 = (paramXStart + radius).toDouble()
        val y1 = (paramYStart + radius).toDouble()
        val x2 = (paramXEnd - radius).toDouble()
        val y2 = (paramYEnd - radius).toDouble()

        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer

        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(red, green, blue, alpha)
        worldrenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                worldrenderer.pos(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius, 0.0).endVertex()
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                worldrenderer.pos(x2 + sin(i * degree) * radius, y1 + cos(i * degree) * radius, 0.0).endVertex()
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                worldrenderer.pos(x1 + sin(i * degree) * radius, y1 + cos(i * degree) * radius, 0.0).endVertex()
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            worldrenderer.pos(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius, 0.0).endVertex()
            i += 1.0
        }

        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    @JvmStatic
    fun drawFilledCircle(xx: Int, yy: Int, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)

        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)
        glBegin(GL11.GL_TRIANGLE_FAN)

        for (i in 0 until sections) {
            x = (radius * sin((i * dAngle))).toFloat()
            y = (radius * cos((i * dAngle))).toFloat()

            glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            GL11.glVertex2f(xx + x, yy + y)
        }

        GlStateManager.color(0f, 0f, 0f)

        glEnd()

        GL11.glPopAttrib()
    }

    fun drawFilledCircle(xx: Float, yy: Float, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)

        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)
        glBegin(GL11.GL_TRIANGLE_FAN)

        for (i in 0 until sections) {
            x = (radius * sin((i * dAngle))).toFloat()
            y = (radius * cos((i * dAngle))).toFloat()

            glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            GL11.glVertex2f(xx + x, yy + y)
        }

        GlStateManager.color(0f, 0f, 0f)

        glEnd()

        GL11.glPopAttrib()
    }

    fun drawLine(x: Float, y: Float, x1: Float, y1: Float, width: Float) {
        glDisable(GL11.GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL11.GL_LINES)
        GL11.glVertex2f(x, y)
        GL11.glVertex2f(x1, y1)
        glEnd()
        glEnable(GL11.GL_TEXTURE_2D)
    }

    fun drawLine(x: Double, y: Double, x1: Double, y1: Double, width: Float) {
        glDisable(GL11.GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL11.GL_LINES)
        glVertex2d(x, y)
        glVertex2d(x1, y1)
        glEnd()
        glEnable(GL11.GL_TEXTURE_2D)
    }

    fun startDrawing() {
        glEnable(3042)
        glEnable(3042)
        glBlendFunc(770, 771)
        glEnable(2848)
        glDisable(3553)
        glDisable(2929)
        Minecraft.getMinecraft().entityRenderer.setupCameraTransform(
            Minecraft.getMinecraft().timer.renderPartialTicks,
            0
        )
    }

    fun stopDrawing() {
        glDisable(3042)
        glEnable(3553)
        glDisable(2848)
        glDisable(3042)
        glEnable(2929)
    }

    fun drawExhiRect(x: Float, y: Float, x2: Float, y2: Float, alpha: Float) {
        drawRect(x - 3.5f, y - 3.5f, x2 + 3.5f, y2 + 3.5f, Color(0f, 0f, 0f, alpha).rgb)
        drawRect(x - 3f, y - 3f, x2 + 3f, y2 + 3f, Color(50f / 255f, 50f / 255f, 50f / 255f, alpha).rgb)
        drawRect(x - 2.5f, y - 2.5f, x2 + 2.5f, y2 + 2.5f, Color(26f / 255f, 26f / 255f, 26f / 255f, alpha).rgb)
        drawRect(x - 0.5f, y - 0.5f, x2 + 0.5f, y2 + 0.5f, Color(50f / 255f, 50f / 255f, 50f / 255f, alpha).rgb)
        drawRect(x, y, x2, y2, Color(18f / 255f, 18 / 255f, 18f / 255f, alpha).rgb)
    }

    fun drawEntityOnScreen(posX: Double, posY: Double, scale: Float, entity: EntityLivingBase?) {
        GlStateManager.pushMatrix()
        GlStateManager.enableColorMaterial()

        GlStateManager.translate(posX, posY, 50.0)
        GlStateManager.scale((-scale), scale, scale)
        GlStateManager.rotate(180f, 0f, 0f, 1f)
        GlStateManager.rotate(135f, 0f, 1f, 0f)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.rotate(-135f, 0f, 1f, 0f)
        GlStateManager.translate(0.0, 0.0, 0.0)

        val rendermanager = mc.renderManager
        rendermanager.setPlayerViewY(180f)
        rendermanager.isRenderShadow = false
        rendermanager.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0f, 1f)
        rendermanager.isRenderShadow = true

        GlStateManager.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit)
        GlStateManager.disableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
    }

    fun drawEntityOnScreen(posX: Int, posY: Int, scale: Int, entity: EntityLivingBase?) {
        drawEntityOnScreen(posX.toDouble(), posY.toDouble(), scale.toFloat(), entity)
    }

    fun drawScaledCustomSizeModalRect(
        x: Int,
        y: Int,
        u: Float,
        v: Float,
        uWidth: Int,
        vHeight: Int,
        width: Int,
        height: Int,
        tileWidth: Float,
        tileHeight: Float
    ) {
        val f = 1.0f / tileWidth
        val f1 = 1.0f / tileHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + uWidth.toFloat()) * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + uWidth.toFloat()) * f).toDouble(), (v * f1).toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    fun drawScaledCustomSizeModalCircle(
        x: Int,
        y: Int,
        u: Float,
        v: Float,
        uWidth: Int,
        vHeight: Int,
        width: Int,
        height: Int,
        tileWidth: Float,
        tileHeight: Float
    ) {
        val f = 1.0f / tileWidth
        val f1 = 1.0f / tileHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX)
        val xRadius = width / 2f
        val yRadius = height / 2f
        val uRadius = (((u + uWidth.toFloat()) * f) - (u * f)) / 2f
        val vRadius = (((v + vHeight.toFloat()) * f1) - (v * f1)) / 2f
        var i = 0
        while (i <= 360) {
            val xPosOffset = sin(i * Math.PI / 180.0)
            val yPosOffset = cos(i * Math.PI / 180.0)
            worldrenderer.pos(x + xRadius + xPosOffset * xRadius, y + yRadius + yPosOffset * yRadius, 0.0)
                .tex(u * f + uRadius + xPosOffset * uRadius, v * f1 + vRadius + yPosOffset * vRadius).endVertex()
            i += 10
        }
        tessellator.draw()
    }

    fun drawHead(skin: ResourceLocation?, x: Int, y: Int, width: Int, height: Int, color: Int) {
//        float f3 = (float) (color >> 24 & 255) / 255.0F;
//        float f = (float) (color >> 16 & 255) / 255.0F;
//        float f1 = (float) (color >> 8 & 255) / 255.0F;
//        float f2 = (float) (color & 255) / 255.0F;
//        GlStateManager.color(f, f1, f2, f3);
        mc.textureManager.bindTexture(skin)
        drawScaledCustomSizeModalRect(
            x, y, 8f, 8f, 8, 8, width, height,
            64f, 64f
        )
        drawScaledCustomSizeModalRect(
            x, y, 40f, 8f, 8, 8, width, height,
            64f, 64f
        )
    }

    fun drawAnimatedGradient(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        val currentTime = System.currentTimeMillis()
        if (startTime == 0L) {
            startTime = currentTime
        }

        val elapsedTime = currentTime - startTime
        val animationDuration = 500
        val progress = (elapsedTime % animationDuration).toFloat() / animationDuration

        val color1: Int
        val color2: Int

        if (elapsedTime / animationDuration % 2 == 0L) {
            // Custom Color 1 to Custom Color 2
            color1 = interpolateColors(col1, col2, progress)
            color2 = interpolateColors(col2, col1, progress)
        } else {
            // Custom Color 2 to Custom Color 1
            color1 = interpolateColors(col2, col1, progress)
            color2 = interpolateColors(col1, col2, progress)
        }

        drawGradientSideways(left, top, right, bottom, color1, color2)

        if (elapsedTime >= 2 * animationDuration) {
            // Reset the start time to continue the loop
            startTime = currentTime
        }
    }

    fun drawRoundedGradientOutlineCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        width: Float,
        radius: Float,
        color: Int,
        color2: Int,
        color3: Int,
        color4: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        setColour(-1)
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)

        GL11.glPushAttrib(0)
        GL11.glScaled(0.5, 0.5, 0.5)
        x *= 2.0f
        y *= 2.0f
        x1 *= 2.0f
        y1 *= 2.0f
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glLineWidth(width)
        glBegin(GL11.GL_LINE_LOOP)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color3)
        i = 0
        while (i <= 90) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y1 - radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        setColour(color4)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        glEnd()
        glLineWidth(1f)
        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
        glDisable(GL11.GL_BLEND)
        glEnable(GL11.GL_TEXTURE_2D)
        GL11.glScaled(2.0, 2.0, 2.0)
        GL11.glPopAttrib()


        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL11.GL_FLAT)
        setColour(-1)
    }

    fun drawRoundedGradientOutlineCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        width: Float,
        radius: Float,
        color: Int,
        color2: Int
    ) {
        var x = x
        var y = y
        var x1 = x1
        var y1 = y1
        setColour(-1)
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)

        GL11.glPushAttrib(0)
        GL11.glScaled(0.5, 0.5, 0.5)
        x *= 2.0f
        y *= 2.0f
        x1 *= 2.0f
        y1 *= 2.0f
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glLineWidth(width)
        glBegin(GL11.GL_LINE_LOOP)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1 - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 0
        while (i <= 90) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y1 - radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1 - radius + sin(i * Math.PI / 180.0) * radius,
                y + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        glEnd()
        glLineWidth(1f)
        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
        glDisable(GL11.GL_BLEND)
        glEnable(GL11.GL_TEXTURE_2D)
        GL11.glScaled(2.0, 2.0, 2.0)
        GL11.glPopAttrib()


        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
        glShadeModel(GL11.GL_FLAT)
        setColour(-1)
    }

    fun interpolateColors(color1: Int, color2: Int, progress: Float): Int {
        val alpha = ((1.0 - progress) * (color1 ushr 24) + progress * (color2 ushr 24)).toInt()
        val red = ((1.0 - progress) * ((color1 shr 16) and 0xFF) + progress * ((color2 shr 16) and 0xFF)).toInt()
        val green = ((1.0 - progress) * ((color1 shr 8) and 0xFF) + progress * ((color2 shr 8) and 0xFF)).toInt()
        val blue = ((1.0 - progress) * (color1 and 0xFF) + progress * (color2 and 0xFF)).toInt()

        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

    fun quickDrawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int) {
        quickDrawRect(x, y, x2, y2, color2)

        glColor(color1)
        glLineWidth(width)

        glBegin(GL11.GL_LINE_LOOP)

        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())

        glEnd()
    }

    fun rectangleBorderedx(
        x: Double,
        y: Double,
        x1: Double,
        y1: Double,
        width: Double,
        internalColor: Int,
        borderColor: Int
    ) {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        rectangle(x + width, y, x1 - width, y + width, borderColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        rectangle(x, y, x + width, y1, borderColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        rectangle(x1 - width, y, x1, y1, borderColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        rectangle(x + width, y1 - width, x1 - width, y1, borderColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    fun quickDrawHead(skin: ResourceLocation?, x: Int, y: Int, width: Int, height: Int) {
        mc.textureManager.bindTexture(skin)
        drawScaledCustomSizeModalRect(
            x, y, 8f, 8f, 8, 8, width, height,
            64f, 64f
        )
        drawScaledCustomSizeModalRect(
            x, y, 40f, 8f, 8, 8, width, height,
            64f, 64f
        )
    }

    // skid in https://github.com/WYSI-Foundation/LiquidBouncePlus/
    fun drawBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int) {
        glEnable(GL11.GL_BLEND)
        glDisable(GL11.GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL11.GL_LINE_SMOOTH)

        glColor(color1)
        glLineWidth(width)

        glBegin(GL11.GL_LINE_LOOP)

        glVertex2d((x2 + 1).toDouble(), (y - 1).toDouble())
        glVertex2d((x - 1).toDouble(), (y - 1).toDouble())
        glVertex2d((x - 1).toDouble(), (y2 + 1).toDouble())
        glVertex2d((x2 + 1).toDouble(), (y2 + 1).toDouble())

        glEnd()

        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        glDisable(GL11.GL_LINE_SMOOTH)
    }

    fun drawOutLineRect(
        x: Double,
        y: Double,
        x1: Double,
        y1: Double,
        width: Double,
        internalColor: Int,
        borderColor: Int
    ) {
        drawRect(x + width, y + width, x1 - width, y1 - width, internalColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        drawRect(x + width, y, x1 - width, y + width, borderColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        drawRect(x, y, x + width, y1, borderColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        drawRect(x1 - width, y, x1, y1, borderColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        drawRect(x + width, y1 - width, x1 - width, y1, borderColor)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    fun enableSmoothLine(width: Float) {
        glDisable(3008)
        glEnable(3042)
        glBlendFunc(770, 771)
        glDisable(3553)
        glDisable(2929)
        glDepthMask(false)
        glEnable(2884)
        glEnable(2848)
        glHint(3154, 4354)
        glHint(3155, 4354)
        glLineWidth(width)
    }

    fun disableSmoothLine() {
        glEnable(3553)
        glEnable(2929)
        glDisable(3042)
        glEnable(3008)
        glDepthMask(true)
        GL11.glCullFace(1029)
        glDisable(2848)
        glHint(3154, 4352)
        glHint(3155, 4352)
    }

    fun startSmooth() {
        glEnable(2848)
        glEnable(2881)
        glEnable(2832)
        glEnable(3042)
        glBlendFunc(770, 771)
        glHint(3154, 4354)
        glHint(3155, 4354)
        glHint(3153, 4354)
    }

    fun endSmooth() {
        glDisable(2848)
        glDisable(2881)
        glEnable(2832)
    }


    @JvmStatic
    fun drawNewRect(left: Double, top: Double, right: Double, bottom: Double, color: Int) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        if (left < right) {
            val i = left
            left = right
            right = i
        }
        if (top < bottom) {
            val j = top
            top = bottom
            bottom = j
        }
        val f3 = (color shr 24 and 0xFF).toFloat() / 255.0f
        val f = (color shr 16 and 0xFF).toFloat() / 255.0f
        val f1 = (color shr 8 and 0xFF).toFloat() / 255.0f
        val f2 = (color and 0xFF).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val vertexbuffer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.color(f, f1, f2, f3)
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION)
        vertexbuffer.pos(left, bottom, 0.0).endVertex()
        vertexbuffer.pos(right, bottom, 0.0).endVertex()
        vertexbuffer.pos(right, top, 0.0).endVertex()
        vertexbuffer.pos(left, top, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun renderBox(x: Double, y: Double, z: Double, width: Float, height: Float, c: Color) {
        var y = y
        val halfwidth = width / 2.0f
        val halfheight = height / 2.0f
        GlStateManager.pushMatrix()
        GlStateManager.depthMask(false)
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GlStateManager.disableBlend()
        GlStateManager.disableDepth()
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        y++
        worldRenderer.pos(x - halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        tessellator.draw()
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        GlStateManager.enableLighting()
        GlStateManager.enableCull()
        GlStateManager.enableBlend()
        GlStateManager.popMatrix()
    }

    fun renderOutlines(x: Double, y: Double, z: Double, width: Float, height: Float, c: Color, outlinewidth: Float) {
        var y = y
        val halfwidth = width / 2.0f
        val halfheight = height / 2.0f
        GlStateManager.pushMatrix()
        GlStateManager.depthMask(false)
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GlStateManager.disableBlend()
        GlStateManager.disableDepth()
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(1, DefaultVertexFormats.POSITION_COLOR)
        y++
        glLineWidth(outlinewidth)
        worldRenderer.pos(x - halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y - halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z - halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x - halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        worldRenderer.pos(x + halfwidth, y + halfheight, z + halfwidth).color(c.red, c.green, c.blue, c.alpha)
            .endVertex()
        tessellator.draw()
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        GlStateManager.enableLighting()
        GlStateManager.enableCull()
        GlStateManager.enableBlend()
        GlStateManager.popMatrix()
    }

    fun reAlpha(color: Int, alpha: Float): Int {
        val c = Color(color)
        val r = 0.003921569f * c.red.toFloat()
        val g = 0.003921569f * c.green.toFloat()
        val b = 0.003921569f * c.blue.toFloat()
        return Color(r, g, b, alpha).rgb
    }

    fun SkyRainbow(var2: Int, st: Float, bright: Float): Int {
        var v1 = ceil((System.currentTimeMillis() + (var2 * 109L)).toDouble()) / 5
        return Color.getHSBColor(
            if ((((360.0.also { v1 %= it }) / 360.0).toFloat()) < 0.5) -((v1 / 360.0).toFloat()) else (v1 / 360.0).toFloat(),
            st,
            bright
        ).rgb
    }

    fun skyRainbow(var2: Int, st: Float, bright: Float): Color {
        var v1 = ceil((System.currentTimeMillis() + (var2 * 109L)).toDouble()) / 5
        return Color.getHSBColor(
            if ((((360.0.also { v1 %= it }) / 360.0).toFloat()) < 0.5) -((v1 / 360.0).toFloat()) else (v1 / 360.0).toFloat(),
            st,
            bright
        )
    }

    fun drawFilledForCircle(xx: Float, yy: Float, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float

        glPushAttrib(GL_ENABLE_BIT)

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_TRIANGLE_FAN)

        for (i in 0 until sections) {
            x = (radius * sin((i * dAngle))).toFloat()
            y = (radius * cos((i * dAngle))).toFloat()

            glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            glVertex2f(xx + x, yy + y)
        }

        GlStateManager.color(0f, 0f, 0f)

        glEnd()

        glPopAttrib()
    }

    fun drawQuads(
        leftTop: FloatArray,
        leftBottom: FloatArray,
        rightTop: FloatArray,
        rightBottom: FloatArray,
        rightColor: Color,
        leftColor: Color
    ) {
        GLUtil.setup2DRendering()
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glPushMatrix()
        glBegin(GL_QUADS)
        glColor(leftColor.rgb)
        glVertex2d(leftTop[0].toDouble(), leftTop[1].toDouble())
        glVertex2d(leftBottom[0].toDouble(), leftBottom[1].toDouble())
        glColor(rightColor.rgb)
        glVertex2d(rightBottom[0].toDouble(), rightBottom[1].toDouble())
        glVertex2d(rightTop[0].toDouble(), rightTop[1].toDouble())
        glEnd()
        glPopMatrix()
        glDisable(GL_LINE_SMOOTH)
        GLUtil.end2DRendering()
        resettColor()
    }

    fun drawBacktrackBox(axisAlignedBB: AxisAlignedBB, color: Color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glLineWidth(2f)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color.red, color.green, color.blue, 90)
        drawFilledBox(axisAlignedBB)
        resetColor()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    fun otherDrawOutlinedBoundingBox(yaw: Float, x: Double, y: Double, z: Double, width: Double, height: Double) {
        var yaw = yaw
        var width = width
        width *= 1.5
        yaw = MathHelper.wrapAngleTo180_float(yaw) + 45.0f
        var yaw1: Float
        var yaw2: Float
        var yaw3: Float
        var yaw4: Float
        if (yaw < 0.0f) {
            yaw1 = 0.0f
            yaw1 += 360.0f - abs(yaw)
        } else {
            yaw1 = yaw
        }
        yaw1 *= -1.0f
        yaw1 = (yaw1 * 0.017453292519943295).toFloat()

        yaw += 90f
        if (yaw < 0.0f) {
            yaw2 = 0.0f
            yaw2 += 360.0f - abs(yaw)
        } else {
            yaw2 = yaw
        }
        yaw2 *= -1.0f
        yaw2 = (yaw2 * 0.017453292519943295).toFloat()

        yaw += 90f
        if (yaw < 0.0f) {
            yaw3 = 0.0f
            yaw3 += 360.0f - abs(yaw)
        } else {
            yaw3 = yaw
        }
        yaw3 *= -1.0f
        yaw3 = (yaw3 * 0.017453292519943295).toFloat()

        yaw += 90f
        if (yaw < 0.0f) {
            yaw4 = 0.0f
            yaw4 += 360.0f - abs(yaw)
        } else {
            yaw4 = yaw
        }
        yaw4 *= -1.0f
        yaw4 = (yaw4 * 0.017453292519943295).toFloat()

        val x1 = (sin(yaw1.toDouble()) * width + x).toFloat()
        val z1 = (cos(yaw1.toDouble()) * width + z).toFloat()
        val x2 = (sin(yaw2.toDouble()) * width + x).toFloat()
        val z2 = (cos(yaw2.toDouble()) * width + z).toFloat()
        val x3 = (sin(yaw3.toDouble()) * width + x).toFloat()
        val z3 = (cos(yaw3.toDouble()) * width + z).toFloat()
        val x4 = (sin(yaw4.toDouble()) * width + x).toFloat()
        val z4 = (cos(yaw4.toDouble()) * width + z).toFloat()
        val y2 = (y + height).toFloat()
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        worldrenderer.pos(x1.toDouble(), y, z1.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y2.toDouble(), z1.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y2.toDouble(), z2.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y, z2.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y, z1.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y, z4.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y, z3.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y2.toDouble(), z3.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y, z4.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y2.toDouble(), z3.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y2.toDouble(), z2.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y, z2.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y, z3.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y, z4.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y2.toDouble(), z1.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y, z1.toDouble()).endVertex()
        tessellator.draw()
    }

    fun otherDrawBoundingBox(yaw: Float, x: Double, y: Double, z: Double, width: Double, height: Double) {
        var yaw = yaw
        var width = width
        width *= 1.5
        yaw = MathHelper.wrapAngleTo180_float(yaw) + 45.0f
        var yaw1: Float
        var yaw2: Float
        var yaw3: Float
        var yaw4: Float
        if (yaw < 0.0f) {
            yaw1 = 0.0f
            yaw1 += 360.0f - abs(yaw)
        } else {
            yaw1 = yaw
        }
        yaw1 *= -1.0f
        yaw1 = (yaw1.toDouble() * 0.017453292519943295).toFloat()

        yaw += 90f
        if (yaw < 0.0f) {
            yaw2 = 0.0f
            yaw2 += 360.0f - abs(yaw)
        } else {
            yaw2 = yaw
        }

        yaw2 *= -1.0f
        yaw2 = (yaw2.toDouble() * 0.017453292519943295).toFloat()
        yaw += 90f
        if (yaw < 0.0f) {
            yaw3 = 0.0f
            yaw3 += 360.0f - abs(yaw)
        } else {
            yaw3 = yaw
        }

        yaw3 *= -1.0f
        yaw3 = (yaw3.toDouble() * 0.017453292519943295).toFloat()
        yaw += 90f
        if (yaw < 0.0f) {
            yaw4 = 0.0f
            yaw4 += 360.0f - abs(yaw)
        } else {
            yaw4 = yaw
        }

        yaw4 *= -1.0f
        yaw4 = (yaw4.toDouble() * 0.017453292519943295).toFloat()
        val x1 = (sin(yaw1.toDouble()) * width + x).toFloat()
        val z1 = (cos(yaw1.toDouble()) * width + z).toFloat()
        val x2 = (sin(yaw2.toDouble()) * width + x).toFloat()
        val z2 = (cos(yaw2.toDouble()) * width + z).toFloat()
        val x3 = (sin(yaw3.toDouble()) * width + x).toFloat()
        val z3 = (cos(yaw3.toDouble()) * width + z).toFloat()
        val x4 = (sin(yaw4.toDouble()) * width + x).toFloat()
        val z4 = (cos(yaw4.toDouble()) * width + z).toFloat()
        val y1 = y.toFloat()
        val y2 = (y + height).toFloat()
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        worldrenderer.pos(x1.toDouble(), y1.toDouble(), z1.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y2.toDouble(), z1.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y2.toDouble(), z2.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y1.toDouble(), z2.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y1.toDouble(), z2.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y2.toDouble(), z2.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y2.toDouble(), z3.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y1.toDouble(), z3.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y1.toDouble(), z3.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y2.toDouble(), z3.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y1.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y1.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y2.toDouble(), z1.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y1.toDouble(), z1.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y1.toDouble(), z1.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y1.toDouble(), z2.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y1.toDouble(), z3.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y1.toDouble(), z4.toDouble()).endVertex()
        worldrenderer.pos(x1.toDouble(), y2.toDouble(), z1.toDouble()).endVertex()
        worldrenderer.pos(x2.toDouble(), y2.toDouble(), z2.toDouble()).endVertex()
        worldrenderer.pos(x3.toDouble(), y2.toDouble(), z3.toDouble()).endVertex()
        worldrenderer.pos(x4.toDouble(), y2.toDouble(), z4.toDouble()).endVertex()
        tessellator.draw()
    }
}
