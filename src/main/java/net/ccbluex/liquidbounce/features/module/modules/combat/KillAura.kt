/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat


import kevin.utils.plus
import kevin.utils.times
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.extensions.offset
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight
import net.ccbluex.liquidbounce.features.module.modules.movement.StrafeFix
import net.ccbluex.liquidbounce.features.module.modules.movement.TargetStrafe
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeCam
import net.ccbluex.liquidbounce.features.module.modules.world.scaffold.Scaffold
import net.ccbluex.liquidbounce.features.module.modules.world.scaffold.Scaffold2
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.ClientUtils.runTimeTicks
import net.ccbluex.liquidbounce.utils.EntityUtils.isLookingOnEntities
import net.ccbluex.liquidbounce.utils.EntityUtils.rotation
import net.ccbluex.liquidbounce.utils.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.RotationUtils.Companion.getVectorForRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.Companion.isVisible
import net.ccbluex.liquidbounce.utils.RotationUtils.Companion.targetRotation
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.*
import net.minecraft.network.play.client.*
import net.minecraft.potion.Potion
import net.minecraft.util.*
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.util.*
import kotlin.math.*

@ModuleInfo(name = "KillAura", category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_R)
object KillAura : Module() {

    /**
     * OPTIONS
     */

    // CPS

    private val clickDisplay = BoolValue("Click-Options", true)

