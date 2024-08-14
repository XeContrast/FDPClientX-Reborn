/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.MathUtils.calculateGaussianValue
import net.ccbluex.liquidbounce.utils.animations.Animation
import net.ccbluex.liquidbounce.utils.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.utils.render.ColorUtils.interpolateColorsBackAndForth
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbowc
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.ShaderUtil
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import java.awt.Color
import java.util.*
import java.util.function.Consumer

@ModuleInfo(name = "GlowESP", category = ModuleCategory.VISUAL)
class GlowESP : Module() {
    val radius: FloatValue = FloatValue("Radius", 2f, 1f, 30f)
    val exposure: FloatValue = FloatValue("Exposure", 2.2f, 1f, 3.5f)
    val seperate: BoolValue = BoolValue("Seperate Texture", false)
    val Players: BoolValue = BoolValue("Players", false)
    val Animals: BoolValue = BoolValue("Animals", false)
    val Mobs: BoolValue = BoolValue("Mobs", false)

    val colorMode: ListValue = ListValue(
        "ColorMode",
        arrayOf("Rainbow", "Light Rainbow", "Static", "Double Color", "Default"),
        "Light Rainbow"
    )
    val movingcolors: BoolValue = BoolValue("MovingColors", false)
    val hueInterpolation: BoolValue = BoolValue("hueInterpolation", false)
    private val outlineShader = ShaderUtil("shaders/outline.frag")
    private val glowShader = ShaderUtil("shaders/glow.frag")

    var framebuffer: Framebuffer? = null
    var outlineFrameBuffer: Framebuffer? = null
    var glowFrameBuffer: Framebuffer? = null
    private val frustum = Frustum()
    private val frustum2 = Frustum()

    private val entities: MutableList<Entity> = ArrayList()

    private fun isInView(ent: Entity): Boolean {
        frustum2.setPosition(mc.renderViewEntity.posX, mc.renderViewEntity.posY, mc.renderViewEntity.posZ)
        return frustum2.isBoundingBoxInFrustum(ent.entityBoundingBox) || ent.ignoreFrustumCheck
    }

    override fun onEnable() {
        super.onEnable()
        fadeIn = DecelerateAnimation(250, 1.0)
    }

    fun createFrameBuffers() {
        framebuffer = RenderUtils.createFrameBuffer(framebuffer)
        outlineFrameBuffer = RenderUtils.createFrameBuffer(outlineFrameBuffer)
        glowFrameBuffer = RenderUtils.createFrameBuffer(glowFrameBuffer)
    }


    @EventTarget
    fun onrender3D(event: Render3DEvent) {
        createFrameBuffers()
        collectEntities()
        framebuffer!!.framebufferClear()
        framebuffer!!.bindFramebuffer(true)
        renderEntities(event.partialTicks)
        framebuffer!!.unbindFramebuffer()
        mc.framebuffer.bindFramebuffer(true)
        GlStateManager.disableLighting()
    }

    @EventTarget
    fun onrender2D(event: Render2DEvent?) {
        val sr = ScaledResolution(mc)
        if (framebuffer != null && outlineFrameBuffer != null && entities.isNotEmpty()) {
            GlStateManager.enableAlpha()
            GlStateManager.alphaFunc(516, 0.0f)
            GlStateManager.enableBlend()
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

            outlineFrameBuffer!!.framebufferClear()
            outlineFrameBuffer!!.bindFramebuffer(true)
            outlineShader.init()
            setupOutlineUniforms(0f, 1f)
            RenderUtils.bindTexture(framebuffer!!.framebufferTexture)
            ShaderUtil.drawQuads()
            outlineShader.init()
            setupOutlineUniforms(1f, 0f)
            RenderUtils.bindTexture(framebuffer!!.framebufferTexture)
            ShaderUtil.drawQuads()
            outlineShader.unload()
            outlineFrameBuffer!!.unbindFramebuffer()

            GlStateManager.color(1f, 1f, 1f, 1f)
            glowFrameBuffer!!.framebufferClear()
            glowFrameBuffer!!.bindFramebuffer(true)
            glowShader.init()
            setupGlowUniforms(1f, 0f)
            RenderUtils.bindTexture(outlineFrameBuffer!!.framebufferTexture)
            ShaderUtil.drawQuads()
            glowShader.unload()
            glowFrameBuffer!!.unbindFramebuffer()

            mc.framebuffer.bindFramebuffer(true)
            glowShader.init()
            setupGlowUniforms(0f, 1f)
            if (seperate.get()) {
                GL13.glActiveTexture(GL13.GL_TEXTURE16)
                RenderUtils.bindTexture(framebuffer!!.framebufferTexture)
            }
            GL13.glActiveTexture(GL13.GL_TEXTURE0)
            RenderUtils.bindTexture(glowFrameBuffer!!.framebufferTexture)
            ShaderUtil.drawQuads()
            glowShader.unload()
        }
    }


