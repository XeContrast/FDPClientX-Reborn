/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.MovementUtils.strafe
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacketNoEvent
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.init.Items
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.awt.Color

@ModuleInfo(name = "BowJump", category = ModuleCategory.MOVEMENT)
class BowJump : Module() {
    private val hypixelBypassValue = BoolValue("hypixelBypass", true)
    private val modeValue = ListValue("BoostMode", arrayOf("Strafe", "SpeedInAir"), "Strafe")
    private val speedInAirBoostValue = FloatValue("SpeedInAir", 0.03f, 0.02f, 0.1f)
    private val boostValue = FloatValue("Boost", 4.25f, 0f, 10f)
    private val heightValue = FloatValue("Height", 0.42f, 0f, 10f)
    private val timerValue = FloatValue("Timer", 1f, 0.1f, 10f)
    private val delayBeforeLaunch = IntegerValue("DelayBeforeArrowLaunch", 1, 1, 20)

    var autodisable = BoolValue("AutoDisable",true)
    private val renderValue = BoolValue("RenderStatus", true)

    private var bowState = 0
    private var lastPlayerTick: Long = 0


    private var lastSlot = -1

    override fun onEnable() {
        if (mc.thePlayer == null) return
        bowState = 0
        lastPlayerTick = -1
        lastSlot = mc.thePlayer.inventory.currentItem

        strafe(0.0f)
        mc.thePlayer.onGround = false
        mc.thePlayer.jumpMovementFactor = 0.0f
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (mc.thePlayer.onGround && bowState < 3) event.cancelEvent()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is C09PacketHeldItemChange) {
            lastSlot = event.packet.slotId
            event.cancelEvent()
        }

        if (event.packet is C03PacketPlayer) {
            if (bowState < 3) event.packet.isMoving = false
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        mc.timer.timerSpeed = 1f

        var forceDisable = false
        when (bowState) {
            0 -> {
                val slot = bowSlot
                if (slot < 0 || !mc.thePlayer.inventory.hasItem(Items.arrow)) {
                    forceDisable = true
                    bowState = 5
                } else if (lastPlayerTick == -1L) {

                    if (lastSlot != slot) sendPacketNoEvent(C09PacketHeldItemChange(slot))
                    sendPacketNoEvent(
                        C08PacketPlayerBlockPlacement(
                            BlockPos(-1, -1, -1),
                            255,
                            mc.thePlayer.inventoryContainer.getSlot(slot + 36).stack,
                            0f,
                            0f,
                            0f
                        )
                    )

                    lastPlayerTick = mc.thePlayer.ticksExisted.toLong()
                    bowState = 1
                }
            }

            1 -> {
                val reSlot = bowSlot
                if (mc.thePlayer.ticksExisted - lastPlayerTick > delayBeforeLaunch.get()) {
                    sendPacketNoEvent(C05PacketPlayerLook(mc.thePlayer.rotationYaw, -90f, mc.thePlayer.onGround))
                    sendPacketNoEvent(
                        C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                            BlockPos.ORIGIN,
                            EnumFacing.DOWN
                        )
                    )

                    if (lastSlot != reSlot) sendPacketNoEvent(C09PacketHeldItemChange(lastSlot))
                    bowState = 2
                }
            }

            2 -> if (mc.thePlayer.hurtTime > 0) bowState = 3
            3 -> {
                if (hypixelBypassValue.get()) {
                    if (mc.thePlayer.hurtTime >= 8) {
                        mc.thePlayer.motionY = 0.58
                        mc.thePlayer.jump()
                    }
                    if (mc.thePlayer.hurtTime == 8) {
                        strafe(0.72f)
                    }

                    if (mc.thePlayer.hurtTime == 7) {
                        mc.thePlayer.motionY += 0.03
                    }

                    if (mc.thePlayer.hurtTime <= 6) {
                        mc.thePlayer.motionY += 0.015
                    }
                    mc.timer.timerSpeed = timerValue.get()
                    if (mc.thePlayer.onGround && mc.thePlayer.ticksExisted - lastPlayerTick >= 1) bowState = 5
                } else {
                    when (modeValue.get()) {
                        "Strafe" -> {
                            strafe(boostValue.get())
                        }

                        "SpeedInAir" -> {
                            mc.thePlayer.speedInAir = speedInAirBoostValue.value
                            mc.thePlayer.jump()
                        }
                    }
                    mc.thePlayer.motionY = heightValue.get().toDouble()
                    bowState = 4
                    lastPlayerTick = mc.thePlayer.ticksExisted.toLong()
                }
                mc.timer.timerSpeed = timerValue.get()
                if (mc.thePlayer.onGround && mc.thePlayer.ticksExisted - lastPlayerTick >= 1) bowState = 5
            }

            4 -> {
                mc.timer.timerSpeed = timerValue.get()
                if (mc.thePlayer.onGround && mc.thePlayer.ticksExisted - lastPlayerTick >= 1) bowState = 5
            }
        }
        if (bowState < 3) {
            mc.thePlayer.movementInput.moveForward = 0f
            mc.thePlayer.movementInput.moveStrafe = 0f
        }

        if (bowState == 5 && (autodisable.get() || forceDisable)) this.state = false
    }

    @EventTarget
    fun onWorld(event: WorldEvent?) {
        this.state = false //prevent weird things
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
        mc.thePlayer.speedInAir = 0.02f
    }

    private val bowSlot: Int
        get() {
            for (i in 36..44) {
                val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (stack != null && stack.item is ItemBow) {
                    return i - 36
                }
            }
            return -1
        }

    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        if (!renderValue.get()) return
        val scaledRes = ScaledResolution(mc)

        val width = bowState.toFloat() / 5f * 60f

        Fonts.font40.drawCenteredString(
            bowStatus,
            scaledRes.scaledWidth / 2f,
            scaledRes.scaledHeight / 2f + 14f,
            -1,
            true
        )
        RenderUtils.drawRect(
            scaledRes.scaledWidth / 2f - 31f,
            scaledRes.scaledHeight / 2f + 25f,
            scaledRes.scaledWidth / 2f + 31f,
            scaledRes.scaledHeight / 2f + 29f,
            -0x60000000
        )
        RenderUtils.drawRect(
            scaledRes.scaledWidth / 2f - 30f,
            scaledRes.scaledHeight / 2f + 26f,
            scaledRes.scaledWidth / 2f - 30f + width,
            scaledRes.scaledHeight / 2f + 28f,
            statusColor
        )
    }

    private val bowStatus: String
        get() = when (bowState) {
            0 -> "Idle..."
            1 -> "Preparing..."
            2 -> "Waiting for damage..."
            3, 4 -> "Boost!"
            else -> "Task completed."
        }

    private val statusColor: Color
        get() = when (bowState) {
            0 -> Color(21, 21, 21)
            1 -> Color(48, 48, 48)
            2 -> Color.yellow
            3, 4 -> Color.green
            else -> Color(0, 111, 255)
        }
}
