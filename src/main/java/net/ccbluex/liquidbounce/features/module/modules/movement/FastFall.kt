package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "FastFall", category = ModuleCategory.MOVEMENT)
class FastFall : Module() {
    private var idk = 0
    private var freeze = false

    private val listValue = ListValue("Mode", arrayOf("Normal", "Polar"), "Normal")
    private val maxFallDistance = FloatValue("MaxFallDistance", 3f, 0f, 10f)
    private var freezeValue = FloatValue("FreezeTick", 10f, 6f, 50f).displayable {listValue.equals("Polar")}

    override fun onEnable() {
        idk = 0
        freeze = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (listValue.equals("Normal")) {
            if (mc.thePlayer.fallDistance >= maxFallDistance.get()) {
                mc.thePlayer.motionY -= 5.0
            }
        } else {
            if (mc.thePlayer.fallDistance >= maxFallDistance.get()) {
                if (idk == 0) {
                    idk = freezeValue.get().toInt()
                }
            }
            if (idk > 0){
                idk -= 1
                freeze = idk > 5
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (listValue.equals("Polar")) {
            if (freeze) {
                if (packet is C03PacketPlayer) {
                    packet.y += mc.thePlayer.posY + 0.01
                }
            }
        }
    }
    @EventTarget
    fun onMove(event: MoveEvent) {
        if (freeze) {
            event.cancelEvent()
        }
    }
}