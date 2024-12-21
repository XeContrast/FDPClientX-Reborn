/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge

import net.ccbluex.liquidbounce.injection.transformers.ForgeNetworkTransformer
import net.ccbluex.liquidbounce.injection.transformers.OptimizeTransformer
import net.ccbluex.liquidbounce.injection.transformers.ViaForgeSupportTransformer
import net.ccbluex.liquidbounce.script.remapper.injection.transformers.AbstractJavaLinkerTransformer
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.Mixins

@MCVersion("1.8.9")
class TransformerLoader : IFMLLoadingPlugin {
    init {
        MixinBootstrap.init()
        Mixins.addConfiguration("mixins.fdpclient.json")
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT)
    }

    /**
     * Return a list of classes that implements the IClassTransformer interface
     *
     * @return a list of classes that implements the IClassTransformer interface
     */
    override fun getASMTransformerClass(): Array<String> {
        return arrayOf(
            ForgeNetworkTransformer::class.java.name,
            AbstractJavaLinkerTransformer::class.java.name,
            ViaForgeSupportTransformer::class.java.name,
            OptimizeTransformer::class.java.name
        )
    }

    /**
     * Return a class name that implements "ModContainer" for injection into the mod list
     * The "getName" function should return a name that other mods can, if need be,
     * depend on.
     * Trivially, this modcontainer will be loaded before all regular mod containers,
     * which means it will be forced to be "immutable" - not susceptible to normal
     * sorting behaviour.
     * All other mod behaviours are available however- this container can receive and handle
     * normal loading events
     */
    override fun getModContainerClass(): String? {
        return null
    }

    /**
     * Return the class name of an implementor of "IFMLCallHook", that will be run, in the
     * main thread, to perform any additional setup this coremod may require. It will be
     * run **prior** to Minecraft starting, so it CANNOT operate on minecraft
     * itself. The game will deliberately crash if this code is detected to trigger a
     * minecraft class loading
     */
    override fun getSetupClass(): String? {
        return null
    }

    /**
     * Inject coremod data into this coremod
     * This data includes:
     * "mcLocation" : the location of the minecraft directory,
     * "coremodList" : the list of coremods
     * "coremodLocation" : the file this coremod loaded from,
     *
     * @param data NoTime
     */
    override fun injectData(data: Map<String, Any>) {
    }

    /**
     * Return an optional access transformer class for this coremod. It will be injected post-deobf
     * so ensure your ATs conform to the new srgnames scheme.
     *
     * @return the name of an access transformer class or null if none is provided
     */
    override fun getAccessTransformerClass(): String? {
        return null
    }
}
