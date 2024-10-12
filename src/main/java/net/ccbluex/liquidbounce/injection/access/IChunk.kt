package net.ccbluex.liquidbounce.injection.access

import net.minecraft.block.state.IBlockState
import net.minecraft.world.EnumSkyBlock

interface IChunk {
    fun getLightFor(var1: EnumSkyBlock?, var2: Int, var3: Int, var4: Int): Int

    fun getLightSubtracted(var1: Int, var2: Int, var3: Int, var4: Int): Int

    fun canSeeSky(var1: Int, var2: Int, var3: Int): Boolean

    fun setLightFor(var1: EnumSkyBlock?, var2: Int, var3: Int, var4: Int, var5: Int)

    fun getBlockState(var1: Int, var2: Int, var3: Int): IBlockState?
}

