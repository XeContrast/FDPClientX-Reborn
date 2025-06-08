/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import kevin.utils.component1
import kevin.utils.component2
import kevin.utils.component3
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.extensions.lerpWith
import net.ccbluex.liquidbounce.extensions.rotation
import net.ccbluex.liquidbounce.extensions.step
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.currentTarget
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.dynamicPitchFactor
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.dynamicYawFactor
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.maxPitchFactor
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.maxSpeed
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.maxYawFactor
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.minPitchFactor
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.minSpeed
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.minYawFactor
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.tolerance
import net.ccbluex.liquidbounce.features.module.modules.movement.StrafeFix
import net.ccbluex.liquidbounce.utils.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.getNearestPointBB
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.projectile.EntityEgg
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.*
import java.util.*
import kotlin.math.*

class RotationUtils : MinecraftInstance(), Listenable {
    /**
     * Handle minecraft tick
     *
     * @param event Tick event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val rotation = targetRotation ?: return

        if (keepLength > 0) {
            keepLength--
        } else {
            if (getRotationDifference(rotation, mc.thePlayer.rotation) <= angleThresholdForReset) {
                reset()
            } else {
                val speed = RandomUtils.nextFloat(speedForReset.first, speedForReset.second)
                targetRotation = limitAngleChange(rotation, mc.thePlayer.rotation, speed).fixedSensitivity()
            }
        }

        if (random.nextGaussian() > 0.8) x = Math.random()
        if (random.nextGaussian() > 0.8) y = Math.random()
        if (random.nextGaussian() > 0.8) z = Math.random()
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (StrafeFix.handleEvents()) {
            targetRotation?.let {
                it.applyStrafeToPlayer(event,!StrafeFix.silentFixVaule.get())
                event.cancelEvent()
            }
        }
    }

    /**
     * Handle packet
     *
     * @param event Packet Event
     */
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            targetRotation?.let { rotation ->
                val (yaw,pitch) = serverRotation
                if (rotation.yaw == yaw && rotation.pitch == pitch)
                    return

                packet.yaw = rotation.yaw
                packet.pitch = rotation.pitch
                packet.rotating = true
            }

