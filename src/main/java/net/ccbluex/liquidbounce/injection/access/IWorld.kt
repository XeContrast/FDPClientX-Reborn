package net.ccbluex.liquidbounce.injection.access

import net.minecraft.block.state.IBlockState
import net.minecraft.world.EnumSkyBlock
import net.minecraft.world.chunk.Chunk

interface IWorld {
    fun isAreaLoaded(var1: Int, var2: Int, var3: Int, var4: Int, var5: Boolean): Boolean

    fun isBlockLoaded(var1: Int, var2: Int, var3: Int): Boolean

    fun isBlockLoaded(var1: Int, var2: Int, var3: Int, var4: Boolean): Boolean

    fun isValid(var1: Int, var2: Int, var3: Int): Boolean

    fun canSeeSky(var1: Int, var2: Int, var3: Int): Boolean

    fun getCombinedLight(var1: Int, var2: Int, var3: Int, var4: Int): Int

    fun getRawLight(var1: Int, var2: Int, var3: Int, var4: EnumSkyBlock?): Int

    fun getLight(var1: Int, var2: Int, var3: Int, var4: Boolean): Int

    fun getLightFor(var1: EnumSkyBlock?, var2: Int, var3: Int, var4: Int): Int

    fun getLightFromNeighbors(var1: Int, var2: Int, var3: Int): Int

    fun getLightFromNeighborsFor(var1: EnumSkyBlock?, var2: Int, var3: Int, var4: Int): Int

    fun setLightFor(var1: EnumSkyBlock?, var2: Int, var3: Int, var4: Int, var5: Int)

    fun checkLight(var1: Int, var2: Int, var3: Int): Boolean

    fun checkLightFor(var1: EnumSkyBlock?, var2: Int, var3: Int, var4: Int): Boolean

    fun getLightBrightness(var1: Int, var2: Int, var3: Int): Float

    fun getBlockState(n: Int, n2: Int, n3: Int): IBlockState?

    fun setBlockState(var1: Int, var2: Int, var3: Int, var4: IBlockState?, var5: Int): Boolean

    fun markBlockForUpdate(var1: Int, var2: Int, var3: Int)

    fun markAndNotifyBlock(
        var1: Int,
        var2: Int,
        var3: Int,
        var4: Chunk?,
        var5: IBlockState?,
        var6: IBlockState?,
        var7: Int
    )

    fun notifyLightSet(var1: Int, var2: Int, var3: Int)

    fun getChunkFromBlockCoords(var1: Int, var2: Int, var3: Int): Chunk?
}