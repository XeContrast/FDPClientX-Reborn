package net.ccbluex.liquidbounce.utils

import kevin.utils.minus
import kevin.utils.multiply
import net.ccbluex.liquidbounce.features.module.modules.client.Animations
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getState
import net.ccbluex.liquidbounce.utils.block.BlockUtils.toVec
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.minecraft.block.BlockSlime
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.*
import net.minecraft.entity.boss.EntityDragonPart
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.*
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.potion.Potion
import net.minecraft.stats.AchievementList
import net.minecraft.stats.StatList
import net.minecraft.util.*
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.event.ForgeEventFactory
import kotlin.math.roundToInt

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
        return (`val` * one).roundToInt() / one
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

    fun attackTargetEntityWithCurrentItem(entity: Entity) {
        if (ForgeHooks.onPlayerAttackTarget(mc.thePlayer, entity)) {
            if (entity.canAttackWithItem() && !entity.hitByEntity(
                    mc.thePlayer
                )
            ) {
                var f: Float =
                    mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.attackDamage).attributeValue.toFloat()
                var i = 0
                val f1: Float = if (entity is EntityLivingBase) {
                    EnchantmentHelper.getModifierForCreature(
                        mc.thePlayer.heldItem,
                        entity.creatureAttribute
                    )
                } else {
                    EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, EnumCreatureAttribute.UNDEFINED)
                }

                i += EnchantmentHelper.getKnockbackModifier(mc.thePlayer)
                if (mc.thePlayer.isSprinting) {
                    ++i
                }

                if (f > 0.0f || f1 > 0.0f) {
                    val flag =
                        mc.thePlayer.fallDistance > 0.0f && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder && !mc.thePlayer.isInWater && !mc.thePlayer.isPotionActive(
                            Potion.blindness
                        ) && (mc.thePlayer.ridingEntity == null) && entity is EntityLivingBase
                    if (flag && f > 0.0f) {
                        f *= 1.5f
                    }

                    f += f1
                    var flag1 = false
                    val j = EnchantmentHelper.getFireAspectModifier(mc.thePlayer)
                    if (entity is EntityLivingBase && j > 0 && !entity.isBurning) {
                        flag1 = true
                        entity.setFire(1)
                    }

                    val d0 = entity.motionX
                    val d1 = entity.motionY
                    val d2 = entity.motionZ
                    val flag2 =
                        entity.attackEntityFrom(DamageSource.causePlayerDamage(mc.thePlayer), f)
                    if (flag2) {
                        if (i > 0) {
                            entity.addVelocity(
                                (-MathHelper.sin(mc.thePlayer.rotationYaw * 3.1415927f / 180.0f) * i.toFloat() * 0.5f).toDouble(),
                                0.1,
                                (MathHelper.cos(mc.thePlayer.rotationYaw * 3.1415927f / 180.0f) * i.toFloat() * 0.5f).toDouble()
                            )
                            mc.thePlayer.motionX *= 0.6
                            mc.thePlayer.motionZ *= 0.6
                            mc.thePlayer.serverSprintState = false
                        }

                        if (entity is EntityPlayerMP && entity.velocityChanged) {
                            entity.playerNetServerHandler.sendPacket(
                                S12PacketEntityVelocity(entity)
                            )
                            entity.velocityChanged = false
                            entity.motionX = d0
                            entity.motionY = d1
                            entity.motionZ = d2
                        }

                        if (flag) {
                            mc.thePlayer.onCriticalHit(entity)
                        }

                        if (f1 > 0.0f) {
                            mc.thePlayer.onEnchantmentCritical(entity)
                        }

                        if (f >= 18.0f) {
                            mc.thePlayer.triggerAchievement(AchievementList.overkill)
                        }

                        mc.thePlayer.setLastAttacker(entity)
                        if (entity is EntityLivingBase) {
                            EnchantmentHelper.applyThornEnchantments(
                                entity,
                                mc.thePlayer
                            )
                        }

                        EnchantmentHelper.applyArthropodEnchantments(mc.thePlayer, entity)
                        val itemstack: ItemStack = mc.thePlayer.currentEquippedItem
                        var entity = entity
                        if (entity is EntityDragonPart) {
                            val ientitymultipart = entity.entityDragonObj
                            if (ientitymultipart is EntityLivingBase) {
                                entity = ientitymultipart
                            }
                        }

                        if (entity is EntityLivingBase) {
                            itemstack.hitEntity(entity, mc.thePlayer)
                            if (itemstack.stackSize <= 0) {
                                mc.thePlayer.destroyCurrentEquippedItem()
                            }
                        }
                        if (entity is EntityLivingBase) {
                            mc.thePlayer.addStat(StatList.damageDealtStat, (f * 10.0f).roundToInt())
                            if (j > 0) {
                                entity.setFire(j * 4)
                            }
                        }

                        mc.thePlayer.addExhaustion(0.3f)
                    } else if (flag1) {
                        entity.extinguish()
                    }
                }
            }
        }
    }

    // Modified mc.playerController.onPlayerRightClick() that sends correct stack in its C08
    fun EntityPlayerSP.onPlayerRightClick(
        clickPos: BlockPos, side: EnumFacing, clickVec: Vec3,
        stack: ItemStack?,
    ): Boolean {
        if (clickPos !in worldObj.worldBorder)
            return false

        mc.playerController?.updateController()

        val (facingX, facingY, facingZ) = (clickVec - clickPos.toVec()).toFloatTriple()

        val sendClick = {
            sendPacket(C08PacketPlayerBlockPlacement(clickPos, side.index, stack, facingX, facingY, facingZ))
            true
        }

        // If player is a spectator, send click and return true
        if (mc.playerController.isSpectator)
            return sendClick()

        val item = stack?.item

        if (item?.onItemUseFirst(stack, this, worldObj, clickPos, side, facingX, facingY, facingZ) == true)
            return true

        val blockState = getState(clickPos)

        // If click had activated a block, send click and return true
        if ((!isSneaking || item == null || item.doesSneakBypassUse(worldObj, clickPos, this))
            && blockState.block?.onBlockActivated(worldObj,
                clickPos,
                blockState,
                this,
                side,
                facingX,
                facingY,
                facingZ
            ) == true)
            return sendClick()

        if (item is ItemBlock && !item.canPlaceBlockOnSide(worldObj, clickPos, side, this, stack))
            return false

        sendClick()

        if (stack == null)
            return false

        val prevMetadata = stack.metadata
        val prevSize = stack.stackSize

        return stack.onItemUse(this, worldObj, clickPos, side, facingX, facingY, facingZ).also {
            if (mc.playerController.isInCreativeMode) {
                stack.itemDamage = prevMetadata
                stack.stackSize = prevSize
            } else if (stack.stackSize <= 0) {
                ForgeEventFactory.onPlayerDestroyItem(this, stack)
            }
        }
    }

}

fun EntityPlayerSP.attackEntityWithModifiedSprint(
    entity: Entity, affectMovementBySprint: Boolean? = null, swing: () -> Unit
) {
    swing()

    MovementUtils.affectSprintOnAttack = affectMovementBySprint

    try {
        mc.playerController?.attackEntity(this, entity)
    } catch (any: Exception) {
        // Unlikely to happen, but if it does, we just want to make sure affectSprintOnAttack is null.
        any.printStackTrace()
    }

    MovementUtils.affectSprintOnAttack = null

    CPSCounter.registerClick(CPSCounter.MouseButton.LEFT)
}
