/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import com.google.common.base.Predicate
import com.google.common.base.Predicates
import net.ccbluex.liquidbounce.utils.RotationUtils.Companion.getVectorForRotation
import net.ccbluex.liquidbounce.utils.RotationUtils.Companion.serverRotation
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EntitySelectors
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3

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
                Predicates.and(EntitySelectors.NOT_SPECTATING,
                    Predicate { obj: Entity? -> obj!!.canBeCollidedWith() })
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
}