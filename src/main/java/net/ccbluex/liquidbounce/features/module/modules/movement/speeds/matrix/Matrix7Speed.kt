package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.settings.GameSettings

class Matrix7Speed : SpeedMode("Matrix7") {

    override fun onDisable() {
        mc.thePlayer.speedInAir = 0.02f
    }

    @EventTarget
    override fun onUpdate() {
        mc.thePlayer.motionY -= 0.00348
        mc.thePlayer.jumpMovementFactor = 0.026f
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        if (MovementUtils.isMoving) {
            if (mc.thePlayer.onGround) {
                mc.gameSettings.keyBindJump.pressed = false
                mc.thePlayer.jump()
                MovementUtils.strafe()
            } else {
                if (MovementUtils.getSpeed() < 0.19f) {
                    MovementUtils.strafe()
                }
            }
            if (mc.thePlayer.fallDistance <= 0.4 && mc.thePlayer.moveStrafing == 0f) {
                mc.thePlayer.speedInAir = 0.02035f
            } else {
                mc.thePlayer.speedInAir = 0.02f
            }
        }
    }
}