package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.command.rawinput.RescanCommand
import net.ccbluex.liquidbounce.features.command.rawinput.ToggleCommand
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.KeybindHandler
import net.ccbluex.liquidbounce.utils.RawInputHandler
import net.ccbluex.liquidbounce.utils.RawInputHandler.Companion.getMouse
import net.ccbluex.liquidbounce.utils.RawMouseHelper
import net.java.games.input.Controller
import net.java.games.input.Mouse
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly


@ModuleInfo("RawInput", category = ModuleCategory.CLIENT)
object RawInput : Module() {
    var mouse: Mouse? = null
    private lateinit var controllers: Array<Controller>
    var dx: Int = 0
    var dy: Int = 0

    fun init(event: FMLInitializationEvent?) {
        ClientCommandHandler.instance.registerCommand(RescanCommand())
        ClientCommandHandler.instance.registerCommand(ToggleCommand())
        Minecraft.getMinecraft().mouseHelper = RawMouseHelper()
        FMLCommonHandler.instance().bus().register(KeybindHandler())
        MinecraftForge.EVENT_BUS.register(RawInputHandler())

        RawInputHandler.init()
    }

    @EventTarget
    fun onWorldTick(event: TickEvent.ClientTickEvent?) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("yo"))
    }

    @EventTarget
    fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
        if (event.entity != null && event.entity is EntityPlayer
            && !(event.entity as EntityPlayer).entityWorld.isRemote
        ) {
            getMouse()
        }
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent?) {
        KeybindHandler.init()
    }
}