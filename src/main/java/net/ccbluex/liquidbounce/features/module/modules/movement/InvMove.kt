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
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.settings.GameSettings
import net.minecraft.client.settings.KeyBinding
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

@ModuleInfo(name = "InvMove", category = ModuleCategory.MOVEMENT)
object InvMove : Module() {

    private val chest = BoolValue("Contains",true)
    private val inv = BoolValue("Inv",true)
    private val noDetectableValue = BoolValue("NoDetectable", false)
    private val bypassValue = ListValue("Bypass", arrayOf("NoOpenPacket", "Blink", "PacketInv","Intave","SaveC0E", "None"), "None")
    private val rotateValue = BoolValue("Rotate", false)
    private val noMoveClicksValue = BoolValue("NoMoveClicks", false)
    val noSprintValue = ListValue("NoSprint", arrayOf("Real", "PacketSpoof", "None"), "None")

    private val blinkPacketList = ArrayDeque<C03PacketPlayer>()
    private val clickPacketList = ArrayDeque<C0EPacketClickWindow>()
    private val packetListYes = ArrayDeque<C0EPacketClickWindow>()
    private var lastInvOpen = false
    var invOpen = false
        private set
    
    private var isInv = false

    private val affectedBindings = arrayOf(
        mc.gameSettings.keyBindForward,
        mc.gameSettings.keyBindBack,
        mc.gameSettings.keyBindRight,
        mc.gameSettings.keyBindLeft,
        mc.gameSettings.keyBindJump,
        mc.gameSettings.keyBindSprint
    )

    private fun isButtonPressed(keyBinding: KeyBinding): Boolean {
        return if (keyBinding.keyCode < 0) {
            Mouse.isButtonDown(keyBinding.keyCode + 100)
        } else {
            GameSettings.isKeyDown(keyBinding)
        }
    }

    private fun updateKeyState() {
        if (mc.currentScreen != null && mc.currentScreen !is GuiChat && (!noDetectableValue.get() || mc.currentScreen !is GuiContainer)) {
            for (affectedBinding in affectedBindings)
                affectedBinding.pressed = isButtonPressed(affectedBinding)

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
        if (!shouldMove())
            return

        updateKeyState()
    }

    @EventTarget
    fun onScreen(event: ScreenEvent) {
        if (!shouldMove())
            return
        
        updateKeyState()
    }

    @EventTarget
    fun onClick(event: ClickWindowEvent) {
        if (noMoveClicksValue.get() && MovementUtils.isMoving) {
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
                    mc.netHandler.addToSendQueue(
                        C0BPacketEntityAction(
                            mc.thePlayer,
                            C0BPacketEntityAction.Action.STOP_SPRINTING
                        )
                    )
                }
                if (mc.thePlayer.isSneaking) {
                    mc.netHandler.addToSendQueue(
                        C0BPacketEntityAction(
                            mc.thePlayer,
                            C0BPacketEntityAction.Action.STOP_SNEAKING
                        )
                    )
                }
            }
        }
        if (packet is S2EPacketCloseWindow || packet is C0DPacketCloseWindow) {
            invOpen = false
            if (bypassValue.get() == "Intave") mc.gameSettings.keyBindSneak.pressed = false
            if (noSprintValue.equals("PacketSpoof")) {
                if (mc.thePlayer.isSprinting) {
                    mc.netHandler.addToSendQueue(
                        C0BPacketEntityAction(
                            mc.thePlayer,
                            C0BPacketEntityAction.Action.START_SPRINTING
                        )
                    )
                }
                if (mc.thePlayer.isSneaking) {
                    mc.netHandler.addToSendQueue(
                        C0BPacketEntityAction(
                            mc.thePlayer,
                            C0BPacketEntityAction.Action.START_SNEAKING
                        )
                    )
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

                        PacketUtils.sendPacket(
                            C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT),
                            false
                        )
                        PacketUtils.sendPacket(packetListYes.iterator().next())
//                        packetListYes.forEach {
//                            PacketUtils.sendPacket(it,false)
//                        }
                        packetListYes.clear()
                        PacketUtils.sendPacket(C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId), false)
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
                            PacketUtils.sendPacket(it, false)
                        }
                        blinkPacketList.clear()
                    }
                }
            }

            "savec0e" -> {
                if (InventoryUtils.serverOpenInventory || InventoryUtils.serverOpenContainer) {
                    if (packet is C0EPacketClickWindow) {
                        clickPacketList.add(packet)
                        event.cancelEvent()
                    }
                } else if (clickPacketList.isNotEmpty()) {
                    clickPacketList.forEach {
                        PacketUtils.sendPacket(it, false)
                    }
                    clickPacketList.clear()
                }
            }
        }
    }

    @EventTarget
    private fun onStrafe(event: StrafeEvent) {
        if (InventoryUtils.serverOpenInventory && bypassValue.get() == "Intave" && mc.currentScreen != null) {
            mc.gameSettings.keyBindSneak.pressed = true
        }
    }

    @EventTarget
    private fun onJump(event: JumpEvent) {
        if (InventoryUtils.serverOpenInventory && bypassValue.get() == "Intave" && mc.currentScreen != null) {
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        reset()
        clickPacketList.clear()
        blinkPacketList.clear()
        invOpen = false
        lastInvOpen = false
    }

    fun shouldMove() : Boolean {
        if (!chest.get()) {
            if (mc.currentScreen is GuiContainer)
                return false
        }

        if (!inv.get()) {
            if (mc.currentScreen is GuiInventory)
                return false
        }

        return true
    }

    private fun reset() {
        if (mc.thePlayer == null || mc.theWorld == null)
            return

        for (affectedBinding in affectedBindings)
            affectedBinding.pressed = false.takeIf { affectedBinding.pressed }!!
    }

    override fun onDisable() {
        reset()

        blinkPacketList.clear()
        clickPacketList.clear()
        lastInvOpen = false
        invOpen = false
    }

    override val tag: String
        get() = bypassValue.get()

}
