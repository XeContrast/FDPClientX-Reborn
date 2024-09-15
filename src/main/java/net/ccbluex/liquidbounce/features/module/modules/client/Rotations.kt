/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils.Companion.serverRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.Companion.targetRotation

@ModuleInfo(name = "Rotations", category = ModuleCategory.CLIENT)
object Rotations : Module() {

    private val realistic = BoolValue("Realistic", false)
    private val body = BoolValue("Body", true).displayable { !realistic.get() }
    private val smooth = BoolValue("SmoothRotation",true)
    private val Smoothing = FloatValue("SmoothFacing",0.15f,0.1f,0.9f).displayable { smooth.get() }

    var prevHeadPitch = 0f
    var headPitch = 0f


    private var lastRotation: Rotation? = null

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        prevHeadPitch = headPitch
        headPitch = serverRotation!!.pitch

        if (!shouldRotate() || realistic.get()) {
            return
        }

        thePlayer.rotationYawHead = serverRotation!!.yaw

        if (body.get()) {
            thePlayer.renderYawOffset = thePlayer.rotationYawHead
        }

        lastRotation = targetRotation
    }

    fun lerp(tickDelta: Float, old: Float, new: Float): Float {
        return old + (new - old) * tickDelta
    }

    /**
     * Rotate when current rotation is not null or special modules which do not make use of RotationUtils like Derp are enabled.
     */
    fun shouldRotate() = state || targetRotation != null

    /**
     * Imitate the game's head and body rotation logic
     */
    fun shouldUseRealisticMode() = realistic.get() && shouldRotate()

    /**
     * Smooth out rotations between two points
     */
    private fun smoothRotation(from: Rotation, to: Rotation): Rotation {
        val diffYaw = to.yaw - from.yaw
        val diffPitch = to.pitch - from.pitch

        val smoothedYaw = from.yaw + diffYaw * Smoothing.get()
        val smoothedPitch = from.pitch + diffPitch * Smoothing.get()

        return Rotation(smoothedYaw, smoothedPitch)
    }

    /**
     * Which rotation should the module use?
     */
    fun getRotation(): Rotation? {
        return if (smooth.get() && lastRotation != null && targetRotation != null) {
            smoothRotation(lastRotation!!, targetRotation!!)
        } else {
            targetRotation
        }
    }
}
