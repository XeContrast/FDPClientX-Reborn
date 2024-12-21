/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import com.google.common.base.Predicate
import com.google.common.base.Predicates
import net.ccbluex.liquidbounce.features.module.modules.combat.Backtrack
import net.ccbluex.liquidbounce.features.module.modules.combat.Backtrack.loopThroughBacktrackData
import net.ccbluex.liquidbounce.utils.RotationUtils.Companion.getVectorForRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.Companion.isVisible
import net.ccbluex.liquidbounce.utils.RotationUtils.Companion.serverRotation
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.*
import java.util.*

object RaycastUtils : MinecraftInstance() {

    @JvmStatic
    @JvmOverloads
    fun raycastEntity(
        range: Double,
        yaw: Float = serverRotation!!.yaw,
        pitch: Float = serverRotation!!.pitch,
        entityFilter: (Entity) -> Boolean
    ): Entity? {
        val renderViewEntity = mc.renderViewEntity

        if (renderViewEntity != null && mc.theWorld != null) {
            var blockReachDistance = range
            val eyePosition = renderViewEntity.eyes
            val entityLook = getVectorForRotation(Rotation(yaw, pitch))
            val vec = eyePosition.addVector(
                entityLook.xCoord * blockReachDistance,
                entityLook.yCoord * blockReachDistance,
                entityLook.zCoord * blockReachDistance
            )

            val entityList = mc.theWorld.getEntitiesInAABBexcluding(
                renderViewEntity, renderViewEntity.entityBoundingBox.addCoord(
                    entityLook.xCoord * blockReachDistance,
                    entityLook.yCoord * blockReachDistance,
                    entityLook.zCoord * blockReachDistance
                ).expand(1.0, 1.0, 1.0)
            ) {
                it != null && (it !is EntityPlayer || !it.isSpectator) && it.canBeCollidedWith()
            }

            var pointedEntity: Entity? = null

            for (entity in entityList) {
                if (!entityFilter(entity)) continue

                val checkEntity = {
                    val axisAlignedBB = entity.hitBox

                    val movingObjectPosition = axisAlignedBB.calculateIntercept(eyePosition, vec)

                    if (axisAlignedBB.isVecInside(eyePosition)) {
                        if (blockReachDistance >= 0.0) {
                            pointedEntity = entity
                            blockReachDistance = 0.0
                        }
                    } else if (movingObjectPosition != null) {
                        val eyeDistance = eyePosition.distanceTo(movingObjectPosition.hitVec)

                        if (eyeDistance < blockReachDistance || blockReachDistance == 0.0) {
                            if (entity == renderViewEntity.ridingEntity && !renderViewEntity.canRiderInteract()) {
                                if (blockReachDistance == 0.0) pointedEntity = entity
                            } else {
                                pointedEntity = entity
                                blockReachDistance = eyeDistance
                            }
                        }
                    }

                    false
                }

                // Check newest entity first
                checkEntity()
                if (Backtrack.mode.get() == "Legacy")
                    loopThroughBacktrackData(entity, checkEntity)
            }

            return pointedEntity
        }

        return null
    }
    fun raycastEntity(range: Double, yaw: Float, pitch: Float, entityFilter: IEntityFilter): Entity? {
        val renderViewEntity = mc.renderViewEntity

        if (renderViewEntity != null && mc.theWorld != null) {
            var blockReachDistance = range
            val eyePosition = renderViewEntity.getPositionEyes(1f)

            val yawCos = MathHelper.cos(-yaw * 0.017453292f - Math.PI.toFloat())
            val yawSin = MathHelper.sin(-yaw * 0.017453292f - Math.PI.toFloat())
            val pitchCos = -MathHelper.cos(-pitch * 0.017453292f)
            val pitchSin = MathHelper.sin(-pitch * 0.017453292f)

            val entityLook = Vec3((yawSin * pitchCos).toDouble(), pitchSin.toDouble(), (yawCos * pitchCos).toDouble())
            val vector = eyePosition.addVector(
                entityLook.xCoord * blockReachDistance,
                entityLook.yCoord * blockReachDistance,
                entityLook.zCoord * blockReachDistance
            )
            val entityList = mc.theWorld.getEntitiesInAABBexcluding(
                renderViewEntity,
                renderViewEntity.entityBoundingBox.addCoord(
                    entityLook.xCoord * blockReachDistance,
                    entityLook.yCoord * blockReachDistance,
                    entityLook.zCoord * blockReachDistance
                ).expand(1.0, 1.0, 1.0),
                Predicates.and(EntitySelectors.NOT_SPECTATING
                ) { obj: Entity? -> obj!!.canBeCollidedWith() }
            )

            var pointedEntity: Entity? = null

            for (entity in entityList) {
                if (!entityFilter.canRaycast(entity)) continue

                val collisionBorderSize = entity.collisionBorderSize
                val axisAlignedBB = entity.entityBoundingBox.expand(
                    collisionBorderSize.toDouble(),
                    collisionBorderSize.toDouble(),
                    collisionBorderSize.toDouble()
                )
                val movingObjectPosition = axisAlignedBB.calculateIntercept(eyePosition, vector)

                if (axisAlignedBB.isVecInside(eyePosition)) {
                    if (blockReachDistance >= 0.0) {
                        pointedEntity = entity
                        blockReachDistance = 0.0
                    }
                } else if (movingObjectPosition != null) {
                    val eyeDistance = eyePosition.distanceTo(movingObjectPosition.hitVec)

                    if (eyeDistance < blockReachDistance || blockReachDistance == 0.0) {
                        if (entity === renderViewEntity.ridingEntity && !renderViewEntity.canRiderInteract()) {
                            if (blockReachDistance == 0.0) pointedEntity = entity
                        } else {
                            pointedEntity = entity
                            blockReachDistance = eyeDistance
                        }
                    }
                }
            }

            return pointedEntity
        }

        return null
    }
    fun raycastEntity(range: Double, entityFilter: IEntityFilter?): Entity? {
        return raycastEntity(
            range, RotationUtils.targetRotation!!.yaw, RotationUtils.targetRotation!!.pitch,
            entityFilter!!
        )
    }
    interface IEntityFilter {
        fun canRaycast(entity: Entity?): Boolean
    }

