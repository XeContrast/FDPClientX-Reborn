package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import kotlin.random.Random

@ModuleInfo(
    name = "TickBase",
    description = "Automatically speeds up/down when ??????",
    category = ModuleCategory.COMBAT
)
object TickBase : Module() {

    private var playerTicks = 0
    private var smartTick = 0
    private var cooldownTick = 0
    private var confirmTick = false
    private var confirmMove = false

    // Condition to prevent getting timer speed stuck
    private var confirmAttack = false

    // Condition to makesure timer isn't reset on lagback, when not attacking
    private var confirmLagBack = false

    // Condition to makesure timer isn't reset on knockback, when timer isn't changed
    private var confirmKnockback = false

    private val timerBoostMode = ListValue("TimerMode", arrayOf("Normal", "Smart", "SmartMove"), "Normal")

    private val ticksValue = IntegerValue("Ticks", 10, 1,20)
    private val timerBoostValue = FloatValue("TimerBoost", 1.5f, 0.01f,35f)
    private val timerChargedValue = FloatValue("TimerCharged", 0.45f, 0.05f,5f)

    // Normal Mode Settings
    private val rangeValue = FloatValue("Range", 3.5f, 1f,5f).displayable { timerBoostMode.get() == "Normal" }
    private val cooldownTickValue = IntegerValue("CooldownTick", 10, 1,50).displayable { timerBoostMode.get() == "Normal" }

    // Smart & SmartMove Mode Settings
    private val minRange = FloatValue("MinRange", 1f, 1f,5f).displayable { timerBoostMode.get() != "Normal" }
    private val maxRange = FloatValue("MaxRange", 5f, 1f,5f).displayable { timerBoostMode.get() != "Normal" }

    private val minTickDelay: IntegerValue = object : IntegerValue("MinTickDelay", 50, 1,100) {
        override fun onChange(oldValue: Int, newValue: Int) {
            newValue.coerceAtMost(maxTickDelay.get())
        }
    }.displayable {timerBoostMode.get() == "Smart"} as IntegerValue

    private val maxTickDelay: IntegerValue = object : IntegerValue("MaxTickDelay", 100, 1,100) {
        override fun onChange(oldValue: Int, newValue: Int) {
            newValue.coerceAtLeast(minTickDelay.get())
        }
    }.displayable {timerBoostMode.get() == "Smart"} as IntegerValue

    private val lookThreshold = FloatValue("LookThreshold", 0.5f, 0.1f,1f).displayable { timerBoostMode.get() == "SmartMove" }

    // Optional
    private val onlykillaura = BoolValue("OnlyKillaura",false)
    private val resetOnlagBack = BoolValue("ResetOnLagback", false)
    private val resetOnKnockback = BoolValue("ResetOnKnockback", false)
    private val chatDebug = BoolValue("ChatDebug", true).displayable { resetOnlagBack.get() || resetOnKnockback.get() }

    private fun timerReset() {
        mc.timer.timerSpeed = 1f
    }

    override fun onEnable() {
        timerReset()
    }

