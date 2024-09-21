package dev.tr7zw.skinlayers

class Config {
    @JvmField
    var enableHat: Boolean = true
    @JvmField
    var enableJacket: Boolean = true
    @JvmField
    var enableLeftSleeve: Boolean = true
    @JvmField
    var enableRightSleeve: Boolean = true
    @JvmField
    var enableLeftPants: Boolean = true
    @JvmField
    var enableRightPants: Boolean = true

    @JvmField
    var baseVoxelSize: Float = 1.15f
    @JvmField
    var bodyVoxelWidthSize: Float = 1.05f

    //public float bodyVoxelHeightSize = 1.02f;
    @JvmField
    var headVoxelSize: Float = 1.18f

    @JvmField
    var renderDistanceLOD: Int = 14

    @JvmField
    var enableSkulls: Boolean = true
    @JvmField
    var enableSkullsItems: Boolean = true
    @JvmField
    var skullVoxelSize: Float = 1.1f

    @JvmField
    var fastRender: Boolean = true
}
