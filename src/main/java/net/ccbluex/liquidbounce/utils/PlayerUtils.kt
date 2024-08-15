package net.ccbluex.liquidbounce.utils

import kevin.utils.multiply
import net.ccbluex.liquidbounce.features.module.modules.client.Animations
import net.ccbluex.liquidbounce.utils.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.minecraft.block.BlockSlime
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemBucketMilk
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import net.minecraft.potion.Potion
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraft.util.MovementInput

object PlayerUtils {
    fun randomUnicode(str: String): String {
        val stringBuilder = StringBuilder()
        for (c in str.toCharArray()) {
            if (Math.random()> 0.5 && c.code in 33..128) {
                stringBuilder.append(Character.toChars(c.code + 65248))
            } else {
                stringBuilder.append(c)
            }
        }
        return stringBuilder.toString()
    }
    fun EntityPlayer.stopXZ() {
        mc.thePlayer.motionZ = 0.0
        mc.thePlayer.motionX = 0.0
    }
    fun getIncremental(`val`: Double, inc: Double): Double {
        val one = 1.0 / inc
        return Math.round(`val` * one) / one
    }
    fun getAr(player : EntityLivingBase):Double{
        var arPercentage: Double = (player.totalArmorValue / player.maxHealth).toDouble()
        arPercentage = MathHelper.clamp_double(arPercentage, 0.0, 1.0)
        return 100 * arPercentage
    }
    fun EntityPlayer.getPing(): Int {
        val playerInfo = mc.netHandler.getPlayerInfo(uniqueID)
        return playerInfo?.responseTime ?: 0
    }
    fun getHp(player : EntityLivingBase):Double{
        val heal = player.health.toInt().toFloat()
        var hpPercentage: Double = (heal / player.maxHealth).toDouble()
        hpPercentage = MathHelper.clamp_double(hpPercentage, 0.0, 1.0)
        return 100 * hpPercentage
    }
    fun AxisAlignedBB.getLookingTargetRange(thePlayer: EntityPlayerSP, rotation: Rotation? = null, range: Double=6.0): Double {
        val eyes = thePlayer.eyes
        val movingObj = this.calculateIntercept(eyes, (rotation ?: RotationUtils.bestServerRotation())!!.toDirection().multiply(range).add(eyes)) ?: return Double.MAX_VALUE
        return movingObj.hitVec.distanceTo(eyes)
    }
    fun isUsingFood(): Boolean {
        val usingItem = mc.thePlayer.itemInUse.item
        return if (mc.thePlayer.itemInUse != null) {
            mc.thePlayer.isUsingItem && (usingItem is ItemFood || usingItem is ItemBucketMilk || usingItem is ItemPotion)
        } else false
    }
    fun isBlockUnder(): Boolean {
        if (mc.thePlayer.posY < 0) return false
        var off = 0
        while (off < mc.thePlayer.posY.toInt() + 2) {
            val bb: AxisAlignedBB = mc.thePlayer.entityBoundingBox
                .offset(0.0, -off.toDouble(), 0.0)
            if (mc.theWorld.getCollidingBoundingBoxes(
                    mc.thePlayer,
                    bb
                ).isNotEmpty()
            ) {
                return true
            }
            off += 2
        }
        return false
    }

    fun findSlimeBlock(): Int? {
        for (i in 0..8) {
            val itemStack = mc.thePlayer.inventory.getStackInSlot(i)
            if (itemStack != null && itemStack.item != null) if (itemStack.item is ItemBlock) {
                val block = itemStack.item as ItemBlock
                if (block.getBlock() is BlockSlime) return Integer.valueOf(i)
            }
        }
        return Integer.valueOf(-1)
    }
    fun swing() {
        val player: EntityPlayerSP = mc.thePlayer
        val swingEnd = (if (player.isPotionActive(Potion.digSpeed)) (6 - (1 + player.getActivePotionEffect(Potion.digSpeed).amplifier)) else (if (player.isPotionActive(Potion.digSlowdown)) (6 + (1 + player.getActivePotionEffect(Potion.digSlowdown).amplifier) * 2) else 6)) * if (Animations.state) Animations.swingSpeedValue.get() else 1F
        if (!player.isSwingInProgress || player.swingProgressInt >= swingEnd / 2 || player.swingProgressInt < 0) {
            player.swingProgressInt = -1
            player.isSwingInProgress = true
        }
    }

}
