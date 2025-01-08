package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.style.styles.Slight.RenderUtil
import net.ccbluex.liquidbounce.utils.render.ColorUtils.getAlphaFromColor
import net.ccbluex.liquidbounce.utils.render.ColorUtils.getBlueFromColor
import net.ccbluex.liquidbounce.utils.render.ColorUtils.getGreenFromColor
import net.ccbluex.liquidbounce.utils.render.ColorUtils.getRedFromColor
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.util.*
import java.util.function.Consumer
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "HitBubbles", category = ModuleCategory.VISUAL)
class HitBubbles : Module() {
    private val tessellator: Tessellator = Tessellator.getInstance()
    private val buffer: WorldRenderer = tessellator.worldRenderer
    private val BUBBLE_TEXTURE = ResourceLocation("fdpclient/bubble.png")

    private val alphaPC: Float
        get() = 1f

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase && Objects.requireNonNull(
                FDPClient.moduleManager.getModule(
                    KillAura::class.java
                )
            )?.currentTarget == null
        ) {
            if (!event.targetEntity.isEntityAlive()) {
                return
            }
            val to = event.targetEntity.getPositionVector()
                .addVector(0.0, (event.targetEntity.height / 1.6f).toDouble(), 0.0)
            addBubble(to)
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (Objects.requireNonNull(FDPClient.moduleManager.getModule(KillAura::class.java))?.currentTarget != null) {
            if (Objects.requireNonNull(
                    Objects.requireNonNull(
                        FDPClient.moduleManager.getModule(
                            KillAura::class.java
                        )
                    )?.currentTarget
                )!!.hurtTime == 9
            ) {
                val to = Objects.requireNonNull(
                    Objects.requireNonNull(
                        FDPClient.moduleManager.getModule(
                            KillAura::class.java
                        )
                    )?.currentTarget
                )!!.positionVector.addVector(
                    0.0, (Objects.requireNonNull(
                        Objects.requireNonNull(
                            FDPClient.moduleManager.getModule(
                                KillAura::class.java
                            )
                        )?.currentTarget
                    )!!.height / 1.6f).toDouble(), 0.0
                )
                addBubble(to)
            }
        }
    }

    private fun setupDrawsBubbles3D(render: Runnable) {
        val manager = mc.renderManager
        val conpense = Vec3(manager.renderPosX, manager.renderPosY, manager.renderPosZ)
        val light = GL11.glIsEnabled(GL11.GL_LIGHTING)
        GlStateManager.pushMatrix()
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.depthMask(false)
        GlStateManager.disableCull()
        if (light) GlStateManager.disableLighting()
        GL11.glShadeModel(GL11.GL_SMOOTH)
        GlStateManager.tryBlendFuncSeparate(770, 32772, 1, 0)
        GL11.glTranslated(-conpense.xCoord, -conpense.yCoord, -conpense.zCoord)
        mc.textureManager.bindTexture(this.BUBBLE_TEXTURE)
        render.run()

        GL11.glTranslated(conpense.xCoord, conpense.yCoord, conpense.zCoord)
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.resetColor()
        GL11.glShadeModel(GL11.GL_FLAT)
        if (light) GlStateManager.enableLighting()
        GlStateManager.enableCull()
        GlStateManager.depthMask(true)
        GlStateManager.enableAlpha()
        GlStateManager.popMatrix()
        GlStateManager.popMatrix()
    }

    private fun drawBubble(bubble: Bubble, alphaPC: Float) {
        GL11.glPushMatrix()
        GL11.glTranslated(bubble.pos.xCoord, bubble.pos.yCoord, bubble.pos.zCoord)
        val extS: Float = bubble.deltaTime
        GlStateManager.translate(
            -sin(Math.toRadians(bubble.viewPitch.toDouble())) * extS.toDouble() / 3.0, sin(
                Math.toRadians(bubble.viewYaw.toDouble())
            ) * extS.toDouble() / 2.0, -cos(Math.toRadians(bubble.viewPitch.toDouble())) * extS.toDouble() / 3.0
        )
        GL11.glNormal3d(1.0, 1.0, 1.0)
        GL11.glRotated(bubble.viewPitch.toDouble(), 0.0, 1.0, 0.0)
        GL11.glRotated(bubble.viewYaw.toDouble(), if (mc.gameSettings.thirdPersonView == 2) -1.0 else 1.0, 0.0, 0.0)
        GL11.glScaled(-0.1, -0.1, 0.1)
        this.drawBeginsNullCoord(bubble, alphaPC)
        GL11.glPopMatrix()
    }

    private fun drawBeginsNullCoord(bubble: Bubble, alphaPC: Float) {
        val r: Float = 50.0f * bubble.deltaTime * (1.0f - bubble.deltaTime)
        val speedRotate = 3
        val III = (System.currentTimeMillis() % (3600 / speedRotate).toLong()).toFloat() / 10.0f * speedRotate.toFloat()
        RenderUtil.customRotatedObject2D(-1.0f, -1.0f, 2.0f, 2.0f, -III)
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
        buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(
            getRedFromColor(rainbow(0).rgb), getGreenFromColor(rainbow(0).rgb), getBlueFromColor(
                rainbow(0).rgb
            ), getAlphaFromColor(rainbow(0).rgb)
        ).endVertex()
        buffer.pos(0.0, r.toDouble(), 0.0).tex(0.0, 1.0).color(
            getRedFromColor(rainbow(90).rgb), getGreenFromColor(
                rainbow(90).rgb
            ), getBlueFromColor(rainbow(90).rgb), getAlphaFromColor(rainbow(90).rgb)
        ).endVertex()
        buffer.pos(r.toDouble(), r.toDouble(), 0.0).tex(1.0, 1.0).color(
            getRedFromColor(rainbow(180).rgb), getGreenFromColor(
                rainbow(180).rgb
            ), getBlueFromColor(rainbow(180).rgb), getAlphaFromColor(rainbow(180).rgb)
        ).endVertex()
        buffer.pos(r.toDouble(), 0.0, 0.0).tex(1.0, 0.0).color(
            getRedFromColor(rainbow(270).rgb), getGreenFromColor(
                rainbow(270).rgb
            ), getBlueFromColor(rainbow(270).rgb), getAlphaFromColor(rainbow(270).rgb)
        ).endVertex()
        GlStateManager.blendFunc(770, 772)
        GlStateManager.translate(-r / 2.0f, -r / 2.0f, 0.0f)
        GlStateManager.shadeModel(7425)
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0)
        tessellator.draw()
        GlStateManager.shadeModel(7424)
        GlStateManager.translate(r / 2.0f, r / 2.0f, 0.0f)
        GlStateManager.blendFunc(770, 771)
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val aPC = this.alphaPC
        if (aPC.toDouble() < 0.05) {
            return
        }
        if (bubbles.isEmpty()) {
            return
        }
        this.removeAuto()
        this.setupDrawsBubbles3D {
            bubbles.forEach(Consumer { bubble: Bubble? ->
                if (bubble != null && bubble.deltaTime <= 1.0f) {
                    this.drawBubble(bubble, aPC)
                }
            })
        }
    }

    private fun removeAuto() {
        bubbles.removeIf { bubble: Bubble -> bubble.deltaTime >= 1.0f }
    }

    class Bubble(var viewYaw: Float, var viewPitch: Float, var pos: Vec3) {
        var time: Long = System.currentTimeMillis()
        var maxTime: Float = Companion.maxTime

        val deltaTime: Float
            get() = (System.currentTimeMillis() - this.time).toFloat() / this.maxTime
    }

    companion object {
        val bubbles: ArrayList<Bubble> = ArrayList()
        private val maxTime: Float
            get() = 1000.0f

        private fun addBubble(addToCoord: Vec3) {
            val manager = mc.renderManager
            bubbles.add(Bubble(manager.playerViewX, -manager.playerViewY, addToCoord))
        }
    }
}