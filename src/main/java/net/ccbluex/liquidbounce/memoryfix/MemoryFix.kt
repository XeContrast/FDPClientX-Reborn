//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package net.ccbluex.liquidbounce.memoryfix

import net.ccbluex.liquidbounce.memoryfix.UpdateChecker.UpdateResponse
import net.minecraft.client.Minecraft
import net.minecraft.util.IChatComponent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@Mod(modid = "memoryfix", useMetadata = true, acceptedMinecraftVersions = "[1.8.9]")
class MemoryFix {
    private var messageDelay = 0
    private var updateMessage: IChatComponent? = null

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent?) {
        MinecraftForge.EVENT_BUS.register(this)
        val updateUrl = System.getProperty(
            "net.ccbluex.liquidbounce.memoryfix.updateurl",
            "https://mods.purple.services/update/check/MemoryFix/0.3"
        )
        val updater = UpdateChecker(updateUrl) { res: UpdateResponse ->
            this.updateMessage = res.updateMessage
        }
        updater.start()
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent?) {
        if (this.updateMessage != null && (Minecraft.getMinecraft().thePlayer != null) && (++this.messageDelay == 80)) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(this.updateMessage)
            this.updateMessage = null
        }
    }
}