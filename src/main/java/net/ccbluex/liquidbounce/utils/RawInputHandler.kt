package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EventTarget
import net.java.games.input.Controller
import net.java.games.input.DirectAndRawInputEnvironmentPlugin
import net.java.games.input.Mouse
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import net.minecraft.util.MouseHelper
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.apache.commons.lang3.ArrayUtils

class RawInputHandler {
    var shouldGetMouse: Boolean = false

    private val worldJoinTimer = 0

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

    companion object {
        var controllers: Array<Controller> = TODO()
        var mouseControllers: Array<Controller>? = null
        var mouse: Mouse? = null
        var dx: Int = 0
        var dy: Int = 0
        fun init() {
            startInputThread()
        }

        fun startInputThread() {
            val inputThread = Thread {
                while (true) {
                    if (mouse != null && Minecraft.getMinecraft().currentScreen == null) {
                        mouse!!.poll()
                        dx += mouse!!.x.pollData.toInt()
                        dy += mouse!!.y.pollData.toInt()
                    } else if (mouse != null) {
                        mouse!!.poll()
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

        fun getMouse() {
            val getMouseThread = Thread {
                val directEnv = DirectAndRawInputEnvironmentPlugin()
                controllers = directEnv.controllers

                mouseControllers = null
                mouse = null

                for (i in controllers) {
                    if (i.type === Controller.Type.MOUSE) {
                        mouseControllers =
                            ArrayUtils.add(
                                mouseControllers,
                                i
                            )
                    }
                }

                // Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Move your mouse"));
                while (mouse == null) {
                    for (i in mouseControllers!!) {
                        i.poll()
                        val mouseX = (i as Mouse).x.pollData

                        if (mouseX > 0.1f || mouseX < -0.1f) {
                            mouse = i
                            // Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Mouse Found"));
                        }
                    }
                }
            }
            getMouseThread.name = "getMouseThread"
            getMouseThread.start()
        }

        fun toggleRawInput() {
            val player = Minecraft.getMinecraft().thePlayer
            val saveYaw = player.rotationYaw
            val savePitch = player.rotationPitch

            if (Minecraft.getMinecraft().mouseHelper is RawMouseHelper) {
                Minecraft.getMinecraft().mouseHelper = MouseHelper()
                Minecraft.getMinecraft().mouseHelper.grabMouseCursor()
                Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("Toggled OFF"))
            } else {
                Minecraft.getMinecraft().mouseHelper = RawMouseHelper()
                Minecraft.getMinecraft().mouseHelper.grabMouseCursor()
                Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("Toggled ON"))
            }
            player.rotationYaw = saveYaw
            player.rotationPitch = savePitch
        }
    }
}