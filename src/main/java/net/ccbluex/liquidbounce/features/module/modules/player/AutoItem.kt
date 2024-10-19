/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.FakeItemRender
import net.ccbluex.liquidbounce.utils.SpoofItemUtils
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.minecraft.enchantment.Enchantment
import net.minecraft.event.ClickEvent
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition


@ModuleInfo(name = "AutoItem", category = ModuleCategory.PLAYER)
object AutoItem : Module() {
    private val autoTool = BoolValue("AutoTool", true)
    private val fakeItem = BoolValue("FakeItem", false).displayable { autoTool.get() }
    private val switchBack = BoolValue("SwitchBack", false).displayable { autoTool.get() }
    private val onlySneaking = BoolValue("OnlySneaking", false).displayable { autoTool.get() }
    private val autoWeapon = BoolValue("AutoWeapon", false)
    private val onlySwordValue = BoolValue("OnlySword", false).displayable { autoWeapon.get() }
    private val silent = BoolValue("Spoof", false)
    private var formerSlot = 0
    private var attackEnemy = false
    private var prevItemWeapon = 0
    private var spoofTick = 0

    @EventTarget
    fun onClick(event: ClickBlockEvent) {
        if (autoTool.get()) switchSlot(event.clickedBlock ?: return)
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        attackEnemy = true
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (autoWeapon.get()) {
            if (event.packet is C02PacketUseEntity && event.packet.action == C02PacketUseEntity.Action.ATTACK &&
                attackEnemy
            ) {
                attackEnemy = false

                // Find best weapon in hotbar (#Kotlin Style)
                val (slot, _) = (0..8)
                    .map { Pair(it, mc.thePlayer.inventory.getStackInSlot(it)) }
                    .filter { it.second != null && (it.second.item is ItemSword || (it.second.item is ItemTool && !onlySwordValue.get())) }
                    .maxByOrNull {
                        (it.second.attributeModifiers["generic.attackDamage"].first()?.amount
                            ?: 0.0) + 1.25 * ItemUtils.getEnchantment(it.second, Enchantment.sharpness)
                    } ?: return

                if (slot == mc.thePlayer.inventory.currentItem) { // If in hand no need to swap
                    return
                }

                // Switch to best weapon
                if (!SpoofItemUtils.spoofing) {
                    prevItemWeapon = mc.thePlayer.inventory.currentItem
                    if (silent.get())
                        SpoofItemUtils.startSpoof(prevItemWeapon)
                }
                spoofTick = 15
                mc.thePlayer.inventory.currentItem = slot
                mc.playerController.updateController()


                // Resend attack packet
                mc.netHandler.addToSendQueue(event.packet)
                event.cancelEvent()
            }
        }
    }
    private fun switchSlot(blockPos: BlockPos) {
        var bestSpeed = 1F
        var bestSlot = -1

        val blockState = mc.theWorld.getBlockState(blockPos)

        if (onlySneaking.get() && !mc.thePlayer.isSneaking) return

        for (i in 0..8) {
            val item = mc.thePlayer.inventory.getStackInSlot(i) ?: continue
            val speed = item.getStrVsBlock(blockState.block)

            if (speed > bestSpeed) {
                bestSpeed = speed
                bestSlot = i
            }
        }

        if (bestSlot != -1 && mc.thePlayer.inventory.currentItem != bestSlot) {
            if (fakeItem.get() && FakeItemRender.fakeItem == -1) {
                FakeItemRender.fakeItem = mc.thePlayer.inventory.currentItem
            }
            if (formerSlot == -1) {
                formerSlot = mc.thePlayer.inventory.currentItem
            }
            mc.thePlayer.inventory.currentItem = bestSlot
        }

    }
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (autoTool.get()) {
            if (!mc.gameSettings.keyBindAttack.isKeyDown) {
                if (switchBack.get() && formerSlot != -1) {
                    mc.thePlayer.inventory.currentItem = formerSlot
                    formerSlot = -1
                }
                FakeItemRender.fakeItem = -1
            }
        }
        if (autoWeapon.get()) {
            if (spoofTick > 0) {
                if (spoofTick == 1) {
                    mc.thePlayer.inventory.currentItem
                    SpoofItemUtils.stopSpoof()
                }
                spoofTick--
            }
        }
    }
}