    /**
     * Modified mouse object pickup
     */
    fun runWithModifiedRaycastResult(
        rotation: Rotation,
        range: Double,
        wallRange: Double,
        action: (MovingObjectPosition) -> Unit
    ) {

        val entity = mc.renderViewEntity

        val prevPointedEntity = mc.pointedEntity
        val prevObjectMouseOver = mc.objectMouseOver

        if (entity != null && mc.theWorld != null) {
            mc.pointedEntity = null

            val buildReach = if (mc.playerController.currentGameType.isCreative) 5.0 else 4.5

            val vec3 = entity.eyes
            val vec31 = getVectorForRotation(rotation)
            val vec32 = vec3.addVector(vec31.xCoord * buildReach, vec31.yCoord * buildReach, vec31.zCoord * buildReach)

            mc.objectMouseOver = entity.worldObj.rayTraceBlocks(vec3, vec32, false, false, true)

            var d1 = buildReach
            var flag = false

            if (mc.playerController.extendedReach()) {
                d1 = 6.0
            } else if (buildReach > 3) {
                flag = true
            }

            if (mc.objectMouseOver != null) {
                d1 = mc.objectMouseOver.hitVec.distanceTo(vec3)
            }

            var pointedEntity: Entity? = null
            var vec33: Vec3? = null

            val list = mc.theWorld.getEntities(EntityLivingBase::class.java) {
                it != null && (it !is EntityPlayer || !it.isSpectator) && it.canBeCollidedWith() && it != entity
            }

            var d2 = d1

            for (entity1 in list) {
                val f1 = entity1.collisionBorderSize
                val boxes = ArrayList<AxisAlignedBB>()

                boxes.add(entity1.entityBoundingBox.expand(f1.toDouble(), f1.toDouble(), f1.toDouble()))

                loopThroughBacktrackData(entity1) {
                    boxes.add(entity1.entityBoundingBox.expand(f1.toDouble(), f1.toDouble(), f1.toDouble()))
                    false
                }

                for (box in boxes) {
                    val intercept = box.calculateIntercept(vec3, vec32)

                    if (box.isVecInside(vec3)) {
                        if (d2 >= 0) {
                            pointedEntity = entity1
                            vec33 = if (intercept == null) vec3 else intercept.hitVec
                            d2 = 0.0
                        }
                    } else if (intercept != null) {
                        val d3 = vec3.distanceTo(intercept.hitVec)

                        if (!isVisible(intercept.hitVec)) {
                            if (d3 <= wallRange) {
                                if (d3 < d2 || d2 == 0.0) {
                                    pointedEntity = entity1
                                    vec33 = intercept.hitVec
                                    d2 = d3
                                }
                            }

                            continue
                        }

                        if (d3 < d2 || d2 == 0.0) {
                            if (entity1 === entity.ridingEntity && !entity.canRiderInteract()) {
                                if (d2 == 0.0) {
                                    pointedEntity = entity1
                                    vec33 = intercept.hitVec
                                }
                            } else {
                                pointedEntity = entity1
                                vec33 = intercept.hitVec
                                d2 = d3
                            }
                        }
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > range) {
                pointedEntity = null
                mc.objectMouseOver = MovingObjectPosition(
                    MovingObjectPosition.MovingObjectType.MISS,
                    Objects.requireNonNull(vec33),
                    null,
                    BlockPos(vec33)
                )
            }

            if (pointedEntity != null && (d2 < d1 || mc.objectMouseOver == null)) {
                mc.objectMouseOver = MovingObjectPosition(pointedEntity, vec33)

                if (pointedEntity is EntityLivingBase || pointedEntity is EntityItemFrame) {
                    mc.pointedEntity = pointedEntity
                }
            }

            action(mc.objectMouseOver)

            mc.objectMouseOver = prevObjectMouseOver
            mc.pointedEntity = prevPointedEntity
        }
    }
}