/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.utils.particles.EvictingList
import net.ccbluex.liquidbounce.utils.particles.Particle
import net.ccbluex.liquidbounce.utils.particles.Vec3
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.ParticleTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.projectile.EntityEgg
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.function.Consumer
import kotlin.math.abs

@ModuleInfo(name = "Particles", category = ModuleCategory.VISUAL)
class Particles : Module() {
    private val amount = IntegerValue("Amount", 10, 1, 20)

    private val physics = BoolValue("Physics", true)

    private val startred = IntegerValue("StartRed", 255 ,0, 255)
    private val startgreen = IntegerValue("StartGreen", 150 ,0, 255)
    private val startblue = IntegerValue("StartBlue", 200 ,0, 255)
    private val endred = IntegerValue("EndRed", 100 ,0, 255)
    private val endgreen = IntegerValue("EndGreen", 110 ,0, 255)
    private val endblue = IntegerValue("EndBlue", 195 ,0, 255)

    private val particles: MutableList<Particle> = EvictingList(100)
    private val timer = ParticleTimer()
    private var target: EntityLivingBase? = null

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) target = event.targetEntity
    }

    @EventTarget
    fun onMotion(event: MotionEvent?) {
        if (target != null && target!!.hurtTime >= 9 && mc.thePlayer.getDistance(
                target!!.posX,
                target!!.posY,
                target!!.posZ
            ) < 10
        ) {
            for (i in 0 until amount.get()) particles.add(
                Particle(
                    Vec3(
                        target!!.posX + (Math.random() - 0.5) * 0.5,
                        target!!.posY + Math.random() * 1 + 0.5,
                        target!!.posZ + (Math.random() - 0.5) * 0.5
                    )
                )
            )

            target = null
        }
    }
    private fun renderParticles(particles: List<Particle>) {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        var i = 0
        try {
            for (particle in particles) {
                i++
                val v = particle.position
                var draw = true

                val x = v.xCoord - mc.renderManager.renderPosX
                val y = v.yCoord - mc.renderManager.renderPosY
                val z = v.zCoord - mc.renderManager.renderPosZ

                val distanceFromPlayer = mc.thePlayer.getDistance(v.xCoord, v.yCoord - 1, v.zCoord)
                var quality = (distanceFromPlayer * 4 + 10).toInt()

                if (quality > 350) quality = 350

                if (!RenderUtils.isInViewFrustrum(EntityEgg(mc.theWorld, v.xCoord, v.yCoord, v.zCoord))) draw = false

                if (i % 10 != 0 && distanceFromPlayer > 25) draw = false

                if (i % 3 == 0 && distanceFromPlayer > 15) draw = false

                if (draw) {
                    GL11.glPushMatrix()
                    GL11.glTranslated(x, y, z)

                    val scale = 0.04f
                    GL11.glScalef(-scale, -scale, -scale)

                    GL11.glRotated(-mc.renderManager.playerViewY.toDouble(), 0.0, 1.0, 0.0)
                    GL11.glRotated(
                        mc.renderManager.playerViewX.toDouble(),
                        if (mc.gameSettings.thirdPersonView == 2) -1.0 else 1.0,
                        0.0,
                        0.0
                    )
                    val c = RenderUtils.getGradientOffset(
                        Color(startred.get(), startgreen.get(), startblue.get(), 1), Color(endred.get(), endgreen.get(), endblue.get(), 1),
                        (abs(
                            (System.currentTimeMillis() / 100 + (20 / 10)).toDouble()
                        ) / 10)
                    )
                    RenderUtils.drawFilledCircleNoGL(0, 0, 0.7, c.hashCode(), quality)

                    if (distanceFromPlayer < 4) RenderUtils.drawFilledCircleNoGL(
                        0,
                        0,
                        1.4,
                        Color(c.red, c.green, c.blue, 50).hashCode(),
                        quality
                    )

                    if (distanceFromPlayer < 20) RenderUtils.drawFilledCircleNoGL(
                        0,
                        0,
                        2.3,
                        Color(c.red, c.green, c.blue, 30).hashCode(),
                        quality
                    )

                    GL11.glScalef(0.8f, 0.8f, 0.8f)
                    GL11.glPopMatrix()
                }
            }
        } catch (ignored: ConcurrentModificationException) {
        }

        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)

        GL11.glColor3d(255.0, 255.0, 255.0)
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        if (particles.isEmpty()) return

        var i = 0
        while (i <= timer.elapsedTime / 1E+11) {
            if (physics.get()) particles.forEach(Consumer { obj: Particle -> obj.update() })
            else particles.forEach(Consumer { obj: Particle -> obj.updateWithoutPhysics() })
            i++
        }

        particles.removeIf { particle: Particle ->
            mc.thePlayer.getDistanceSq(
                particle.position.xCoord,
                particle.position.yCoord,
                particle.position.zCoord
            ) > 50 * 10
        }

        timer.reset()

        renderParticles(particles)
    }
}