package net.ccbluex.liquidbounce.extensions

import net.ccbluex.liquidbounce.utils.block.block
import net.minecraft.block.material.Material
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

val BlockPos.material: Material?
    get() = this.block?.material

fun BlockPos.toVec() = Vec3(this)