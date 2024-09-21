package dev.tr7zw.skinlayers

import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent


@Mod(
    modid = "skinlayers3d",
    name = "3dSkinLayers",
    version = "@VER@",
    clientSideOnly = true,
    guiFactory = "dev.tr7zw.skinlayers.config.GuiFactory"
)
class SkinLayersMod : SkinLayersModBase() {
    //Forge only
    private var onServer = false

    init {
        try {
            val clientClass: Class<*> = Minecraft::class.java
        } catch (ex: Throwable) {
            println("EntityCulling Mod installed on a Server. Going to sleep.")
            onServer = true
        }
        onInitialize()
    }

    @Mod.EventHandler
    fun onPostInit(event: FMLPostInitializationEvent?) {
        MinecraftForge.EVENT_BUS.register(this)
    }
}
