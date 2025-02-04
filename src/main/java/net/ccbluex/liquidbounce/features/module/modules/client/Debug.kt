package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.extensions.prevRotation
import net.ccbluex.liquidbounce.extensions.rotation
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.utils.RotationUtils.Companion.targetRotation

@ModuleInfo("Debug", category = ModuleCategory.CLIENT)
class Debug : Module() {
    private val turnSpeed by BoolValue("TrunSpeedShow",false)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (turnSpeed) {
            val player = mc.thePlayer ?: return
//            val yawDifference = (player.rotation.yaw - player.prevRotation.yaw) / 0.05
//            val pitchDifference = (player.rotation.pitch - player.prevRotation.pitch) / 0.05
            val rotation = targetRotation ?: player.rotation

//            Chat.print("Yaw: $yawDifference, Pitch: $pitchDifference")
        }
    }
}