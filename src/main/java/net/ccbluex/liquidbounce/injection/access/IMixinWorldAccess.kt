package net.ccbluex.liquidbounce.injection.access

interface IMixinWorldAccess {
    fun markBlockForUpdate(var1: Int, var2: Int, var3: Int)

    fun notifyLightSet(var1: Int, var2: Int, var3: Int)
}
