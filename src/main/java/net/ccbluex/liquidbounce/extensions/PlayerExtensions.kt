package net.ccbluex.liquidbounce.extensions

import kevin.utils.multiply
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.hypot

val Entity.currPos: Vec3
    get() = this.positionVector
fun Entity.getDistance(pos: BlockPos) : Double {
    val x = this.posX - pos.x
    val y = this.posY - pos.y
    val z = this.posZ - pos.z
    return MathHelper.sqrt_double(x * x + y * y + z * z).toDouble()
}
val EntityLivingBase.isMoving: Boolean
    get() = this.run { moveForward != 0F || moveStrafing != 0F }
val Entity.rotation: Rotation
    get() = Rotation(rotationYaw, rotationPitch)
val IMixinEntity.interpolatedPosition
    get() = Vec3(lerpX, lerpY, lerpZ)
fun Entity.interpolatedPosition(start: Vec3) = Vec3(
    start.xCoord + (posX - start.xCoord) * mc.timer.renderPartialTicks,
    start.yCoord + (posY - start.yCoord) * mc.timer.renderPartialTicks,
    start.zCoord + (posZ - start.zCoord) * mc.timer.renderPartialTicks
)
fun Entity.getLookDistanceToEntityBox(entity: Entity =this, rotation: Rotation? = null, range: Double=10.0): Double {
    val eyes = this.eyes
    val end = (rotation?: RotationUtils.bestServerRotation())!!.toDirection().multiply(range).add(eyes)
    return entity.entityBoundingBox.calculateIntercept(eyes, end)?.hitVec?.distanceTo(eyes) ?: Double.MAX_VALUE
}

fun Entity.speed() : Double {
    return hypot(this.posX - this.prevPosX, this.posZ - this.prevPosZ) * 20
}

fun Entity.getSmoothDistanceToEntity(entityIn: Entity?): Float {
    val pTicks = Minecraft.getMinecraft().timer.renderPartialTicks
    val xposme: Double = this.lastTickPosX + (this.posX - this.lastTickPosX) * pTicks.toDouble()
    val yposme: Double = this.lastTickPosY + (this.posY - this.lastTickPosY) * pTicks.toDouble()
    val zposme: Double = this.lastTickPosZ + (this.posZ - this.lastTickPosZ) * pTicks.toDouble()
    var xposent = 0.0
    var yposent = 0.0
    var zposent = 0.0
    if (entityIn != null) {
        xposent = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * pTicks.toDouble()
        yposent = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * pTicks.toDouble()
        zposent = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * pTicks.toDouble()
    }
    val f = (xposme - xposent).toFloat()
    val f1 = (yposme - yposent).toFloat()
    val f2 = (zposme - zposent).toFloat()
    return if (entityIn != null) MathHelper.sqrt_double((f * f + f1 * f1 + f2 * f2).toDouble()) else 0.0f
}

fun EntityPlayer.getSmoothDistanceToCoord(x: Float, y: Float, z: Float): Float {
    val pTicks = Minecraft.getMinecraft().timer.renderPartialTicks
    val xposme: Double = this.lastTickPosX + (this.posX - this.lastTickPosX) * pTicks.toDouble()
    val yposme: Double = this.lastTickPosY + (this.posY - this.lastTickPosY) * pTicks.toDouble()
    val zposme: Double = this.lastTickPosZ + (this.posZ - this.lastTickPosZ) * pTicks.toDouble()
    val f = (xposme - x.toDouble()).toFloat()
    val f1 = (yposme - y.toDouble()).toFloat()
    val f2 = (zposme - z.toDouble()).toFloat()
    return MathHelper.sqrt_double((f * f + f1 * f1 + f2 * f2).toDouble())
}

fun BlockPos.getAllInBoxMutable(radius: Int): Iterable<BlockPos> {
    return BlockPos.getAllInBoxMutable(add(-radius, -radius, -radius), add(radius, radius, radius))
}

fun EntityPlayerSP.tryJump() {
    if (!mc.gameSettings.keyBindJump.isKeyDown) {
        jump()
    }
}

/**
 * Its sole purpose is to prevent duplicate sprint state updates.
 */
infix fun EntityLivingBase.setSprintSafely(new: Boolean) {
    if (new == isSprinting) {
        return
    }
    isSprinting = new
}