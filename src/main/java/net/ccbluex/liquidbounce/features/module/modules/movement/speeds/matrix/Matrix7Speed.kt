package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings

class Matrix7Speed : SpeedMode("Matrix7") {
    private val timer = BoolValue("GroundTimer",false)

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }
    @EventTarget
    override fun onUpdate() {
        mc.thePlayer.motionY -= 0.00348
        mc.thePlayer.jumpMovementFactor = 0.026f
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (mc.thePlayer.onGround) {
            mc.gameSettings.keyBindJump.pressed = false
            if (MovementUtils.isMoving()) {
                mc.thePlayer.jump()
                if (timer.get()) {
                    mc.timer.timerSpeed = 1.35f
                }
                MovementUtils.strafe()
            }
        } else {
            if (mc.timer.timerSpeed != 1f) {
                mc.timer.timerSpeed = 1f
            }
            if (MovementUtils.getSpeed() < 0.19f) {
                MovementUtils.strafe()
            }
        }
    }
}