/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.EntityUtils.isLookingOnEntities
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.particles.Vec3
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.block.BlockAir
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.play.server.S32PacketConfirmTransaction
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.schedule
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.sqrt
//
@ModuleInfo(name = "Velocity", category = ModuleCategory.COMBAT)
object Velocity : Module() {
    private val mainMode =
        ListValue("MainMode", arrayOf("Vanilla", "Cancel", "AAC", "Matrix","GrimAC", "Reverse", "Other"), "Vanilla")
    private val vanillaMode =
        ListValue(
            "VanillaMode",
            arrayOf("Jump", "Simple", "Glitch"),
            "Jump"
        ).displayable { mainMode.get() == "Vanilla" }
    private val grimMode =
        ListValue(
            "GrimACMode",
            arrayOf("GrimReduce","GrimVertical"),
            "GrimReduce"
        ).displayable { mainMode.get() == "GrimAC" }
    private val cancelMode = ListValue(
        "CancelMode",
        arrayOf("S12Cancel", "S32Cancel", "SendC0F", "HighVersion", "GrimS32Cancel", "GrimC07", "Spoof"),
        "S12Cancel"
    ).displayable { mainMode.get() in arrayOf("Cancel") }
    private val matrixMode = ListValue(
        "Mode", arrayOf(
            "Ground",
            "Reduce",
            "LowReduce",
            "Reverse",
            "Simple",
            "Spoof",
            "Clip"
        ), "Ground"
    ).displayable { mainMode.get() == "Matrix" }
    private val aacMode = ListValue(
        "AACMode",
        arrayOf("AAC4Reduce", "AAC5Reduce","AAC5.2Reduce", "AAC5.2.0", "AAC5Vertical", "AAC5.2.0Combat", "AACPush", "AACZero"),
        "AAC4Reduce"
    ).displayable { mainMode.get() == "AAC" }
    private val reverseMode = ListValue(
        "ReverseMode",
        arrayOf("Reverse", "SmoothReverse"),
        "Reverse"
    ).displayable { mainMode.get() == "Reverse" }
    private val otherMode = ListValue(
        "OtherMode",
        arrayOf("AttackReduce", "IntaveReduce", "Karhu", "Delay", "Phase"),
        "AttackReduce"
    ).displayable { mainMode.get() == "Other" }

    private val horizontal = FloatValue("Horizontal", 0F, -1F, 1F).displayable {
        mainMode.get() == "Vanilla" && (vanillaMode.get() == "Simple")
    }
    private val vertical = FloatValue("Vertical", 0F, -1F, 1F).displayable {
        mainMode.get() in arrayOf(
            "AAC",
            "Vanilla"
        ) && (vanillaMode.get() == "Simple" || aacMode.get() == "AAC5.2.0")
    }
    private val limitMaxMotionValue =
        BoolValue("LimitMaxMotion", false).displayable { mainMode.get() == "Vanilla" && vanillaMode.get() == "Simple" }
    private val maxXZMotion = FloatValue(
        "MaxXZMotion",
        0.4f,
        0f,
        1.9f
    ).displayable { mainMode.get() == "Vanilla" && vanillaMode.get() == "Simple" && limitMaxMotionValue.get() }
    private val maxYMotion = FloatValue(
        "MaxYMotion",
        0.36f,
        0f,
        0.46f
    ).displayable { mainMode.get() == "Vanilla" && vanillaMode.get() == "Simple" && limitMaxMotionValue.get() }

    private val modifyTimerValue =
        BoolValue("ModifyTimer", true).displayable { mainMode.get() == "Cancel" && cancelMode.get() == "Spoof" }
    private val mtimerValue = FloatValue(
        "Timer",
        0.6F,
        0.1F,
        1F
    ).displayable { modifyTimerValue.get() && mainMode.get() == "Cancel" && cancelMode.get() == "Spoof" }

    private val reverseStrength = FloatValue(
        "ReverseStrength",
        1f,
        0.1f,
        1f
    ).displayable { mainMode.get() == "Reverse" && reverseMode.get() == "Reverse" }
    private val reverse2Strength = FloatValue(
        "ReverseStrength",
        1f,
        0.02f,
        0.1f
    ).displayable { mainMode.get() == "Reverse" && reverseMode.get() == "SmoothReverse" }
    private val onLook = BoolValue("onLook", false).displayable {
        reverseMode.get() in arrayOf(
            "Reverse",
            "SmoothReverse"
        ) && mainMode.get() == "Reverse"
    }
    private val range = FloatValue("Range", 3.0F, 1F, 5.0F).displayable {
        onLook.get() && reverseMode.get() in arrayOf("Reverse", "SmoothReverse") && mainMode.get() == "Reverse"
    }
    private val maxAngleDifference = FloatValue("MaxAngleDifference", 45.0f, 5.0f, 90f).displayable {
        onLook.get() && reverseMode.get() in arrayOf("Reverse", "SmoothReverse") && mainMode.get() == "Reverse"
    }

    private val alwaysValue =
        BoolValue("Always", true).displayable { mainMode.get() == "Cancel" && cancelMode.get() == "GrimC07" }
    private val onlyAirValue =
        BoolValue("OnlyBreakAir", true).displayable { mainMode.get() == "Cancel" && cancelMode.get() == "GrimC07" }
    private val worldValue =
        BoolValue("BreakOnWorld", false).displayable { mainMode.get() == "Cancel" && cancelMode.get() == "GrimC07" }
    private val sendC03Value =
        BoolValue(
            "SendC03",
            false
        ).displayable { mainMode.get() == "Cancel" && cancelMode.get() == "GrimC07" } // bypass latest but flag timer
    private val C06Value =
        BoolValue(
            "Send1.17C06",
            false
        ).displayable { mainMode.get() == "Cancel" && cancelMode.get() == "GrimC07" && sendC03Value.get() } // need via to 1.17+
    private val flagPauseValue = IntegerValue(
        "FlagPause-Time",
        50,
        0,
        5000
    ).displayable { mainMode.get() == "Cancel" && cancelMode.get() == "GrimC07" }

