package dev.tr7zw.skinlayers

import com.mojang.authlib.GameProfile
import dev.tr7zw.skinlayers.accessor.PlayerSettings
import dev.tr7zw.skinlayers.accessor.SkullSettings
import dev.tr7zw.skinlayers.opengl.NativeImage
import dev.tr7zw.skinlayers.render.CustomizableModelPart
import dev.tr7zw.skinlayers.render.SolidPixelWrapper
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.util.ResourceLocation

object SkinUtil {
    @JvmStatic
    fun hasCustomSkin(player: AbstractClientPlayer): Boolean {
        return DefaultPlayerSkin.getDefaultSkin(player.uniqueID) != player.locationSkin
    }

    private fun getSkinTexture(player: AbstractClientPlayer): NativeImage? {
        return getTexture(player.locationSkin)
    }

    private fun getTexture(resource: ResourceLocation): NativeImage? {
        val skin = NativeImage(64, 64, false)
        val textureManager = Minecraft.getMinecraft().textureManager
        val abstractTexture = textureManager.getTexture(resource) ?: return null
        // fail save

        GlStateManager.bindTexture(abstractTexture.glTextureId)
        skin.downloadTexture(0, false)
        return skin
    }

    @JvmStatic
    fun setup3dLayers(
        abstractClientPlayerEntity: AbstractClientPlayer,
        settings: PlayerSettings,
        thinArms: Boolean,
        model: ModelPlayer?
    ) {
        if (!hasCustomSkin(abstractClientPlayerEntity)) {
            return  // default skin
        }
        val skin = getSkinTexture(abstractClientPlayerEntity) ?: return
        // fail save

        val layers = arrayOfNulls<CustomizableModelPart>(5)
        layers[0] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 0, 48, true, 0f)
        layers[1] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 0, 32, true, 0f)
        if (thinArms) {
            layers[2] = SolidPixelWrapper.wrapBox(skin, 3, 12, 4, 48, 48, true, -2.5f)
            layers[3] = SolidPixelWrapper.wrapBox(skin, 3, 12, 4, 40, 32, true, -2.5f)
        } else {
            layers[2] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 48, 48, true, -2.5f)
            layers[3] = SolidPixelWrapper.wrapBox(skin, 4, 12, 4, 40, 32, true, -2.5f)
        }
        layers[4] = SolidPixelWrapper.wrapBox(skin, 8, 12, 4, 16, 32, true, -0.8f)
        settings.setupSkinLayers(layers)
        settings.setupHeadLayers(SolidPixelWrapper.wrapBox(skin, 8, 8, 8, 32, 0, false, 0.6f))
        skin.close()
    }

    fun setup3dLayers(gameprofile: GameProfile?, settings: SkullSettings?): Boolean {
        // no gameprofile
        return false
    }
}