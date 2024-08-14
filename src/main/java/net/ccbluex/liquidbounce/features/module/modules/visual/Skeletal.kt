/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateModelEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.util.*

@ModuleInfo(name = "Skeletal", category = ModuleCategory.VISUAL)
class Skeletal : Module() {
    private val playerRotationMap: MutableMap<*, *> = WeakHashMap<Any?, Any?>()

    private val red = IntegerValue("Red", 255, 0, 255)
    private val green = IntegerValue("Green", 255, 0, 255)
    private val blue = IntegerValue("Blue", 255, 0, 255)

    private val smoothLines = BoolValue("SmoothLines", false)

    @EventTarget
    fun onModelUpdate(event: UpdateModelEvent) {
        val model = event.model
        this.playerRotationMap[event.player as Nothing] = arrayOf(model.bipedHead.rotateAngleX, model.bipedHead.rotateAngleY, model.bipedHead.rotateAngleZ, model.bipedRightArm.rotateAngleX, model.bipedRightArm.rotateAngleY, model.bipedRightArm.rotateAngleZ, model.bipedLeftArm.rotateAngleX, model.bipedLeftArm.rotateAngleY, model.bipedLeftArm.rotateAngleZ, model.bipedRightLeg.rotateAngleX, model.bipedRightLeg.rotateAngleY, model.bipedRightLeg.rotateAngleZ, model.bipedLeftLeg.rotateAngleX, model.bipedLeftLeg.rotateAngleY, model.bipedLeftLeg.rotateAngleZ) as Nothing
    }