    override fun onDisable() {
        timerReset()
        smartTick = 0
        cooldownTick = 0
        playerTicks = 0
    }
    @EventTarget
    fun onTick(event: TickEvent) {
        if (onlykillaura.get() && !FDPClient.moduleManager[KillAura::class.java]!!.state) {
            return
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity !is EntityLivingBase || shouldResetTimer()) {
            timerReset()
            return
        } else {
            confirmAttack = true
        }

        val targetEntity = event.targetEntity
        val entityDistance = mc.thePlayer.getDistanceToEntityBox(targetEntity)
        val randomTickDelay = Random.nextInt(minTickDelay.get(), maxTickDelay.get())
        val randomRange = Random.nextDouble(minRange.get().toDouble(), maxRange.get().toDouble())

        smartTick++
        cooldownTick++

        val shouldSlowed = when (timerBoostMode.get()) {
            "Normal" -> cooldownTick >= cooldownTickValue.get() && entityDistance <= rangeValue.get()
            "Smart" -> smartTick >= randomTickDelay && entityDistance <= randomRange
            else -> false
        }

        if (shouldSlowed && confirmAttack) {
            confirmAttack = false
            playerTicks = ticksValue.get()

            if (resetOnKnockback.get()) {
                confirmKnockback = true
            }
            if (resetOnlagBack.get()) {
                confirmLagBack = true
            }
            cooldownTick = 0
            smartTick = 0
        } else {
            timerReset()
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (timerBoostMode.get() != "SmartMove") {
            return
        }

        val randomTickDelay = Random.nextInt(minTickDelay.get(), maxTickDelay.get())
        val randomRange = Random.nextDouble(minRange.get().toDouble(), maxRange.get().toDouble())

        if (isPlayerMoving()) {
            smartTick++

            if (smartTick >= randomTickDelay) {
                confirmTick = true
                smartTick = 0
            }
        } else {
            smartTick = 0
            confirmMove = false
        }

        val nearbyEntity = getNearestEntityInRange()

        if (nearbyEntity != null && isPlayerMoving()) {
            if (isLookingTowardsEntities(nearbyEntity)) {
                val entityDistance = mc.thePlayer.getDistanceToEntityBox(nearbyEntity)

                if (confirmTick && entityDistance <= randomRange) {
                    playerTicks = ticksValue.get()
                    confirmTick = false
                    confirmMove = true

                    if (resetOnKnockback.get()) {
                        confirmKnockback = true
                    }
                    if (resetOnlagBack.get()) {
                        confirmLagBack = true
                    }
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // Randomize the timer & charged delay a bit, to bypass some AntiCheat
        val timerboost = Random.nextDouble(0.5, 0.56)
        val charged = Random.nextDouble(0.75, 0.91)

        if (playerTicks <= 0) {
            timerReset()
            return
        }

        val tickProgress = playerTicks.toDouble() / ticksValue.get().toDouble()
        val playerSpeed = when {
            tickProgress < timerboost -> timerBoostValue.get()
            tickProgress < charged -> timerChargedValue.get()
            else -> 1f
        }

        val speedAdjustment = if (playerSpeed >= 0) playerSpeed else 1f + ticksValue.get() - playerTicks
        val adjustedTimerSpeed = maxOf(speedAdjustment, 0f)

        mc.timer.timerSpeed = adjustedTimerSpeed

        playerTicks--
    }

    /**
     * This check is useful to prevent player from changing speed while looking away
     * And prevent player from changing speed toward unintended entity/target while moving.
     */
    private fun isLookingTowardsEntities(entity: Entity): Boolean {
        val lookVec = mc.thePlayer.lookVec.normalize()
        val playerPos = mc.thePlayer.positionVector.addVector(0.0, mc.thePlayer.eyeHeight.toDouble(), 0.0)
        val entityPos = entity.positionVector.addVector(0.0, entity.eyeHeight.toDouble(), 0.0)

        val directionToEntity = entityPos.subtract(playerPos).normalize()
        val dotProductThreshold = lookVec.dotProduct(directionToEntity)

        // Player needs to be facing the entity/target with chosen dotproduct, in this case default threshold of at least 0.5
        return dotProductThreshold > lookThreshold.get()
    }

    /**
     * Check if player is moving
     */
    private fun isPlayerMoving(): Boolean {
        return mc.thePlayer.moveForward != 0f || mc.thePlayer.moveStrafing != 0f
    }

    /**
     * Get all entities in the world.
     */
    private fun getAllEntities(): List<Entity> {
        return mc.theWorld.loadedEntityList
            .filter { EntityUtils.isSelected(it, true) }
            .toList()
    }

    /**
     * Find the nearest entity in range.
     */
    private fun getNearestEntityInRange(): Entity? {
        val player = mc.thePlayer

        val entitiesInRange = getAllEntities()
            .filter { player.getDistanceToEntityBox(it) <= rangeValue.get() }

        return entitiesInRange.minByOrNull { player.getDistanceToEntityBox(it) }
    }

    /**
     * Separate condition to make it cleaner
     */
    private fun shouldResetTimer(): Boolean {
        return (playerTicks >= 1
                || mc.thePlayer.isSpectator || mc.thePlayer.isDead
                || mc.thePlayer.isInWater || mc.thePlayer.isInLava
                || mc.thePlayer.isInWeb || mc.thePlayer.isOnLadder
                || mc.thePlayer.isRiding)
    }

    /**
     * Lagback Reset is Inspired from Nextgen TimerRange
     * Reset Timer on Lagback & Knockback.
     */
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (isPlayerMoving() && !shouldResetTimer()
            && mc.timer.timerSpeed > 1.0 || mc.timer.timerSpeed < 1.0 ) {

            // Check for lagback
            if (resetOnlagBack.get() && confirmLagBack) {
                if (packet is S08PacketPlayerPosLook) {
                    confirmLagBack = false
                    timerReset()
                    if (chatDebug.get()) {
                        Chat.print("Lagback Received | Timer Reset")
                    }
                }
            }

            // Check for knockback
            if (resetOnKnockback.get() && confirmKnockback) {
                if (packet is S12PacketEntityVelocity && mc.thePlayer.entityId == packet.entityID
                    && packet.motionY > 0 && (packet.motionX.toDouble() != 0.0 || packet.motionZ.toDouble() != 0.0)) {
                    confirmKnockback = false
                    timerReset()
                    if (chatDebug.get()) {
                        Chat.print("Knockback Received | Timer Reset")
                    }
                }
            }
        }
    }

    /**
     * HUD Tag
     */
    override val tag: String
        get() = timerBoostMode.get()
}