/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.init.Items.egg
import net.minecraft.init.Items.snowball

@ModuleInfo(name = "AutoProjectile", description = "", category = ModuleCategory.COMBAT)
object AutoProjectile : Module() {
    private val facingEnemy = BoolValue("FacingEnemy", true)

    private val mode = ListValue("Mode", arrayOf("Normal", "Smart"), "Normal")
    private val range = FloatValue("Range", 8F, 1F,20F)
    private val throwDelay = IntegerValue("ThrowDelay", 1000, 50,2000) { mode.get() != "Smart" }

    private val minThrowDelay: IntegerValue = object : IntegerValue("MinThrowDelay", 1000, 50,2000) {
        override fun isSupported() = mode.get() == "Smart"
        override fun onChange(oldValue: Int, newValue: Int) {
            val v = maxThrowDelay.get()
            if (v < newValue) set(v)
        }
    }

    private val maxThrowDelay: IntegerValue = object : IntegerValue("MaxThrowDelay", 1500, 50,2000) {
        override fun isSupported() = mode.get() == "Smart"
        override fun onChange(oldValue: Int, newValue: Int) {
            val v = minThrowDelay.get()
            if (v > newValue) set(v)
        }
    }

    private val switchBackDelay = IntegerValue("SwitchBackDelay", 500, 50,2000)

    private val throwTimer = MSTimer()
    private val projectilePullTimer = MSTimer()

    private var projectileInUse = false
    private var switchBack = -1

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val usingProjectile = (mc.thePlayer.isUsingItem && (mc.thePlayer.heldItem?.item == snowball || mc.thePlayer.heldItem?.item == egg)) || projectileInUse

        if (usingProjectile) {
            if (projectilePullTimer.hasTimePassed(switchBackDelay.get().toLong())) {
                if (switchBack != -1 && mc.thePlayer.inventory.currentItem != switchBack) {
                    mc.thePlayer.inventory.currentItem = switchBack

                    mc.playerController.updateController()
                } else {
                    mc.thePlayer.stopUsingItem()
                }

                switchBack = -1
                projectileInUse = false

                throwTimer.reset()
            }
        } else {
            var throwProjectile = false

            if (facingEnemy.get()) {
                var facingEntity = mc.objectMouseOver?.entityHit

                if (facingEntity == null) {
                    facingEntity = raycastEntity(range.get().toDouble()) { isSelected(it, true) }
                }

                if (isSelected(facingEntity ?: return, true)) {
                    throwProjectile = true
                }
            } else {
                throwProjectile = true
            }

            if (throwProjectile) {
                if (mode.get() == "Normal" && throwTimer.hasTimePassed(throwDelay.get().toLong())) {
                    if (mc.thePlayer.heldItem?.item != snowball && mc.thePlayer.heldItem?.item != egg) {
                        val projectile = findProjectile()

                        if (projectile == -1) {
                            return
                        }

                        switchBack = mc.thePlayer.inventory.currentItem

                        mc.thePlayer.inventory.currentItem = projectile - 36
                        mc.playerController.updateController()
                    }

                    throwProjectile()
                }

                val randomThrowDelay = RandomUtils.nextInt(minThrowDelay.get(), maxThrowDelay.get())
                if (mode.get() == "Smart" && throwTimer.hasTimePassed(randomThrowDelay.toLong())) {
                    if (mc.thePlayer.heldItem?.item != snowball && mc.thePlayer.heldItem?.item != egg) {
                        val projectile = findProjectile()

                        if (projectile == -1) {
                            return
                        }

                        switchBack = mc.thePlayer.inventory.currentItem

                        mc.thePlayer.inventory.currentItem = projectile - 36
                        mc.playerController.updateController()
                    }

                    throwProjectile()
                }
            }
        }
    }

    /**
     * Throw projectile (snowball/egg)
     */
    private fun throwProjectile() {
        val projectile = findProjectile()

        mc.thePlayer.inventory.currentItem = projectile - 36

        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventoryContainer.getSlot(projectile).stack)

        projectileInUse = true
        projectilePullTimer.reset()
    }

    /**
     * Find projectile (snowball/egg) in inventory
     */
    private fun findProjectile(): Int {
        for (i in 36 until 45) {
            val stack = mc.thePlayer?.inventoryContainer?.getSlot(i)?.stack
            if (stack != null) {
                if (stack.item == snowball || stack.item == egg) {
                    return i
                }
            }
        }
        return -1
    }

    /**
     * Reset everything when disabled
     */
    override fun onDisable() {
        throwTimer.reset()
        projectilePullTimer.reset()
        projectileInUse = false
        switchBack = -1
    }

    /**
     * HUD Tag
     */
    override val tag: String
        get() = mode.get()
}