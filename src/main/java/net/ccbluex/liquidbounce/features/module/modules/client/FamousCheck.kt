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
            HackerData("Equal","A_Equal"),
            HackerData("AKQ33","A_Equal"),
            HackerData("Holiday_","Holiday"),
            HackerData("MouCha","Holiday"),
            HackerData("Xe_","Xebook1(Owner)"),
            HackerData("longyan","LongYan"),
            HackerData("longsir","WanFan"),
            HackerData("RealLonbg","WanFan"),
            HackerData("wansir","WanFan"),
            HackerData("Float","FloatMemory_Official"),
            HackerData("HuHua","沭桦Next"),
            HackerData("Rem_","沭桦Next"),
            HackerData("ImSad_","BoySir_"),
            HackerData("RN_","Random_Name"),
            HackerData("DouSha","豆沙"),
            HackerData("Zekruin","蒸菜icu"),
            HackerData("nerock","河南猪"),
            HackerData("weipu","Weipu42"),

            HackerData("Yao_Mao"),
            HackerData("Bad_Smoke"),
            HackerData("tea_tea"),
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

                val playerInfoMap = mc.netHandler?.playerInfoMap ?: return

                val playerInfos = synchronized(playerInfoMap) {
                    playerInfoMap.mapNotNull { playerInfo ->
                        playerInfo?.gameProfile?.name?.let { playerName ->
                            playerName to playerInfo.responseTime
                        }
                    }
                }

                playerInfos.forEach { (player, _) ->
                    hackerList.forEach { checker ->
                        if (checker.check(player) && !sentMessages.contains(player) && player != mc.thePlayer.name) {
                            sentMessages.add(player)
                            when (sendMode.get().lowercase()) {
                                "sendchat" -> mc.thePlayer.sendChatMessage("发现Hacker:${player}(${checker.message})")
                                "alert" -> Chat.alert("发现Hacker:${player}(${checker.message})")
                            }
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
    fun onWorld(event: WorldEvent) {
        sentMessages.clear()
    }
}