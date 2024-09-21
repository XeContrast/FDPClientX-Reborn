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
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "InvMove", category = ModuleCategory.MOVEMENT)
object InvMove : Module() {

    private val chest = BoolValue("Chest",true)
    private val inv = BoolValue("Inv",true)
    private val noDetectableValue = BoolValue("NoDetectable", false)
    private val bypassValue = ListValue("Bypass", arrayOf("NoOpenPacket", "Blink", "PacketInv","Jump","Intave", "None"), "None")
    private val rotateValue = BoolValue("Rotate", false)
    private val noMoveClicksValue = BoolValue("NoMoveClicks", false)
    val noSprintValue = ListValue("NoSprint", arrayOf("Real", "PacketSpoof", "None"), "None")

    private val blinkPacketList = mutableListOf<C03PacketPlayer>()
    private val packetListYes = mutableListOf<C0EPacketClickWindow>()
    private var lastInvOpen = false
    var invOpen = false
        private set
    
    private var isInv = false

    private fun updateKeyState() {
        if (mc.currentScreen != null && mc.currentScreen !is GuiChat && (!noDetectableValue.get() || mc.currentScreen !is GuiContainer)) {
            mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
            mc.gameSettings.keyBindBack.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindBack)
            mc.gameSettings.keyBindRight.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindRight)
            mc.gameSettings.keyBindLeft.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)
            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
            mc.gameSettings.keyBindSprint.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSprint)

            if (rotateValue.get()) {
                if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                    if (mc.thePlayer.rotationPitch > -90) {
                        mc.thePlayer.rotationPitch -= 5
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                    if (mc.thePlayer.rotationPitch < 90) {
                        mc.thePlayer.rotationPitch += 5
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                    mc.thePlayer.rotationYaw -= 5
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                    mc.thePlayer.rotationYaw += 5
                }
            }
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if ((!chest.get() && mc.currentScreen is GuiChest) || (!inv.get() && mc.currentScreen is GuiInventory)) return
        updateKeyState()
    }

    @EventTarget
    fun onScreen(event: ScreenEvent) {
        updateKeyState()
    }

    @EventTarget
    fun onClick(event: ClickWindowEvent) {
        if (noMoveClicksValue.get() && MovementUtils.isMoving()) {
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        lastInvOpen = invOpen
        if (packet is S2DPacketOpenWindow || (packet is C16PacketClientStatus && packet.status == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)) {
            invOpen = true
            if (noSprintValue.equals("PacketSpoof")) {
                if (mc.thePlayer.isSprinting) {
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                }
                if (mc.thePlayer.isSneaking) {
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
                }
            }
        }
        if (packet is S2EPacketCloseWindow || packet is C0DPacketCloseWindow) {
            invOpen = false
            if (bypassValue.get() == "Intave") mc.gameSettings.keyBindSneak.pressed = false
            if (noSprintValue.equals("PacketSpoof")) {
                if (mc.thePlayer.isSprinting) {
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                }
                if (mc.thePlayer.isSneaking) {
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING))
                }
            }
        }

        when (bypassValue.get().lowercase()) {
            "packetinv" -> {
                when (packet) {
                    is C16PacketClientStatus -> {
                        if (packet.status == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                            event.cancelEvent()
                            isInv = true
                        }
                    }
                    is C0DPacketCloseWindow -> {
                        event.cancelEvent()
                        isInv = false
                    }
                    is C0EPacketClickWindow -> {
                        if (isInv) return
                        packetListYes.clear()
                        packetListYes.add(packet)

                        event.cancelEvent()

                        PacketUtils.sendPacketNoEvent(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
                        packetListYes.forEach {
                            PacketUtils.sendPacketNoEvent(it)
                        }
                        packetListYes.clear()
                        PacketUtils.sendPacketNoEvent(C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId))
                    }
                }
            }
            "noopenpacket" -> {
                if (packet is C16PacketClientStatus && packet.status == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                    event.cancelEvent()
                }
            }
            "blink" -> {
                if (packet is C03PacketPlayer) {
                    if (lastInvOpen) {
                        blinkPacketList.add(packet)
                        event.cancelEvent()
                    } else if (blinkPacketList.isNotEmpty()) {
                        blinkPacketList.add(packet)
                        event.cancelEvent()
                        blinkPacketList.forEach {
                            PacketUtils.sendPacketNoEvent(it)
                        }
                        blinkPacketList.clear()
                    }
                }
            }
            "jump" -> {
                if (mc.currentScreen is GuiChest && mc.thePlayer.onGround && lastInvOpen) mc.thePlayer.jump()
            }
        }
    }

    @EventTarget
    private fun onStrafe(event: StrafeEvent) {
        if (lastInvOpen && bypassValue.get() == "Intave" && mc.currentScreen != null) {
            mc.gameSettings.keyBindSneak.pressed = true
        }
    }

    @EventTarget
    private fun onJump(event: JumpEvent) {
        if (lastInvOpen && bypassValue.get() == "Intave" && mc.currentScreen != null) {
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        reset()
        blinkPacketList.clear()
        invOpen = false
        lastInvOpen = false
    }

    private fun reset() {
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindForward) || mc.currentScreen != null) {
            mc.gameSettings.keyBindForward.pressed = false
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindBack) || mc.currentScreen != null) {
            mc.gameSettings.keyBindBack.pressed = false
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight) || mc.currentScreen != null) {
            mc.gameSettings.keyBindRight.pressed = false
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft) || mc.currentScreen != null) {
            mc.gameSettings.keyBindLeft.pressed = false
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindJump) || mc.currentScreen != null) {
            mc.gameSettings.keyBindJump.pressed = false
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSprint) || mc.currentScreen != null) {
            mc.gameSettings.keyBindSprint.pressed = false
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || mc.currentScreen != null) {
            mc.gameSettings.keyBindSneak.pressed = false
        }
    }

    override fun onDisable() {
        reset()

        blinkPacketList.clear()
        lastInvOpen = false
        invOpen = false
    }

    override val tag: String
        get() = bypassValue.get()

}
