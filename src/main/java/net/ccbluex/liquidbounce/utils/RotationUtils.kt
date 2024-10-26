/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import kevin.utils.component1
import kevin.utils.component2
import kevin.utils.component3
import kevin.utils.minus
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.utils.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.extensions.eyes
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
    fun onTick(event: TickEvent?) {
        if (targetRotation != null) {
            //ClientUtils.INSTANCE.displayAlert(keepLength + " " + revTick);
            keepLength--

            if (keepLength <= 0) {
                if (revTick > 0) {
                    revTick--
                }
                reset()
            }
        }

        if (random.nextGaussian() > 0.8) x = Math.random()
        if (random.nextGaussian() > 0.8) y = Math.random()
        if (random.nextGaussian() > 0.8) z = Math.random()
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

            if (targetRotation != null && !keepCurrentRotation && (targetRotation!!.yaw != serverRotation!!.yaw || targetRotation!!.pitch != serverRotation!!.pitch)) {
                packet.yaw = targetRotation!!.yaw
                packet.pitch = targetRotation!!.pitch
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
        private var revTick = 0


        @JvmField
        var targetRotation: Rotation? = null
        @JvmField
        var serverRotation: Rotation? = Rotation(0f, 0f)

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

            var xMin: Double
            var yMin: Double
            var zMin: Double
            var xMax: Double
            var yMax: Double
            var zMax: Double
            var xDist: Double
            var yDist: Double
            var zDist: Double

            xMin = 0.15
            xMax = 0.85
            xDist = 0.1
            yMin = 0.15
            yMax = 1.00
            yDist = 0.1
            zMin = 0.15
            zMax = 0.85
            zDist = 0.1

            var curVec3: Vec3? = null

            when (calMode) {
                "LiquidBounce" -> {}
                "Full" -> {
                    xMin = 0.00
                    xMax = 1.00
                    yMin = 0.00
                    zMin = 0.00
                    zMax = 1.00
                }

                "HalfUp" -> {
                    xMin = 0.10
                    xMax = 0.90
                    yMin = 0.50
                    yMax = 0.90
                    zMin = 0.10
                    zMax = 0.90
                }

                "HalfDown" -> {
                    xMin = 0.10
                    xMax = 0.90
                    yMin = 0.10
                    yMax = 0.50
                    zMin = 0.10
                    zMax = 0.90
                }

                "CenterSimple" -> {
                    xMin = 0.45
                    xMax = 0.55
                    xDist = 0.0125
                    yMin = 0.65
                    yMax = 0.75
                    yDist = 0.0125
                    zMin = 0.45
                    zMax = 0.55
                    zDist = 0.0125
                }

                "CenterLine" -> {
                    xMin = 0.45
                    xMax = 0.55
                    xDist = 0.0125
                    yMin = 0.10
                    yMax = 0.90
                    zMin = 0.45
                    zMax = 0.55
                    zDist = 0.0125
                }
            }
            var xSearch = xMin
            while (xSearch < xMax) {
                var ySearch = yMin
                while (ySearch < yMax) {
                    var zSearch = zMin
                    while (zSearch < zMax) {
                        val vec3 = Vec3(
                            bb.minX + (bb.maxX - bb.minX) * xSearch,
                            bb.minY + (bb.maxY - bb.minY) * ySearch,
                            bb.minZ + (bb.maxZ - bb.minZ) * zSearch
                        )
                        val rotation = toRotation(vec3, predict)

                        if (throughWalls || isVisible(vec3)) {
                            val currentVec = VecRotation(vec3, rotation)

                            if (vecRotation == null || (getRotationDifference(currentVec.rotation) < getRotationDifference(
                                    vecRotation.rotation
                                ))
                            ) {
                                vecRotation = currentVec
                                curVec3 = vec3
                            }
                        }
                        zSearch += zDist
                    }
                    ySearch += yDist
                }
                xSearch += xDist
            }

            if (vecRotation == null || randMode == "Off") return vecRotation

            var rand1 = random.nextDouble()
            var rand2 = random.nextDouble()
            var rand3 = random.nextDouble()

            val xRange = bb.maxX - bb.minX
            val yRange = bb.maxY - bb.minY
            val zRange = bb.maxZ - bb.minZ
            var minRange = 999999.0

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
            }
            val randomRotation = toRotation(randomVec3, predict)


            /*
        for(double xSearch = 0.00D; xSearch < 1.00D; xSearch += 0.05D) {
            for (double ySearch = 0.00D; ySearch < 1.00D; ySearch += 0.05D) {
                for (double zSearch = 0.00D; zSearch < 1.00D; zSearch += 0.05D) {
                    final Vec3 vec3 = new Vec3(curVec3.xCoord - ((randMode == "Horizonal") ? 0.0D : (xPrecent * (curVec3.xCoord - bb.minX) + minRange * randomRange * xSearch)),
                                               curVec3.yCoord - ((randMode == "Vertical") ? 0.0D : (yPrecent * (curVec3.yCoord - bb.minY) + minRange * randomRange * ySearch)),
                                               curVec3.zCoord - ((randMode == "Horizonal") ? 0.0D : (zPrecent * (curVec3.zCoord - bb.minZ) + minRange * randomRange * zSearch)));
                    final Rotation rotation = toRotation(vec3, predict);
                    if(throughWalls || isVisible(vec3)) {
                        final VecRotation currentVec = new VecRotation(vec3, rotation);
                        if (vecRotation == null || (getRotationDifference(currentVec.getRotation(), randomRotation) < getRotationDifference(vecRotation.getRotation(), randomRotation)))
                            vecRotation = currentVec;
                    }
                }
            }
        }
        I Give Up :sadface: */
            vecRotation = VecRotation(randomVec3, randomRotation)

            return vecRotation
        }

        fun calculateCenter(
            calMode: String?,
            randMode: Boolean?,
            randomRange: Double,
            legitRandom: Boolean,
            bb: AxisAlignedBB,
            predict: Boolean,
            throughWalls: Boolean
        ): VecRotation? {
            var vecRotation: VecRotation? = null

            var xMin: Double
            var yMin: Double
            var zMin: Double
            var xMax: Double
            var yMax: Double
            var zMax: Double
            var xDist: Double
            var yDist: Double
            var zDist: Double

            xMin = 0.15
            xMax = 0.85
            xDist = 0.1
            yMin = 0.15
            yMax = 1.00
            yDist = 0.1
            zMin = 0.15
            zMax = 0.85
            zDist = 0.1

            var curVec3: Vec3? = null

            when (calMode) {
                "HalfUp" -> {
                    xMin = 0.10
                    xMax = 0.90
                    yMin = 0.50
                    yMax = 0.90
                    zMin = 0.10
                    zMax = 0.90
                }

                "CenterSimple" -> {
                    xMin = 0.45
                    xMax = 0.55
                    xDist = 0.0125
                    yMin = 0.65
                    yMax = 0.75
                    yDist = 0.0125
                    zMin = 0.45
                    zMax = 0.55
                    zDist = 0.0125
                }

                "CenterLine" -> {
                    xMin = 0.45
                    xMax = 0.55
                    xDist = 0.0125
                    yMin = 0.50
                    yMax = 0.90
                    zMin = 0.45
                    zMax = 0.55
                    zDist = 0.0125
                }

                "CenterHead" -> {
                    xMin = 0.45
                    xMax = 0.55
                    xDist = 0.0125
                    yMin = 0.85
                    yMax = 0.95
                    zMin = 0.45
                    zMax = 0.55
                    zDist = 0.0125
                }

                "CenterBody" -> {
                    xMin = 0.45
                    xMax = 0.55
                    xDist = 0.0125
                    yMin = 0.70
                    yMax = 0.95
                    zMin = 0.45
                    zMax = 0.55
                    zDist = 0.0125
                }

                "LockHead" -> {
                    xMin = 0.55
                    xMax = 0.55
                    xDist = 0.0125
                    yMin = 0.949
                    yMax = 0.95
                    zMin = 0.55
                    zMax = 0.55
                    zDist = 0.0125
                }
            }
            var xSearch = xMin
            while (xSearch < xMax) {
                var ySearch = yMin
                while (ySearch < yMax) {
                    var zSearch = zMin
                    while (zSearch < zMax) {
                        val vec3 = Vec3(
                            bb.minX + (bb.maxX - bb.minX) * xSearch,
                            bb.minY + (bb.maxY - bb.minY) * ySearch,
                            bb.minZ + (bb.maxZ - bb.minZ) * zSearch
                        )
                        val rotation = toRotation(vec3, predict)

                        if (throughWalls || isVisible(vec3)) {
                            val currentVec = VecRotation(vec3, rotation)

                            if (vecRotation == null || (getRotationDifference(currentVec.rotation) < getRotationDifference(
                                    vecRotation.rotation
                                ))
                            ) {
                                vecRotation = currentVec
                                curVec3 = vec3
                            }
                        }
                        zSearch += zDist
                    }
                    ySearch += yDist
                }
                xSearch += xDist
            }

            if (vecRotation == null || !randMode!!) return vecRotation

            var rand1 = random.nextDouble()
            var rand2 = random.nextDouble()
            var rand3 = random.nextDouble()

            val xRange = bb.maxX - bb.minX
            val yRange = bb.maxY - bb.minY
            val zRange = bb.maxZ - bb.minZ
            var minRange = 999999.0

            if (xRange <= minRange) minRange = xRange
            if (yRange <= minRange) minRange = yRange
            if (zRange <= minRange) minRange = zRange

            rand1 *= minRange * randomRange
            rand2 *= minRange * randomRange
            rand3 *= minRange * randomRange

            val xPrecent = minRange * randomRange / xRange
            val yPrecent = minRange * randomRange / yRange
            val zPrecent = minRange * randomRange / zRange

            val randomVec3 = if (legitRandom) Vec3(
                curVec3!!.xCoord,
                curVec3.yCoord - yPrecent * (curVec3.yCoord - bb.minY) + rand2,
                curVec3.zCoord
            ) else Vec3(
                curVec3!!.xCoord - xPrecent * (curVec3.xCoord - bb.minX) + rand1,
                curVec3.yCoord - yPrecent * (curVec3.yCoord - bb.minY) + rand2,
                curVec3.zCoord - zPrecent * (curVec3.zCoord - bb.minZ) + rand3
            )

            val randomRotation = toRotation(randomVec3, predict)
            vecRotation = VecRotation(randomVec3, randomRotation)

            return vecRotation
        }

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
            return if (serverRotation == null) 0.0 else getRotationDifference(rotation, serverRotation)
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
        fun limitAngleChange(currentRotation: Rotation, targetRotation: Rotation, turnSpeed: Float): Rotation {
            val yawDifference = getAngleDifference(targetRotation.yaw, currentRotation.yaw)
            val pitchDifference = getAngleDifference(targetRotation.pitch, currentRotation.pitch)

            return Rotation(
                currentRotation.yaw + if (yawDifference > turnSpeed) turnSpeed else yawDifference.coerceAtLeast(-turnSpeed),
                currentRotation.pitch + if (pitchDifference > turnSpeed) turnSpeed else pitchDifference.coerceAtLeast(-turnSpeed)
            )
        }
        fun limitAngleChange(
            currentRotation: Rotation,
            targetRotation: Rotation?,
            horizontalSpeed: Float,
            verticalSpeed: Float
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
            if (java.lang.Double.isNaN(rotation.yaw.toDouble()) || java.lang.Double.isNaN(rotation.pitch.toDouble()) || rotation.pitch > 90 || rotation.pitch < -90) return

            rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)
            targetRotation = rotation
            keepLength = kl
            revTick = 0
        }

        var speedForReset = 0f to 0f
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

        fun setTargetRotationReverse(rotation: Rotation, kl: Int, rt: Int) {
            if (java.lang.Double.isNaN(rotation.yaw.toDouble()) || java.lang.Double.isNaN(rotation.pitch.toDouble()) || rotation.pitch > 90 || rotation.pitch < -90) return

            rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)
            targetRotation = rotation
            keepLength = kl
            revTick = rt + 1
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
            targetRotation = if (revTick > 0) {
                Rotation(
                    targetRotation!!.yaw - getAngleDifference(
                        targetRotation!!.yaw, mc.thePlayer.rotationYaw
                    ) / revTick,
                    targetRotation!!.pitch - getAngleDifference(
                        targetRotation!!.pitch, mc.thePlayer.rotationPitch
                    ) / revTick
                )
            } else null
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
