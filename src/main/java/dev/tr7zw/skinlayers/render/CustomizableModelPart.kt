package dev.tr7zw.skinlayers.render

import net.minecraft.client.model.ModelBox
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator

/**
 * Cut down copy of the Vanilla ModelPart to bypass Optifine/Sodium screwing
 * with the CustomizableCube class
 *
 */
class CustomizableModelPart(private val cubes: List<CustomizableCube>) {
    @JvmField
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var visible: Boolean = true

    fun copyFrom(modelPart: ModelBox) {
        this.x = modelPart.posX1
        this.y = modelPart.posY1
        this.z = modelPart.posZ1
    }

    fun setPos(f: Float, g: Float, h: Float) {
        this.x = f
        this.y = g
        this.z = h
    }


    fun render(redTint: Boolean) {
        if (!this.visible) return
        GlStateManager.pushMatrix()
        translateAndRotate()
        compile(redTint)
        GlStateManager.popMatrix()
    }

    private fun translateAndRotate() {
        GlStateManager.translate((this.x / 16.0f), (this.y / 16.0f), (this.z / 16.0f))
    }

    private fun compile(redTint: Boolean) {
        for (cube in this.cubes) cube.render(Tessellator.getInstance().worldRenderer)
    }
}
