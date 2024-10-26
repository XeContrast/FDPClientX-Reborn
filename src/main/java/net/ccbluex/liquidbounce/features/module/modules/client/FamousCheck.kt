package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.script.api.global.Chat

@ModuleInfo("FamousCheck","Check Famous Hacker", category = ModuleCategory.CLIENT)
class FamousCheck : Module() {
    private val sendMode = ListValue("ListMode", arrayOf("SendChat","Alert"),"SendChat")

    private val sentMessages = mutableSetOf<String>()

    @EventTarget
    private fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        val hackerList = listOf(
            //Hacker 包含词
            HackerData("A_Equal","A_Equal"),
            HackerData("AKQ33","A_Equal"),
            HackerData("Holiday_","Holiday"),
            HackerData("Xe_","Xe及他的儿子"),
            HackerData("longyan","LongYan"),
            HackerData("longsir","WanFan"),
            HackerData("RealLonbg","WanFan"),
            HackerData("wansir","WanFan"),
            HackerData("Float","FloatMemory_Official"),
            HackerData("HuHua","沭桦Next"),
            HackerData("Rem_","沭桦Next"),
            HackerData("ImSad_","BoySir_"),
            HackerData("RN_","Random_Name"),

            )

        world.playerEntities.forEach { other ->
            if (other != mc.thePlayer) {
                val name = other.gameProfile.name

                hackerList.forEach { checker ->
                    if (checker.check(name) && !sentMessages.contains(other.gameProfile.name.toString())) {
                        sentMessages.add(other.gameProfile.name.toString())
                        when (sendMode.get().lowercase()) {
                            "sendchat" -> player.sendChatMessage("发现Hacker:${name}(${checker.message})")
                            "alert" -> Chat.alert("发现Hacker:${name}(${checker.message})")
                        }
                    }
                }
            }
        }
    }

    data class HackerData(
        val prefix: String,
        val message: String = "Null",
        val mode: Mode = Mode.Contains
    ) {
        fun check(string: String) : Boolean {
            return when (mode) {
                Mode.Contains -> {
                    string.contains(prefix,ignoreCase = true)
                }
                Mode.StartWith -> {
                    string.startsWith(prefix,ignoreCase = true)
                }
                Mode.Same -> {
                    string == prefix
                }
            }
        }
    }

    enum class Mode {
        Contains,
        StartWith,
        Same
    }

    @EventTarget
    override fun onDisable() {
        sentMessages.clear()
    }

    @EventTarget
    private fun onWorld(event: WorldEvent) {
        sentMessages.clear()
    }
}