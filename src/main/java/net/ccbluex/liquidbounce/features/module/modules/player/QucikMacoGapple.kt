//package net.ccbluex.liquidbounce.features.module.modules.player
//
//import net.ccbluex.liquidbounce.event.*
//import net.ccbluex.liquidbounce.features.module.Module
//import net.ccbluex.liquidbounce.features.module.ModuleCategory
//import net.ccbluex.liquidbounce.features.module.ModuleInfo
//import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
//import net.ccbluex.liquidbounce.features.value.BoolValue
//import net.ccbluex.liquidbounce.features.value.IntegerValue
//import net.ccbluex.liquidbounce.features.value.ListValue
//import net.ccbluex.liquidbounce.script.api.global.Chat
//import net.ccbluex.liquidbounce.ui.font.Fonts
//import net.ccbluex.liquidbounce.utils.PacketUtils
//import net.ccbluex.liquidbounce.utils.render.RoundedUtil
//import net.minecraft.item.ItemAppleGold
//import net.minecraft.item.ItemStack
//import net.minecraft.network.Packet
//import net.minecraft.network.play.INetHandlerPlayServer
//import net.minecraft.network.play.client.*
//import net.minecraft.network.play.server.S12PacketEntityVelocity
//import java.awt.Color
//import java.util.concurrent.LinkedBlockingQueue
//import java.util.function.Consumer
//import kotlin.math.roundToInt
//
//@ModuleInfo("QucikGapple", category = ModuleCategory.PLAYER)
//class QucikMacoGapple : Module() {
//    private val packet = IntegerValue("Packet",32,30,36)
//    private val speed = IntegerValue("Speed", 3, 0, 5)
//
//    private val mode = ListValue("Mode", arrayOf("Stuck","Move"),"Stuck")
//
//    private val autoDis = BoolValue("AutoDis", true)
//
//    private val packets = LinkedBlockingQueue<Packet<*>>()
//    var i: Int = 0
//    private var tick = 0
//    private var isS12: Boolean = false
//
//    @EventTarget
//    override fun onDisable() {
//        if (!packets.isEmpty()) {
//            packets.forEach(Consumer { packet1: Packet<*>? ->
//                if (packet1 !is C01PacketChatMessage) PacketUtils.sendPacketNoEvent(
//                    packet1 as Packet<INetHandlerPlayServer>
//                )
//            })
//        }
//    }
//
//    @EventTarget
//    override fun onEnable() {
//        packets.clear()
//
//        isS12 = false
//        tick = 0
//
//        i = 0
//    }
//
//    @EventTarget
//    private fun onRender(eventRender2D: Render2DEvent) {
//        val sr = eventRender2D.scaledResolution
//
//        val width = 50f
//        val x = sr.scaledWidth / 2f - width / 2
//        val y = sr.scaledHeight / 2f + 12
//
//            RoundedUtil.drawRound(x - 120, y - 20, width + 80, 25f, 0f, Color(0, 0, 0, 125))
//        Fonts.minecraftFont.drawString(
//                "[C03Player] Size: " + this.i,
//            (x - 105).roundToInt(),
//            (y - 16).roundToInt(),
//                Color(
//                    255,
//                    255,
//                    255,
//                    255
//                ).rgb
//            )
//            RoundedUtil.drawRound(x - 105, y - 5, (packet.value * 3).toFloat(), 5f, 1f, Color(100, 100, 100))
//            RoundedUtil.drawRound(
//                x - 105,
//                y - 5,
//                (this.i * 3).toFloat(),
//                5f,
//                1f,
//                Color(
//                    255,
//                    255,
//                    255,
//                    255
//                )
//            )
//        }
//    @EventTarget
//    private fun onPacket(event: PacketEvent) {
//        val packet = event.packet
//
//
//        if (packet is C01PacketChatMessage || packet is C0EPacketClickWindow || packet is C07PacketPlayerDigging || packet is C08PacketPlayerBlockPlacement || packet is C09PacketHeldItemChange) {
//            event.cancelEvent()
//        }
//
//        if (packet is C03PacketPlayer) {
//            i++
//        }
//
//        if (event.eventType == EventState.RECEIVE) {
//            if (packet !is C08PacketPlayerBlockPlacement && packet !is C0DPacketCloseWindow && packet !is C0EPacketClickWindow && packet !is C09PacketHeldItemChange && packet !is C01PacketChatMessage && packet !is C07PacketPlayerDigging && packet !is C0APacketAnimation) {
//                packets.add(packet)
//                event.cancelEvent()
//            }
//        }
//
//        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer.entityId) {
//            isS12 = true
//        }
//    }
//
//    @EventTarget
//    private fun onMotion(eventMotion: MotionEvent) {
//        if (eventMotion.eventState == EventState.PRE) {
//            if (speed.value != 5) {
//                if (mc.thePlayer.ticksExisted % speed.value == 0) {
//                    while (!packets.isEmpty()) {
//                        val packet = packets.poll()
//
//                        if (packet is C01PacketChatMessage) {
//                            break
//                        }
//
//                        if (packet is C03PacketPlayer) {
//                            i--
//                        }
//
//                        PacketUtils.sendPacketNoEvent(packet as Packet<INetHandlerPlayServer>)
//                    }
//                }
//            }
//        }
//        if (eventMotion.eventState == EventState.POST) {
//            packets.add(C01PacketChatMessage("cnm"))
//        }
//    }
//
//    @EventTarget
//    private fun onTick(eventTick: TickEvent) {
//        if (speed.value == 5) {
//            if (!packets.isEmpty()) {
//                val packet = packets.poll()
//
//                if (packet is C03PacketPlayer) {
//                    i--
//                }
//
//                if (packet !is C01PacketChatMessage) {
//                    PacketUtils.sendPacketNoEvent(packet as Packet<INetHandlerPlayServer>)
//                }
//            }
//        }
//
//        if (appleGold > 0) {
//            tick = 0
//            if (i > packet.value) {
//                PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(appleGold))
//                PacketUtils.sendPacketNoEvent(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem))
//
//                while (!packets.isEmpty()) {
//                    val packet = packets.poll()
//
//                    if (packet is C01PacketChatMessage) continue
//
//                    PacketUtils.sendPacketNoEvent(packet as Packet<INetHandlerPlayServer>)
//                }
//
//                PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
//                if (autoDis.value || KillAura.currentTarget == null) {
//                    state = false
//                } else {
//                    i = 0
//                }
//            }
//        } else {
//            tick++
//
//            if (tick == 8) {
//                Chat.print("背包中未找到任何金苹果喵~")
//                state = false
//            }
//        }
//    }
//
//    private val appleGold: Int
//        get() {
//            for (i in 0..8) {
//                val stack = mc.thePlayer.inventoryContainer.getSlot(i + 36).stack
//                if (!mc.thePlayer.inventoryContainer.getSlot(i + 36).hasStack || mc.thePlayer.inventoryContainer.getSlot(i + 36).stack.item !is ItemAppleGold || !isGoldenApple(
//                        stack
//                    )
//                ) continue
//                return i
//            }
//            return -1
//        }
//
//    private fun isGoldenApple(stack: ItemStack): Boolean {
//        return if (isEnchantedGoldenApple(stack)) isEnchantedGoldenApple(stack) else stack.item is ItemAppleGold
//    }
//
//    private fun isEnchantedGoldenApple(stack: ItemStack): Boolean {
//        if (stack.item is ItemAppleGold) {
//            return hasEffect(stack)
//        }
//        return false
//    }
//
//    private fun hasEffect(p_hasEffect_1_: ItemStack): Boolean {
//        return p_hasEffect_1_.metadata > 0
//    }
//
//}