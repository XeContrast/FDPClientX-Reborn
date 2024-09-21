package dev.tr7zw.skinlayers.render

import dev.tr7zw.skinlayers.Direction
import dev.tr7zw.skinlayers.opengl.NativeImage

object SolidPixelWrapper {
    fun wrapBox(
        natImage: NativeImage, width: Int,
        height: Int, depth: Int, textureU: Int, textureV: Int, topPivot: Boolean, rotationOffset: Float
    ): CustomizableModelPart {
        val cubes: MutableList<CustomizableCube> = ArrayList()
        val pixelSize = 1f
        val staticXOffset = -width / 2f
        val staticYOffset = if (topPivot) +rotationOffset else -height + rotationOffset
        val staticZOffset = -depth / 2f
        // Front/back
        for (u in 0 until width) {
            for (v in 0 until height) {
                // front
                addPixel(
                    natImage, cubes, pixelSize, u == 0 || v == 0 || u == width - 1 || v == height - 1,
                    textureU + depth + u, textureV + depth + v, staticXOffset + u, staticYOffset + v, staticZOffset,
                    Direction.SOUTH
                )
                // back
                addPixel(
                    natImage, cubes, pixelSize, u == 0 || v == 0 || u == width - 1 || v == height - 1,
                    textureU + 2 * depth + width + u, textureV + depth + v, staticXOffset + width - 1 - u,
                    staticYOffset + v, staticZOffset + depth - 1, Direction.NORTH
                )
            }
        }

        // sides
        for (u in 0 until depth) {
            for (v in 0 until height) {
                // left
                addPixel(
                    natImage, cubes, pixelSize, u == 0 || v == 0 || u == depth - 1 || v == height - 1,
                    textureU - 1 + depth - u, textureV + depth + v, staticXOffset, staticYOffset + v,
                    staticZOffset + u, Direction.EAST
                )
                // right
                addPixel(
                    natImage, cubes, pixelSize, u == 0 || v == 0 || u == depth - 1 || v == height - 1,
                    textureU + depth + width + u, textureV + depth + v, staticXOffset + width - 1f,
                    staticYOffset + v, staticZOffset + u, Direction.WEST
                )
            }
        }
        // top/bottom
        for (u in 0 until width) {
            for (v in 0 until depth) {
                // top
                addPixel(
                    natImage, cubes, pixelSize, u == 0 || v == 0 || u == width - 1 || v == depth - 1,
                    textureU + depth + u, textureV + depth - 1 - v, staticXOffset + u, staticYOffset,
                    staticZOffset + v, Direction.UP
                ) // Sides are flipped cause ?!?
                // bottom
                addPixel(
                    natImage, cubes, pixelSize, u == 0 || v == 0 || u == width - 1 || v == depth - 1,
                    textureU + depth + width + u, textureV + depth - 1 - v, staticXOffset + u,
                    staticYOffset + height - 1f, staticZOffset + v, Direction.DOWN
                ) // Sides are flipped cause ?!?
            }
        }

        return CustomizableModelPart(cubes)
    }

    private val offsets = arrayOf(intArrayOf(0, 1), intArrayOf(0, -1), intArrayOf(1, 0), intArrayOf(-1, 0))
    private val hiddenDirN = arrayOf(
        Direction.WEST, Direction.EAST, Direction.UP,
        Direction.DOWN
    )
    private val hiddenDirS = arrayOf(
        Direction.EAST, Direction.WEST, Direction.UP,
        Direction.DOWN
    )
    private val hiddenDirW = arrayOf(
        Direction.SOUTH, Direction.NORTH, Direction.UP,
        Direction.DOWN
    )
    private val hiddenDirE = arrayOf(
        Direction.NORTH, Direction.SOUTH, Direction.UP,
        Direction.DOWN
    )
    private val hiddenDirUD = arrayOf(
        Direction.EAST, Direction.WEST, Direction.NORTH,
        Direction.SOUTH
    )

    private fun addPixel(
        natImage: NativeImage, cubes: MutableList<CustomizableCube>, pixelSize: Float, onBorder: Boolean, u: Int,
        v: Int, x: Float, y: Float, z: Float, dir: Direction
    ) {
        if (natImage.getLuminanceOrAlpha(u, v).toInt() != 0) {
            val hide: MutableSet<Direction> = HashSet()
            if (!onBorder) {
                for (i in offsets.indices) {
                    val tU = u + offsets[i][1]
                    val tV = v + offsets[i][0]
                    if (tU in 0..63 && tV >= 0 && tV < 64 && natImage.getLuminanceOrAlpha(tU, tV).toInt() != 0) {
                        if (dir == Direction.NORTH) hide.add(hiddenDirN[i])
                        if (dir == Direction.SOUTH) hide.add(hiddenDirS[i])
                        if (dir == Direction.EAST) hide.add(hiddenDirE[i])
                        if (dir == Direction.WEST) hide.add(hiddenDirW[i])
                        if (dir == Direction.UP || dir == Direction.DOWN) hide.add(
                            hiddenDirUD[i]
                        )
                    }
                }
                hide.add(dir)
            }
            cubes.addAll(
                CustomizableCubeListBuilder.create().texOffs(u - 2, v - 1)
                    .addBox(x, y, z, pixelSize, hide.toTypedArray<Direction?>()).cubes
            )
        }
    }
}
