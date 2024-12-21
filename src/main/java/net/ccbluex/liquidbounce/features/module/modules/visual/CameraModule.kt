package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue

@ModuleInfo("CameraModule", category = ModuleCategory.VISUAL)
object CameraModule : Module() {
    val cameraclip = BoolValue("CameraClip",false)
    val nobob = BoolValue("NoBob",false)
    val nofov = BoolValue("NoFov",false)
    val nofovValue = FloatValue("FOV",1f,0f,1.5f).displayable { nofov.get() }
    val motionCamera: BoolValue = BoolValue("Motion", false)
    val interpolation = FloatValue("MotionInterpolation", 0.15f, 0.05f, 0.5f).displayable { motionCamera.get() }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (nobob.get()) mc.thePlayer.distanceWalkedModified = 0f
    }
}