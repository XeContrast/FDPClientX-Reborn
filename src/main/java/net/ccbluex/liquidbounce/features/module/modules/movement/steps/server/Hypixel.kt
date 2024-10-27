package net.ccbluex.liquidbounce.features.module.modules.movement.steps.server

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Step
import net.ccbluex.liquidbounce.features.module.modules.movement.steps.StepMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.MovementUtils

class Hypixel : StepMode("Hypixel") {
    private val test = BoolValue("Test",false)
    private var offGroundTicks = -1
    private var stepping = false
    private var lastStep: Long = -1
    override fun onDisable() {
        offGroundTicks = -1
        stepping = false
    }

    @EventTarget
    override fun onMotion(event: MotionEvent) {
        Step.off = true
        Step.doncheck = true
        if (event.eventState == EventState.PRE) {
            val time = System.currentTimeMillis()
            if (mc.thePlayer.onGround && mc.thePlayer.isCollidedHorizontally && MovementUtils.isMoving && time - lastStep >= Step.delayValue.get()) {
                stepping = true
                lastStep = time
            }
        }
    }

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0
        } else if (offGroundTicks != -1) {
            offGroundTicks++
        }

        if (stepping) {
            if (!MovementUtils.isMoving || !mc.thePlayer.isCollidedHorizontally) {
                stepping = false
                return
            }

            when (if (test.get()) offGroundTicks % 16 else offGroundTicks) {
                0 -> {
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.motionX = 0.0
                    MovementUtils.strafe()
                    mc.thePlayer.jump()
                }

                1, 2, 3, 4, 5, 6, 7, 8, 9 -> {
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.motionX = 0.0
                }
                10, 11, 13, 14, 15 -> {
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.motionX = 0.0
                }

                16 -> {
                    mc.thePlayer.jump()
                    stepping = false
                }
            }
        }
    }
}