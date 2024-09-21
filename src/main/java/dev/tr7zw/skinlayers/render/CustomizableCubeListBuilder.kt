package dev.tr7zw.skinlayers.render

import com.google.common.collect.Lists
import dev.tr7zw.skinlayers.Direction
import lombok.Getter

class CustomizableCubeListBuilder {
    @Getter
    val cubes: MutableList<CustomizableCube> = Lists.newArrayList()
    private var xTexOffs = 0
    private var yTexOffs = 0
    private val mirror = false

    fun texOffs(i: Int, j: Int): CustomizableCubeListBuilder {
        this.xTexOffs = i
        this.yTexOffs = j
        return this
    }

    fun addBox(x: Float, y: Float, z: Float, pixelSize: Float, hide: Array<Direction?>?): CustomizableCubeListBuilder {
        val textureSize = 64
        cubes.add(
            CustomizableCube(
                xTexOffs, yTexOffs, x, y, z, pixelSize, pixelSize, pixelSize, 0f, 0f, 0f,
                this.mirror, textureSize.toFloat(), textureSize.toFloat(), hide
            )
        )
        return this
    }

    companion object {
        fun create(): CustomizableCubeListBuilder {
            return CustomizableCubeListBuilder()
        }
    }
}
