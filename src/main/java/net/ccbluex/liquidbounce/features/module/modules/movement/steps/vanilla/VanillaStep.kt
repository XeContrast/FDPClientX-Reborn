package net.ccbluex.liquidbounce.features.module.modules.movement.steps.vanilla

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.StepTest
import net.ccbluex.liquidbounce.features.module.modules.movement.steps.StepMode

class VanillaStep : StepMode("Vanilla") {
    override fun onUpdate(event: UpdateEvent) {
        StepTest.off = false
    }
}