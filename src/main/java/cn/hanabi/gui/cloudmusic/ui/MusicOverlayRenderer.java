package cn.hanabi.gui.cloudmusic.ui;

import cn.hanabi.gui.cloudmusic.MusicManager;
import net.ccbluex.liquidbounce.font.FontLoaders;
import net.ccbluex.liquidbounce.ui.realpha;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.ccbluex.liquidbounce.utils.extensions.RendererExtensionKt;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public enum MusicOverlayRenderer {
    INSTANCE;

    public String downloadProgress = "0";

    public long readedSecs = 0;
    public long totalSecs = 0;

    public float animation = 0;

    public MSTimer timer = new MSTimer();

    public boolean firstTime = true;


    public void renderOverlay() {
        int addonX = 10;
        int addonY = 60;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        if (MusicManager.INSTANCE.getCurrentTrack() != null && MusicManager.INSTANCE.getMediaPlayer() != null) {
            readedSecs = (int) MusicManager.INSTANCE.getMediaPlayer().getCurrentTime().toSeconds();
            totalSecs = (int) MusicManager.INSTANCE.getMediaPlayer().getStopTime().toSeconds();
        }

        if (MusicManager.INSTANCE.getCurrentTrack() != null && MusicManager.INSTANCE.getMediaPlayer() != null) {
            FontLoaders.C18.DisplayFonts(FontLoaders.C18, MusicManager.INSTANCE.getCurrentTrack().name + " - " + MusicManager.INSTANCE.getCurrentTrack().artists, 36f + addonX, 10 + addonY, Color.WHITE.getRGB());
            FontLoaders.C18.DisplayFonts(FontLoaders.C18, formatSeconds((int) readedSecs) + "/" + formatSeconds((int) totalSecs), 36f + addonX, 20f + addonY, 0xffffffff);

            if (MusicManager.INSTANCE.circleLocations.containsKey(MusicManager.INSTANCE.getCurrentTrack().id)) {
                GL11.glPushMatrix();
                GL11.glColor4f(1, 1, 1, 1);
                ResourceLocation icon = MusicManager.INSTANCE.circleLocations.get(MusicManager.INSTANCE.getCurrentTrack().id);
                RenderUtils.drawImage(icon, 4 + addonX, 6 + addonY, 28, 28);
                GL11.glPopMatrix();
            } else {
                MusicManager.INSTANCE.getCircle(MusicManager.INSTANCE.getCurrentTrack());
            }

            try {
                float currentProgress = (float) (MusicManager.INSTANCE.getMediaPlayer().getCurrentTime().toSeconds() / Math.max(1, MusicManager.INSTANCE.getMediaPlayer().getStopTime().toSeconds())) * 100;
                RenderUtils.drawArc(18 + addonX, 19 + addonY, 14, Color.WHITE.getRGB(), 0, 360, 4);
                RenderUtils.drawArc(18 + addonX, 19 + addonY, 14, Color.BLUE.getRGB(), 180, 180 + (currentProgress * 3.6f), 4);
            } catch (Exception ignored) {
            }
        }

        if (MusicManager.INSTANCE.lyric) {
            {

                FontRenderer lyricFont = Minecraft.getMinecraft().fontRendererObj;
                int addonYlyr = 50;
                //Lyric
                int col = MusicManager.INSTANCE.tlrc.isEmpty() ? Color.GRAY.getRGB() : 0xff00af87;
                GlStateManager.disableBlend();
                RendererExtensionKt.drawCenteredString(lyricFont, MusicManager.INSTANCE.lrcCur.contains("_EMPTY_") ? "等待中......." : MusicManager.INSTANCE.lrcCur, (sr.getScaledWidth() / 2f - 0.5f), sr.getScaledHeight() - 140 - 80 + addonYlyr, 0xff00af87);
                RendererExtensionKt.drawCenteredString(lyricFont, MusicManager.INSTANCE.tlrcCur.contains("_EMPTY_") ? "Waiting......." : MusicManager.INSTANCE.tlrcCur, (sr.getScaledWidth() / 2f), (sr.getScaledHeight() - 125 + 0.5f - 80 + addonYlyr), col);
                GlStateManager.enableBlend();
            }
        }

        if ((MusicManager.showMsg)) {
            if (firstTime) {
                timer.reset();
                firstTime = false;
            }

            FontRenderer wqy = Minecraft.getMinecraft().fontRendererObj;
            FontRenderer sans = Minecraft.getMinecraft().fontRendererObj;

            float width1 = wqy.getStringWidth(MusicManager.INSTANCE.getCurrentTrack().name);
            float width2 = sans.getStringWidth("Now playing");
            float allWidth = (Math.max(Math.max(width1, width2), 150));

            RenderUtils.drawRect(sr.getScaledWidth() - animation, 5, sr.getScaledWidth(), 40, realpha.reAlpha(Color.BLACK.getRGB(), 0.7f));

            if (MusicManager.INSTANCE.circleLocations.containsKey(MusicManager.INSTANCE.getCurrentTrack().id)) {
                GL11.glPushMatrix();
                GL11.glColor4f(1, 1, 1, 1);
                ResourceLocation icon = MusicManager.INSTANCE.circleLocations.get(MusicManager.INSTANCE.getCurrentTrack().id);
                RenderUtils.drawImage2(icon, sr.getScaledWidth() - animation + 5, 8, 28, 28);
                GL11.glPopMatrix();
            } else {
                MusicManager.INSTANCE.getCircle(MusicManager.INSTANCE.getCurrentTrack());
            }

            RenderUtils.drawArc(sr.getScaledWidth() - animation - 31 + 50, 22, 14, Color.WHITE.getRGB(), 0, 360, 2);

            sans.drawString("Now playing", (int) (sr.getScaledWidth() - animation - 12 + 50), 8, Color.WHITE.getRGB());
            wqy.drawString(MusicManager.INSTANCE.getCurrentTrack().name, (int) (sr.getScaledWidth() - animation - 12 + 50), 26, Color.WHITE.getRGB());

            if (timer.hasTimePassed(5000)) {
                this.animation = (float) RenderUtils.getAnimationStateSmooth(0, animation, 10.0f / Minecraft.getDebugFPS());
                if (this.animation <= 0) {
                    MusicManager.showMsg = false;
                    firstTime = true;
                }
            } else {
                this.animation = (float) RenderUtils.getAnimationStateSmooth(allWidth, animation, 10.0f / Minecraft.getDebugFPS());
            }

        }

        GlStateManager.resetColor();
    }

    public String formatSeconds(int seconds) {
        String rstl = "";
        int mins = seconds / 60;
        if (mins < 10) {
            rstl += "0";
        }
        rstl += mins + ":";
        seconds %= 60;
        if (seconds < 10) {
            rstl += "0";
        }
        rstl += seconds;
        return rstl;
    }
}
