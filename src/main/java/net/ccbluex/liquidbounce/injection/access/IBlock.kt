package net.ccbluex.liquidbounce.injection.access

import net.minecraft.world.IBlockAccess

interface IBlock {
    fun getLightValue(var1: IBlockAccess?, var2: Int, var3: Int, var4: Int): Int

    fun getLightOpacity(var1: IBlockAccess?, var2: Int, var3: Int, var4: Int): Int
}

