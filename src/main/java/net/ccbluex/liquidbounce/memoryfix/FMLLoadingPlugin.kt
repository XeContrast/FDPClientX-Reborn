package net.ccbluex.liquidbounce.memoryfix

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex

@MCVersion("1.8.9")
@SortingIndex(1001)
class FMLLoadingPlugin : IFMLLoadingPlugin {
    override fun getASMTransformerClass(): Array<String> {
        return arrayOf(
            ClassTransformer::class.java.name
        )
    }

    override fun getModContainerClass(): String? {
        return null
    }

    override fun getSetupClass(): String? {
        return null
    }

    override fun injectData(data: Map<String, Any>) {
    }

    override fun getAccessTransformerClass(): String? {
        return null
    }
}