    private val reduceAmount = FloatValue(
        "ReduceAmount",
        0.8f,
        0.0f,
        1f
    ).displayable { mainMode.get() == "Other" && otherMode.get() == "AttackReduce" }

    private val jumpCooldownMode = ListValue(
        "JumpCooldownMode",
        arrayOf("Ticks", "ReceivedHits"),
        "Ticks"
    ).displayable { vanillaMode.get() == "Jump" && mainMode.get() == "Vanilla" }
    private val ticksUntilJump = IntegerValue(
        "TicksUntilJump",
        4,
        0,
        20
    ).displayable { jumpCooldownMode.get() == "Ticks" && vanillaMode.get() == "Jump" && mainMode.get() == "Vanilla" }
    private val hitsUntilJump = IntegerValue(
        "ReceivedHitsUntilJump",
        2,
        0,
        5
    ).displayable { jumpCooldownMode.get() == "ReceivedHits" && vanillaMode.get() == "Jump" && mainMode.get() == "Vanilla" }
    private val chance =
        IntegerValue("Chance", 100, 0, 100).displayable { vanillaMode.get() == "Jump" && mainMode.get() == "Vanilla" }

    private val delayValue = IntegerValue(
        "Delayed-Delay",
        300,
        50,
        1000
    ).displayable { otherMode.get() == "Delay" && mainMode.get() == "Other" }
    private val blinkValue =
        BoolValue("Delayed-Blink", true).displayable { otherMode.get() == "Delay" && mainMode.get() == "Other" }
    private val blinkOutbound = BoolValue(
        "Delayed-BlinkOutgoing",
        true
    ).displayable { blinkValue.get() && otherMode.get() == "Delay" && mainMode.get() == "Other" }
    private val delayC0F = BoolValue(
        "Delayed-DelayTransaction",
        true
    ).displayable { !blinkValue.get() && otherMode.get() == "Delay" && mainMode.get() == "Other" }

    private val aacPushXZReducer =
        FloatValue("AACPushXZReducer", 2F, 1F, 3F).displayable { aacMode.get() == "AACPush" && mainMode.get() == "AAC" }
    private val aacPushYReducer =
        BoolValue("AACPushYReducer", true).displayable { aacMode.get() == "AACPush" && mainMode.get() == "AAC" }

    private val phaseHeightValue =
        FloatValue("Height", 0.5F, 0F, 1F).displayable { mainMode.get() == "Other" && otherMode.get() == "Phase" }
    private val phaseOnlyGroundValue =
        BoolValue("OnlyGround", true).displayable { mainMode.get() == "Other" && otherMode.get() == "Phase" }
    private val phaseMode = ListValue(
        "Mode",
        arrayOf("Normal", "Packet"),
        "Normal"
    ).displayable { mainMode.get() == "Other" && otherMode.get() == "Phase" }

    private val reduceFactor = FloatValue(
        "Factor",
        0.6f,
        0.0f,
        1f
    ).displayable { otherMode.get() == "IntaveReduce" && mainMode.get() == "Other" }
    private val hurtTimeMode = ListValue(
        "HurtTimeMode",
        arrayOf("Single", "Range"),
        "Single"
    ).displayable { otherMode.get() == "IntaveReduce" && mainMode.get() == "Other" }
    private val hurtTime = IntegerValue(
        "HurtTime",
        9,
        1,
        10
    ).displayable { otherMode.get() == "IntaveReduce" && mainMode.get() == "Other" && hurtTimeMode.get() == "Single" }
    private val minHurtTime = IntegerValue(
        "MinHurtTime",
        9,
        1,
        10
    ).displayable { otherMode.get() == "IntaveReduce" && mainMode.get() == "Other" && hurtTimeMode.get() == "Range" }
    private val maxHurtTime = IntegerValue(
        "MaxHurtTime",
        9,
        1,
        10
    ).displayable { otherMode.get() == "IntaveReduce" && mainMode.get() == "Other" && hurtTimeMode.get() == "Range" }

    private val smartVelo =
        BoolValue("SmartVelo", true).displayable { grimMode.get() == "GrimVertical" && mainMode.get() == "GrimAC" }
    private val sendc0fValue =
        BoolValue("C0F", false).displayable { grimMode.get() == "GrimVertical" && mainMode.get() == "GrimAC" }
    private val C0fpacketamount = IntegerValue(
        "C0FPacketAmount",
        0,
        1,
        20
    ).displayable { grimMode.get() == "GrimVertical" && mainMode.get() == "GrimAC" && sendc0fValue.get() }
    private val C02packetamount = IntegerValue(
        "C02PacketAmount",
        1,
        1,
        20
    ).displayable { grimMode.get() == "GrimVertical" && mainMode.get() == "GrimAC" }
    private val sprintSpoof =
        BoolValue("SpoofSprint", true).displayable { grimMode.get() == "GrimVertical" && mainMode.get() == "GrimAC" }
    private val playerJump =
        BoolValue("PlayerJump", true).displayable { grimMode.get() == "GrimVertical" && mainMode.get() == "GrimAC" }
    private val spoofJump =
        BoolValue("SpoofJump", false).displayable { grimMode.get() == "GrimVertical" && mainMode.get() == "GrimAC" }
    private val callEvent =
        BoolValue("CallEvent", true).displayable { grimMode.get() == "GrimVertical" && mainMode.get() == "GrimAC" }

    private val reduceCount = IntegerValue("ReduceCount",4,1,10).displayable { mainMode.get() == "GrimAC" && grimMode.get() == "GrimReduce" }
    private val reduceTimes = IntegerValue("ReduceTimes",1,1,5).displayable { mainMode.get() == "GrimAC" && grimMode.get() == "GrimReduce" }

    //
    private var hasReceivedVelocity = false
    private val velocityTimer = MSTimer()
    private var velocityInput = false

    //SendC0f
    private var tran = false

