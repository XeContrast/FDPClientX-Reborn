/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.extensions.isMoving
import net.ccbluex.liquidbounce.extensions.setSprintSafely
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.world.scaffold.Scaffold
import net.ccbluex.liquidbounce.features.module.modules.world.scaffold.Scaffold2
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.potion.Potion
import net.minecraft.util.MovementInput
import kotlin.math.abs

@ModuleInfo(name = "Sprint", category = ModuleCategory.MOVEMENT, defaultOn = true)
object Sprint : Module() {
    private val blindness = BoolValue("Blindness",true)
    private val usingItem = BoolValue("UsingItem",true) 
    private val inventory = BoolValue("Inventory",true) 
    private val food = BoolValue("Food",true) 

    private val alwaysCorrect = BoolValue("AlwaysCorrectSprint", false)

    val jumpDirectionsValue = BoolValue("JumpDirections", false) 
    private val allDirectionsValue = BoolValue("AllDirections", false) 
    private val allDirectionsBypassValue = ListValue("AllDirectionsBypass", arrayOf("Rotate", "RotateSpoof", "Toggle", "Spoof", "SpamSprint", "NoStopSprint", "Minemora", "None"), "None") { allDirectionsValue.get() }
    private val allDirectionsLimitSpeedGround = BoolValue("AllDirectionsLimitSpeedOnlyGround", true)
    private val allDirectionsLimitSpeedValue by FloatValue("AllDirectionsLimitSpeed", 0.7f, 0.5f, 1f) { allDirectionsValue.get() }
    private val noPacketValue by BoolValue("NoPackets", true)
    private var switchStat = false
    private var forceSprint = false
    private var isSprinting = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (allDirectionsValue.get()) {
            when(allDirectionsBypassValue.get()) {
                "NoStopSprint" -> {
                    forceSprint = true
                }
                "SpamSprint" -> {
                    forceSprint = true
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                }
                "Spoof" -> {
                    mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    switchStat = true
                }
            }
            if (RotationUtils.getRotationDifference(Rotation(MovementUtils.movingYaw, mc.thePlayer.rotationPitch), Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) > 30) {
                when(allDirectionsBypassValue.get()) {
                    "Rotate" -> RotationUtils.setTargetRotation(Rotation(MovementUtils.movingYaw, mc.thePlayer.rotationPitch), 2)
                    "RotateSpoof" -> {
                        switchStat = !switchStat
                        if (switchStat) {
                            RotationUtils.setTargetRotation(Rotation(MovementUtils.movingYaw, mc.thePlayer.rotationPitch))
                        }
                    }
                    "Toggle" -> {
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
                        mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                    }
                    "Minemora" -> {
                        if (mc.thePlayer.onGround && RotationUtils.getRotationDifference(Rotation(MovementUtils.movingYaw, mc.thePlayer.rotationPitch)) > 60) {
                            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.0000013, mc.thePlayer.posZ)
                            mc.thePlayer.motionY = 0.0
                        }
                    }
                    "LimitSpeed" -> {
                        if (!allDirectionsLimitSpeedGround.get() || mc.thePlayer.onGround) {
                            MovementUtils.limitSpeedByPercent(allDirectionsLimitSpeedValue)
                        }
                    }
                }
            }
        } else {
            switchStat = false
            forceSprint = false
        }
    }
    
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C0BPacketEntityAction) {
            if (allDirectionsValue.get()) {
                when(allDirectionsBypassValue.get()) {
                    "SpamSprint", "NoStopSprint" -> {
                        if (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                            event.cancelEvent()
                        }
                    }
                    "Toggle" -> {
                        if (switchStat) {
                            if (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                                event.cancelEvent()
                            } else {
                                switchStat = !switchStat
                            }
                        } else {
                            if (packet.action == C0BPacketEntityAction.Action.START_SPRINTING) {
                                event.cancelEvent()
                            } else {
                                switchStat = !switchStat
                            }
                        }
                    }
                    "Spoof" -> {
                        if (switchStat) {
                            if (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING || packet.action == C0BPacketEntityAction.Action.START_SPRINTING) {
                                event.cancelEvent()
                            }
                        }
                    }
                }
            }
            if (noPacketValue && !event.isCancelled) {
                if (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING || packet.action == C0BPacketEntityAction.Action.START_SPRINTING) {
                    event.cancelEvent()
                }
            }
        }
    }

    fun correctSprintState(movementInput: MovementInput, isUsingItem: Boolean) {
        val player = mc.thePlayer ?: return

        if (Scaffold.handleEvents() && Scaffold.sprintModeValue.get() != "Fix") {
            if (!Scaffold.sprint) {
                player setSprintSafely false
                isSprinting = false
                return
            } else if (player.isMoving) {
                player setSprintSafely true
                isSprinting = true
                return
            }
        }

        if (handleEvents() || alwaysCorrect.get()) {
            player setSprintSafely !shouldStopSprinting(movementInput, isUsingItem)
            isSprinting = player.isSprinting
            if (player.isSprinting && allDirectionsValue.get()) {
                if (!allDirectionsLimitSpeedGround.get() || player.onGround) {
                    player.motionX *= allDirectionsLimitSpeedValue
                    player.motionZ *= allDirectionsLimitSpeedValue
                }
            }
        }
    }

    private fun shouldStopSprinting(movementInput: MovementInput, isUsingItem: Boolean): Boolean {
        val player = mc.thePlayer ?: return false

        val modifiedForward = if (RotationUtils.targetRotation != null && (StrafeFix.handleEvents() && !StrafeFix.silentFixVaule.get())) {
            player.movementInput.moveForward
        } else {
            movementInput.moveForward
        }

        if (!player.isMoving) {
            return true
        }
//
//        if (player.isRiding)
//            return true

        if ((!Scaffold2.canSprint && Scaffold2.handleEvents()))
            return true

        if (player.isCollidedHorizontally) {
            return true
        }

        if ((blindness.get()) && player.isPotionActive(Potion.blindness) && !player.isSprinting) {
            return true
        }

        if ((food.get()) && !(player.foodStats.foodLevel > 6f || player.capabilities.allowFlying)) {
            return true
        }

        if ((usingItem.get()) && !NoSlow.handleEvents() && isUsingItem) {
            return true
        }

        if ((inventory.get()) && InventoryUtils.serverOpenInventory) {
            return true
        }

        if (allDirectionsValue.get()) {
            return false
        }

        return modifiedForward < 0.8
//
//        val threshold = if ((!usingItem.get() || NoSlow.handleEvents()) && isUsingItem) 0.2 else 0.8
//        val playerForwardInput = player.movementInput.moveForward
//
//        if (!checkServerSide.get()) {
//            return if (RotationUtils.targetRotation != null) {
//                abs(playerForwardInput) < threshold || playerForwardInput < 0 && modifiedForward < threshold
//            } else {
//                playerForwardInput < threshold
//            }
//        }
//
//        if (checkServerSideGround.get() && !player.onGround) {
//            return RotationUtils.targetRotation == null && modifiedForward < threshold
//        }
//
//        return modifiedForward < threshold
    }
}
