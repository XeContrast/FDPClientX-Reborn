package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.RawMouseHelper
import net.java.games.input.Controller
import net.java.games.input.ControllerEnvironment
import net.java.games.input.Mouse
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.event.FMLInitializationEvent

@ModuleInfo("RawInput", category = ModuleCategory.CLIENT)
object RawInput : Module() {
    var mouse: Mouse? = null
    private lateinit var controllers: Array<Controller>
    var dx: Int = 0
    var dy: Int = 0

    fun init(event: FMLInitializationEvent?) {
        Minecraft.getMinecraft().mouseHelper = RawMouseHelper()
        controllers = ControllerEnvironment.getDefaultEnvironment().controllers

        val inputThread = Thread {
            while (true) {
                var i = 0
                while (i < controllers.size && mouse == null) {
                    if (controllers[i].type === Controller.Type.MOUSE) {
                        controllers[i].poll()
                        if ((controllers[i] as Mouse).x.pollData.toDouble() != 0.0 || (controllers[i] as Mouse).y.pollData.toDouble() != 0.0) mouse =
                            controllers[i] as Mouse
                    }
                    i++
                }
                if (mouse != null) {
                    mouse!!.poll()

                    dx += mouse!!.x.pollData.toInt()
                    dy += mouse!!.y.pollData.toInt()
                }
                try {
                    Thread.sleep(1)
                } catch (e: InterruptedException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }
        }
        inputThread.name = "inputThread"
        inputThread.start()
    }
}