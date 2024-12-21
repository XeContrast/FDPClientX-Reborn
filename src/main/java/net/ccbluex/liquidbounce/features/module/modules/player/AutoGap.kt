package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.utils.animations.ContinualAnimation
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.BlinkComponent
import net.ccbluex.liquidbounce.utils.BlinkComponent.dispatch
import net.ccbluex.liquidbounce.utils.BlinkComponent.setExempt
import net.ccbluex.liquidbounce.utils.InventoryUtils.findItem
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RoundedUtil
import net.ccbluex.liquidbounce.utils.timer.TimerUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.init.Items
import net.minecraft.network.play.client.*
import java.text.DecimalFormat
import kotlin.math.min

@ModuleInfo(name = "AutoGap", category = ModuleCategory.PLAYER)
class AutoGap : Module() {
    val mode: ListValue = ListValue("Mode", arrayOf("Dev"), "Dev")
    val health: FloatValue = FloatValue("Health", 15f, 1f, 20f)
    val delay: FloatValue = FloatValue("Delay", 75f, 0f, 300f)
    private val timer = TimerUtils()
    @JvmField
    var eating: Boolean = false
    private var movingPackets = 0
    private var slot = 0
    val animations = ContinualAnimation()

    override fun onEnable() {
        movingPackets = 0
        slot = -1
        eating = false
    }

    override fun onDisable() {
        eating = false
        dispatch()
        movingPackets = 0
    }

    @EventTarget
    fun onWorld(event: WorldEvent?) {
        eating = false
        movingPackets = 0
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (mode.get() == "Dev") {
            if (event.eventState == EventState.POST && eating) {
                movingPackets++
            }

            if (event.eventState == EventState.PRE) {
                if (mc.thePlayer == null || !mc.thePlayer.isEntityAlive) {
                    eating = false
                    dispatch()
                    movingPackets = 0

                    return
                }

                if (!mc.playerController.getCurrentGameType().isSurvivalOrAdventure || !timer.hasTimeElapsed(delay.get())) {
                    eating = false
                    dispatch()
                    movingPackets = 0

                    return
                }

                slot = findItem(36, 45, Items.golden_apple) - 36
                if (slot == -1 || mc.thePlayer.health >= health.get()) {
                    if (eating) {
                        eating = false
                        dispatch()
                        movingPackets = 0
                    }
                } else {
                    eating = true
                    setExempt(
                        C0EPacketClickWindow::class.java,
                        C16PacketClientStatus::class.java,
                        C0DPacketCloseWindow::class.java,
                        C09PacketHeldItemChange::class.java
                    )
                    BlinkComponent.blinking = true
                    if (movingPackets >= 32) {
                        sendPacket(C09PacketHeldItemChange(slot), true)
                        sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.heldItem), false)
                        mc.thePlayer.itemInUseCount -= 32
                        dispatch()
                        movingPackets = 0
                        sendPacket(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem), false)
                        timer.reset()
                    } else if (mc.thePlayer.ticksExisted % 3 == 0) {
                        while (!BlinkComponent.packets.isEmpty()) {
                            val packet = BlinkComponent.packets.poll()

                            if (packet is C03PacketPlayer) {
                                movingPackets--
                            }

                            dispatch()
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onMoveMath(event: MoveMathEvent) {
        if (eating) event.isCancelled = true
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        if (mode.get() == "Dev") {
            val resolution = ScaledResolution(mc)
            val x = resolution.scaledWidth / 2
            val y = resolution.scaledHeight - 75
            val thickness = 5f

            val percentage = (min(movingPackets.toDouble(), 32.0) / 32f).toFloat()

            val width = 100
            val half = width / 2
            animations.animate((width - 2) * percentage, 40)

            RoundedUtil.drawRound(
                (x - half - 1).toFloat(),
                (y - 1 - 12).toFloat(),
                (width + 1).toFloat(),
                ((thickness + 1).toInt() + 12 + 3).toFloat(),
                2f,
                rainbow()
            )
            RoundedUtil.drawRound(
                (x - half - 1).toFloat(),
                (y - 1).toFloat(),
                (width + 1).toFloat(),
                (thickness + 1).toInt().toFloat(),
                2f,
                rainbow()
            )

            RoundedUtil.drawGradientHorizontal(
                (x - half).toFloat(),
                (y + 1).toFloat(),
                animations.output,
                thickness,
                2f,
                rainbow(),
                rainbow(90)
            )

            Fonts.font35.drawCenteredString("Time", x.toFloat(), (y - 1 - 11 + 3).toFloat(), -1)

            Fonts.font35.drawCenteredString(
                DecimalFormat("0.0").format((percentage * 100).toDouble()) + "%",
                x.toFloat(),
                (y + 2).toFloat(),
                -1
            )
        }
    }
}
