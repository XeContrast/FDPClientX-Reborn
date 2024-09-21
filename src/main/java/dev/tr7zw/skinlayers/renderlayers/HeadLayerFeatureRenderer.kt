package dev.tr7zw.skinlayers.renderlayers

import com.google.common.collect.Sets
import dev.tr7zw.skinlayers.SkinLayersModBase
import dev.tr7zw.skinlayers.SkinUtil.hasCustomSkin
import dev.tr7zw.skinlayers.SkinUtil.setup3dLayers
import dev.tr7zw.skinlayers.accessor.PlayerEntityModelAccessor
import dev.tr7zw.skinlayers.accessor.PlayerSettings
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderPlayer
import net.minecraft.client.renderer.entity.layers.LayerRenderer
import net.minecraft.init.Items
import net.minecraft.item.Item

class HeadLayerFeatureRenderer(private val playerRenderer: RenderPlayer) : LayerRenderer<AbstractClientPlayer> {
    private val hideHeadLayers: Set<Item> = Sets.newHashSet(Items.skull)
    private val thinArms = (playerRenderer as PlayerEntityModelAccessor).hasThinArms()

    override fun doRenderLayer(
        player: AbstractClientPlayer, paramFloat1: Float, paramFloat2: Float, paramFloat3: Float,
        deltaTick: Float, paramFloat5: Float, paramFloat6: Float, paramFloat7: Float
    ) {
        if (!player.hasSkin() || player.isInvisible || !SkinLayersModBase.config!!.enableHat) {
            return
        }
        if (mc.thePlayer.positionVector.squareDistanceTo(player.positionVector) > SkinLayersModBase.config!!.renderDistanceLOD * SkinLayersModBase.config!!.renderDistanceLOD) return

        val itemStack = player.getEquipmentInSlot(1) //TODO
        if (itemStack != null && hideHeadLayers.contains(itemStack.item)) {
            return
        }

        val settings = player as PlayerSettings
        // check for it being setup first to speedup the rendering
        if (settings.headLayers == null && !setupModel(player, settings)) {
            return  // no head layer setup and wasn't able to setup
        }

        //this.playerRenderer.bindTexture(player.getLocationSkin());
        renderCustomHelmet(settings, player)
    }

    private fun setupModel(abstractClientPlayerEntity: AbstractClientPlayer, settings: PlayerSettings): Boolean {
        if (!hasCustomSkin(abstractClientPlayerEntity)) {
            return false // default skin
        }
        setup3dLayers(abstractClientPlayerEntity, settings, thinArms, null)
        return true
    }

    private fun renderCustomHelmet(settings: PlayerSettings, abstractClientPlayer: AbstractClientPlayer) {
        if (settings.headLayers == null) return
        if (playerRenderer.mainModel.bipedHead.isHidden) return
        val voxelSize = SkinLayersModBase.config!!.headVoxelSize
        GlStateManager.pushMatrix()
        if (abstractClientPlayer.isSneaking) {
            GlStateManager.translate(0.0f, 0.2f, 0.0f)
        }
        playerRenderer.mainModel.bipedHead.postRender(0.0625f)
        //this.getParentModel().head.translateAndRotate(matrixStack);
        GlStateManager.scale(0.0625, 0.0625, 0.0625)
        GlStateManager.scale(voxelSize, voxelSize, voxelSize)


        // Overlay refuses to work correctly, this is a workaround for now
        val tintRed = abstractClientPlayer.hurtTime > 0 || abstractClientPlayer.deathTime > 0
        settings.headLayers.render(tintRed)
        GlStateManager.popMatrix()
    }

    override fun shouldCombineTextures(): Boolean {
        return false
    }


    companion object {
        private val mc: Minecraft = Minecraft.getMinecraft()
    }
}
