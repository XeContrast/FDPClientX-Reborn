package dev.tr7zw.skinlayers.config

import com.google.common.collect.Lists
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiListExtended
import net.minecraft.client.gui.GuiOptionButton
import net.minecraft.client.settings.GameSettings
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class GuiButtonRowList(
    p_i45015_1_: Minecraft?, p_i45015_2_: Int, p_i45015_3_: Int, p_i45015_4_: Int, p_i45015_5_: Int,
    p_i45015_6_: Int, buttons: List<GuiButton>
) : GuiListExtended(p_i45015_1_, p_i45015_2_, p_i45015_3_, p_i45015_4_, p_i45015_5_, p_i45015_6_) {
    private val field_148184_k: MutableList<Row> = Lists.newArrayList()

    init {
        this.field_148163_i = false
        var lvt_8_1_ = 0
        while (lvt_8_1_ < buttons.size) {
            buttons[lvt_8_1_].xPosition = p_i45015_2_ / 2 - 155
            if (lvt_8_1_ < buttons.size - 1) {
                buttons[lvt_8_1_ + 1].xPosition = p_i45015_2_ / 2 - 155 + 160
            }
            field_148184_k.add(
                Row(
                    buttons[lvt_8_1_],
                    if ((lvt_8_1_ < buttons.size - 1)) buttons[lvt_8_1_ + 1] else null
                )
            )
            lvt_8_1_ += 2
        }
    }

    override fun getListEntry(p_getListEntry_1_: Int): Row {
        return field_148184_k[p_getListEntry_1_]
    }

    override fun getSize(): Int {
        return field_148184_k.size
    }

    override fun getListWidth(): Int {
        return 400
    }

    override fun getScrollBarX(): Int {
        return super.getScrollBarX() + 32
    }

    class Row(private val field_148323_b: GuiButton?, private val field_148324_c: GuiButton?) : IGuiListEntry {
        private val field_148325_a: Minecraft = Minecraft.getMinecraft()

        override fun drawEntry(
            p_drawEntry_1_: Int, p_drawEntry_2_: Int, p_drawEntry_3_: Int, p_drawEntry_4_: Int,
            p_drawEntry_5_: Int, p_drawEntry_6_: Int, p_drawEntry_7_: Int, p_drawEntry_8_: Boolean
        ) {
            if (this.field_148323_b != null) {
                field_148323_b.yPosition = p_drawEntry_3_
                field_148323_b.drawButton(this.field_148325_a, p_drawEntry_6_, p_drawEntry_7_)
            }
            if (this.field_148324_c != null) {
                field_148324_c.yPosition = p_drawEntry_3_
                field_148324_c.drawButton(this.field_148325_a, p_drawEntry_6_, p_drawEntry_7_)
            }
        }

        override fun mousePressed(
            p_mousePressed_1_: Int, p_mousePressed_2_: Int, p_mousePressed_3_: Int,
            p_mousePressed_4_: Int, p_mousePressed_5_: Int, p_mousePressed_6_: Int
        ): Boolean {
            if (field_148323_b!!.mousePressed(this.field_148325_a, p_mousePressed_2_, p_mousePressed_3_)) {
                if (field_148323_b is GuiOptionButton) {
                    field_148325_a.gameSettings
                        .setOptionValue((field_148323_b as GuiOptionButton?)!!.returnEnumOptions(), 1)
                    field_148323_b.displayString = field_148325_a.gameSettings
                        .getKeyBinding(GameSettings.Options.getEnumOptions(field_148323_b.id))
                }
                return true
            }
            return (this.field_148324_c != null
                    && field_148324_c.mousePressed(this.field_148325_a, p_mousePressed_2_, p_mousePressed_3_))
        }

        override fun mouseReleased(
            p_mouseReleased_1_: Int, p_mouseReleased_2_: Int, p_mouseReleased_3_: Int,
            p_mouseReleased_4_: Int, p_mouseReleased_5_: Int, p_mouseReleased_6_: Int
        ) {
            if (this.field_148323_b != null) field_148323_b.mouseReleased(p_mouseReleased_2_, p_mouseReleased_3_)
            if (this.field_148324_c != null) field_148324_c.mouseReleased(p_mouseReleased_2_, p_mouseReleased_3_)
        }

        override fun setSelected(p_setSelected_1_: Int, p_setSelected_2_: Int, p_setSelected_3_: Int) {
        }
    }
}