package dev.tr7zw.skinlayers

import dev.tr7zw.skinlayers.accessor.SkullSettings
import dev.tr7zw.skinlayers.render.CustomizableModelPart
import net.minecraft.item.ItemStack
import java.util.*

object SkullRendererCache {
    var renderNext: Boolean = false
    var lastSkull: SkullSettings? = null
    var itemCache: WeakHashMap<ItemStack, SkullSettings> = WeakHashMap()

    class ItemSettings : SkullSettings {
        private var hatModel: CustomizableModelPart? = null

        override fun getHeadLayers(): CustomizableModelPart {
            return hatModel!!
        }

        override fun setupHeadLayers(box: CustomizableModelPart) {
            this.hatModel = box
        }
    }
}
