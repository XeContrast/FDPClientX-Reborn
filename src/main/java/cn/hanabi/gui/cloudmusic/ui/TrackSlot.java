package cn.hanabi.gui.cloudmusic.ui;

import cn.hanabi.gui.cloudmusic.MusicManager;
import cn.hanabi.gui.cloudmusic.impl.Track;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class TrackSlot {

    public Track track;
    public float x;
    public float y;

    public TrackSlot(Track t) {
        this.track = t;
    }

    public void render(float a, float b, int mouseX, int mouseY) {
        this.x = a;
        this.y = b;

        RenderUtils.drawRoundedRect(x, y, x + 137, y + 20, 2, 0xff34373c);

        Minecraft.getMinecraft().fontRendererObj.drawString(track.name, (int) (x + 2), (int) (y + 1), Color.WHITE.getRGB());
        Minecraft.getMinecraft().fontRendererObj.drawString(track.artists, (int) (x + 2), (int) (y + 10), Color.WHITE.getRGB());

        RenderUtils.drawRoundedRect(x + 122, y, x + 137, y + 20, 2, 0xff34373c);
        //RenderUtils.drawGradientSideways(x + 100, y, x + 124, y + 20, 0x00818181, 0xff34373c);

        Minecraft.getMinecraft().fontRendererObj.drawString(">", (int) (x + 125.5f), (int) (y + 5.5f), Color.WHITE.getRGB());

        //RenderUtils.drawOutlinedRect(x + 125, y + 5, x + 135, y + 15, .5f, Color.RED.getRGB());
    }

    public void click(int mouseX, int mouseY, int mouseButton) {
        if (RenderUtils.isHovering(mouseX, mouseY, x + 125, y + 5, x + 135, y + 15) && mouseButton == 0) {
            try {
                MusicManager.INSTANCE.play(track);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
