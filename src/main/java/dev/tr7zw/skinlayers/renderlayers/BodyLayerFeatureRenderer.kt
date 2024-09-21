package dev.tr7zw.skinlayers.renderlayers

import dev.tr7zw.skinlayers.SkinLayersModBase
import dev.tr7zw.skinlayers.SkinUtil.hasCustomSkin
import dev.tr7zw.skinlayers.SkinUtil.setup3dLayers
import dev.tr7zw.skinlayers.accessor.PlayerEntityModelAccessor
import dev.tr7zw.skinlayers.accessor.PlayerSettings
import dev.tr7zw.skinlayers.render.CustomizableModelPart
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderPlayer
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.entity.player.EnumPlayerModelParts
import java.util.function.Supplier

class BodyLayerFeatureRenderer
    (playerRenderer: RenderPlayer) : LayerRenderer<AbstractClientPlayer> {
    private val thinArms = (playerRenderer as PlayerEntityModelAccessor).hasThinArms()
    override fun doRenderLayer(
        player: AbstractClientPlayer, paramFloat1: Float, paramFloat2: Float, paramFloat3: Float,
        deltaTick: Float, paramFloat5: Float, paramFloat6: Float, paramFloat7: Float
    ) {
        if (!player.hasSkin() || player.isInvisible) {
            return
        }
        if (mc.theWorld == null) {
            return  // in a menu or something and the model gets rendered
        }
        if (mc.thePlayer.positionVector.squareDistanceTo(player.positionVector) > SkinLayersModBase.config!!.renderDistanceLOD * SkinLayersModBase.config!!.renderDistanceLOD) return

        val settings = player as PlayerSettings
        // check for it being setup first to speedup the rendering
        if (settings.skinLayers == null && !setupModel(player, settings)) {
            return  // no head layer setup and wasn't able to setup
        }


        //this.playerRenderer.bindTexture(player.getLocationSkin());
        renderLayers(player, settings.skinLayers)
    }

    private fun setupModel(abstractClientPlayerEntity: AbstractClientPlayer, settings: PlayerSettings): Boolean {
        if (!hasCustomSkin(abstractClientPlayerEntity)) {
            return false // default skin
        }
        setup3dLayers(abstractClientPlayerEntity, settings, thinArms, null)
        return true
    }

    private val bodyLayers: MutableList<Layer> = ArrayList()

    init {
        bodyLayers.add(
            Layer(
                0,
                false,
                EnumPlayerModelParts.LEFT_PANTS_LEG,
                Shape.LEGS,
                { playerRenderer.mainModel.bipedLeftLeg },
                { SkinLayersModBase.config!!.enableLeftPants })
        )
        bodyLayers.add(
            Layer(
                1,
                false,
                EnumPlayerModelParts.RIGHT_PANTS_LEG,
                Shape.LEGS,
                { playerRenderer.mainModel.bipedRightLeg },
                { SkinLayersModBase.config!!.enableRightPants })
        )
        bodyLayers.add(
            Layer(
                2,
                false,
                EnumPlayerModelParts.LEFT_SLEEVE,
                if (thinArms) Shape.ARMS_SLIM else Shape.ARMS,
                { playerRenderer.mainModel.bipedLeftArm },
                { SkinLayersModBase.config!!.enableLeftSleeve })
        )
        bodyLayers.add(
            Layer(
                3,
                true,
                EnumPlayerModelParts.RIGHT_SLEEVE,
                if (thinArms) Shape.ARMS_SLIM else Shape.ARMS,
                { playerRenderer.mainModel.bipedRightArm },
                { SkinLayersModBase.config!!.enableRightSleeve })
        )
        bodyLayers.add(
            Layer(
                4,
                false,
                EnumPlayerModelParts.JACKET,
                Shape.BODY,
                { playerRenderer.mainModel.bipedBody },
                { SkinLayersModBase.config!!.enableJacket })
        )
    }

    internal class Layer(
        var layersId: Int, var mirrored: Boolean, var modelPart: EnumPlayerModelParts, var shape: Shape,
        var vanillaGetter: Supplier<ModelRenderer>, var configGetter: Supplier<Boolean>
    )


    enum class Shape(val yOffsetMagicValue: Float) {
        HEAD(0F), BODY(0.6f), LEGS(-0.2f), ARMS(0.4f), ARMS_SLIM(0.4f)
    }

    private fun renderLayers(abstractClientPlayer: AbstractClientPlayer, layers: Array<CustomizableModelPart>?) {
        if (layers == null) return
        val pixelScaling = SkinLayersModBase.config!!.baseVoxelSize
        val heightScaling = 1.035f
        var widthScaling: Float
        // Overlay refuses to work correctly, this is a workaround for now
        val redTint = abstractClientPlayer.hurtTime > 0 || abstractClientPlayer.deathTime > 0
        for (layer in bodyLayers) {
            if (abstractClientPlayer.isWearing(layer.modelPart) && !layer.vanillaGetter.get().isHidden && layer.configGetter.get()) {
                GlStateManager.pushMatrix()
                if (abstractClientPlayer.isSneaking) {
                    GlStateManager.translate(0.0f, 0.2f, 0.0f)
                }
                layer.vanillaGetter.get().postRender(0.0625f)
                if (layer.shape == Shape.ARMS) {
                    layers[layer.layersId].x = 0.998f * 16
                } else if (layer.shape == Shape.ARMS_SLIM) {
                    layers[layer.layersId].x = 0.499f * 16
                }
                widthScaling = if (layer.shape == Shape.BODY) {
                    SkinLayersModBase.config!!.bodyVoxelWidthSize
                } else {
                    SkinLayersModBase.config!!.baseVoxelSize
                }
                if (layer.mirrored) {
                    layers[layer.layersId].x *= -1f
                }
                GlStateManager.scale(0.0625, 0.0625, 0.0625)
                GlStateManager.scale(widthScaling, heightScaling, pixelScaling)
                layers[layer.layersId].y = layer.shape.yOffsetMagicValue

                layers[layer.layersId].render(redTint)
                GlStateManager.popMatrix()
            }
        }
    }

    override fun shouldCombineTextures(): Boolean {
        return false
    }

    companion object {
        private val mc: Minecraft = Minecraft.getMinecraft()
    }
}