    private val maxCpsValue: IntegerValue = object : IntegerValue("MaxCPS", 12, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCpsValue.get()
            if (i > newValue) set(i)

            attackDelay = getAttackDelay(minCpsValue.get(), this.get())
        }
    }.displayable { !simulateCooldown.get() && clickDisplay.get() } as IntegerValue

    private val minCpsValue: IntegerValue = object : IntegerValue("MinCPS", 8, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCpsValue.get()
            if (i < newValue) set(i)

            attackDelay = getAttackDelay(this.get(), maxCpsValue.get())
        }
    }.displayable { !simulateCooldown.get() && clickDisplay.get() } as IntegerValue

    private val CpsReduceValue = BoolValue("CPSReduceVelocity", false) { clickDisplay.get() }

    // Attack Setting

    private val attackDisplay = BoolValue("Attack-Options", true)

    private val swingValue =
        ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal") { attackDisplay.get() }

    private val attackTimingValue =
        ListValue("AttackTiming", arrayOf("All", "Pre", "Post"), "All") { attackDisplay.get() }

    private val hitselectValue = BoolValue("HitSelect", false) { attackDisplay.get() }
    private val hitselectRangeValue = FloatValue(
        "HitSelectRange",
        3.0f,
        2f,
        4f
    ) { hitselectValue.get() && hitselectValue.stateDisplayable }

    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10) { attackDisplay.get() }
    private val clickOnly = BoolValue("ClickOnly", false) { attackDisplay.get() }
    private val simulateCooldown = BoolValue("CoolDown", false) { attackDisplay.get() }
    private val cooldownNoDupAtk =
        BoolValue("NoDuplicateAttack", false) { simulateCooldown.get() && attackDisplay.get() }

    // Range
    private val rangeDisplay = BoolValue("Range-Options", true)


    private val reachMode = ListValue("ReachMode", arrayOf("Normal","Air","TargetPosY"),"Normal")
    private val rangeValue: FloatValue = object : FloatValue("Target-Range", 3.0f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = scanRange.get()
            if (i < newValue) set(i)
        }
    }.displayable { rangeDisplay.get() && reachMode.equals("Normal") } as FloatValue

    private val groundRangeValue = FloatValue("GroundRange",3.0f,0f,8f) { reachMode.equals("Air") && rangeDisplay.get()}
    private val airRangeValue = FloatValue("AirRange",3.0f,0f,8f) { reachMode.equals("Air") && rangeDisplay.get() }

    private val smoothReach = ListValue("SmoothReach", arrayOf("Normal","Smooth"),"Normal") { reachMode.equals("TargetPosY") && rangeDisplay.get()}
    private val smoothRangeValue = FloatValue("SmoothReachRange",1.0f,0.0f,3.0f) { reachMode.equals("TargetPosY") && smoothReach.get() != "Normal" && rangeDisplay.get()}
    private val lowRangeValue = FloatValue("LowTargetPosYRange",3.0f,0f,8f) { reachMode.equals("TargetPosY") && smoothReach.get() == "Normal" && rangeDisplay.get()}
    private val middleRangeValue = FloatValue("MiddleTargetPosYRange",3.0f,0f,8f) { reachMode.equals("TargetPosY") && rangeDisplay.get()}
    private val highRangeValue = FloatValue("HighTargetPosYRange",3.0f,0f,8f) { reachMode.equals("TargetPosY") && smoothReach.get() == "Normal" && rangeDisplay.get() }

    private val scanRange = FloatValue("scanRange", 6f, 0f, 8f) { rangeDisplay.get() }
    private val throughWallsRange by FloatValue("ThroughWallsRange", 3f, 0f,8f) { rangeDisplay.get() }

    private val rangeSprintReducementValue =
        FloatValue("RangeSprintReduction", 0f, 0f, 0.4f) { rangeDisplay.get() }

    private val swingRangeValue = object : FloatValue("SwingRange", 5f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = max( scanRange.get(), rangeValue.get() )
            if (i < newValue) set(i)
            if (maxRange > newValue) set(maxRange)
        }
    }.displayable { rangeDisplay.get() } as FloatValue
    private val generateSpotBasedOnDistance by BoolValue("GenerateSpotBasedOnDistance", false)

    // Modes
    private val modeDisplay = BoolValue("Mode-Options", true)

    private val priorityValue = ListValue(
        "Priority", arrayOf(
            "Health",
            "Distance",
            "LivingTime",
            "Fov",
            "Armor",
            "HurtResistance",
            "HurtTime",
            "RegenAmplifier"
        ), "Health"
    ) { modeDisplay.get() }

    private val targetModeValue =
        ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch") { modeDisplay.get() }

    private val maxSwitchFOV = FloatValue(
        "MaxSwitchFOV",
        90f,
        30f,
        180f
    ) { targetModeValue.equals("Switch") && modeDisplay.get() }
    private val switchDelayValue = IntegerValue(
        "SwitchDelay",
        15,
        1,
        2000
    ) { targetModeValue.equals("Switch") && modeDisplay.get() }

    private val limitedMultiTargetsValue = IntegerValue(
        "LimitedMultiTargets",
        0,
        0,
        50
    ) { targetModeValue.equals("Multi") && modeDisplay.get() }

    // AutoBlock
    private val autoblockDisplay = BoolValue("AutoBlock-Settings", true)

    private val autoBlockValue =
        ListValue("AutoBlock", arrayOf("Range", "Fake", "Off"), "Range") { autoblockDisplay.get() }

    private val autoBlockRangeValue = object : FloatValue("AutoBlockRange", 5f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = scanRange.get()
            if (i < newValue) set(i)
        }
    }.displayable { !autoBlockValue.equals("Off") && autoBlockValue.stateDisplayable }
    private val autoBlockPacketValue = ListValue(
        "AutoBlockPacket",
        arrayOf(
            "AfterAttack",
            "Vanilla",
            "Delayed",
            "Legit",
            "Legit2",
            "OldIntave",
            "Test",
            "HoldKey",
            "KeyBlock",
            "Test2",
            "Blink",
            "Hypixel"
        ),
        "Vanilla"
    ) { autoBlockValue.equals("Range") && autoBlockValue.stateDisplayable }
    private val unmode = ListValue("UnBlockMode", arrayOf("Basic","Change","Empty"),"Basic") { autoBlockValue.equals("Range") && autoBlockValue.stateDisplayable }
    private val interactAutoBlockValue =
        BoolValue("InteractAutoBlock", false) { autoBlockPacketValue.stateDisplayable }
    private val smartAutoBlockValue =
        BoolValue("SmartAutoBlock", false) { autoBlockPacketValue.stateDisplayable }
    private val blockRateValue =
        IntegerValue("BlockRate", 100, 1, 100) { autoBlockPacketValue.stateDisplayable }
    private val legitBlockBlinkValue = BoolValue(
        "Legit2Blink",
        true
    ) { autoBlockPacketValue.stateDisplayable && autoBlockPacketValue.equals("Legit2") }
    private val blinkBlockMode = ListValue(
        "BlinkBlockType",
        arrayOf("Blatant", "Legit3tick", "Legit4tick", "Legit5tick", "Dynamic"),
        "Legit3tick"
    ) { autoBlockPacketValue.stateDisplayable && autoBlockPacketValue.equals("Blink") }
    private val alwaysBlockDisplayValue = BoolValue(
        "AlwaysRenderBlocking",
        true
    ) { autoBlockValue.stateDisplayable && autoBlockValue.equals("Range") }

    // Hit delay
    private val useHitDelay = BoolValue("UseHitDelay", false)
    private val hitDelayTicks = IntegerValue("HitDelayTicks", 1, 1, 5) { useHitDelay.get() }

    // Rotations
    private val rotationDisplay = BoolValue("Rotation Options:", true)

    private val rotationModeValue = ListValue(
        "RotationMode",
        arrayOf(
            "None",
            "LiquidBounce",
            "ForceCenter",
            "SmoothCenter",
            "SmoothLiquid",
            "LockView",
            "Optimal",
            "SmoothCustom"
        ),
        "LiquidBounce"
    ) { rotationDisplay.get() }

    private val customRotationValue = ListValue(
        "CustomRotationMode",
        arrayOf(
            "LiquidBounce",
            "Full",
            "HalfUp",
            "HalfDown",
            "CenterSimple",
            "CenterLine",
            "CenterLarge",
            "CenterDot",
            "MidRange",
            "HeadRange",
            "Optimal"
        ),
        "HalfUp"
    ) { rotationDisplay.get() && rotationModeValue.equals("SmoothCustom") }

    private val silentRotationValue =
        BoolValue("SilentRotation", true) { !rotationModeValue.equals("None") && rotationDisplay.get() }

    private val angleThresholdUntilReset by FloatValue("AngleThresholdUntilReset", 5f, 0.1f,180f)
    private val maxYawTurnSpeedValue: FloatValue = object : FloatValue("MaxYawTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minYawTurnSpeedValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { rotationDisplay.get() && rotationModeValue.get() in arrayOf("LiquidBounce","ForceCenter","Optimal") } as FloatValue

    private val minYawTurnSpeedValue: FloatValue = object : FloatValue("MinYawTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxYawTurnSpeedValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { rotationDisplay.get() && rotationModeValue.get() in arrayOf("LiquidBounce","ForceCenter","Optimal") } as FloatValue

    private val maxPitchTurnSpeedValue: FloatValue = object : FloatValue("MaxPitchTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPitchTurnSpeedValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { rotationDisplay.get() && rotationModeValue.get() in arrayOf("LiquidBounce","ForceCenter","Optimal") } as FloatValue

    private val minPitchTurnSpeedValue: FloatValue = object : FloatValue("MinPitchTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPitchTurnSpeedValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { rotationDisplay.get() && rotationModeValue.get() in arrayOf("LiquidBounce","ForceCenter","Optimal") } as FloatValue

    private val maxTurnSpeedValue: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeedValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { rotationDisplay.get() && rotationModeValue.get() in arrayOf("SmoothCustom","SmoothLiquid","SmoothCenter") } as FloatValue

    private val minTurnSpeedValue: FloatValue = object : FloatValue("MinTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeedValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { rotationDisplay.get() && rotationModeValue.get() in arrayOf("SmoothCustom","SmoothLiquid","SmoothCenter") } as FloatValue

    private val maxResetSpeedValue: FloatValue = object : FloatValue("MaxResetTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minResetSpeedValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { rotationDisplay.get() } as FloatValue

    private val minResetSpeedValue: FloatValue = object : FloatValue("MinResetTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxResetSpeedValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { rotationDisplay.get() } as FloatValue

    private val rotationSmoothModeValue = ListValue(
        "SmoothMode",
        arrayOf("Custom", "Line", "Quad", "Sine", "QuadSine","QBC"),
        "Custom"
    ) {
        rotationDisplay.get() && rotationModeValue.get() in arrayOf("SmoothCustom","SmoothLiquid","SmoothCenter")
    }
    private val rotationSmoothValue = FloatValue(
        "CustomSmooth",
        2f,
        1f,
        10f
    ) { rotationSmoothModeValue.equals("Custom") && rotationSmoothModeValue.stateDisplayable }

    // Random Value
    private val randomCenterModeValue = ListValue(
        "RandomCenter",
        arrayOf("Off", "Cubic", "Horizontal", "Vertical","Noise","Gaussian","PerlinNoise"),
        "Off"
    ) { rotationDisplay.get() }
    private val randomCenRangeValue = FloatValue(
        "RandomRange",
        0.0f,
        0.0f,
        1.2f
    ) { !randomCenterModeValue.equals("Off") && rotationDisplay.get() }
    val minPitchFactor = FloatValue("Min Pitch Factor", 0F, 0F, 1F) {
        randomCenterModeValue.get() == "Noise"
    }
    val minYawFactor = FloatValue("Min Yaw Factor", 0F, 0F, 1F) {
        randomCenterModeValue.get() == "Noise"
    }
    val maxPitchFactor = FloatValue("Max Pitch Factor", 0F, 0F, 1F) {
        randomCenterModeValue.get() == "Noise"
    }
    val maxYawFactor = FloatValue("Max Yaw Factor", 0F, 0F, 1F) {
        randomCenterModeValue.get() == "Noise"
    }
    val dynamicPitchFactor = FloatValue("Dynaimc Pitch Factor", 0F, 0F, 1F) {
        randomCenterModeValue.get() == "Noise"
    }
    val dynamicYawFactor = FloatValue("Dynaimc Yaw Factor", 0F, 0F, 1F) {
        randomCenterModeValue.get() == "Noise"
    }
    val tolerance = FloatValue("Tolerance", 0.1f, 0.01f, 0.1f) {
        randomCenterModeValue.get() == "Noise"
    }
    val minSpeed = FloatValue("Min Speed", 0.1f, 0.01f, 1F) {
        randomCenterModeValue.get() == "Noise"
    }
    val maxSpeed = FloatValue("Max Speed", 0.2f, 0.01f, 1F) {
        randomCenterModeValue.get() == "Noise"
    }

    // Keep Rotate
    private val rotationRevValue = BoolValue(
        "RotationReverse",
        false
    ) { !rotationModeValue.equals("None") && rotationDisplay.get() }
    private val keepDirectionValue =
        BoolValue("KeepDirection", true) { !rotationModeValue.equals("None") && rotationDisplay.get() }
    private val keepDirectionTickValue = IntegerValue(
        "KeepDirectionTick",
        15,
        1,
        20
    ) { keepDirectionValue.get() && keepDirectionValue.stateDisplayable }
    private val rotationDelayValue =
        BoolValue("RotationDelay", false) { !rotationModeValue.equals("None") && rotationDisplay.get() }
    private val rotationDelayMSValue = IntegerValue(
        "RotationDelayMS",
        300,
        0,
        1000
    ) { rotationDelayValue.get() && rotationDelayValue.stateDisplayable }

    private val fovValue = FloatValue("FOV", 180f, 0f, 180f) { rotationDisplay.get() }

    // Predict
    private val predictValue =
        BoolValue("Predict", true) { !rotationModeValue.equals("None") && rotationDisplay.get() }

    private val maxPredictSizeValue: FloatValue = object : FloatValue("MaxPredictSize", 1f, -2f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictSizeValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { predictValue.stateDisplayable && predictValue.get() } as FloatValue

    private val minPredictSizeValue: FloatValue = object : FloatValue("MinPredictSize", 1f, -2f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictSizeValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { predictValue.stateDisplayable && predictValue.get() } as FloatValue

    // Bypass
    private val bypassDisplay = BoolValue("Bypass-Options", true)

    private val raycastValue = BoolValue("RayCast", true) { bypassDisplay.get() }
    private val raycastTargetValue =
        BoolValue("RaycastOnlyTarget", false) { raycastValue.get() && raycastValue.stateDisplayable }
    private val raycastIgnored = BoolValue(
        "RayCastIgnored",
        false
    ) { raycastValue.get() && rotationModeValue.get() != "None" }
    private val livingRaycast = BoolValue("LivingRayCast", true) { raycastValue.get() && rotationModeValue.get() != "None" }

    private val failRateValue = FloatValue("FailRate", 0f, 0f, 100f) { bypassDisplay.get() }
    private val fakeSwingValue =
        BoolValue("FakeSwing", true) { failRateValue.get() != 0f && failRateValue.stateDisplayable }
    private val rotationStrafeValue = ListValue(
        "Strafe",
        arrayOf("Off", "Strict", "Silent","Vanilla"),
        "Silent"
    ) { silentRotationValue.get() && !rotationModeValue.equals("None") && bypassDisplay.get() }

    // Tools
    private val toolsDisplay = BoolValue("Tools-Options", true)

    private val noScaffValue = BoolValue("NoScaffold", false) { toolsDisplay.get() }
    private val noFlyValue = BoolValue("NoFly", false) { toolsDisplay.get() }
    private val noEat = BoolValue("NoEat", false) { toolsDisplay.get() }
    private val noBlocking = BoolValue("NoBlocking", false) { toolsDisplay.get() }
    private val noBadPacketsValue = BoolValue("NoBadPackets", false) { toolsDisplay.get() }
    private val noInventoryAttackValue =
        ListValue("NoInvAttack", arrayOf("Spoof", "CancelRun", "Off"), "Off") { toolsDisplay.get() }
    private val noInventoryDelayValue = IntegerValue(
        "NoInvDelay",
        200,
        0,
        500
    ) { !noInventoryAttackValue.equals("Off") && noInventoryAttackValue.stateDisplayable }
    private val onSwording = BoolValue("OnSword", false) { toolsDisplay.get() }
    private val displayDebug = BoolValue("Debug", false) { toolsDisplay.get() }

    private val displayMode = ListValue("DisplayMode", arrayOf("Simple", "LessSimple", "Complicated"), "Simple")

    /**
     * MODULE
     */

    // Target
    var currentTarget: EntityLivingBase? = null
    private var hitable = false
    private var packetSent = false
    private val prevTargetEntities = mutableListOf<Int>()
    private val discoveredTargets = mutableListOf<EntityLivingBase>()
    private val inRangeDiscoveredTargets = mutableListOf<EntityLivingBase>()
    private val canFakeBlock: Boolean
        get() = inRangeDiscoveredTargets.isNotEmpty()

    // Attack delay
    private val attackTimer = MSTimer()
    private val switchTimer = MSTimer()
    private val rotationTimer = MSTimer()
    private var attackDelay = 0L
    var clicks = 0
    private var attackTickTimes = mutableListOf<Pair<MovingObjectPosition, Int>>()

    // Container Delay
    private var containerOpen = -1L

    // Swing
    private var canSwing = false

    // Last Tick Can Be Seen
    private var lastCanBeSeen = false

    // Fake block status
    var blockingStatus = false

    var attack = false

    val displayBlocking: Boolean
        get() = blockingStatus || (((autoBlockValue.equals("Fake") || (alwaysBlockDisplayValue.get() && autoBlockValue.equals(
            "Range"
        ))) && canFakeBlock))

    private var predictAmount = RandomUtils.nextFloat(minPredictSizeValue.get(), maxPredictSizeValue.get()).takeIf { predictValue.get() } ?: 1.0f

    // hit select
    private var canHitselect = false
    private val hitselectTimer = MSTimer()

    private val delayBlockTimer = MSTimer()
    private var delayBlock = false
    private var legitBlocking = 0
    private var legitCancelAtk = false

    private var test2_block = false
    private var wasBlink = false

    /**
     * Enable kill aura module
     */
    override fun onEnable() {
        mc.thePlayer ?: return
        mc.theWorld ?: return
        lastCanBeSeen = false
        delayBlock = false
        legitBlocking = 0

        updateTarget()
    }

    /**
     * Disable kill aura module
     */
    override fun onDisable() {
        FDPClient.moduleManager[TargetStrafe::class.java]!!.doStrafe = false
        currentTarget = null
        hitable = false
        packetSent = false
        prevTargetEntities.clear()
        discoveredTargets.clear()
        inRangeDiscoveredTargets.clear()
        attackTimer.reset()
        attackTickTimes.clear()
        clicks = 0
        canSwing = false
        attack = false

        stopBlocking()
        if (autoBlockPacketValue.equals("HoldKey") || autoBlockPacketValue.equals("KeyBlock")) {
            mc.gameSettings.keyBindUseItem.pressed = false
        }

        RotationUtils.serverRotation.let {
            RotationUtils.setTargetRotationReverse(
                it,
                keepDirectionTickValue.get().takeIf { keepDirectionValue.get() } ?: 1,
                minResetSpeedValue.get() to maxResetSpeedValue.get(),
                angleThresholdUntilReset
            )
        }
        if (wasBlink) {
            BlinkUtils.setBlinkState(off = true, release = true)
            wasBlink = false
        }
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender2D(
        event: Render2DEvent
    ) {
        if (displayDebug.get()) {
            val player = mc.thePlayer ?: return
            val sr = ScaledResolution(mc)
            val blockingStatus = player.isBlocking
            val maxRange = this.maxRange

            val reach = player.getDistanceToEntityBox(currentTarget ?: return)

            val formattedReach = String.format("%.2f", reach)

            val rangeString = "Range: $maxRange"
            val reachString = "Reach: $formattedReach"

            val status =
                "Blocking: ${if (blockingStatus) "Yes" else "No"}, CPS: $clicks, $reachString, $rangeString"
            Fonts.minecraftFont.drawStringWithShadow(
                status,
                sr.scaledWidth / 2f - Fonts.minecraftFont.getStringWidth(status) / 2f,
                sr.scaledHeight / 2f - 60f,
                Color.orange.rgb
            )
        }
    }

    /**
     * Motion event
     */
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST) {
            packetSent = false
        }

        updateHitable()
        val target = this.currentTarget ?: discoveredTargets.getOrNull(0) ?: return

        if (autoBlockValue.equals("Range") && autoBlockPacketValue.equals("HoldKey") && canBlock) {
            if (inRangeDiscoveredTargets.isEmpty()) {
                mc.gameSettings.keyBindUseItem.pressed = false
            } else if (mc.thePlayer.getDistanceToEntityBox(target) < maxRange) {
                mc.gameSettings.keyBindUseItem.pressed = true
            }
        }


        if ((attackTimingValue.equals("Pre") && event.eventState != EventState.PRE) || (attackTimingValue.equals("Post") && event.eventState != EventState.POST) || attackTimingValue.equals(
                "All"
            ) || attackTimingValue.equals("Both")
        )
            return

        runAttackLoop()

    }

    @EventTarget
    fun onWorldChange(event: WorldEvent) {
        attackTickTimes.clear()
    }

    /**
     * Update event
     */
    @EventTarget
    fun onUpdate(ignoredEvent: UpdateEvent) {
        if (clickOnly.get() && !mc.gameSettings.keyBindAttack.isKeyDown) return

        if (cancelRun) {
            currentTarget = null
            hitable = false
            stopBlocking()
            discoveredTargets.clear()
            inRangeDiscoveredTargets.clear()
            if (wasBlink) {
                BlinkUtils.setBlinkState(off = true, release = true)
                wasBlink = false
            }
            return
        }

        if (autoBlockPacketValue.equals("Hypixel") && blockingStatus && autoBlockValue.equals("Range")) {
            val curritem = mc.thePlayer.inventory.currentItem
            val nextitem = (curritem + 1) % 9
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(nextitem))
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(curritem))
        }

        when (reachMode.get().lowercase()) {
            "air" -> {
                val reach = groundRangeValue.get().takeIf { mc.thePlayer?.onGround ?: return } ?: airRangeValue.get()
                rangeValue.set(reach)
            }
            "targetposy" -> this.currentTarget?.let {
                val posY = mc.thePlayer?.posY ?: return
                val smooth = (it.posY - posY) * smoothRangeValue.get() + middleRangeValue.get()
                val reach = when {
                    it.posY > posY -> highRangeValue.get()
                    it.posY == posY -> middleRangeValue.get()
                    else -> lowRangeValue.get()
                }.takeIf { smoothReach.get() == "Normal" } ?: smooth
                rangeValue.set(reach)
            } ?: rangeValue.set(middleRangeValue.get())
        }

        if (noInventoryAttackValue.equals("CancelRun") && (mc.currentScreen is GuiContainer ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())
        ) {
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            if (wasBlink) {
                BlinkUtils.setBlinkState(off = true, release = true)
                wasBlink = false
            }
            return
        }

        updateTarget()

        if (discoveredTargets.isEmpty()) {
            stopBlocking()
            if (wasBlink) {
                BlinkUtils.setBlinkState(off = true, release = true)
                wasBlink = false
            }
            return
        }


        FDPClient.moduleManager[TargetStrafe::class.java]!!.targetEntity = currentTarget ?: return

        FDPClient.moduleManager[StrafeFix::class.java]!!.applyForceStrafe(
            rotationStrafeValue.equals("Silent"),
            !rotationStrafeValue.equals("Off") && !rotationModeValue.equals("None")
        )

        val target = this.currentTarget ?: discoveredTargets.getOrNull(0) ?: return

        if (autoBlockValue.equals("Range")) {
            if (autoBlockPacketValue.equals("Test")) {
                if (mc.thePlayer.swingProgressInt == 1) {
                    startBlocking(
                        target,
                        interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange)
                    )
                }
            }

            if (autoBlockPacketValue.equals("Legit2")) {
                if (mc.thePlayer.ticksExisted % 4 == 1 && (!smartAutoBlockValue.get() || mc.thePlayer.hurtTime < 3)) {
                    if (legitBlockBlinkValue.get() || wasBlink) {
                        BlinkUtils.setBlinkState(off = true, release = true)
                        wasBlink = false
                    }
                    startBlocking(
                        target,
                        interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange)
                    )
                } else if (mc.thePlayer.ticksExisted % 4 == 3 || (smartAutoBlockValue.get() && mc.thePlayer.hurtTime > 3)) {
                    if (legitBlockBlinkValue.get()) {
                        BlinkUtils.setBlinkState(all = true)
                        wasBlink = true
                    }
                    stopBlocking()
                }
            }

            if (autoBlockPacketValue.equals("Blink")) {
                if (mc.thePlayer.ticksExisted % 2 == 1 && blinkBlockMode.equals("Blatant")) {
                    if (blockingStatus) {
                        BlinkUtils.setBlinkState(all = true)
                        wasBlink = true
                        stopBlocking()
                    }
                }
            }


            legitCancelAtk = false
            if (autoBlockPacketValue.equals("Legit")) {
                if (mc.thePlayer.hurtTime > 8) {
                    legitBlocking = 0
                    if (blockingStatus) {
                        stopBlocking()
                        blockingStatus = false
                        legitCancelAtk = true
                    }
                } else {
                    if (mc.thePlayer.hurtTime == 1) {
                        legitBlocking = 3
                    } else if (legitBlocking > 0) {
                        legitBlocking--
                        // this code is correct u idiots
                        if (discoveredTargets.isNotEmpty() && !blockingStatus) {
                            val target = this.currentTarget ?: discoveredTargets.first()
                            startBlocking(
                                target,
                                interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange)
                            )
                            blockingStatus = true
                        }
                        if (clicks > 2)
                            clicks = 2
                        legitCancelAtk = true
                    } else {
                        if (!canHitselect && hitselectValue.get()) {
                            legitBlocking = 3
                        } else {
                            if (blockingStatus) stopBlocking()
                            blockingStatus = false
                            legitCancelAtk = true
                            // prevent hypixel flag
                        }
                    }
                }
            }
        }


        if (attackTimingValue.equals("All")) {
            runAttackLoop()
        }

        if (legitBlocking < 1 && autoBlockPacketValue.equals("Legit")) {
            if (blockingStatus) stopBlocking()
            blockingStatus = false
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (rotationStrafeValue.equals("Vanilla")) {
            targetRotation?.let {
                val (yaw) = it
                event.yaw = yaw
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (rotationStrafeValue.equals("Vanilla")) {
            targetRotation?.let {
                val (yaw) = it
                event.yaw = yaw
            }
        }
    }

    private fun runAttackLoop() {

        if (CpsReduceValue.get() && mc.thePlayer.hurtTime > 8) {
            clicks += 4
        }

        // hit select (take damage to get yvelo to crit, for legit killaura)
        if (hitselectValue.get()) {
            if (canHitselect) {
                if (inRangeDiscoveredTargets.isEmpty() && hitselectTimer.hasTimePassed(900L)) canHitselect = false
            } else {
                if (mc.thePlayer.hurtTime > 7) {
                    canHitselect = true
                    hitselectTimer.reset()
                }
                inRangeDiscoveredTargets.forEachIndexed { _, entity ->
                    if (mc.thePlayer.getDistanceToEntityBox(
                            entity
                        ) < hitselectRangeValue.get()
                    ) canHitselect = true; hitselectTimer.reset()
                }
            }
            if (!canHitselect) {
                if (clicks > 0)
                    clicks = 1
                return
            }
        }

        if (autoBlockValue.equals("Range")) {
            when (autoBlockPacketValue.get().lowercase()) {
                "legit" -> if (legitCancelAtk) return
                "legit2" -> if (mc.thePlayer.ticksExisted % 4 > 0 && (!smartAutoBlockValue.get() || mc.thePlayer.hurtTime < 3)) return
                "test", "test2" -> {
                    if (blockingStatus) {
                        stopBlocking()
                        return
                    }
                }

                "blink" -> {
                    when (blinkBlockMode.get().lowercase()) {
                        "blatant" -> if (mc.thePlayer.ticksExisted % 2 == 1) return
                        "legit3tick", "legit4tick", "legit5tick" -> {
                            val blockTicks = when (blinkBlockMode.get().lowercase()) {
                                "legit3tick" -> 3
                                "legit4tick" -> 4
                                "legit5tick" -> 5
                                else -> 3
                            }
                            when (mc.thePlayer.ticksExisted % blockTicks) {
                                0 -> {
                                    if (blockingStatus) {
                                        BlinkUtils.setBlinkState(all = true)
                                        wasBlink = true
                                        stopBlocking()
                                    }
                                    return
                                }

                                blockTicks - 1 -> {
                                    blinkBlock()
                                    return
                                }

                            }
                        }

                        "dynamic" -> {
                            if (mc.thePlayer.hurtTime < 4) {
                                when (mc.thePlayer.ticksExisted % 3) {
                                    0 -> {
                                        if (blockingStatus) {
                                            BlinkUtils.setBlinkState(all = true)
                                            wasBlink = true
                                            stopBlocking()
                                        }
                                        return
                                    }

                                    2 -> {
                                        blinkBlock()
                                        return
                                    }

                                }
                            } else {
                                if (blockingStatus || wasBlink) {
                                    if (blockingStatus) stopBlocking()
                                    BlinkUtils.setBlinkState(off = true, release = true)
                                    wasBlink = false
                                    return
                                }
                            }
                        }
                    }
                }
            }

        }


        if (simulateCooldown.get() && CooldownHelper.getAttackCooldownProgress() < 1.0f) {
            return
        }

        if (simulateCooldown.get() && cooldownNoDupAtk.get() && clicks > 0) {
            clicks = 1
        }

        try {
            while (clicks > 0) {
                runAttack()
                clicks--
            }
        } catch (e: java.lang.IllegalStateException) {
            return
        }

        if (autoBlockValue.equals("Range") && autoBlockPacketValue.equals("Blink") && blinkBlockMode.equals("Blatant")) {
            blinkBlock()
        }

        test2_block = true
    }

    /**
     * Attack enemy
     */
    private fun runAttack() {
        currentTarget ?: return

        // Settings
        val failRate = failRateValue.get()
        val openInventory = noInventoryAttackValue.equals("Spoof") && mc.currentScreen is GuiInventory
        val failHit = failRate > 0 && Random().nextInt(100) <= failRate

        // Check is not hitable or check failrate
        if (hitable && !failHit) {
            // Close inventory when open
            if (openInventory) {
                mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
            }

            // Attack
            if (!targetModeValue.equals("Multi")) {
                attackEntity(if (raycastValue.get()) {
                    (raycastEntity(maxRange.toDouble()) {
                        it is EntityLivingBase && it !is EntityArmorStand && (!raycastTargetValue.get() || EntityUtils.canRayCast(
                            it
                        )) && !EntityUtils.isFriend(it)
                    } ?: currentTarget!!) as EntityLivingBase
                } else {
                    currentTarget!!
                })
            } else {
                inRangeDiscoveredTargets.forEachIndexed { index, entity ->
                    if (limitedMultiTargetsValue.get() == 0 || index < limitedMultiTargetsValue.get()) {
                        attackEntity(entity)
                    }
                }
            }

            if (targetModeValue.equals("Switch")) {
                if (switchTimer.hasTimePassed(switchDelayValue.get().toLong())) {
                    prevTargetEntities.add(currentTarget!!.entityId)
                    switchTimer.reset()
                }
            } else {
                prevTargetEntities.add(currentTarget!!.entityId)
            }

            // Open inventory
            if (openInventory) {
                mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
            }
        } else if (fakeSwingValue.get() || canSwing) {
            runSwing()
        }
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return
        // Settings
        val fov = fovValue.get()
        val switchMode = targetModeValue.equals("Switch")

        // Find possible targets
        discoveredTargets.clear()

        world.loadedEntityList
            .filterIsInstance<EntityLivingBase>()
            .filter { EntityUtils.isSelected(it,true) }
            .filter { !(switchMode && prevTargetEntities.contains(it.entityId)) }
            .forEach { entity ->

                var distance = player.getDistanceToEntityBox(entity)

                if (Backtrack.handleEvents()) {
                    val trackedDistance = Backtrack.getNearestTrackedDistance(entity)
                    distance = minOf(distance, trackedDistance)
                }

                val entityFov = RotationUtils.getRotationDifference(entity)

                if (distance <= scanRange.get() && (fov == 180F || entityFov <= fov)) {
                    if (switchMode && isLookingOnEntities(entity, maxSwitchFOV.get().toDouble()) || !switchMode)
                        discoveredTargets.add(entity)
                }
            }

        when (priorityValue.get().lowercase()) {
            "distance" -> discoveredTargets.sortBy {
                var result = 0.0

                Backtrack.runWithNearestTrackedDistance(it) {
                    result = mc.thePlayer.getDistanceToEntityBox(it) // Sort by distance
                }

                result } // Sort by distance
            "health" -> discoveredTargets.sortBy { it.health + it.absorptionAmount } // Sort by health
            "fov" -> discoveredTargets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> discoveredTargets.sortBy { -it.ticksExisted } // Sort by existence
            "armor" -> discoveredTargets.sortBy { it.totalArmorValue } // Sort by armor
            "hurttime" -> discoveredTargets.sortBy { it.hurtTime } // Sort by hurt time
            "hurtresistance" -> discoveredTargets.sortBy { it.hurtResistantTime } // hurt resistant time
            "regenamplifier" -> discoveredTargets.sortBy {
                if (it.isPotionActive(Potion.regeneration)) it.getActivePotionEffect(
                    Potion.regeneration
                ).amplifier else -1
            }
        }

        inRangeDiscoveredTargets.clear()
        inRangeDiscoveredTargets.addAll(discoveredTargets.filter { mc.thePlayer.getDistanceToEntityBox(it) < (swingRangeValue.get() - if (mc.thePlayer.isSprinting) rangeSprintReducementValue.get() else 0F) })

        // Cleanup last targets when no targets found and try again
        if (inRangeDiscoveredTargets.isEmpty() && prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
            return
        }

        // Find best target
        for (entity in inRangeDiscoveredTargets) {
            // Update rotations to current target
            if (!updateRotations(entity)) {
                var success = false

                Backtrack.runWithNearestTrackedDistance(entity) {
                    success = updateRotations(entity)
                }

                if (!success) {
                    // when failed then try another target
                    continue
                }

            }

            // Set target to current entity
            if (mc.thePlayer.getDistanceToEntityBox(entity) < scanRange.get()) {
                currentTarget = entity
                FDPClient.moduleManager[TargetStrafe::class.java]!!.targetEntity = currentTarget ?: return
                FDPClient.moduleManager[TargetStrafe::class.java]!!.doStrafe =
                    FDPClient.moduleManager[TargetStrafe::class.java]!!.toggleStrafe()
                return
            }
        }

        currentTarget = null
        FDPClient.moduleManager[TargetStrafe::class.java]!!.doStrafe = false
    }

    private fun runSwing() {
        when (swingValue.get()) {
            "packet" -> mc.netHandler.addToSendQueue(C0APacketAnimation())
            "Normal" -> mc.thePlayer.swingItem()
        }
    }

    /**
     * Attack [entity]
     * @throws IllegalStateException when bad packets protection
     */
    private fun attackEntity(entity: EntityLivingBase) {
        if (packetSent && noBadPacketsValue.get()) return
        if (mc.thePlayer.getDistanceToEntityBox(entity) > rangeValue.get())
            return

        // Call attack event
        val event = AttackEvent(entity)
        FDPClient.eventManager.callEvent(event)
        if (event.isCancelled) return

        // Stop blocking
        preAttack()

        // Attack target
        runSwing()
        packetSent = true
        mc.playerController.attackEntity(mc.thePlayer,entity)

        postAttack(entity)

        CooldownHelper.resetLastAttackedTicks()
    }

    private fun preAttack() {
        if (mc.thePlayer.isBlocking || blockingStatus) {
            when (autoBlockPacketValue.get().lowercase()) {
                "afterattack", "delayed" -> stopBlocking()
                "oldintave" -> {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    blockingStatus = false
                }

                "keyblock" -> mc.gameSettings.keyBindUseItem.pressed = false
            }
        }
    }

    private fun postAttack(entity: EntityLivingBase) {
        if (mc.thePlayer.isBlocking || (autoBlockValue.equals("Range") && canBlock)) {
            if (blockRateValue.get() > 0 && Random().nextInt(100) <= blockRateValue.get()) {
                if (smartAutoBlockValue.get() && clicks != 1 && mc.thePlayer.hurtTime < 4 && mc.thePlayer.getDistanceToEntityBox(
                        entity
                    ) < 4
                ) {
                    return
                }
                when (autoBlockPacketValue.get().lowercase()) {
                    "vanilla", "afterattack", "oldintave","hypixel" -> startBlocking(
                        entity,
                        interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(entity) < maxRange)
                    )

                    "delayed", "keyblock" -> delayBlockTimer.reset()
                }
            }
        }
    }

    /**
     * Update killaura rotations to enemy
     */
    private fun updateRotations(entity: Entity): Boolean {
        if (rotationModeValue.equals("None")) {
            return true
        }

        // 视角差异
        val entityFov = RotationUtils.getRotationDifference(
            RotationUtils.toRotation(
                RotationUtils.getCenter(entity.hitBox),
                true
            ), RotationUtils.serverRotation
        )

        // 可以被看见
        if (entityFov <= mc.gameSettings.fovSetting) lastCanBeSeen = true
        else if (lastCanBeSeen) { // 不可以被看见但是上一次tick可以看见
            rotationTimer.reset() // 重置计时器
            lastCanBeSeen = false
        }

        val prediction = entity.currPos.subtract(entity.prevPos).times(2 + predictAmount.toDouble())
        val boundingBox = entity.hitBox.offset(prediction)

        val rModes = mapOf(
            "LiquidBounce" to "LiquidBounce",
            "SmoothCenter" to "LiquidBounce",
            "ForceCenter" to "CenterLine",
            "SmoothCenter" to "CenterLine",
            "Optimal" to  "Optimal",
            "LockView" to  "CenterSimple",
            "SmoothCustom" to  customRotationValue.get()
        )

        val (_, directRotation) =
            RotationUtils.calculateCenter(
                rModes[rotationModeValue.get()],
                randomCenterModeValue.get(),
                (randomCenRangeValue.get()).toDouble(),
                boundingBox,
                predict = predictValue.get(),
                throughWalls = throughWallsRange,
                scanRange = scanRange.get(),
                attackRange = rangeValue.get(),
                distanceBasedSpot = generateSpotBasedOnDistance
            ) ?: return false


        var diffAngle = RotationUtils.getRotationDifference(RotationUtils.serverRotation, directRotation).coerceAtMost(180.0)
        if (diffAngle < 0) diffAngle = -diffAngle

        val calculateSpeed = when (rotationSmoothModeValue.get()) {
            "Custom" -> diffAngle / rotationSmoothValue.get()
            "Line" -> (diffAngle / 360) * maxTurnSpeedValue.get() + (1 - diffAngle / 360) * minTurnSpeedValue.get()
            "Quad" -> (diffAngle / 360.0).pow(2.0) * maxTurnSpeedValue.get() + (1 - (diffAngle / 360.0).pow(2.0)) * minTurnSpeedValue.get()
            "Sine" -> (-cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5) * maxTurnSpeedValue.get() + (cos(diffAngle / 360 * Math.PI) * 0.5 + 0.5) * minTurnSpeedValue.get()
            "QuadSine" -> (-cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5).pow(2.0) * maxTurnSpeedValue.get() + (1 - (-cos(
                diffAngle.toDegrees()
            ) * 0.5 + 0.5).pow(2.0)) * minTurnSpeedValue.get()
            "QBC" -> (1 - (sin(PI / 2 * ((diffAngle % 360) / 360.0)))) * minTurnSpeedValue.get() + (sin(PI / 2 * ((diffAngle % 360) / 360.0))) * maxTurnSpeedValue.get()

            else -> 360.0
        }

        if (!lastCanBeSeen && rotationDelayValue.get() && !rotationTimer.hasTimePassed(
                rotationDelayMSValue.get().toLong()
            )
        ) return true

        val rotation = when (rotationModeValue.get()) {
            "LiquidBounce", "ForceCenter", "Optimal" -> RotationUtils.limitAngleChange(
                RotationUtils.serverRotation,
                directRotation,
                RandomUtils.nextFloat(minYawTurnSpeedValue.get(), maxYawTurnSpeedValue.get()),
                RandomUtils.nextFloat(minPitchTurnSpeedValue.get(), maxPitchTurnSpeedValue.get())
            )

            "LockView" -> RotationUtils.limitAngleChange(
                RotationUtils.serverRotation,
                directRotation,
                180.0F
            )

            "SmoothCenter", "SmoothLiquid", "SmoothCustom" -> RotationUtils.limitAngleChange(
                RotationUtils.serverRotation,
                directRotation,
                (calculateSpeed).toFloat()
            )

            else -> return true
        }

        if (silentRotationValue.get()) {
            RotationUtils.setTargetRotationReverse(
                rotation,
                keepDirectionTickValue.get().takeIf { keepDirectionValue.get() } ?: 1,
                minResetSpeedValue.get() to maxResetSpeedValue.get(),
                angleThresholdUntilReset
            )
        } else {
            rotation.toPlayer(mc.thePlayer)
        }
        return true
    }

    /**
     * Check if enemy is hittable with current rotations
     */
    private fun updateHitable() {
        val eyes = mc.thePlayer.eyes

        val currentRotation = targetRotation ?: mc.thePlayer.rotation
        val target = this.currentTarget ?: return

        if (rotationModeValue.get() == "None") {
            hitable = mc.thePlayer.getDistanceToEntityBox(target) <= rangeValue.get()
            return
        }

        var chosenEntity: Entity? = null

        if (raycastValue.get()) {
            chosenEntity = raycastEntity(
                rangeValue.get().toDouble(),
                currentRotation.yaw,
                currentRotation.pitch
            ) { entity -> !livingRaycast.get() || entity is EntityLivingBase && entity !is EntityArmorStand }

            if (chosenEntity != null && chosenEntity is EntityLivingBase) {
                if (raycastIgnored.get() && target != chosenEntity) {
                    this.currentTarget = chosenEntity
                }
            }

            hitable = this.currentTarget == chosenEntity
        } else {
            hitable = RotationUtils.isRotationFaced(target, rangeValue.get().toDouble(), currentRotation)
        }

        var shouldExcept = false

        chosenEntity ?: this.currentTarget?.run {
            if (ForwardTrack.handleEvents()) {
                ForwardTrack.includeEntityTruePos(this) {
                    checkIfAimingAtBox(this, currentRotation, eyes, onSuccess = {
                        hitable = true

                        shouldExcept = true
                    })
                }
            }
        }

        if (!hitable || shouldExcept) {
            return
        }

        val targetToCheck = chosenEntity ?: this.currentTarget ?: return

        // If player is inside entity, automatic yes because the intercept below cannot check for that
        // Minecraft does the same, see #EntityRenderer line 353
        if (targetToCheck.hitBox.isVecInside(eyes)) {
            return
        }

        var checkNormally = true

        if (Backtrack.handleEvents()) {
            Backtrack.loopThroughBacktrackData(targetToCheck) {
                var result = false

                checkIfAimingAtBox(targetToCheck, currentRotation, eyes, onSuccess = {
                    checkNormally = false

                    result = true
                }, onFail = {
                    result = false
                })

                return@loopThroughBacktrackData result
            }
        } else if (ForwardTrack.handleEvents()) {
            ForwardTrack.includeEntityTruePos(targetToCheck) {
                checkIfAimingAtBox(targetToCheck, currentRotation, eyes, onSuccess = { checkNormally = false })
            }
        }

        if (!checkNormally) {
            return
        }

        // Recreate raycast logic
        val intercept = targetToCheck.hitBox.calculateIntercept(
            eyes,
            eyes + getVectorForRotation(currentRotation) * rangeValue.get().toDouble()
        )

        // Is the entity box raycast vector visible? If not, check through-wall range
        hitable =
            isVisible(intercept.hitVec) || mc.thePlayer.getDistanceToEntityBox(targetToCheck) <= throughWallsRange
    }

    private fun checkIfAimingAtBox(
        targetToCheck: Entity, currentRotation: Rotation, eyes: Vec3, onSuccess: () -> Unit,
        onFail: () -> Unit = { },
    ) {
        if (targetToCheck.hitBox.isVecInside(eyes)) {
            onSuccess()
            return
        }

        // Recreate raycast logic
        val intercept = targetToCheck.hitBox.calculateIntercept(
            eyes,
            eyes + getVectorForRotation(currentRotation) * rangeValue.get().toDouble()
        )

        if (intercept != null) {
            // Is the entity box raycast vector visible? If not, check through-wall range
            hitable =
                isVisible(intercept.hitVec) || mc.thePlayer.getDistanceToEntityBox(targetToCheck) <= throughWallsRange

            if (hitable) {
                onSuccess()
                return
            }
        }

        onFail()
    }

    /**
     * Start blocking
     */
    private fun startBlocking(interactEntity: Entity, interact: Boolean) {
        if (autoBlockValue.equals("Range") && mc.thePlayer.getDistanceToEntityBox(interactEntity) > autoBlockRangeValue.get()) {
            return
        }

        if (blockingStatus) {
            return
        }

        if (packetSent && noBadPacketsValue.get()) {
            return
        }

        val player = mc.thePlayer ?: return

        if (interact) {
            val positionEye = mc.renderViewEntity?.getPositionEyes(1F)

            interactEntity.collisionBorderSize.toDouble()
            val boundingBox = interactEntity.hitBox

            val (yaw, pitch) = targetRotation ?: player.rotation
            val yawCos = cos(-yaw.toRadians() - Math.PI.toFloat())
            val yawSin = sin(-yaw.toRadians() - Math.PI.toFloat())
            val pitchCos = -cos(-pitch.toRadians())
            val pitchSin = sin(-pitch.toRadians())
            val range = min(maxRange.toDouble(), mc.thePlayer!!.getDistanceToEntityBox(interactEntity)) + 1
            val lookAt =
                positionEye!!.addVector(yawSin * pitchCos * range, pitchSin * range, yawCos * pitchCos * range)

            val movingObject = boundingBox.calculateIntercept(positionEye, lookAt) ?: return
            val hitVec = movingObject.hitVec

            mc.netHandler.addToSendQueue(
                C02PacketUseEntity(
                    interactEntity, Vec3(
                        hitVec.xCoord - interactEntity.posX,
                        hitVec.yCoord - interactEntity.posY,
                        hitVec.zCoord - interactEntity.posZ
                    )
                )
            )
            //mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, C02PacketUseEntity.Action.INTERACT))
        }

        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blockingStatus = true
        packetSent = true
    }

    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockingStatus) {
            if (packetSent && noBadPacketsValue.get()) {
                return
            }
            when (unmode.get().lowercase()) {
                "basic" -> {
                    mc.netHandler.addToSendQueue(
                        C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                            BlockPos.ORIGIN, //if (MovementUtils.isMoving()) BlockPos(-1, -1, -1) else BlockPos.ORIGIN,
                            EnumFacing.DOWN
                        )
                    )
                }
                "Change" -> {
                    mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem + 1) % 9))
                    mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                }
                "empty" -> {
                    mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.firstEmptyStack))
                    mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                }
            }
            blockingStatus = false
            packetSent = true
        }
    }

    private fun blinkBlock() {
        BlinkUtils.setBlinkState(off = true, release = true)
        wasBlink = false
        val target = this.currentTarget ?: discoveredTargets.getOrNull(0) ?: return
        startBlocking(
            target,
            interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange)
        )
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (cancelRun) {
            currentTarget = null
            hitable = false
            stopBlocking()
            discoveredTargets.clear()
            inRangeDiscoveredTargets.clear()
        }

        currentTarget?.let {
            if (attackTimer.hasTimePassed(attackDelay) && it.hurtTime <= hurtTimeValue.get()) {
                clicks++
                attackTimer.reset()
                attackDelay = getAttackDelay(minCpsValue.get(), maxCpsValue.get())
            }
            if (attackTimer.hasTimePassed((attackDelay.toDouble() * 0.9).toLong()) && (autoBlockValue.equals(
                    "Range"
                ) && canBlock) && autoBlockPacketValue.equals("KeyBlock")
            ) {
                mc.gameSettings.keyBindUseItem.pressed = false
            }
            if (delayBlockTimer.hasTimePassed(30) && (autoBlockValue.equals("Range") && canBlock)) {
                if (autoBlockPacketValue.equals("KeyBlock"))
                    mc.gameSettings.keyBindUseItem.pressed = true
            }
        }
        if (autoBlockPacketValue.equals("Delayed")) {
            val target = this.currentTarget ?: discoveredTargets.getOrNull(0) ?: return
            startBlocking(
                target,
                interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange)
            )
        }

        if (autoBlockValue.equals("Range") && autoBlockPacketValue.equals("Test2") && !blockingStatus && test2_block) {
            if (discoveredTargets.isNotEmpty()) {
                val target = this.currentTarget ?: discoveredTargets.first()
                startBlocking(
                    target,
                    interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange)
                )
                blockingStatus = true
                test2_block = false
            }
        }
    }

    /**
     * Attack Delay
     */
    private fun getAttackDelay(minCps: Int, maxCps: Int): Long {
        return TimeUtils.randomClickDelay(minCps.coerceAtMost(maxCps), minCps.coerceAtLeast(maxCps))
    }

    /**
     * Check if raycast landed on a different object
     *
     * The game requires at least 1 tick of cooldown on raycast object type change (miss, block, entity)
     * We are doing the same thing here but allow more cool down.
     */

    // no finished
    private fun shouldDelayClick(type: MovingObjectPosition.MovingObjectType): Boolean {
        if (!useHitDelay.get()) {
            return false
        }

        val lastAttack = attackTickTimes.lastOrNull()

        return lastAttack != null && lastAttack.first.typeOfHit != type && runTimeTicks - lastAttack.second <= hitDelayTicks.get()
    }

    /**
     * Check if run should be cancelled
     */
    private val cancelRun: Boolean
        get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer)
                || FDPClient.moduleManager[FreeCam::class.java]!!.state
                || (noScaffValue.get() && FDPClient.moduleManager[Scaffold::class.java]!!.state)
                || (noScaffValue.get() && FDPClient.moduleManager[Scaffold2::class.java]!!.state)
                || (noFlyValue.get() && FDPClient.moduleManager[Flight::class.java]!!.state)
                || (noEat.get() && mc.thePlayer.isUsingItem && (mc.thePlayer.heldItem?.item is ItemFood || mc.thePlayer.heldItem?.item is ItemBucketMilk || mc.thePlayer.isUsingItem && (mc.thePlayer.heldItem?.item is ItemPotion)))
                || (noBlocking.get() && mc.thePlayer.isUsingItem && mc.thePlayer.heldItem?.item is ItemBlock)
                || (noInventoryAttackValue.equals("CancelRun") && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get()))
                || (onSwording.get() && mc.thePlayer.heldItem?.item !is ItemSword)


    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0

    /**
     * Check if player is able to block
     */
    private val canBlock: Boolean
        get() = mc.thePlayer?.heldItem?.item is ItemSword


    /**
     * Range
     */
    private val maxRange
        get() = max(rangeValue.get() + scanRange.get(), throughWallsRange)

    /**
     * HUD Tag
     */
    override val tag: String
        get() = when (displayMode.get().lowercase()) {
            "simple" -> (targetModeValue.get() + "${discoveredTargets.size.takeIf { targetModeValue.get() == "Switch" } ?: ""}")
            "lesssimple" -> rangeValue.get().toString() + " " + targetModeValue.get() + " " + autoBlockValue.get()
            "complicated" -> "M:" + targetModeValue.get() + ", AB:" + autoBlockValue.get() + ", R:" + rangeValue.get() + ", CPS:" + minCpsValue.get() + " - " + maxCpsValue.get()
            else -> targetModeValue.get() + ""
        }
}