/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.handler.network

import io.netty.buffer.Unpooled
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.client.ClientSpoof
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minecraft.network.play.server.S3FPacketCustomPayload
import java.util.*

class ClientSpoof : MinecraftInstance(), Listenable {
    @EventTarget
    fun handle(event: PacketEvent) {
        val packet = event.packet
        val clientSpoof = FDPClient.moduleManager.getModule(
            ClientSpoof::class.java
        )

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning && Objects.requireNonNull<ClientSpoof?>(
                clientSpoof
            ).modeValue.equals("Vanilla")
        ) {
            try {
                if (packet.javaClass.name == "net.minecraftforge.fml.common.network.internal.FMLProxyPacket") event.cancelEvent()

                if (packet is C17PacketCustomPayload) {

                    if (!packet.channelName.startsWith("MC|")) event.cancelEvent()
                    else if (packet.channelName.equals("MC|Brand", ignoreCase = true)) packet.data =
                        (PacketBuffer(Unpooled.buffer()).writeString("vanilla"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning && Objects.requireNonNull<ClientSpoof?>(
                clientSpoof
            ).modeValue.equals("LabyMod")
        ) {
            try {
                if (packet.javaClass.name == "net.minecraftforge.fml.common.network.internal.FMLProxyPacket") event.cancelEvent()

                if (packet is S3FPacketCustomPayload) {
                    if (packet.channelName == "REGISTER") {
                        mc.netHandler.addToSendQueue(
                            C17PacketCustomPayload(
                                "labymod3:main",
                                info
                            )
                        )
                        mc.netHandler.addToSendQueue(
                            C17PacketCustomPayload(
                                "LMC",
                                info
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning && Objects.requireNonNull<ClientSpoof?>(
                clientSpoof
            ).modeValue.equals("CheatBreaker")
        ) {
            try {
                if (packet.javaClass.name == "net.minecraftforge.fml.common.network.internal.FMLProxyPacket") event.cancelEvent()

                if (packet is C17PacketCustomPayload) {

                    if (!packet.channelName.startsWith("MC|")) event.cancelEvent()
                    else if (packet.channelName.equals("MC|Brand", ignoreCase = true)) packet.data =
                        (PacketBuffer(Unpooled.buffer()).writeString("CB"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (enabled && !Minecraft.getMinecraft().isIntegratedServerRunning && Objects.requireNonNull<ClientSpoof?>(
                clientSpoof
            ).modeValue.equals("PvPLounge")
        ) {
            try {
                if (packet.javaClass.name == "net.minecraftforge.fml.common.network.internal.FMLProxyPacket") event.cancelEvent()

                if (packet is C17PacketCustomPayload) {

                    if (!packet.channelName.startsWith("MC|")) event.cancelEvent()
                    else if (packet.channelName.equals("MC|Brand", ignoreCase = true)) packet.data =
                        (PacketBuffer(Unpooled.buffer()).writeString("PLC18"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val info: PacketBuffer
        get() = PacketBuffer(Unpooled.buffer())
            .writeString("INFO")
            .writeString(
                """{  
   "version": "3.9.25",
   "ccp": {  
      "enabled": true,
      "version": 2
   },
   "shadow":{  
      "enabled": true,
      "version": 1
   },
   "addons": [  
      {  
         "uuid": "null",
         "name": "null"
      }
   ],
   "mods": [
      {  
         "hash":"sha256:null",
         "name":"null.jar"
      }
   ]
}"""
            )

    override fun handleEvents(): Boolean {
        return true
    }

    companion object {
        const val enabled: Boolean = true
    }
}
