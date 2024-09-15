package com.kuri0.rawinput

import com.kuri0.rawinput.RawInput
import net.java.games.input.Controller
import net.java.games.input.ControllerEnvironment
import net.java.games.input.Mouse
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.lang.reflect.Constructor
import java.util.*

@Mod(modid = RawInput.MODID, version = RawInput.VERSION)
class RawInput {
    private var hasSentWarningForSession = false

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent?) {
        // Abort mission if OS is not windows - Erymanthus / RayDeeUx
        if (!(System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows"))) {
            MinecraftForge.EVENT_BUS.register(UnintendedUsageWarnings())
            ClientCommandHandler.instance.registerCommand(OpenFileCommand())
            return
        }

        ClientCommandHandler.instance.registerCommand(RescanCommand())
        Minecraft.getMinecraft().mouseHelper = RawMouseHelper()

        val inputThread = Thread {
            var enviro: ControllerEnvironment? = null
            while (true) {
                if (enviro == null) {
                    try {
                        enviro = createDefaultEnvironment()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (mouse == null) {
                    try {
                        val controllers = enviro.controllers
                        for (controller in controllers) {
                            try {
                                if (controller.type === Controller.Type.MOUSE) {
                                    controller.poll()
                                    val px = (controller as Mouse).x.pollData
                                    val py = controller.y.pollData
                                    val eps = 0.1f

                                    // check if mouse is moving
                                    if (px < -eps || px > eps || py < -eps || py > eps) {
                                        mouse = controller
                                        Minecraft.getMinecraft().thePlayer.addChatMessage(
                                            ChatComponentText(
                                                EnumChatFormatting.GREEN.toString() + "[RawInput] Found mouse"
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                // skip to next
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    mouse!!.poll()
                    if (Minecraft.getMinecraft().currentScreen == null) {
                        dx += mouse!!.x.pollData.toInt()
                        dy += mouse!!.y.pollData.toInt()
                    }
                }
                try {
                    Thread.sleep(1)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        inputThread.name = "inputThread"
        inputThread.start()
    }

    @SubscribeEvent
    fun sendWarning(event: ClientTickEvent?) {
        if (hasSentWarningForSession || (System.getProperty("os.name")
                .lowercase(Locale.getDefault()).contains("windows")) || Minecraft.getMinecraft().thePlayer == null
        ) return
        Minecraft.getMinecraft().thePlayer.addChatMessage(
            ChatComponentText(
                EnumChatFormatting.AQUA.toString() + "[RawInput] You are currently on " + System.getProperty(
                    "os.name"
                ) + ". RawInput is designed exclusively for Windows players, so if you feel that this message was a mistake, please screenshot this message and ping Erymanthus#5074 in the SkyClient Discord server: https://inv.wtf/skyclient"
            )
        )
        //prevent sending warning more than once per session
        hasSentWarningForSession = true
    }

    companion object {
        const val MODID: String = "rawinput"
        const val VERSION: String = "1.1.5"

        @JvmField
        var mouse: Mouse? = null

        // Delta for mouse
        @JvmField
        var dx: Int = 0
        @JvmField
        var dy: Int = 0

        @Throws(ReflectiveOperationException::class)
        private fun createDefaultEnvironment(): ControllerEnvironment {    // Find constructor (class is package private, so we can't access it directly)
            val constructor =
                Class.forName("net.java.games.input.DefaultControllerEnvironment").declaredConstructors[0] as Constructor<ControllerEnvironment>

            // Constructor is package private, so we have to deactivate access control checks
            constructor.isAccessible = true
            // Create object with default constructor
            return constructor.newInstance()
        }
    }
}
