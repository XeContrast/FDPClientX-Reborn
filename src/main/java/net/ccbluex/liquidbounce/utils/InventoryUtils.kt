/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.PlayerUtils.getPing
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.block.Block
import net.minecraft.block.BlockBush
import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.potion.Potion

object InventoryUtils : MinecraftInstance(), Listenable {
    val CLICK_TIMER = MSTimer()
    val INV_TIMER = MSTimer()
    val BLOCK_BLACKLIST = listOf(Blocks.enchanting_table, Blocks.chest, Blocks.ender_chest, Blocks.trapped_chest,
        Blocks.anvil, Blocks.sand, Blocks.web, Blocks.torch, Blocks.crafting_table, Blocks.furnace, Blocks.waterlily,
        Blocks.dispenser, Blocks.stone_pressure_plate, Blocks.wooden_pressure_plate, Blocks.red_flower, Blocks.flower_pot, Blocks.yellow_flower,
        Blocks.noteblock, Blocks.dropper, Blocks.standing_banner, Blocks.wall_banner, Blocks.tnt)

    fun findItem(startSlot: Int, endSlot: Int, item: Item): Int {
        for (i in startSlot until endSlot) {
            val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (stack != null && stack.item === item) {
                return i
            }
        }
        return -1
    }

    fun hasSpaceHotbar(): Boolean {
        for (i in 36..44) {
            mc.thePlayer.inventoryContainer.getSlot(i).stack ?: return true
        }
        return false
    }

    @JvmStatic
    fun findAutoBlockBlock(): Int {
        for (i in 36..44) {
            val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (itemStack != null && itemStack.item is ItemBlock) {
                val itemBlock = itemStack.item as ItemBlock
                val block = itemBlock.getBlock()
                if (canPlaceBlock(block) && itemStack.stackSize > 0) {
                    return i
                }
            }
        }
        return -1
    }
    fun findAutoBlockBlock(biggest: Boolean): Int {
        if (biggest) {
            var a = -1
            var aa = 0
            for (i in 36..44) {
                if (mc.thePlayer.inventoryContainer.getSlot(i).hasStack) {
                    val aaa = mc.thePlayer.inventoryContainer.getSlot(i).stack.item
                    val aaaa = mc.thePlayer.inventoryContainer.getSlot(i).stack
                    if (aaa is ItemBlock && aaaa.stackSize > aa) {
                        aa = aaaa.stackSize
                        a = i
                    }
                }
            }
            return a
        } else {
            for (i in 36..44) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (itemStack != null && itemStack.item is ItemBlock) {
                    val itemBlock = itemStack.item as ItemBlock
                    val block = itemBlock.getBlock()
                    if (canPlaceBlock(block) && (mc.thePlayer.getPing() > 100 && itemStack.stackSize > 2 || itemStack.stackSize != 0)) {
                        return i
                    }
                }
            }
            return -1
        }
    }

    fun canPlaceBlock(block: Block): Boolean {
        return block.isFullCube && !BLOCK_BLACKLIST.contains(block)
    }

    fun isBlockListBlock(itemBlock: ItemBlock): Boolean {
        val block = itemBlock.getBlock()
        return BLOCK_BLACKLIST.contains(block) || !block.isFullCube
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C0EPacketClickWindow || packet is C08PacketPlayerBlockPlacement) {
            INV_TIMER.reset()
        }
        if (packet is C08PacketPlayerBlockPlacement) {
            CLICK_TIMER.reset()
        } else if (packet is C0EPacketClickWindow) {
            CLICK_TIMER.reset()
        }
    }

    fun openPacket() {
        mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
    }

    fun closePacket() {
        mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
    }

    fun isPositivePotionEffect(id: Int): Boolean {
        return id == Potion.regeneration.id || id == Potion.moveSpeed.id ||
                id == Potion.heal.id || id == Potion.nightVision.id ||
                id == Potion.jump.id || id == Potion.invisibility.id ||
                id == Potion.resistance.id || id == Potion.waterBreathing.id ||
                id == Potion.absorption.id || id == Potion.digSpeed.id ||
                id == Potion.damageBoost.id || id == Potion.healthBoost.id ||
                id == Potion.fireResistance.id
    }

    fun isPositivePotion(item: ItemPotion, stack: ItemStack): Boolean {
        item.getEffects(stack).forEach {
            if (isPositivePotionEffect(it.potionID)) {
                return true
            }
        }

        return false
    }

    fun getItemDurability(stack: ItemStack): Float {
        if (stack.isItemStackDamageable && stack.maxDamage> 0) {
            return (stack.maxDamage - stack.itemDamage) / stack.maxDamage.toFloat()
        }
        return 1f
    }

    fun swap(slot: Int, hotBarNumber: Int) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hotBarNumber, 2, mc.thePlayer)
    }

    override fun handleEvents() = true
    fun findBlockInHotbar(): Int {
        val player = mc.thePlayer ?: return -1
        val inventory = player.inventoryContainer

        return (36..44).filter {
            val stack = inventory.getSlot(it).stack ?: return@filter false
            val block = if (stack.item is ItemBlock) (stack.item as ItemBlock).block else return@filter false

            stack.item is ItemBlock && stack.stackSize > 0 && block !in BLOCK_BLACKLIST && block !is BlockBush
        }.minByOrNull { (inventory.getSlot(it).stack.item as ItemBlock).block.isFullCube } ?: -1
    }

    fun findLargestBlockStackInHotbar(): Int {
        val player = mc.thePlayer ?: return -1
        val inventory = player.inventoryContainer

        return (36..44).filter {
            val stack = inventory.getSlot(it).stack ?: return@filter false
            val block = if (stack.item is ItemBlock) (stack.item as ItemBlock).block else return@filter false

            stack.item is ItemBlock && stack.stackSize > 0 && block.isFullCube && block !in BLOCK_BLACKLIST && block !is BlockBush
        }.maxByOrNull { inventory.getSlot(it).stack.stackSize } ?: -1
    }
}
