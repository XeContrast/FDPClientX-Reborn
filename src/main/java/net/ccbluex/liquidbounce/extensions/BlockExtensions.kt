package net.ccbluex.liquidbounce.extensions

import net.ccbluex.liquidbounce.utils.block.block
import net.minecraft.block.material.Material
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.util.Vec3i

val BlockPos.material: Material?
    get() = this.block?.material

fun BlockPos.toVec() = Vec3(this)

fun BlockPos.MutableBlockPos.set(vec3i: Vec3i, xOffset: Int = 0, yOffset: Int = 0, zOffset: Int = 0): BlockPos.MutableBlockPos =
    set(vec3i.x + xOffset, vec3i.y + yOffset, vec3i.z + zOffset)