            if (packet.rotating) serverRotation = Rotation(packet.yaw, packet.pitch)
        }
    }

    /**
     * @return YESSSS!!!
     */
    override fun handleEvents(): Boolean {
        return true
    }

    companion object {
        @JvmField
        var cameraYaw: Float = 0f

        @JvmField
        var cameraPitch: Float = 0f

        @JvmField
        var perspectiveToggled: Boolean = false

        private val random = Random()

        private var keepLength = 0


        @JvmField
        var targetRotation: Rotation? = null
        @JvmField
        var serverRotation = Rotation(0f, 0f)

        const val keepCurrentRotation: Boolean = false

        private var x = random.nextDouble()
        private var y = random.nextDouble()
        private var z = random.nextDouble()

        /**
         * Face block
         *
         * @param blockPos target block
         */
        fun faceBlock(blockPos: BlockPos?): VecRotation? {
            if (blockPos == null) return null

            var vecRotation: VecRotation? = null

            var xSearch = 0.1
            while (xSearch < 0.9) {
                var ySearch = 0.1
                while (ySearch < 0.9) {
                    var zSearch = 0.1
                    while (zSearch < 0.9) {
                        val eyesPos = Vec3(
                            mc.thePlayer.posX,
                            mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
                            mc.thePlayer.posZ
                        )
                        val posVec = Vec3(blockPos).addVector(xSearch, ySearch, zSearch)
                        val dist = eyesPos.distanceTo(posVec)

                        val diffX = posVec.xCoord - eyesPos.xCoord
                        val diffY = posVec.yCoord - eyesPos.yCoord
                        val diffZ = posVec.zCoord - eyesPos.zCoord

                        val diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()

                        val rotation = Rotation(
                            MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                            MathHelper.wrapAngleTo180_float(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat())
                        )

                        val rotationVector = getVectorForRotation(rotation)
                        val vector = eyesPos.addVector(
                            rotationVector.xCoord * dist, rotationVector.yCoord * dist,
                            rotationVector.zCoord * dist
                        )
                        val obj = mc.theWorld.rayTraceBlocks(
                            eyesPos, vector, false,
                            false, true
                        )

                        if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                            val currentVec = VecRotation(posVec, rotation)

                            if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(
                                    vecRotation.rotation
                                )
                            ) vecRotation = currentVec
                        }
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }

            return vecRotation
        }

        fun getFixedSensitivityAngle(targetAngle: Float, startAngle: Float = 0f, gcd: Float = getFixedAngleDelta()) =
            startAngle + ((targetAngle - startAngle) / gcd).roundToInt() * gcd

        /**
         * Allows you to check if your crosshair is over your target entity
         *
         * @param targetEntity       your target entity
         * @param blockReachDistance your reach
         * @return if crosshair is over target
         */
        fun isRotationFaced(targetEntity: Entity, blockReachDistance: Double, rotation: Rotation) = raycastEntity(
            blockReachDistance,
            rotation.yaw,
            rotation.pitch
        ) { entity: Entity -> targetEntity == entity } != null

        /**
         *
         * @param entity
         * @return
         */
        fun getRotationsEntity(entity: EntityLivingBase): Rotation {
            return getRotations(entity.posX, entity.posY + entity.eyeHeight - 0.4, entity.posZ)
        }

        /**
         * Allows you to check if your enemy is behind a wall
         */
        fun isVisible(vec3: Vec3) = mc.theWorld.rayTraceBlocks(mc.thePlayer.eyes, vec3) == null

        /**
         *
         * @param entity
         * @return
         */
        fun getRotationsNonLivingEntity(entity: Entity): Rotation {
            return getRotations(
                entity.posX,
                entity.posY + (entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) * 0.5,
                entity.posZ
            )
        }

        /**
         * Face target with bow
         *
         * @param target your enemy
         * @param silent client side rotations
         * @param predict predict new enemy position
         * @param predictSize predict size of predict
         */
        fun faceBow(target: Entity, silent: Boolean, predict: Boolean, predictSize: Float) {
            val player = mc.thePlayer

            val posX =
                target.posX + (if (predict) (target.posX - target.prevPosX) * predictSize else 0.0) - (player.posX + (if (predict) (player.posX - player.prevPosX) else 0.0))
            val posY =
                target.entityBoundingBox.minY + (if (predict) (target.entityBoundingBox.minY - target.prevPosY) * predictSize else 0.0) + target.eyeHeight - 0.15 - (player.entityBoundingBox.minY + (if (predict) (player.posY - player.prevPosY) else 0.0)) - player.getEyeHeight()
            val posZ =
                target.posZ + (if (predict) (target.posZ - target.prevPosZ) * predictSize else 0.0) - (player.posZ + (if (predict) (player.posZ - player.prevPosZ) else 0.0))
            val posSqrt = sqrt(posX * posX + posZ * posZ)

            var velocity = player.itemInUseDuration / 20f
            velocity = (velocity * velocity + velocity * 2) / 3

            if (velocity > 1) velocity = 1f

            val rotation = Rotation(
                (atan2(posZ, posX) * 180 / Math.PI).toFloat() - 90,
                -Math.toDegrees(atan((velocity * velocity - sqrt(velocity * velocity * velocity * velocity - 0.006f * (0.006f * (posSqrt * posSqrt) + 2 * posY * (velocity * velocity)))) / (0.006f * posSqrt)))
                    .toFloat()
            )

            if (silent) setTargetRotation(rotation)
            else limitAngleChange(
                Rotation(player.rotationYaw, player.rotationPitch), rotation, (10 +
                        Random().nextInt(6)).toFloat()
            ).toPlayer(mc.thePlayer)
        }

        /**
         * Translate vec to rotation
         *
         * @param vec target vec
         * @param predict predict new location of your body
         * @return rotation
         */
        fun toRotation(vec: Vec3, predict: Boolean): Rotation {
            val eyesPos = Vec3(
                mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY +
                        mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ
            )

            if (predict) {
                if (mc.thePlayer.onGround) {
                    eyesPos.addVector(mc.thePlayer.motionX, 0.0, mc.thePlayer.motionZ)
                } else eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)
            }

            val diffX = vec.xCoord - eyesPos.xCoord
            val diffY = vec.yCoord - eyesPos.yCoord
            val diffZ = vec.zCoord - eyesPos.zCoord

            return Rotation(
                MathHelper.wrapAngleTo180_float(
                    Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
                ), MathHelper.wrapAngleTo180_float(
                    (-Math.toDegrees(atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)))).toFloat()
                )
            )
        }

        fun toRotation(vec: Vec3, predict: Boolean = false, fromEntity: Entity = mc.thePlayer): Rotation {
            val eyesPos = fromEntity.eyes
            if (predict) eyesPos.addVector(fromEntity.motionX, fromEntity.motionY, fromEntity.motionZ)

            val (diffX, diffY, diffZ) = vec - eyesPos
            return Rotation(
                MathHelper.wrapAngleTo180_float(
                    atan2(diffZ, diffX).toDegreesF() - 90f
                ), MathHelper.wrapAngleTo180_float(
                    -atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)).toDegreesF()
                )
            )
        }

        /**
         * Get the center of a box
         *
         * @param bb your box
         * @return center of box
         */
        fun getCenter(bb: AxisAlignedBB): Vec3 {
            return Vec3(
                bb.minX + (bb.maxX - bb.minX) * 0.5,
                bb.minY + (bb.maxY - bb.minY) * 0.5,
                bb.minZ + (bb.maxZ - bb.minZ) * 0.5
            )
        }

        /**
         * Search good center
         *
         * @param bb enemy box
         * @param outborder outborder option
         * @param random random option
         * @param predict predict option
         * @param throughWalls throughWalls option
         * @return center
         */
        //TODO : searchCenter Big Update lol(Better Center calculate method & Jitter Support(Better Random Center)) / Co丶Dynamic : Wait until Mid-Autumn Festival
        fun searchCenter(
            bb: AxisAlignedBB,
            outborder: Boolean,
            random: Boolean,
            predict: Boolean,
            throughWalls: Boolean
        ): VecRotation? {
            if (outborder) {
                val vec3 = Vec3(
                    bb.minX + (bb.maxX - bb.minX) * (x * 0.3 + 1.0),
                    bb.minY + (bb.maxY - bb.minY) * (y * 0.3 + 1.0),
                    bb.minZ + (bb.maxZ - bb.minZ) * (z * 0.3 + 1.0)
                )
                return VecRotation(vec3, toRotation(vec3, predict))
            }

            val randomVec = Vec3(
                bb.minX + (bb.maxX - bb.minX) * (x * 0.8 + 0.2),
                bb.minY + (bb.maxY - bb.minY) * (y * 0.8 + 0.2),
                bb.minZ + (bb.maxZ - bb.minZ) * (z * 0.8 + 0.2)
            )
            val randomRotation = toRotation(randomVec, predict)

            var vecRotation: VecRotation? = null

            var xSearch = 0.15
            while (xSearch < 0.85) {
                var ySearch = 0.15
                while (ySearch < 1.0) {
                    var zSearch = 0.15
                    while (zSearch < 0.85) {
                        val vec3 = Vec3(
                            bb.minX + (bb.maxX - bb.minX) * xSearch,
                            bb.minY + (bb.maxY - bb.minY) * ySearch,
                            bb.minZ + (bb.maxZ - bb.minZ) * zSearch
                        )
                        val rotation = toRotation(vec3, predict)

                        if (throughWalls || isVisible(vec3)) {
                            val currentVec = VecRotation(vec3, rotation)

                            if (vecRotation == null || (if (random) getRotationDifference(
                                    currentVec.rotation,
                                    randomRotation
                                ) < getRotationDifference(
                                    vecRotation.rotation,
                                    randomRotation
                                ) else getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation))
                            ) vecRotation = currentVec
                        }
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }

            return vecRotation
        }



        private fun fade(t: Double): Double {
            return t * t * t * (t * (t * 6 - 15) + 10)
        }

        private fun lerp(t: Double, a: Double, b: Double): Double {
            return a + t * (b - a)
        }

        private fun dot(hash: Int, x: Double, y: Double, z: Double): Double {
            val h = hash and 15
            val u = if (h < 8) x else y
            val v = if (h < 4) y else if (h == 12 || h == 14) x else z
            return (if ((h and 1) == 0) u else -u) + (if ((h and 2) == 0) v else -v)
        }

        //柏林噪音
        private fun noise(
            x: Double,
            y: Double,
            z: Double,
            seed: Int):
                Double {
            val p = IntArray(seed)
            val perm = IntArray(seed * 2)

            val random = Random(0) // Seed for reproducibility
            for (i in 0 until seed) {
                p[i] = i
            }
            for (i in seed - 1 downTo 1) {
                val j = random.nextInt(i + 1)
                val temp = p[i]
                p[i] = p[j]
                p[j] = temp
            }
            for (i in 0 until seed) {
                perm[i + seed] = p[i]
                perm[i] = perm[i + seed]
            }


            var x = x
            var y = y
            var z = z
            val X = floor(x).toInt() and 255
            val Y = floor(y).toInt() and 255
            val Z = floor(z).toInt() and 255

            x -= floor(x)
            y -= floor(y)
            z -= floor(z)

            val u = fade(x)
            val v = fade(y)
            val w = fade(z)

            val A = perm[X] + Y
            val AA = perm[A] + Z
            val AB = perm[A + 1] + Z
            val B = perm[X + 1] + Y
            val BA = perm[B] + Z
            val BB = perm[B + 1] + Z

            return lerp(
                w,
                lerp(
                    v,
                    lerp(
                        u,
                        dot(perm[AA], x, y, z),
                        dot(perm[BA], x - 1, y, z)
                    ),
                    lerp(
                        u,
                        dot(perm[AB], x, y - 1, z),
                        dot(perm[BB], x - 1, y - 1, z)
                    )
                ),
                lerp(
                    v,
                    lerp(
                        u,
                        dot(perm[AA + 1], x, y, z - 1),
                        dot(perm[BA + 1], x - 1, y, z - 1)
                    ),
                    lerp(
                        u,
                        dot(perm[AB + 1], x, y - 1, z - 1),
                        dot(perm[BB + 1], x - 1, y - 1, z - 1)
                    )
                )
            )
        }

        enum class BodyPoint(val rank: Int, val range: ClosedFloatingPointRange<Double>) {
            HEAD(1, 0.75..0.9), BODY(0, 0.5..0.75), FEET(-1, 0.1..0.4), UNKNOWN(-2, 0.0..0.0);

            companion object {
                fun fromString(point: String): BodyPoint {
                    return entries.find { it.name.equals(point, ignoreCase = true) } ?: UNKNOWN
                }
            }
        }

        fun rotationDifference(a: Rotation, b: Rotation = serverRotation) =
            hypot(angleDifference(a.yaw, b.yaw), a.pitch - b.pitch)

        fun calculateCenter(
            calMode: String?,
            randMode: String,
            randomRange: Double,
            bb: AxisAlignedBB,
            predict: Boolean,
            throughWalls: Float,
            scanRange: Float,
            attackRange: Float,
            distanceBasedSpot : Boolean,
            bodyPoints: List<String> = listOf("Head", "Feet"),
            findBodyMode: Boolean
        ): VecRotation? {
            //final Rotation randomRotation = toRotation(randomVec, predict);

            var vecRotation: VecRotation? = null

            //min
            val (xMin,yMin,zMin) = when (calMode) {
                "Full","Auto" -> listOf(0.0,0.0,0.0)
                "HalfUp" -> listOf(0.1,0.5,0.1)
                "HalfDown" -> listOf(0.1,0.1,0.1)
                "CenterSimple" -> listOf(0.45,0.65,0.45)
                "CenterLine" -> listOf(0.45,0.1,0.45)
                else -> listOf(0.15,0.15,0.15)
            }

            //max
            val (xMax,yMax,zMax) = when (calMode) {
                "Full","Auto" -> listOf(1.0,1.0,1.0)
                "HalfUp" -> listOf(0.9,0.9,0.9)
                "HalfDown" -> listOf(0.9,0.5,0.9)
                "CenterSimple" -> listOf(0.55,0.75,0.55)
                "CenterLine" -> listOf(0.55,0.9,0.55)
                else -> listOf(0.85,1.0,0.85)
            }

            //Dist
            val (xDist,yDist,zDist) = when (calMode) {
                "CenterSimple","CenterLine" -> listOf(0.0125,0.0125,0.0125)
                else -> listOf(0.1,0.1,0.1)
            }
            val eyes = mc.thePlayer.eyes

            val max = BodyPoint.fromString(bodyPoints[0].takeIf { findBodyMode } ?: "HEAD").range.endInclusive
            val min = BodyPoint.fromString(bodyPoints[1].takeIf { findBodyMode } ?: "FEET").range.start

            var curVec3: Vec3? = null

            val preferredRotation = toRotation(getNearestPointBB(eyes, bb), predict).takeIf {
                distanceBasedSpot
            } ?: targetRotation ?: mc.thePlayer.rotation

            val currRotation = Rotation.ZERO.plus(preferredRotation)

            var attackRotation: Pair<Rotation, Float>? = null
            var lookRotation: Pair<Rotation, Float>? = null

            for (xSearch in xMin..xMax step xDist) {
                for (ySearch in min..max step yDist) {
                    for (zSearch in zMin..zMax step zDist) {
                        val vec3 = bb.lerpWith(xSearch, ySearch, zSearch)
                        val rotation = toRotation(vec3, predict).fixedSensitivity()

                        // Calculate actual hit vec after applying fixed sensitivity to rotation
                        val gcdVec = bb.calculateIntercept(
                            eyes, eyes + getVectorForRotation(rotation) * scanRange.toDouble()
                        )?.hitVec ?: continue

                        val distance = eyes.distanceTo(gcdVec)

                        if (distance > scanRange || (attackRotation != null && distance > attackRange)) continue

                        if (!isVisible(gcdVec) && distance > throughWalls) continue

                        val rotationWithDiff = rotation to rotationDifference(rotation, currRotation)

                        if (distance <= attackRange) {
                            if (attackRotation == null || rotationWithDiff.second < attackRotation.second) attackRotation =
                                rotationWithDiff
                        } else {
                            if (lookRotation == null || rotationWithDiff.second < lookRotation.second) lookRotation =
                                rotationWithDiff
                        }

                        val rot = attackRotation?.first ?: lookRotation?.first ?: run {
                            val vec = getNearestPointBB(eyes, bb)
                            val dist = eyes.distanceTo(vec)

                            if (dist <= scanRange && (dist <= throughWalls || isVisible(vec))) toRotation(vec, predict)
                            else null
                        }

                        val currentVec = VecRotation(vec3, rot ?: return null)

                        if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(
                                vecRotation.rotation
                            )
                        ) {
                            vecRotation = currentVec
                            curVec3 = vec3
                        }
                    }
                }
            }

//            return attackRotation?.first ?: lookRotation?.first ?: run {
//                val vec = getNearestPointBB(eyes, bb)
//                val dist = eyes.distanceTo(vec)
//
//                if (dist <= scanRange && (dist <= throughWalls || isVisible(vec))) toRotation(vec, predict)
//                else null
//            }

            if (randMode == "Noise") {
                if (gaussianHasReachedTarget(curVec3!!, vecRotation!!.vec, tolerance.get())) {
                    val yawFactor = if (dynamicYawFactor.get() > 0f) (MathUtils.randomizeFloat(
                        minYawFactor.get(),
                        maxYawFactor.get()
                    ) + MovementUtils.getSpeed * dynamicYawFactor.get()) else (MathUtils.randomizeFloat(
                        minYawFactor.get(),
                        maxYawFactor.get()
                    ))
                    val pitchFactor = if (dynamicPitchFactor.get() > 0f) (MathUtils.randomizeFloat(
                        minPitchFactor.get(),
                        maxPitchFactor.get()
                    ) + MovementUtils.getSpeed * dynamicPitchFactor.get()) else (MathUtils.randomizeFloat(
                        minPitchFactor.get(),
                        minPitchFactor.get()
                    ))
                    targetRotation?.let {
                        it.yaw += random.nextGaussian().toFloat() * yawFactor
                        it.pitch += random.nextGaussian().toFloat() * pitchFactor
                    }
                } else {
                    targetRotation?.let {
                        it.yaw += MathUtils.interpolate(
                            curVec3.xCoord,
                            vecRotation!!.vec.xCoord,
                            MathUtils.randomizeDouble(minSpeed.get().toDouble(), maxSpeed.get().toDouble())
                        ).toFloat()
                        it.yaw += MathUtils.interpolate(
                            curVec3.yCoord,
                            vecRotation!!.vec.yCoord,
                            MathUtils.randomizeDouble(minSpeed.get().toDouble(), maxSpeed.get().toDouble())
                        ).toFloat()
                    }
                }
            }

            if (randMode == "Off" || randMode == "Noise") return vecRotation

            var rand1 = random.nextDouble()
            var rand2 = random.nextDouble()
            var rand3 = random.nextDouble()

            val xRange = bb.maxX - bb.minX
            val yRange = bb.maxY - bb.minY
            val zRange = bb.maxZ - bb.minZ
            var minRange = Double.MAX_VALUE

            if (xRange <= minRange) minRange = xRange
            if (yRange <= minRange) minRange = yRange
            if (zRange <= minRange) minRange = zRange

            rand1 *= minRange * randomRange
            rand2 *= minRange * randomRange
            rand3 *= minRange * randomRange

            val xPrecent = minRange * randomRange / xRange
            val yPrecent = minRange * randomRange / yRange
            val zPrecent = minRange * randomRange / zRange

            if (curVec3 == null)
                return vecRotation

            var randomVec3 = Vec3(
                curVec3.xCoord - xPrecent * (curVec3.xCoord - bb.minX) + rand1,
                curVec3.yCoord - yPrecent * (curVec3.yCoord - bb.minY) + rand2,
                curVec3.zCoord - zPrecent * (curVec3.zCoord - bb.minZ) + rand3
            )
            when (randMode) {
                "Horizonal" -> randomVec3 = Vec3(
                    curVec3.xCoord - xPrecent * (curVec3.xCoord - bb.minX) + rand1,
                    curVec3.yCoord,
                    curVec3.zCoord - zPrecent * (curVec3.zCoord - bb.minZ) + rand3
                )

                "Vertical" -> randomVec3 = Vec3(
                    curVec3.xCoord,
                    curVec3.yCoord - yPrecent * (curVec3.yCoord - bb.minY) + rand2,
                    curVec3.zCoord
                )

                "Gaussian" -> {
                    randomVec3 = Vec3(
                        curVec3.xCoord + random.nextGaussian(),
                        curVec3.yCoord + random.nextGaussian(),
                        curVec3.zCoord + random.nextGaussian()
                    )
                }

                "PerlinNoise" -> {
                    randomVec3 = Vec3(
                        curVec3.xCoord + noise(mc.thePlayer.posX + MovementUtils.getSpeed,mc.thePlayer.posY + curVec3.yCoord,mc.thePlayer.posZ + zMax, 25565).coerceIn(-randomRange..randomRange),
                        curVec3.yCoord + noise(mc.thePlayer.posX+ MovementUtils.getSpeed,mc.thePlayer.posY + curVec3.yCoord,mc.thePlayer.posZ + zMin, 25565).coerceIn(-randomRange..randomRange),
                        curVec3.zCoord + noise(mc.thePlayer.posX + MovementUtils.getSpeed,mc.thePlayer.posY + curVec3.yCoord,mc.thePlayer.posZ + zDist, 25565).coerceIn(-randomRange..randomRange)
                    )
                }
            }
            val randomRotation = toRotation(randomVec3, predict)

            vecRotation = VecRotation(randomVec3, randomRotation)

            return vecRotation
        }

        fun calculateCenter(
            calMode: String?,
            randMode: String,
            randomRange: Double,
            bb: AxisAlignedBB,
            predict: Boolean,
            throughWalls: Boolean
        ): VecRotation? {
            //final Rotation randomRotation = toRotation(randomVec, predict);

            var vecRotation: VecRotation? = null

            //min
            val (xMin,yMin,zMin) = when (calMode) {
                "Full" -> listOf(0.0,0.0,0.0)
                "HalfUp" -> listOf(0.1,0.5,0.1)
                "HalfDown" -> listOf(0.1,0.1,0.1)
                "CenterSimple" -> listOf(0.45,0.65,0.45)
                "CenterLine" -> listOf(0.45,0.1,0.45)
                else -> listOf(0.15,0.15,0.15)
            }

            //max
            val (xMax,yMax,zMax) = when (calMode) {
                "Full" -> listOf(1.0,1.0,1.0)
                "HalfUp" -> listOf(0.9,0.9,0.9)
                "HalfDown" -> listOf(0.9,0.5,0.9)
                "CenterSimple" -> listOf(0.55,0.75,0.55)
                "CenterLine" -> listOf(0.55,0.9,0.55)
                else -> listOf(0.85,1.0,0.85)
            }

            //Dist
            val (xDist,yDist,zDist) = when (calMode) {
                "CenterSimple","CenterLine" -> listOf(0.0125,0.0125,0.0125)
                else -> listOf(0.1,0.1,0.1)
            }

            val max = BodyPoint.fromString("HEAD").range.endInclusive
            val min = BodyPoint.fromString("FEET").range.start

            var curVec3: Vec3? = null

            for (xSearch in xMin..xMax step xDist) {
                for (ySearch in min..max step yDist) {
                    for (zSearch in zMin..zMax step zDist) {
                        val vec3 = bb.lerpWith(xSearch,ySearch,zSearch)
                        val rotation = toRotation(vec3, predict).fixedSensitivity()

                        if (throughWalls || isVisible(vec3)) {
                            val currentVec = VecRotation(vec3, rotation)

                            if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation)) {
                                vecRotation = currentVec
                                curVec3 = vec3
                            }
                        }
                    }
                }
            }

            if (randMode == "Noise") {
                if (gaussianHasReachedTarget(curVec3!!, vecRotation!!.vec, tolerance.get())) {
                    val yawFactor = if (dynamicYawFactor.get() > 0f) (MathUtils.randomizeFloat(
                        minYawFactor.get(),
                        maxYawFactor.get()
                    ) + MovementUtils.getSpeed * dynamicYawFactor.get()) else (MathUtils.randomizeFloat(
                        minYawFactor.get(),
                        maxYawFactor.get()
                    ))
                    val pitchFactor = if (dynamicPitchFactor.get() > 0f) (MathUtils.randomizeFloat(
                        minPitchFactor.get(),
                        maxPitchFactor.get()
                    ) + MovementUtils.getSpeed * dynamicPitchFactor.get()) else (MathUtils.randomizeFloat(
                        minPitchFactor.get(),
                        minPitchFactor.get()
                    ))
                    targetRotation?.let {
                        it.yaw += random.nextGaussian().toFloat() * yawFactor
                        it.pitch += random.nextGaussian().toFloat() * pitchFactor
                    }
                } else {
                    targetRotation?.let {
                        it.yaw += MathUtils.interpolate(
                            curVec3.xCoord,
                            vecRotation!!.vec.xCoord,
                            MathUtils.randomizeDouble(minSpeed.get().toDouble(), maxSpeed.get().toDouble())
                        ).toFloat()
                        it.yaw += MathUtils.interpolate(
                            curVec3.yCoord,
                            vecRotation!!.vec.yCoord,
                            MathUtils.randomizeDouble(minSpeed.get().toDouble(), maxSpeed.get().toDouble())
                        ).toFloat()
                    }
                }
            }

            if (randMode == "Off" || randMode == "Noise") return vecRotation

            var rand1 = random.nextDouble()
            var rand2 = random.nextDouble()
            var rand3 = random.nextDouble()

            val xRange = bb.maxX - bb.minX
            val yRange = bb.maxY - bb.minY
            val zRange = bb.maxZ - bb.minZ
            var minRange = Double.MAX_VALUE

            if (xRange <= minRange) minRange = xRange
            if (yRange <= minRange) minRange = yRange
            if (zRange <= minRange) minRange = zRange

            rand1 *= minRange * randomRange
            rand2 *= minRange * randomRange
            rand3 *= minRange * randomRange

            val xPrecent = minRange * randomRange / xRange
            val yPrecent = minRange * randomRange / yRange
            val zPrecent = minRange * randomRange / zRange

            var randomVec3 = Vec3(
                curVec3!!.xCoord - xPrecent * (curVec3.xCoord - bb.minX) + rand1,
                curVec3.yCoord - yPrecent * (curVec3.yCoord - bb.minY) + rand2,
                curVec3.zCoord - zPrecent * (curVec3.zCoord - bb.minZ) + rand3
            )
            when (randMode) {
                "Horizonal" -> randomVec3 = Vec3(
                    curVec3.xCoord - xPrecent * (curVec3.xCoord - bb.minX) + rand1,
                    curVec3.yCoord,
                    curVec3.zCoord - zPrecent * (curVec3.zCoord - bb.minZ) + rand3
                )

                "Vertical" -> randomVec3 = Vec3(
                    curVec3.xCoord,
                    curVec3.yCoord - yPrecent * (curVec3.yCoord - bb.minY) + rand2,
                    curVec3.zCoord
                )

                "Gaussian" -> {
                    randomVec3 = Vec3(
                        curVec3.xCoord + random.nextGaussian(),
                        curVec3.yCoord + random.nextGaussian(),
                        curVec3.zCoord + random.nextGaussian()
                    )
                }

                "PerlinNoise" -> {
                    randomVec3 = Vec3(
                        curVec3.xCoord + noise(mc.thePlayer.posX + MovementUtils.getSpeed,mc.thePlayer.posY + curVec3.yCoord,mc.thePlayer.posZ + zMax, 25565).coerceIn(-randomRange..randomRange),
                        curVec3.yCoord + noise(mc.thePlayer.posX+ MovementUtils.getSpeed,mc.thePlayer.posY + curVec3.yCoord,mc.thePlayer.posZ + zMin, 25565).coerceIn(-randomRange..randomRange),
                        curVec3.zCoord + noise(mc.thePlayer.posX + MovementUtils.getSpeed,mc.thePlayer.posY + curVec3.yCoord,mc.thePlayer.posZ + zDist, 25565).coerceIn(-randomRange..randomRange)
                    )
                }
            }
            val randomRotation = toRotation(randomVec3, predict)

            vecRotation = VecRotation(randomVec3, randomRotation)

            return vecRotation
        }

        private fun gaussianHasReachedTarget(vec1: Vec3, vec2: Vec3, tolerance: Float): Boolean {
            return MathHelper.abs((vec1.xCoord - vec2.xCoord).toFloat()) < tolerance && MathHelper.abs((vec1.yCoord - vec2.yCoord).toFloat()) < tolerance && MathHelper.abs(
                (vec1.zCoord - vec2.zCoord).toFloat()
            ) < tolerance
        }

        fun getFixedAngleDelta(sensitivity: Float = mc.gameSettings.mouseSensitivity) =
            (sensitivity * 0.6f + 0.2f).pow(3) * 1.2f

        /**
         * Calculate difference between the client rotation and your entity
         *
         * @param entity your entity
         * @return difference between rotation
         */
        fun getRotationDifference(entity: Entity): Double {
            val rotation = toRotation(getCenter(entity.entityBoundingBox), true)

            return getRotationDifference(rotation, Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch))
        }

        /**
         * Calculate difference between the server rotation and your rotation
         *
         * @param rotation your rotation
         * @return difference between rotation
         */
        fun getRotationDifference(rotation: Rotation): Double {
            return getRotationDifference(rotation, serverRotation)
        }

        fun angleDifference(a: Float, b: Float) = MathHelper.wrapAngleTo180_float(a - b)

        /**
         * Calculate difference between two rotations
         *
         * @param a rotation
         * @param b rotation
         * @return difference between rotation
         */
        fun getRotationDifference(a: Rotation, b: Rotation?): Double {
            return hypot(getAngleDifference(a.yaw, b!!.yaw).toDouble(), (a.pitch - b.pitch).toDouble())
        }

        /**
         * Limit your rotation using a turn speed
         *
         * @param currentRotation your current rotation
         * @param targetRotation your goal rotation
         * @param turnSpeed your turn speed
         * @return limited rotation
         */

        @JvmStatic
        fun limitAngleChange(
            currentRotation: Rotation,
            targetRotation: Rotation?,
            horizontalSpeed: Float,
            verticalSpeed: Float = horizontalSpeed
        ): Rotation {
            val yawDifference = targetRotation?.let { getAngleDifference(it.yaw, currentRotation.yaw) }
            val pitchDifference = targetRotation?.let { getAngleDifference(it.pitch, currentRotation.pitch) }
            return Rotation(
                currentRotation.yaw + (if (yawDifference!! > horizontalSpeed) horizontalSpeed else max(
                    yawDifference,
                    -horizontalSpeed
                )),
                currentRotation.pitch + (if (pitchDifference!! > verticalSpeed) verticalSpeed else max(
                    pitchDifference,
                    -verticalSpeed
                ))
            )
        }



        /**
         * Calculate difference between two angle points
         *
         * @param a angle point
         * @param b angle point
         * @return difference between angle points
         */
        @JvmStatic
        fun getAngleDifference(a: Float, b: Float): Float {
            return ((((a - b) % 360f) + 540f) % 360f) - 180f
        }

        fun getAngles(entity: Entity?): Rotation? {
            if (entity == null) return null
            val thePlayer = mc.thePlayer
            val diffX = entity.posX - thePlayer.posX
            val diffY = entity.posY + entity.eyeHeight * 0.9 - (thePlayer.posY + thePlayer.getEyeHeight())
            val diffZ = entity.posZ - thePlayer.posZ
            val dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble() // @on
            val yaw = (atan2(diffZ, diffX) * 180.0 / Math.PI).toFloat() - 90.0f
            val pitch = -(atan2(diffY, dist) * 180.0 / Math.PI).toFloat()
            return Rotation(
                thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - thePlayer.rotationYaw),
                thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - thePlayer.rotationPitch)
            )
        }

        fun getDirectionToBlock(x: Double, y: Double, z: Double, enumfacing: EnumFacing): Rotation {
            val var4 = EntityEgg(mc.theWorld)
            var4.posX = x + 0.5
            var4.posY = y + 0.5
            var4.posZ = z + 0.5
            var4.posX += enumfacing.directionVec.x.toDouble() * 0.5
            var4.posY += enumfacing.directionVec.y.toDouble() * 0.5
            var4.posZ += enumfacing.directionVec.z.toDouble() * 0.5
            return getRotations(var4.posX, var4.posY, var4.posZ)
        }

        /**
         * Calculate rotation to vector
         *
         * @param rotation your rotation
         * @return target vector
         */
        fun getVectorForRotation(rotation: Rotation): Vec3 {
            val yawCos = MathHelper.cos(-rotation.yaw * 0.017453292f - Math.PI.toFloat())
            val yawSin = MathHelper.sin(-rotation.yaw * 0.017453292f - Math.PI.toFloat())
            val pitchCos = -MathHelper.cos(-rotation.pitch * 0.017453292f)
            val pitchSin = MathHelper.sin(-rotation.pitch * 0.017453292f)
            return Vec3((yawSin * pitchCos).toDouble(), pitchSin.toDouble(), (yawCos * pitchCos).toDouble())
        }
        @JvmStatic
        fun getVectorForRotation(yaw: Float, pitch: Float): Vec3 {
            val yawRad = yaw.toRadians()
            val pitchRad = pitch.toRadians()

            val f = MathHelper.cos(-yawRad - PI.toFloat())
            val f1 = MathHelper.sin(-yawRad - PI.toFloat())
            val f2 = -MathHelper.cos(-pitchRad)
            val f3 = MathHelper.sin(-pitchRad)

            return Vec3((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
        }

        /**
         * Allows you to check if your crosshair is over your target entity
         *
         * @param targetEntity your target entity
         * @param blockReachDistance your reach
         * @return if crosshair is over target
         */
        fun isFaced(targetEntity: Entity, blockReachDistance: Double): Boolean {
            return raycastEntity(blockReachDistance) { entity: Entity -> entity === targetEntity } != null
        }

        /**
         * Set your target rotation
         *
         * @param rotation your target rotation
         */
        fun setTargetRotation(rotation: Rotation) {
            setTargetRotation(rotation, 0)
        }

        /**
         * Set your target rotation
         *
         * @param rotation your target rotation
         */
        fun setTargetRotation(rotation: Rotation, kl: Int) {
            if (rotation.yaw.isNaN() || rotation.pitch.isNaN() || rotation.pitch > 90 || rotation.pitch < -90) return

            rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)
            targetRotation = rotation
            keepLength = kl
        }

        var speedForReset = 180f to 180f
        var angleThresholdForReset = 0f
        @JvmStatic
        fun setTargetRotation(
            rotation: Rotation,
            keepLength: Int = 1,

            resetSpeed: Pair<Float, Float> = 180f to 180f,
            angleThresholdForReset: Float = 180f
        ) {
            if (rotation.yaw.isNaN() || rotation.pitch.isNaN() || rotation.pitch > 90 || rotation.pitch < -90) return

            targetRotation = rotation

            this.keepLength = keepLength
            this.speedForReset = resetSpeed
            this.angleThresholdForReset = angleThresholdForReset
        }

        fun setTargetRotationReverse(
            rotation: Rotation,
            kl: Int,
            resetSpeed: Pair<Float, Float> = 180f to 180f,
            angleThresholdForReset: Float = 180f
        ) {
            if (rotation.yaw.isNaN() || rotation.pitch.isNaN() || rotation.pitch > 90 || rotation.pitch < -90) return

            rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)
            this.targetRotation = rotation
            this.keepLength = kl
            this.speedForReset = resetSpeed
            this.angleThresholdForReset = angleThresholdForReset
        }

        fun bestServerRotation(): Rotation? {
            return if (targetRotation != null) targetRotation else serverRotation
        }

        fun compareRotationDifferenceLesser(
            compareWith: Rotation?,
            toCompare1: Rotation,
            toCompare2: Rotation
        ): Boolean {
            return getRotationDifference(toCompare1, compareWith) < getRotationDifference(toCompare2, compareWith)
        }

        /**
         * Reset your target rotation
         */
        fun reset() {
            keepLength = 0
            targetRotation?.let { rotation ->
                mc.thePlayer?.let {
                    it.rotationYaw = rotation.yaw + getAngleDifference(it.rotationYaw, rotation.yaw)
                    syncRotations()
                }
            }
            targetRotation = null
        }

        fun syncRotations() {
            val player = mc.thePlayer ?: return

            player.prevRotationYaw = player.rotationYaw
            player.prevRotationPitch = player.rotationPitch
            player.renderArmYaw = player.rotationYaw
            player.renderArmPitch = player.rotationPitch
            player.prevRenderArmYaw = player.rotationYaw
            player.prevRotationPitch = player.rotationPitch
        }


        /**
         *
         * @param posX
         * @param posY
         * @param posZ
         * @return
         */
        fun getRotations(posX: Double, posY: Double, posZ: Double): Rotation {
            val player = mc.thePlayer
            val x = posX - player.posX
            val y = posY - (player.posY + player.getEyeHeight().toDouble())
            val z = posZ - player.posZ
            val dist = MathHelper.sqrt_double(x * x + z * z).toDouble()
            val yaw = (atan2(z, x) * 180.0 / 3.141592653589793).toFloat() - 90.0f
            val pitch = (-(atan2(y, dist) * 180.0 / 3.141592653589793)).toFloat()
            return Rotation(yaw, pitch)
        }
    }
}