    fun setupGlowUniforms(dir1: Float, dir2: Float) {
        val color = color
        glowShader.setUniformi("texture", 0)
        if (seperate.get()) {
            glowShader.setUniformi("textureToCheck", 16)
        }
        glowShader.setUniformf("radius", radius.get())
        glowShader.setUniformf("texelSize", 1.0f / mc.displayWidth, 1.0f / mc.displayHeight)
        glowShader.setUniformf("direction", dir1, dir2)
        glowShader.setUniformf("color", color!!.red / 255f, color.green / 255f, color.blue / 255f)
        glowShader.setUniformf("exposure", (exposure.get() * fadeIn!!.output).toFloat())
        glowShader.setUniformi("avoidTexture", if (seperate.get()) 1 else 0)

        val buffer = BufferUtils.createFloatBuffer(256)
        for (i in 1..radius.value.toInt()) {
            buffer.put(calculateGaussianValue(i.toFloat(), radius.get() / 2))
        }
        buffer.rewind()

        GL20.glUniform1(glowShader.getUniform("weights"), buffer)
    }


    fun setupOutlineUniforms(dir1: Float, dir2: Float) {
        val color = color
        outlineShader.setUniformi("texture", 0)
        outlineShader.setUniformf("radius", radius.get() / 1.5f)
        outlineShader.setUniformf("texelSize", 1.0f / mc.displayWidth, 1.0f / mc.displayHeight)
        outlineShader.setUniformf("direction", dir1, dir2)
        outlineShader.setUniformf("color", color!!.red / 255f, color.green / 255f, color.blue / 255f)
    }

    fun renderEntities(ticks: Float) {
        entities.forEach(Consumer { entity: Entity? ->
            renderNameTags = false
            mc.renderManager.renderEntityStatic(entity, ticks, false)
            renderNameTags = true
        })
    }

    private val color: Color?
        get() {
            val colors = clientColors
            return if (movingcolors.get()) {
                colors[0]
            } else {
                interpolateColorsBackAndForth(15, 0, colors[0], colors[1], hueInterpolation.get())
            }
        }

    fun collectEntities() {
        entities.clear()
        for (entity in mc.theWorld.getLoadedEntityList()) {
            if (!isInView(entity)) continue
            if (entity === mc.thePlayer && mc.gameSettings.thirdPersonView == 0) continue
            if (entity is EntityAnimal && Animals.get()) {
                entities.add(entity)
            }

            if (entity is EntityPlayer && Players.get()) {
                entities.add(entity)
            }

            if (entity is EntityMob && Mobs.get()) {
                entities.add(entity)
            }
        }
    }

    val clientColors: Array<Color?>
        get() {
            val firstColor: Color?
            val secondColor: Color?
            when (colorMode.get().lowercase(Locale.getDefault())) {
                "light rainbow" -> {
                    firstColor = rainbowc(15, 1, .6f, 1f, 1f)
                    secondColor = rainbowc(15, 40, .6f, 1f, 1f)
                }

                "rainbow" -> {
                   firstColor = rainbow()
                    secondColor = firstColor
                }

                "double color" -> {
                    firstColor = interpolateColorsBackAndForth(15, 0, Color.PINK, Color.BLUE, hueInterpolation.get())
                    secondColor = interpolateColorsBackAndForth(15, 90, Color.PINK, Color.BLUE, hueInterpolation.get())
                }

                "static" -> {
                    firstColor = Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
                    secondColor = firstColor
                }

                else -> {
                    firstColor = Color(-1)
                    secondColor = Color(-1)
                }
            }
            return arrayOf(firstColor, secondColor)
        }

    companion object {
        val colorRedValue: IntegerValue = IntegerValue("R", 0, 0, 255)
        val colorGreenValue: IntegerValue = IntegerValue("G", 160, 0, 255)
        val colorBlueValue: IntegerValue = IntegerValue("B", 255, 0, 255)

        var renderNameTags: Boolean = true
        var fadeIn: Animation? = null
    }
}