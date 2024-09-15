//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.ccbluex.liquidbounce.memoryfix;

import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(
        modid = "memoryfix",
        useMetadata = true,
        acceptedMinecraftVersions = "[1.8.9]"
)
public class MemoryFix {
    private int messageDelay = 0;
    private IChatComponent updateMessage;

    public MemoryFix() {
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        String updateUrl = System.getProperty("net.ccbluex.liquidbounce.memoryfix.updateurl", "https://mods.purple.services/update/check/MemoryFix/0.3");
        UpdateChecker updater = new UpdateChecker(updateUrl, (res) -> {
            this.updateMessage = res.getUpdateMessage();
        });
        updater.start();
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (this.updateMessage != null && Minecraft.getMinecraft().thePlayer != null && ++this.messageDelay == 80) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(this.updateMessage);
            this.updateMessage = null;
        }

    }
}