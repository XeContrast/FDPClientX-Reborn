package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.timer.TimerUtils
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.login.client.C00PacketLoginStart
import net.minecraft.network.login.client.C01PacketEncryptionResponse
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer

object BlinkComponent : Listenable {
    @EventTarget(priority = -1)
    fun onPacketSend(event: PacketEvent) {
        if (MinecraftInstance.mc.thePlayer == null) {
            packets.clear()
            exemptedPackets.clear()
            return
        }

        if (event.eventType == EventState.SEND) {
            if (MinecraftInstance.mc.thePlayer.isDead || MinecraftInstance.mc.isSingleplayer || !MinecraftInstance.mc.netHandler.doneLoadingTerrain) {
                packets.forEach(PacketUtils::sendPacket)
                packets.clear()
                blinking = false
                exemptedPackets.clear()
                return
            }

            val packet = event.packet

            if (packet is C00Handshake || packet is C00PacketLoginStart ||
                packet is C00PacketServerQuery || packet is C01PacketPing ||
                packet is C01PacketEncryptionResponse || packet is C00PacketKeepAlive
            ) {
                return
            }

            if (blinking) {
                if (!event.isCancelled && exemptedPackets.stream()
                        .noneMatch { packetClass: Class<*> -> packetClass == packet.javaClass }
                ) {
                    packets.add(packet)
                    event.isCancelled = true
                }
            }
        }
    }

    @EventTarget(priority = -1)
    fun onWorld(event: WorldEvent?) {
        packets.clear()
        blinking = false
    }

    override fun handleEvents(): Boolean {
        return true
    }

    @JvmField
    val packets: ConcurrentLinkedQueue<Packet<*>> = ConcurrentLinkedQueue()

    @JvmField
    var blinking: Boolean = false
    var exemptedPackets: ArrayList<Class<*>> = ArrayList()
    var exemptionWatch: TimerUtils = TimerUtils()

    @JvmStatic
    fun setExempt(vararg packets: Class<*>?) {
        exemptedPackets = ArrayList(listOf(*packets))
        exemptionWatch.reset()
    }

    @JvmOverloads
    @JvmStatic
    fun dispatch(clear: Boolean = true) {
        blinking = false
        if (!packets.isEmpty()) {
            packets.forEach(PacketUtils::sendPacket)
            if (clear) {
                packets.clear()
                exemptedPackets.clear()
            }
        }
    }
}
