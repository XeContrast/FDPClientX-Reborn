package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.intave

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.Velocity
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity

class IntaveVelocity : VelocityMode("Intave") {
    private val flagCheck = BoolValue("FlagCheck",false)
    private val intavejump = BoolValue("IntaveJump",false)
    private var work = false
    private var idk = false


    @EventTarget
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook && flagCheck.get()) {
            if (velocity.debug.get()) {
                Chat.alert("Return")
            }
            return
        }
        if (packet is S12PacketEntityVelocity) {
            if (packet.entityID == mc.thePlayer.entityId) {
                work = true
                idk = true
            }
        }
    }

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        if (intavejump.get()) {
            if (mc.thePlayer.hurtTime == 9 && mc.thePlayer.onGround) {
                if (velocity.debug.get()) { Chat.alert("Jump") }
                mc.thePlayer.jump()
            }
        }
    }

    @EventTarget
    override fun onAttack(event: AttackEvent) {
        if (work) {
            if (mc.objectMouseOver.entityHit != null) {
                if (mc.thePlayer.hurtTime > 0 && idk) {
                    mc.thePlayer.isSprinting = false
                    if (mc.thePlayer.isSwingInProgress) {
                        if (Velocity.debug.get()) {
                            Chat.alert("Motion *= 0.6")
                        }
                        mc.thePlayer.motionX *= 0.6
                        mc.thePlayer.motionZ *= 0.6
                    }
                    idk = false
                }
            }
            work = false
        }
    }

    override fun onDisable() {
        idk = false
        work = false
    }
}