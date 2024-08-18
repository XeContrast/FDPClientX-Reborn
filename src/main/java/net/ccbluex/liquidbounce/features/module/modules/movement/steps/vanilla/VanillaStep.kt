package net.ccbluex.liquidbounce.features.module.modules.movement.steps.vanilla

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Step
import net.ccbluex.liquidbounce.features.module.modules.movement.steps.StepMode

class VanillaStep : StepMode("Vanilla") {
    override fun onUpdate(event: UpdateEvent) {
        Step.off = false
        Step.doncheck = false
    }
}