    @EventTarget
    fun onRender(event: Render3DEvent) {
        this.setupRender(true)
        GL11.glEnable(2903)
        GL11.glDisable(2848)

        playerRotationMap.keys.removeIf { var0: Any? -> contain(var0 as EntityPlayer?) }

        val playerRotationMap: Map<*, *> = this.playerRotationMap
        mc.theWorld.playerEntities

        val players: Array<Any?> = playerRotationMap.keys.toTypedArray()
        val playersLength = players.size

        for (i in 0 until playersLength) {
            val player = players[i] as EntityPlayer
            val entPos = playerRotationMap[player] as Array<FloatArray>?

            if (entPos == null || player.entityId == -1488 || !player.isEntityAlive || !RenderUtils.isInViewFrustrum(
                    player
                ) ||
                player.isDead || player === mc.thePlayer || player.isPlayerSleeping || player.isInvisible
            ) continue

            GL11.glPushMatrix()
            val modelRotations = playerRotationMap[player] as Array<FloatArray>?
            GL11.glLineWidth(1.0f)
            GL11.glColor4f(red.get() / 255.0f, green.get() / 255.0f, blue.get() / 255.0f, 1.0f)

            val x = interpolate(
                player.posX,
                player.lastTickPosX,
                event.partialTicks.toDouble()
            ) - mc.renderManager.renderPosX
            val y = interpolate(
                player.posY,
                player.lastTickPosY,
                event.partialTicks.toDouble()
            ) - mc.renderManager.renderPosY
            val z = interpolate(
                player.posZ,
                player.lastTickPosZ,
                event.partialTicks.toDouble()
            ) - mc.renderManager.renderPosZ

            GL11.glTranslated(x, y, z)

            val bodyYawOffset =
                player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * mc.timer.renderPartialTicks

            GL11.glRotatef((-bodyYawOffset), 0.0f, 1.0f, 0.0f)
            GL11.glTranslated(0.0, 0.0, (if (player.isSneaking) -0.235 else 0.0))

            val legHeight = if (player.isSneaking) 0.6f else 0.75f

            GL11.glPushMatrix()
            GL11.glTranslated(-0.125, legHeight.toDouble(), 0.0)

            if (modelRotations!![3][0] != 0.0f) {
                GL11.glRotatef((modelRotations[3][0] * 57.29578f), 1.0f, 0.0f, 0.0f)
            }

            if (modelRotations[3][1] != 0.0f) {
                GL11.glRotatef((modelRotations[3][1] * 57.29578f), 0.0f, 1.0f, 0.0f)
            }

            if (modelRotations[3][2] != 0.0f) {
                GL11.glRotatef((modelRotations[3][2] * 57.29578f), 0.0f, 0.0f, 1.0f)
            }

            GL11.glBegin(3)
            GL11.glVertex3d(0.0, 0.0, 0.0)
            GL11.glVertex3d(0.0, (-legHeight).toDouble(), 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glPushMatrix()
            GL11.glTranslated(0.125, legHeight.toDouble(), 0.0)

            if (modelRotations[4][0] != 0.0f) {
                GL11.glRotatef((modelRotations[4][0] * 57.29578f), 1.0f, 0.0f, 0.0f)
            }

            if (modelRotations[4][1] != 0.0f) {
                GL11.glRotatef((modelRotations[4][1] * 57.29578f), 0.0f, 1.0f, 0.0f)
            }

            if (modelRotations[4][2] != 0.0f) {
                GL11.glRotatef((modelRotations[4][2] * 57.29578f), 0.0f, 0.0f, 1.0f)
            }

            GL11.glBegin(3)
            GL11.glVertex3d(0.0, 0.0, 0.0)
            GL11.glVertex3d(0.0, (-legHeight).toDouble(), 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glTranslated(0.0, 0.0, (if (player.isSneaking) 0.25 else 0.0))
            GL11.glPushMatrix()
            GL11.glTranslated(0.0, (if (player.isSneaking) -0.05 else 0.0), (if (player.isSneaking) -0.01725 else 0.0))
            GL11.glPushMatrix()
            GL11.glTranslated(-0.375, (legHeight + 0.55), 0.0)

            if (modelRotations[1][0] != 0.0f) {
                GL11.glRotatef((modelRotations[1][0] * 57.29578f), 1.0f, 0.0f, 0.0f)
            }

            if (modelRotations[1][1] != 0.0f) {
                GL11.glRotatef((modelRotations[1][1] * 57.29578f), 0.0f, 1.0f, 0.0f)
            }

            if (modelRotations[1][2] != 0.0f) {
                GL11.glRotatef((-modelRotations[1][2] * 57.29578f), 0.0f, 0.0f, 1.0f)
            }

            GL11.glBegin(3)
            GL11.glVertex3d(0.0, 0.0, 0.0)
            GL11.glVertex3d(0.0, -0.5, 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glPushMatrix()
            GL11.glTranslated(0.375, (legHeight + 0.55), 0.0)

            if (modelRotations[2][0] != 0.0f) {
                GL11.glRotatef((modelRotations[2][0] * 57.29578f), 1.0f, 0.0f, 0.0f)
            }

            if (modelRotations[2][1] != 0.0f) {
                GL11.glRotatef((modelRotations[2][1] * 57.29578f), 0.0f, 1.0f, 0.0f)
            }

            if (modelRotations[2][2] != 0.0f) {
                GL11.glRotatef((-modelRotations[2][2] * 57.29578f), 0.0f, 0.0f, 1.0f)
            }

            GL11.glBegin(3)
            GL11.glVertex3d(0.0, 0.0, 0.0)
            GL11.glVertex3d(0.0, -0.5, 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glRotatef((bodyYawOffset - player.rotationYawHead), 0.0f, 1.0f, 0.0f)
            GL11.glPushMatrix()
            GL11.glTranslated(0.0, (legHeight + 0.55), 0.0)

            if (modelRotations[0][0] != 0.0f) {
                GL11.glRotatef((modelRotations[0][0] * 57.29578f), 1.0f, 0.0f, 0.0f)
            }

            GL11.glBegin(3)
            GL11.glVertex3d(0.0, 0.0, 0.0)
            GL11.glVertex3d(0.0, 0.3, 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glPopMatrix()
            GL11.glRotatef((if (player.isSneaking) 25.0f else 0.0f), 1.0f, 0.0f, 0.0f)
            GL11.glTranslated(
                0.0,
                (if (player.isSneaking) -0.16175 else 0.0),
                (if (player.isSneaking) -0.48025 else 0.0)
            )
            GL11.glPushMatrix()
            GL11.glTranslated(0.0, legHeight.toDouble(), 0.0)
            GL11.glBegin(3)
            GL11.glVertex3d(-0.125, 0.0, 0.0)
            GL11.glVertex3d(0.125, 0.0, 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glPushMatrix()
            GL11.glTranslated(0.0, legHeight.toDouble(), 0.0)
            GL11.glBegin(3)
            GL11.glVertex3d(0.0, 0.0, 0.0)
            GL11.glVertex3d(0.0, 0.55, 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glPushMatrix()
            GL11.glTranslated(0.0, (legHeight + 0.55), 0.0)
            GL11.glBegin(3)
            GL11.glVertex3d(-0.375, 0.0, 0.0)
            GL11.glVertex3d(0.375, 0.0, 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            GL11.glPopMatrix()
        }
        this.setupRender(false)
    }

    private fun setupRender(start: Boolean) {
        val smooth = smoothLines.get()

        if (start) {
            if (smooth) {
                RenderUtils.startSmooth()
            } else {
                GL11.glDisable(2848)
            }
            GL11.glDisable(2929)
            GL11.glDisable(3553)
        } else {
            GL11.glEnable(3553)
            GL11.glEnable(2929)
            if (smooth) {
                RenderUtils.endSmooth()
            }
        }
        GL11.glDepthMask((if (!start) 1 else 0) != 0)
    }

    private fun contain(var0: EntityPlayer?): Boolean {
        return !mc.theWorld.playerEntities.contains(var0)
    }

    companion object {
        fun interpolate(current: Double, old: Double, scale: Double): Double {
            return old + (current - old) * scale
        }
    }
}

