package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.intave

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.script.api.global.Chat

class IntaveTimerVelocity : VelocityMode("IntaveTimer") {
    private val OnAir = BoolValue("OnAir", true)
    private val Low = FloatValue("LowTimer", 0.1F, 0.05F, 1F)
    private val LowTimerTicks = IntegerValue("LowTimerTicks", 1, 1, 20)
    private val Max = FloatValue("MaxTimer", 2F, 1F, 5F)
    private val MaxTimerTicks = IntegerValue("MaxTimerTicks", 1, 1, 20)
    var jump = false
    var lowticks = 0
    var maxticks = 0
    var cancel = false
    override fun onDisable() {
        cancel = false
        maxticks= 0
        lowticks=0
        mc.timer.timerSpeed = 1F
        jump = false
    }

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.hurtTime > 0) {
            cancel = true
            when {
                lowticks <= LowTimerTicks.get() -> {
                    if (velocity.debug.get()) {
                        Chat.alert("LowTimer")
                    }
                    lowticks++
                    mc.timer.timerSpeed = Low.get()
                }
                maxticks <= MaxTimerTicks.get() -> {
                    maxticks++
                    if (OnAir.get() && mc.thePlayer.onGround) {
                        if (velocity.debug.get()) {
                            Chat.alert("MaxTimer")
                        }
                        mc.timer.timerSpeed = Max.get()
                    } else if (!OnAir.get()) {
                        if (velocity.debug.get()) {
                            Chat.alert("MaxTimer")
                        }
                        mc.timer.timerSpeed = Max.get()
                    }
                }
                else -> {
                    lowticks = 0
                    maxticks = 0
                }
            }
        } else {
            cancel = false
            mc.timer.timerSpeed = 1F
        }
    }
}