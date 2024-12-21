/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.*
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.utils.EntityUtils.getHealth
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.RaycastUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Items

@ModuleInfo(name = "AutoRod", category = ModuleCategory.COMBAT)
object AutoRod : Module() {

    private val facingEnemy = BoolValue("FacingEnemy", true)

    private val ignoreOnEnemyLowHealth = BoolValue("IgnoreOnEnemyLowHealth", true).displayable { facingEnemy.get() }
    private val healthFromScoreboard = BoolValue("HealthFromScoreboard", false).displayable { facingEnemy.get() && ignoreOnEnemyLowHealth.get() }
    private val absorption = BoolValue("Absorption", false).displayable { facingEnemy.get() && ignoreOnEnemyLowHealth.get() }

    private val activationDistance = FloatValue("ActivationDistance", 8f, 1f,20f)
    private val enemiesNearby = IntegerValue("EnemiesNearby", 1, 1,5)

    // Improve health check customization
    private val playerHealthThreshold = IntegerValue("PlayerHealthThreshold", 5, 1,20)
    private val enemyHealthThreshold = IntegerValue("EnemyHealthThreshold", 5, 1,20).displayable { facingEnemy.get() && ignoreOnEnemyLowHealth.get() }
    private val escapeHealthThreshold = IntegerValue("EscapeHealthThreshold", 10, 1,20)

    private val pushDelay = IntegerValue("PushDelay", 100, 50,1000)
    private val pullbackDelay = IntegerValue("PullbackDelay", 500, 50,1000)

    private val onUsingItem = BoolValue("OnUsingItem", false)

    private val pushTimer = MSTimer()
    private val rodPullTimer = MSTimer()

    private var rodInUse = false
    private var switchBack = -1

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // Check if player is using rod
        val usingRod = (mc.thePlayer.isUsingItem && mc.thePlayer.heldItem?.item == Items.fishing_rod) || rodInUse

        if (usingRod) {
            // Check if rod pull timer has reached delay
            // mc.thePlayer.fishEntity?.caughtEntity != null is always null

            if (rodPullTimer.hasTimePassed(pullbackDelay.get().toLong())) {
                if (switchBack != -1 && mc.thePlayer.inventory.currentItem != switchBack) {
                    // Switch back to previous item
                    mc.thePlayer.inventory.currentItem = switchBack
                    mc.playerController.updateController()
                } else {
                    // Stop using rod
                    mc.thePlayer.stopUsingItem()
                }

                switchBack = -1
                rodInUse = false

                // Reset push timer. Push will always wait for pullback delay.
                pushTimer.reset()
            }
        } else {
            var rod = false

            if (facingEnemy.get() && getHealth(mc.thePlayer, healthFromScoreboard.get(), absorption.get()) >= playerHealthThreshold.get()) {
                var facingEntity = mc.objectMouseOver?.entityHit
                val nearbyEnemies = getAllNearbyEnemies()

                if (facingEntity == null) {
                    // Check if player is looking at enemy.
                    facingEntity = RaycastUtils.raycastEntity(activationDistance.get().toDouble()) { isSelected(it, true) }
                }

                // Check whether player is using items/blocking.
                if (!onUsingItem.get()) {
                    if (mc.thePlayer?.itemInUse?.item != Items.fishing_rod && (mc.thePlayer?.isUsingItem == true || KillAura.blockingStatus)) {
                        return
                    }
                }

                if (isSelected(facingEntity ?: return, true)) {
                    // Checks how many enemy is nearby, if <= then should rod.
                    if (nearbyEnemies?.size!! <= enemiesNearby.get()) {

                        // Check if the enemy's health is below the threshold.
                        if (ignoreOnEnemyLowHealth.get()) {
                            if (getHealth(facingEntity as EntityLivingBase, healthFromScoreboard.get(), absorption.get()) >= enemyHealthThreshold.get()) {
                                rod = true
                            }
                        } else {
                            rod = true
                        }
                    }
                }
            } else if (getHealth(mc.thePlayer, healthFromScoreboard.get(), absorption.get()) <= escapeHealthThreshold.get()) {
                // use rod for escaping when health is low.
                rod = true
            } else if (!facingEnemy.get()) {
                // Rod anyway, spam it.
                rod = true
            }

            if (rod && pushTimer.hasTimePassed(pushDelay.get().toLong())) {
                // Check if player has rod in hand
                if (mc.thePlayer.heldItem?.item != Items.fishing_rod) {
                    // Check if player has rod in hotbar
                    val rod = findRod()

                    if (rod == -1) {
                        // There is no rod in hotbar
                        return
                    }

                    // Switch to rod
                    switchBack = mc.thePlayer.inventory.currentItem

                    mc.thePlayer.inventory.currentItem = rod - 36
                    mc.playerController.updateController()
                }

                rod()
            }
        }
    }

    /**
     * Use rod
     */
    private fun rod() {
        val rod = findRod()

        mc.thePlayer.inventory.currentItem = rod - 36
        // We do not need to send our own packet, because sendUseItem will handle it for us.
        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventoryContainer.getSlot(rod).stack)

        rodInUse = true
        rodPullTimer.reset()
    }

    /**
     * Find rod in inventory
     */
    private fun findRod(
    ): Int {
        for (i in 36 until 45) {
            val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (stack != null && stack.item === Items.fishing_rod) {
                return i
            }
        }
        return -1
    }

    fun getAllNearbyEnemies(): List<Entity>? {
        val player = mc.thePlayer ?: return null

        return mc.theWorld.loadedEntityList.toList()
            .filter { isSelected(it, true) }
            .filter { player.getDistanceToEntityBox(it) < activationDistance.get() }
    }

}