    //GrimS32
    private var cancelPacket = 6
    private var resetPersec = 8
    private var grimTCancel = 0
    private var updates = 0

    //GrimVertical
    private var attack = false
    private var motionXZ = 0.0
    private var lastSprinting = false

    //Matrix
    private var flag = false
    private var isMatrixOnGround = false

    // SmoothReverse
    private var reverseHurt = false

    //GrimC07
    private var flagTimer = MSTimer()

    // Jump
    private var limitUntilJump = 0

    //Delay
    private var blink = false
    private var veloTick = 0
    private val delayTimer = MSTimer()
    private val packets = LinkedBlockingQueue<Packet<INetHandlerPlayClient>>()

    //AAC5COMBAT
    private var templateX = 0
    private var templateY = 0
    private var templateZ = 0

    // AACPush
    private var jump = false

    // IntaveReduce
    private var intaveTick = 0
    private var lastAttackTime = 0L
    private var intaveDamageTick = 0

    //AAC5.2Reduce
    private var lastGround = false

    //GrimSimple
    private var unReduceTimes = 0

    @EventTarget(priority = -1)
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isInWater || thePlayer.isInLava || thePlayer.isInWeb || thePlayer.isDead)
            return

        if ((packet is S12PacketEntityVelocity && thePlayer.entityId == packet.entityID && packet.motionY > 0 && (packet.motionX != 0 || packet.motionZ != 0))
            || (packet is S27PacketExplosion && (thePlayer.motionY + packet.field_149153_g) > 0.0
                    && ((thePlayer.motionX + packet.field_149152_f) != 0.0 || (thePlayer.motionZ + packet.field_149159_h) != 0.0))
        ) {
            velocityTimer.reset()
            when (mainMode.get().lowercase()) {
                "grimac" -> {
                    when (grimMode.get().lowercase()) {
                        "grimreduce" -> unReduceTimes = reduceTimes.get()
                        "grimvertical" -> hasReceivedVelocity = true
                    }
                }
                "vanilla" -> {
                    when (vanillaMode.get().lowercase()) {
                        "Jump" -> {
                            // TODO: Recode and make all velocity modes support velocity direction checks
                            var packetDirection = 0.0
                            when (packet) {
                                is S12PacketEntityVelocity -> {
                                    if (packet.entityID != thePlayer.entityId) return

                                    val motionX = packet.motionX.toDouble()
                                    val motionZ = packet.motionZ.toDouble()

                                    packetDirection = atan2(motionX, motionZ)
                                }

                                is S27PacketExplosion -> {
                                    val motionX = thePlayer.motionX + packet.field_149152_f
                                    val motionZ = thePlayer.motionZ + packet.field_149159_h

                                    packetDirection = atan2(motionX, motionZ)
                                }
                            }
                            val degreePlayer = getDirection()
                            val degreePacket = Math.floorMod(packetDirection.toDegrees().toInt(), 360).toDouble()
                            var angle = abs(degreePacket + degreePlayer)
                            val threshold = 120.0
                            angle = Math.floorMod(angle.toInt(), 360).toDouble()
                            val inRange = angle in 180 - threshold / 2..180 + threshold / 2
                            if (inRange)
                                hasReceivedVelocity = true
                        }

                        "simple" -> handleVelocity(event)
                        "glitch" -> {
                            if (!thePlayer.onGround) {
                                return
                            }

                            hasReceivedVelocity = true
                            event.cancelEvent()
                        }
                    }
                }

                "reverse" -> {
                    when (reverseMode.get().lowercase()) {
                        "reverse", "smoothreverse" -> hasReceivedVelocity = true
                    }
                }

                "aac" -> {
                    when (aacMode.get().lowercase()) {
                        "aac4reduce" -> {
                            if (packet is S12PacketEntityVelocity && !thePlayer.onGround) {
                                packet.motionX = (packet.getMotionX() * 0.6).toInt()
                                packet.motionZ = (packet.getMotionZ() * 0.6).toInt()
                            }
                        }

                        "aac5.2.0" -> {
                            if (event.packet is S12PacketEntityVelocity) {
                                event.cancelEvent()
                                mc.netHandler.addToSendQueue(
                                    C03PacketPlayer.C04PacketPlayerPosition(
                                        mc.thePlayer.posX,
                                        1.7976931348623157E+308,
                                        mc.thePlayer.posZ,
                                        true
                                    )
                                )
                            }
                        }

                        "aac5.2.0combat" -> {
                            if (packet is S12PacketEntityVelocity) {
                                event.cancelEvent()
                                templateX = packet.motionX
                                templateZ = packet.motionZ
                                templateY = packet.motionY
                            }
                        }

                        "aac5reduce", "aaczero" -> hasReceivedVelocity = true
                        "aac5.2reduce" -> {
                            hasReceivedVelocity = true
                            if (mc.thePlayer.onGround) lastGround = true
                            if (packet is S12PacketEntityVelocity) motionXZ = getMotionNoXZ(packet)
                        }
                    }
                }

                "cancel" -> {
                    when (cancelMode.get().lowercase()) {
                        "s12cancel" -> event.cancelEvent()
                        "s32cancel", "sendc0f", "highversion", "grimc07" -> {
                            event.cancelEvent()
                            hasReceivedVelocity = true
                        }

                        "grims32cancel" -> {
                            event.cancelEvent()
                            grimTCancel = cancelPacket
                            hasReceivedVelocity = true
                        }

                        "spoof" -> {
                            if (packet is S12PacketEntityVelocity) {
                                event.cancelEvent()
                                mc.netHandler.addToSendQueue(
                                    C03PacketPlayer.C04PacketPlayerPosition(
                                        mc.thePlayer.posX + packet.motionX / 8000.0,
                                        mc.thePlayer.posY + packet.motionY / 8000.0,
                                        mc.thePlayer.posZ + packet.motionZ / 8000.0,
                                        false
                                    )
                                )
                                if (modifyTimerValue.get()) {
                                    mc.timer.timerSpeed = mtimerValue.get()
                                }
                            }
                        }
                    }
                }

                "matrix" -> {
                    if (packet is S12PacketEntityVelocity) {
                        when (matrixMode.get().lowercase()) {
                            "spoof" -> {
                                event.cancelEvent()
                                mc.netHandler.addToSendQueue(
                                    C03PacketPlayer.C04PacketPlayerPosition(
                                        mc.thePlayer.posX + packet.motionX / -24000.0,
                                        mc.thePlayer.posY + packet.motionY / -24000.0,
                                        mc.thePlayer.posZ + packet.motionZ / 8000.0,
                                        false
                                    )
                                )
                            }

                            "simple" -> {
                                packet.motionX = (packet.getMotionX() * 0.36).toInt()
                                packet.motionZ = (packet.getMotionZ() * 0.36).toInt()
                                if (mc.thePlayer.onGround) {
                                    packet.motionX = (packet.getMotionX() * 0.9).toInt()
                                    packet.motionZ = (packet.getMotionZ() * 0.9).toInt()
                                }
                            }

                            "reverse" -> {
                                if (packet.entityID == mc.thePlayer.entityId) {
                                    if (!flag) {
                                        event.cancelEvent()
                                        flag = true
                                    } else {
                                        flag = false
                                        packet.motionX = (packet.getMotionX() * -0.1).toInt()
                                        packet.motionZ = (packet.getMotionZ() * -0.1).toInt()
                                    }
                                }
                            }

                            "ground" -> {
                                packet.motionX = (packet.getMotionX() * 0.36).toInt()
                                packet.motionZ = (packet.getMotionZ() * 0.36).toInt()
                                if (isMatrixOnGround) {
                                    packet.motionY = (-628.7).toInt()
                                    packet.motionX = (packet.getMotionX() * 0.6).toInt()
                                    packet.motionZ = (packet.getMotionZ() * 0.6).toInt()
                                    thePlayer.onGround = false
                                }
                            }

                            "lowreduce" -> {
                                if (packet.entityID == thePlayer.entityId) {
                                    packet.motionX = (packet.getMotionX() * 0.33).toInt()
                                    packet.motionZ = (packet.getMotionZ() * 0.33).toInt()

                                    if (thePlayer.onGround) {
                                        packet.motionX = (packet.getMotionX() * 0.86).toInt()
                                        packet.motionZ = (packet.getMotionZ() * 0.86).toInt()
                                    }
                                }
                            }
                        }
                    }
                }

                "other" -> {
                    when (otherMode.get().lowercase()) {
                        "delay" -> {
                            if (blinkValue.get()) {
                                if (blinkOutbound.get()) {
                                    BlinkUtils.setBlinkState(all = true)
                                }
                                blink = true
                                delayTimer.reset()
                            } else {
                                event.cancelEvent()
                                veloTick = mc.thePlayer.ticksExisted
                                packets.add(packet as Packet<INetHandlerPlayClient>)
                                queuePacket(delayValue.get().toLong())
                            }
                        }

                        "intavereduce", "attackreduce" -> hasReceivedVelocity = true
                        "phase" -> {
                            if (packet is S12PacketEntityVelocity) {
                                if (!mc.thePlayer.onGround && phaseOnlyGroundValue.get()) {
                                    return
                                }

                                when (phaseMode.get().lowercase()) {
                                    "normal" -> {
                                        mc.thePlayer.setPositionAndUpdate(
                                            mc.thePlayer.posX,
                                            mc.thePlayer.posY - phaseHeightValue.get(),
                                            mc.thePlayer.posZ
                                        )
                                    }

                                    "packet" -> {
                                        if (packet.motionX < 500 && packet.motionY < 500) {
                                            return
                                        }

                                        mc.netHandler.addToSendQueue(
                                            C03PacketPlayer.C04PacketPlayerPosition(
                                                mc.thePlayer.posX,
                                                mc.thePlayer.posY - phaseHeightValue.get(),
                                                mc.thePlayer.posZ,
                                                false
                                            )
                                        )
                                    }
                                }
                                event.cancelEvent()
                                packet.motionX = 0
                                packet.motionY = 0
                                packet.motionZ = 0
                            }
                        }
                    }
                }
            }
        }

        when (mainMode.get().lowercase()) {
            "grimac" -> when (grimMode.get().lowercase()) {
                "grimvertical" -> {
                    if (packet is S12PacketEntityVelocity) {
                        if (packet.getMotionX() == 0 && packet.getMotionZ() == 0 || mc.thePlayer == null || (mc.theWorld?.getEntityByID(
                                packet.entityID
                            ) ?: return) != mc.thePlayer
                        ) { // ignore horizonal velocity
                            return
                        }
                        if (mc.thePlayer!!.onGround && mc.thePlayer.hurtTime > 0) {
                            if (playerJump.get()) {
                                mc.thePlayer.jump()
                            }
                        }
                        velocityInput = true
                        motionXZ = getMotionNoXZ(packet)

                        if (FDPClient.moduleManager[KillAura::class.java]!!.state && FDPClient.moduleManager[KillAura::class.java]!!.currentTarget != null && mc.thePlayer!!.getDistanceToEntityBox(
                                FDPClient.moduleManager[KillAura::class.java]!!.currentTarget!!
                            ) <= 3.00
                        ) {
                            if (mc.thePlayer!!.isSprinting && mc.thePlayer!!.serverSprintState && MovementUtils.isMoving) {
                                repeat(C0fpacketamount.get()) {
                                    if (sendc0fValue.get()) {
                                        mc.netHandler.addToSendQueue(
                                            C0FPacketConfirmTransaction(
                                                nextInt(
                                                    102,
                                                    1000024123
                                                ), nextInt(102, 1000024123).toShort(), true
                                            )
                                        )
                                    }
                                }
                                attack = true
                            } else {
                                if (sprintSpoof.get()) {
                                    repeat(C0fpacketamount.get()) {
                                        if (sendc0fValue.get()) {
                                            mc.netHandler.addToSendQueue(
                                                C0FPacketConfirmTransaction(
                                                    nextInt(
                                                        102,
                                                        1000024123
                                                    ), nextInt(102, 1000024123).toShort(), true
                                                )
                                            )
                                        }
                                    }
                                    mc.netHandler.addToSendQueue(
                                        C0BPacketEntityAction(
                                            mc.thePlayer,
                                            C0BPacketEntityAction.Action.START_SPRINTING
                                        )
                                    )
                                    mc.thePlayer.serverSprintState = false
                                    attack = true
                                    mc.netHandler.addToSendQueue(
                                        C0BPacketEntityAction(
                                            mc.thePlayer,
                                            C0BPacketEntityAction.Action.STOP_SPRINTING
                                        )
                                    )
                                }
                            }
                        }
                    }
                    if (packet is C0BPacketEntityAction && velocityInput && sprintSpoof.get()) {
                        if (packet.action == C0BPacketEntityAction.Action.START_SPRINTING) {
                            if (this.lastSprinting) {
                                FDPClient.eventManager.callEvent(event)
                            }
                            this.lastSprinting = true
                        } else if (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                            if (!this.lastSprinting) {
                                FDPClient.eventManager.callEvent(event)
                            }
                            this.lastSprinting = false
                        }
                    }
                }
            }
            "cancel" -> when (cancelMode.get().lowercase()) {
                "s32cancel" -> {
                    if (packet is S32PacketConfirmTransaction && hasReceivedVelocity) {
                        event.cancelEvent()
                        hasReceivedVelocity = false
                    }
                }

                "sendc0f" -> {
                    if (packet is S32PacketConfirmTransaction) {
                        event.cancelEvent()
                        thePlayer.sendQueue.addToSendQueue(
                            C0FPacketConfirmTransaction(
                                if (tran) 1 else -1,
                                if (tran) -1 else 1,
                                false
                            )
                        )
                        tran = !tran
                    }
                }

                "grimc07" -> {
                    if (packet is S08PacketPlayerPosLook) flagTimer.reset()
                    if (!flagTimer.hasTimePassed(flagPauseValue.get().toLong())) {
                        hasReceivedVelocity = false
                    }
                }

                "highversion" -> {
                    if (hasReceivedVelocity) {
                        repeat(4) {
                            mc.netHandler.addToSendQueue(
                                C03PacketPlayer.C06PacketPlayerPosLook(
                                    mc.thePlayer.posX,
                                    mc.thePlayer.posY,
                                    mc.thePlayer.posZ,
                                    mc.thePlayer.rotationYaw,
                                    mc.thePlayer.rotationPitch,
                                    mc.thePlayer.onGround
                                )
                            )
                        }
                        mc.netHandler.addToSendQueue(
                            C07PacketPlayerDigging(
                                C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, mc.thePlayer.position,
                                EnumFacing.DOWN
                            )
                        )
                        hasReceivedVelocity = false
                    }
                }

                "grims32cancel" -> {
                    if (packet is S32PacketConfirmTransaction && grimTCancel > 0) {
                        event.cancelEvent()
                        grimTCancel--
                    }
                }
            }

            "other" -> {
                when (otherMode.get().lowercase()) {
                    "delay" -> {
                        if (blink && blinkValue.get() && packet.javaClass.simpleName.startsWith(
                                "S",
                                ignoreCase = true
                            ) && mc.thePlayer.ticksExisted > 10
                        ) {
                            event.cancelEvent()
                            packets.add(packet as Packet<INetHandlerPlayClient>)
                        }

                        if (!blinkValue.get() && delayC0F.get()) {
                            if (packet is S32PacketConfirmTransaction && veloTick == mc.thePlayer.ticksExisted) {
                                event.cancelEvent()
                                packets.add(packet)
                                queuePacket(delayValue.get().toLong())
                            }
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val player = mc.thePlayer ?: return

        if (vanillaMode.get() == "Jump" && hasReceivedVelocity && mainMode.get() == "Vanilla") {
            if (!player.isJumping && nextInt(endExclusive = 100) < chance.get() && shouldJump() && player.isSprinting && player.onGround && player.hurtTime == 9) {
                player.jump()
                limitUntilJump = 0
            }
            hasReceivedVelocity = false
            return
        }

        when (jumpCooldownMode.get().lowercase()) {
            "ticks" -> limitUntilJump++
            "receivedhits" -> if (player.hurtTime == 9) limitUntilJump++
        }
    }

    private fun shouldJump() = when (jumpCooldownMode.get().lowercase()) {
        "ticks" -> limitUntilJump >= ticksUntilJump.get()
        "receivedhits" -> limitUntilJump >= hitsUntilJump.get()
        else -> false
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if (mainMode.get() == "Other" && otherMode.get() == "Karhu") {
            if (event.block is BlockAir && mc.thePlayer.hurtTime > 0) {
                val x = event.x
                val y = event.y
                val z = event.z

                if (y == (floor(mc.thePlayer.posY) + 1).toInt()) {
                    event.boundingBox = AxisAlignedBB.fromBounds(0.0, 0.0, 0.0, 1.0, 0.0, 1.0).offset(
                        x.toDouble(),
                        y.toDouble(),
                        z.toDouble()
                    )
                }
            }
        }
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        if (mainMode.get() == "Cancel" && cancelMode.get() == "GrimC07") {
            if (hasReceivedVelocity || alwaysValue.get()) { // packet processed event pls
                val pos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
                if (checkBlock(pos) || checkBlock(pos.up())) {
                    hasReceivedVelocity = false
                }
            }
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val player = mc.thePlayer ?: return
        when (mainMode.get().lowercase()) {
            "aac" -> when (aacMode.get().lowercase()) {
                "aac5.2reduce" -> {
                    if (player.hurtTime in 2..8) {
                        if (lastGround) {
                            player.motionX *= 0.59999999999
                            player.motionZ *= 0.59999999999
                        }
                    }
                    if (player.hurtTime == 0 && lastGround && player.onGround) {
                        lastGround = false
                    }
                    if (hasReceivedVelocity) {
                        if (player.hurtTime == 7 && player.onGround) {
                            if (!mc.gameSettings.keyBindJump.isKeyDown) {
                                player.jump()
                            }
                            player.motionX *= motionXZ
                            player.motionZ *= motionXZ
                        }
                        hasReceivedVelocity = false
                    }
                }
            }
            "other" -> {
                when (otherMode.get().lowercase()) {
                    "attackreduce" -> {
                        if (hasReceivedVelocity) {
                            if (mc.thePlayer.hurtTime >= 4) {
                                player.motionX *= reduceAmount.get()
                                player.motionZ *= reduceAmount.get()
                            } else {
                                hasReceivedVelocity = false
                            }
                        }
                    }

                    "intavereduce" -> {
                        if (!hasReceivedVelocity) return
                        if ((hurtTimeMode.get() == "Single" && player.hurtTime == hurtTime.get()) || (hurtTimeMode.get() == "Range" && player.hurtTime in minHurtTime.get()..maxHurtTime.get())) {
                            if (System.currentTimeMillis() - lastAttackTime <= 8000) {
                                player.motionX *= reduceFactor.get()
                                player.motionZ *= reduceFactor.get()
                            }

                            lastAttackTime = System.currentTimeMillis()
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return

        if (player.isInWater || player.isInLava || player.isInWeb || player.isDead)
            return

        when (mainMode.get().lowercase()) {
            "cancel" -> {
                when (cancelMode.get().lowercase()) {
                    "grims32cancel" -> {
                        updates++

                        if (resetPersec > 0) {
                            if (updates >= 0) {
                                updates = 0
                                if (grimTCancel > 0) {
                                    grimTCancel--
                                }
                            }
                        }
                    }
                }
            }

            "grimac" -> {
                when (grimMode.get().lowercase()) {
                    "grimreduce" -> {
                        if (unReduceTimes > 0 && mc.thePlayer.hurtTime > 0 && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mc.objectMouseOver.entityHit is EntityPlayer) {
                            if (!mc.thePlayer.serverSprintState) {
                                PacketUtils.sendPacket(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
                                mc.thePlayer.serverSprintState = true
                                mc.thePlayer.isSprinting = true
                            }

                            for (i in 0 until reduceCount.get()) {
                                PacketUtils.sendPackets(
                                    C0APacketAnimation(),
                                    C02PacketUseEntity(mc.objectMouseOver.entityHit,C02PacketUseEntity.Action.ATTACK)
                                )

                                mc.thePlayer.motionX *= 0.6
                                mc.thePlayer.motionZ *= 0.6
                            }
                            unReduceTimes--
                        } else {
                            unReduceTimes = 0
                        }
                    }
                    "grimvertical" -> {
                        if (attack) {
                            repeat(C02packetamount.get()) {
                                FDPClient.moduleManager[KillAura::class.java]!!.currentTarget?.let { it1 ->
                                    attackEntity(
                                        it1
                                    )
                                }
                            }

                            if (smartVelo.get()) {
                                mc.thePlayer!!.motionX *= motionXZ
                                mc.thePlayer!!.motionZ *= motionXZ
                            } else {
                                mc.thePlayer!!.motionX *= 0.077760000
                                mc.thePlayer!!.motionZ *= 0.077760000
                            }
                            velocityInput = false
                            attack = false
                        } else if (mc.thePlayer.hurtTime == 6 && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown) {
                            if (spoofJump.get()) {
                                mc.thePlayer.movementInput.jump = true
                            }
                        }
                    }
                }
            }

            "other" -> {
                when (otherMode.get().lowercase()) {
                    "delay" -> {
                        if (blink && blinkValue.get() && delayTimer.hasTimePassed(delayValue.get().toLong())) {
                            clearPackets()
                            blink = false
                        }
                    }

                    "intavereduce" -> {
                        if (!hasReceivedVelocity) return
                        intaveTick++

                        if (mc.thePlayer.hurtTime == 2) {
                            intaveDamageTick++
                            if (player.onGround && intaveTick % 2 == 0 && intaveDamageTick <= 10) {
                                player.jump()
                                intaveTick = 0
                            }
                            hasReceivedVelocity = false
                        }
                    }
                }
            }

            "vanilla" -> {
                when (vanillaMode.get().lowercase()) {
                    "glitch" -> {
                        player.noClip = hasReceivedVelocity

                        if (player.hurtTime == 7) {
                            player.motionY = 0.41999998688698
                        }

                        hasReceivedVelocity = false
                    }
                }
            }

            "reverse" -> {
                when (reverseMode.get().lowercase()) {
                    "reverse" -> {
                        val nearbyEntity = getNearestEntityInRange()

                        if (!hasReceivedVelocity)
                            return

                        if (nearbyEntity != null) {
                            if (!player.onGround) {
                                if (onLook.get() && !isLookingOnEntities(
                                        nearbyEntity,
                                        maxAngleDifference.get().toDouble()
                                    )
                                ) {
                                    return
                                }

                                MovementUtils.getSpeed *= reverseStrength.get()
                            } else if (velocityTimer.hasTimePassed(80))
                                hasReceivedVelocity = false
                        }
                    }

                    "smoothreverse" -> {
                        val nearbyEntity = getNearestEntityInRange()

                        if (hasReceivedVelocity) {
                            if (nearbyEntity == null) {
                                player.speedInAir = 0.02F
                                reverseHurt = false
                            } else {
                                if (onLook.get() && !isLookingOnEntities(
                                        nearbyEntity,
                                        maxAngleDifference.get().toDouble()
                                    )
                                ) {
                                    hasReceivedVelocity = false
                                    player.speedInAir = 0.02F
                                    reverseHurt = false
                                } else {
                                    if (player.hurtTime > 0) {
                                        reverseHurt = true
                                    }

                                    if (!player.onGround) {
                                        player.speedInAir = if (reverseHurt) reverse2Strength.get() else 0.02F
                                    } else if (velocityTimer.hasTimePassed(80)) {
                                        hasReceivedVelocity = false
                                        player.speedInAir = 0.02F
                                        reverseHurt = false
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "aac" -> {
                when (aacMode.get().lowercase()) {
                    "aac5reduce" -> {
                        if (mc.thePlayer.hurtTime > 1 && hasReceivedVelocity) {
                            mc.thePlayer.motionX *= 0.81
                            mc.thePlayer.motionZ *= 0.81
                        }
                        if (hasReceivedVelocity && (mc.thePlayer.hurtTime < 5 || mc.thePlayer.onGround) && velocityTimer.hasTimePassed(
                                120L
                            )
                        ) {
                            hasReceivedVelocity = false
                        }
                    }

                    "aac5vertical" -> {
                        if (mc.thePlayer.hurtTime == 9) {
                            val target = FDPClient.combatManager.getNearByEntity(3f)
                            repeat(12) {
                                mc.thePlayer.sendQueue.addToSendQueue(
                                    C02PacketUseEntity(
                                        target,
                                        C02PacketUseEntity.Action.ATTACK
                                    )
                                )
                                mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                            }
                            mc.thePlayer.motionX *= 0.077760000
                            mc.thePlayer.motionZ *= 0.077760000
                        }
                    }

                    "aac5.2.0combat" -> {
                        if (mc.thePlayer.hurtTime > 0 && hasReceivedVelocity) {
                            hasReceivedVelocity = false
                            mc.thePlayer.motionX = 0.0
                            mc.thePlayer.motionZ = 0.0
                            mc.thePlayer.motionY = 0.0
                            mc.thePlayer.jumpMovementFactor = -0.002f
                            mc.netHandler.addToSendQueue(
                                C03PacketPlayer.C04PacketPlayerPosition(
                                    mc.thePlayer.posX,
                                    1.7976931348623157E+308,
                                    mc.thePlayer.posZ,
                                    true
                                )
                            )
                        }
                        if (velocityTimer.hasTimePassed(80L) && hasReceivedVelocity) {
                            hasReceivedVelocity = false
                            mc.thePlayer.motionX = templateX / 8000.0
                            mc.thePlayer.motionZ = templateZ / 8000.0
                            mc.thePlayer.motionY = templateY / 8000.0
                            mc.thePlayer.jumpMovementFactor = -0.002f
                        }
                    }

                    "aacpush" -> {
                        if (jump) {
                            if (player.onGround)
                                jump = false
                        } else {
                            // Strafe
                            if (player.hurtTime > 0 && player.motionX != 0.0 && player.motionZ != 0.0)
                                player.onGround = true

                            // Reduce Y
                            if (player.hurtResistantTime > 0 && aacPushYReducer.get() && !FDPClient.moduleManager[Speed::class.java]!!.state)
                                player.motionY -= 0.014999993
                        }

                        // Reduce XZ
                        if (player.hurtResistantTime >= 19) {
                            val reduce = aacPushXZReducer.get()

                            player.motionX /= reduce
                            player.motionZ /= reduce
                        }
                    }

                    "aaczero" ->
                        if (player.hurtTime > 0) {
                            if (!hasReceivedVelocity || player.onGround || player.fallDistance > 2F)
                                return

                            player.motionY -= 1.0
                            player.isAirBorne = true
                            player.onGround = true
                        } else
                            hasReceivedVelocity = false
                }
            }

            "matrix" -> {
                when (matrixMode.get().lowercase()) {
                    "ground" -> isMatrixOnGround = player.onGround && !mc.gameSettings.keyBindJump.isKeyDown
                    "clip" -> {
                        if (player.hurtTime in 1..4 && player.fallDistance > 0) {
                            player.motionX = 0.0
                            player.motionZ = 0.0
                        }
                    }

                    "reduce" -> {
                        if (player.hurtTime > 0) {
                            if (player.onGround) {
                                if (player.hurtTime <= 6) {
                                    player.motionX *= 0.70
                                    player.motionZ *= 0.70
                                }
                                if (player.hurtTime <= 5) {
                                    player.motionX *= 0.80
                                    player.motionZ *= 0.80
                                }
                            } else if (player.hurtTime <= 10) {
                                player.motionX *= 0.60
                                player.motionZ *= 0.60
                            }
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (mainMode.get() == "AAC") {
            when (aacMode.get().lowercase()) {
                "aacpush" -> {
                    jump = true

                    if (!thePlayer.isCollidedVertically)
                        event.cancelEvent()
                }

                "aaczero" ->
                    if (thePlayer.hurtTime > 0)
                        event.cancelEvent()
            }
        }
    }

    @EventTarget
    override fun onEnable() {
        flag = false
        isMatrixOnGround = false
        grimTCancel = 0
        packets.clear()
        blink = false
        templateX = 0
        templateY = 0
        templateZ = 0
        motionXZ = 0.01
        unReduceTimes = 0
    }

    @EventTarget
    override fun onDisable() {
        lastGround = false
        mc.timer.timerSpeed = 1f
        flag = false
        isMatrixOnGround = false
        limitUntilJump = 0
        flagTimer.reset()
        velocityTimer.reset()
        templateX = 0
        templateY = 0
        templateZ = 0
        jump = false
        velocityInput = false
        clearPackets()
    }

    private fun handleVelocity(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            // Always cancel event and handle motion from here
            event.cancelEvent()

            if (horizontal.get() == 0f && vertical.get() == 0f)
                return

            // Don't modify player's motionXZ when horizontal value is 0
            if (horizontal.get() != 0f) {
                var motionX = packet.realMotionX
                var motionZ = packet.realMotionZ

                if (limitMaxMotionValue.get()) {
                    val distXZ = sqrt(motionX * motionX + motionZ * motionZ)

                    if (distXZ > maxXZMotion.get()) {
                        val ratioXZ = maxXZMotion.get() / distXZ

                        motionX *= ratioXZ
                        motionZ *= ratioXZ
                    }
                }

                mc.thePlayer.motionX = motionX * horizontal.get().toDouble()
                mc.thePlayer.motionZ = motionZ * horizontal.get().toDouble()
            }

            // Don't modify player's motionY when vertical value is 0
            if (vertical.get() != 0f) {
                var motionY = packet.realMotionY

                if (limitMaxMotionValue.get())
                    motionY = motionY.coerceAtMost(maxYMotion.get() + 0.00075)

                mc.thePlayer.motionY = motionY * vertical.get().toDouble()
            }
        } else if (packet is S27PacketExplosion) {
            // Don't cancel explosions, modify them, they could change blocks in the world
            if (horizontal.get() != 0f && vertical.get() != 0f) {
                packet.field_149152_f = 0f
                packet.field_149153_g = 0f
                packet.field_149159_h = 0f

                return
            }

            // Unlike with S12PacketEntityVelocity explosion packet motions get added to player motion, doesn't replace it
            // Velocity might behave a bit differently, especially LimitMaxMotion
            packet.field_149152_f *= horizontal.get() // motionX
            packet.field_149153_g *= vertical.get() // motionY
            packet.field_149159_h *= horizontal.get() // motionZ

            if (limitMaxMotionValue.get()) {
                val distXZ =
                    sqrt(packet.field_149152_f * packet.field_149152_f + packet.field_149159_h * packet.field_149159_h)
                val distY = packet.field_149153_g
                val maxYMotion = maxYMotion.get() + 0.00075f

                if (distXZ > maxXZMotion.get()) {
                    val ratioXZ = maxXZMotion.get() / distXZ

                    packet.field_149152_f *= ratioXZ
                    packet.field_149159_h *= ratioXZ
                }

                if (distY > maxYMotion) {
                    packet.field_149153_g *= maxYMotion / distY
                }
            }
        }
    }

    private fun getAllEntities(): List<Entity> {
        return mc.theWorld.loadedEntityList
            .filter { isSelected(it, true) }
            .toList()
    }

    private fun getNearestEntityInRange(): Entity? {
        val player = mc.thePlayer

        val entitiesInRange = getAllEntities()
            .filter {
                val distance = player.getDistanceToEntityBox(it)
                (distance <= range.get())
            }

        return entitiesInRange.minByOrNull { player.getDistanceToEntityBox(it) }
    }

    private fun checkBlock(pos: BlockPos): Boolean {
        if (!onlyAirValue.get() || mc.theWorld.isAirBlock(pos)) {
            if (sendC03Value.get()) {
                if (C06Value.get())
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C06PacketPlayerPosLook(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY,
                            mc.thePlayer.posZ,
                            mc.thePlayer.rotationYaw,
                            mc.thePlayer.rotationPitch,
                            mc.thePlayer.onGround
                        )
                    )
                else
                    mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
            }
            mc.netHandler.addToSendQueue(
                C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                    pos,
                    EnumFacing.DOWN
                )
            )
            if (worldValue.get())
                mc.theWorld.setBlockToAir(pos)
            return true
        }
        return false
    }

    private fun getDirection(): Double {
        var moveYaw = mc.thePlayer.rotationYaw
        if (mc.thePlayer.moveForward != 0f && mc.thePlayer.moveStrafing == 0f) {
            moveYaw += if (mc.thePlayer.moveForward > 0) 0 else 180
        } else if (mc.thePlayer.moveForward != 0f && mc.thePlayer.moveStrafing != 0f) {
            if (mc.thePlayer.moveForward > 0) moveYaw += if (mc.thePlayer.moveStrafing > 0) -45 else 45 else moveYaw -= if (mc.thePlayer.moveStrafing > 0) -45 else 45
            moveYaw += if (mc.thePlayer.moveForward > 0) 0 else 180
        } else if (mc.thePlayer.moveStrafing != 0f && mc.thePlayer.moveForward == 0f) {
            moveYaw += if (mc.thePlayer.moveStrafing > 0) -90 else 90
        }
        return Math.floorMod(moveYaw.toInt(), 360).toDouble()
    }

    private fun clearPackets() {
        if (blinkValue.get()) {
            while (!packets.isEmpty()) {
                PacketUtils.handlePacket(packets.take() as Packet<INetHandlerPlayClient?>)
            }
            if (blinkOutbound.get()) {
                BlinkUtils.setBlinkState(off = true, release = true)
            }
        }
    }

    private /*suspend*/ fun queuePacket(delayTime: Long) {
        Timer().schedule(delayTime) {
            PacketUtils.handlePacket(packets.poll() as Packet<INetHandlerPlayClient?>)
        }
    }

    private fun runSwing() {
        mc.netHandler.addToSendQueue(C0APacketAnimation())
    }

    private fun attackEntity(entity: EntityLivingBase) {
        val event = AttackEvent(entity)
        if (callEvent.get()) {
            FDPClient.eventManager.callEvent(event)
            if (event.isCancelled) return
        }
        runSwing()
        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))
    }

    private fun getMotionNoXZ(packetEntityVelocity: S12PacketEntityVelocity): Double {
        val strength: Double = Vec3(
            packetEntityVelocity.getMotionX().toDouble(),
            packetEntityVelocity.getMotionY().toDouble(),
            packetEntityVelocity.getMotionZ().toDouble()
        ).lengthVector()
        val motionNoXZ: Double = if (strength >= 20000.0) {
            if (mc.thePlayer.onGround) {
                0.06425
            } else {
                0.075
            }
        } else if (strength >= 5000.0) {
            if (mc.thePlayer.onGround) {
                0.02625
            } else {
                0.0552
            }
        } else {
            0.0175
        }
        return motionNoXZ
    }

    override val tag: String
        get() = when (mainMode.get().lowercase()) {
            "cancel" -> cancelMode.get()
            "matrix" -> matrixMode.get()
            "other" -> otherMode.get()
            "vanilla" -> vanillaMode.get()
            "aac" -> aacMode.get()
            "reverse" -> reverseMode.get()
            "grimac" -> grimMode.get()
            else -> {
                "Null"
            }
        }.toString()
}
