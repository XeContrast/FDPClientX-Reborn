package cn.hanabi.gui.cloudmusic.ui;

import cn.hanabi.gui.cloudmusic.MusicManager;
import cn.hanabi.gui.cloudmusic.api.CloudMusicAPI;
import cn.hanabi.gui.cloudmusic.impl.Track;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.MediaPlayer.Status;
import net.ccbluex.liquidbounce.utils.ClientUtils;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicPlayerUI extends GuiScreen {
    public float x = 10;
    public float y = 10;
    public float x2 = 0;
    public float y2 = 0;

    public boolean drag = false;
    //	public MouseHandler handler = new MouseHandler(0);
    public CopyOnWriteArrayList<TrackSlot> slots = new CopyOnWriteArrayList<>();

    public float width = 150;
    public float height = 250;

    public boolean extended = false;
    public float sidebarAnimation = 0;

    // 滚动
    public float scrollY = 0;
    public float scrollAni = 0;
    public float minY = -100;

    public CustomTextField textField = new CustomTextField("");

    @Override
    public void initGui() {
        SwingUtilities.invokeLater(JFXPanel::new);
        Keyboard.enableRepeatEvents(true);
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        //侧边栏
        sidebarAnimation = RenderUtils.smoothAnimation(sidebarAnimation, extended ? width + 5 : 0.1f, 50, 0.4f);

        if (Math.ceil(sidebarAnimation) > 1) {
            float newX = x + sidebarAnimation;
            float newWidth = x + width + sidebarAnimation;
            RenderUtils.drawRoundedRect(newX, y, newWidth, y + height, 2, 0xff2f3136);

            //歌单导入输入框

            textField.draw(newX + 6, y + 2);
            RenderUtils.drawRoundedRect(newWidth - 26, y + 5, newWidth - 7, y + 17, 2, RenderUtils.isHovering(mouseX, mouseY, newWidth - 26, y + 5, newWidth - 7, y + 17) || MusicManager.INSTANCE.analyzeThread != null ? new Color(80, 80, 80).getRGB() : 0xff34373c);
            Minecraft.getMinecraft().fontRendererObj.drawString("导入", (int) (newWidth - 23f), (int) (y + 6f), Color.GRAY.getRGB());

            if (textField.textString.isEmpty()) {
                Minecraft.getMinecraft().fontRendererObj.drawString("输入歌单ID", (int) (newX + 8), (int) (y + 6f), Color.GRAY.getRGB());
            }

            if (RenderUtils.isHovering(mouseX, mouseY, newX + 5, y + 20, newWidth - 5, y + height - 4)) {
                int wheel = Mouse.getDWheel() / 2;

                scrollY += wheel;
                if (scrollY <= minY)
                    scrollY = minY;
                if (scrollY >= 0f)
                    scrollY = 0f;

                minY = height - 24;
            } else {
                Mouse.getDWheel(); //用于刷新滚轮数据
            }

            this.scrollAni = (float) RenderUtils.getAnimationState(this.scrollAni, scrollY, Math.max(10, (Math.abs(this.scrollAni - (scrollY))) * 50) * 0.3f);
            float startY = y + 21 + this.scrollAni;
            float yShouldbe = 0;
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            RenderUtils.doGlScissor((int) (newX + 6), (int) (y + 21), 137, 224);
            //RenderUtils.drawRect(newX + 6, y + 21, newX + 143, y + 245, Colors.GREEN.c);

            for (TrackSlot s : slots) {
                if (startY > y && startY < y + height - 4) {
                    s.render(newX + 6, startY, mouseX, mouseY);
                }
                startY += 22;
                yShouldbe += 22;
            }

            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            if (RenderUtils.isHovering(mouseX, mouseY, newX + 5, y + 20, newWidth - 5, y + height - 4)) {
                minY -= yShouldbe;
            }

            //遮板
            //RenderUtils.drawOutlinedRect(newX + 4, y + 18, newWidth - 5f, y + height - 2, 2f, 0xff2f3136);

            if (this.slots.size() > 10) {
                float viewable = 224f;

                float progress = MathHelper.clamp_float(-this.scrollAni / -this.minY, 0, 1);

                float ratio = (viewable / yShouldbe) * viewable;
                float barHeight = Math.max(ratio, 20f);

                float position = progress * (viewable - barHeight);

                RenderUtils.drawRect(newWidth - 5, y + 21, newWidth - 2, y + 245f, new Color(100, 100, 100).getRGB());
                RenderUtils.drawRect(newWidth - 5, y + 21 + position, newWidth - 2, y + 21 + position + barHeight, new Color(73, 73, 73).getRGB());
            }

        } else {
            Mouse.getDWheel(); //用于刷新滚轮数据
        }

        //主框架
        RenderUtils.drawRoundedRect(x, y, x + width, y + height, 2, 0xff2f3136);
        RenderUtils.drawRoundedRect(x, y + height - 60, x + width, y + height, 2, 0xff34373c);
        RenderUtils.drawRect(x, y + height - 60, x + width, y + height - 58, 0xff34373c);

        Minecraft.getMinecraft().fontRendererObj.drawString("网易云音乐", (int) (x + (width / 2) - (Minecraft.getMinecraft().fontRendererObj.getStringWidth("网易云音乐") / 2f) - 2), (int) (y + 5), -1);

        float progress = 0;
        if (MusicManager.INSTANCE.getMediaPlayer() != null) {
            progress = (float) MusicManager.INSTANCE.getMediaPlayer().getCurrentTime().toSeconds() / (float) MusicManager.INSTANCE.getMediaPlayer().getStopTime().toSeconds() * 100;
        }

        //进度条
        RenderUtils.INSTANCE.drawRoundedRect(x + 10, y + height - 50, x + width - 10, y + height - 46, 1.4f, Color.GRAY.getRGB());

        if (MusicManager.INSTANCE.loadingThread != null) {
            RenderUtils.INSTANCE.drawRoundedRect(x + 10, y + height - 50, x + 10 + (1.3f * MusicManager.INSTANCE.downloadProgress), y + height - 46, 1.4f, Color.WHITE.getRGB());
            RenderUtils.circle(x + 10 + (1.3f * MusicManager.INSTANCE.downloadProgress), y + height - 48, 3, new Color(255, 255, 255).getRGB());
            RenderUtils.circle(x + 10 + (1.3f * MusicManager.INSTANCE.downloadProgress), y + height - 48, 2, new Color(255, 50, 50, 255).getRGB());
        } else {
            RenderUtils.INSTANCE.drawRoundedRect(x + 10, y + height - 50, x + 10 + (1.3f * progress), y + height - 46, 1.4f, Color.WHITE.getRGB());
            RenderUtils.circle(x + 10 + (1.3f * progress), y + height - 48, 3, new Color(255, 255, 255).getRGB());
            RenderUtils.circle(x + 10 + (1.3f * progress), y + height - 48, 2, new Color(50, 176, 255, 255).getRGB());
        }

        //按钮
        RenderUtils.circle(x + (width / 2), y + height - 24, 12, 0xff40444b); //播放和暂停

        if (extended) {
            Minecraft.getMinecraft().fontRendererObj.drawString(" · ", (int) (x + width - 15), (int) (y + 5.5f), Color.WHITE.getRGB());
        } else {
            Minecraft.getMinecraft().fontRendererObj.drawString("···", (int) (x + width - 15), (int) (y + 5.5f), Color.WHITE.getRGB());
        }

        Minecraft.getMinecraft().fontRendererObj.drawString("QR", (int) (x + 5), (int) (y + 5.5f), Color.WHITE.getRGB());

        String songName = MusicManager.INSTANCE.currentTrack == null ? "当前未在播放" : MusicManager.INSTANCE.currentTrack.name;
        String songArtist = MusicManager.INSTANCE.currentTrack == null ? "N/A" : MusicManager.INSTANCE.currentTrack.artists;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtils.doGlScissor((int) x, (int) y + (int) (height / 2 - 95), (int) width, 25);
        Minecraft.getMinecraft().fontRendererObj.drawString(songName, (int) (x + (width / 2) - ((float) Minecraft.getMinecraft().fontRendererObj.getStringWidth(songName) / 2) - 1.5f), (int) (y + (height / 2 - 95)), -1);
        Minecraft.getMinecraft().fontRendererObj.drawString(songArtist, (int) (x + (width / 2) - ((float) Minecraft.getMinecraft().fontRendererObj.getStringWidth(songArtist) / 2) - 1.5f), (int) (y + (height / 2 - 82)), -1);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        if (MusicManager.INSTANCE.getMediaPlayer() != null) {

            if (MusicManager.INSTANCE.getMediaPlayer().getStatus() == Status.PLAYING) {
                Minecraft.getMinecraft().fontRendererObj.drawString("| |", (int) (x + (width / 2) - ((float) Minecraft.getMinecraft().fontRendererObj.getStringWidth("K") / 2)), (int) (y + height - 25.5f), Color.WHITE.getRGB());
            } else {
                Minecraft.getMinecraft().fontRendererObj.drawString("|>", (int) (x + (width / 2) - ((float) Minecraft.getMinecraft().fontRendererObj.getStringWidth("J") / 2)), (int) (y + height - 25.5f), Color.WHITE.getRGB());
            }

        } else {
            Minecraft.getMinecraft().fontRendererObj.drawString("|>", (int) (x + (width / 2) - ((float) Minecraft.getMinecraft().fontRendererObj.getStringWidth("J") / 2)), (int) (y + height - 25.5f), Color.WHITE.getRGB());
        }

        Minecraft.getMinecraft().fontRendererObj.drawString("←", (int) (x + width / 2 - ((float) Minecraft.getMinecraft().fontRendererObj.getStringWidth("L") / 2) - 30), (int) (y + height - 25.5f), Color.WHITE.getRGB());
        Minecraft.getMinecraft().fontRendererObj.drawString("→", (int) (x + width / 2 - ((float) Minecraft.getMinecraft().fontRendererObj.getStringWidth("M") / 2) + 27.5f), (int) (y + height - 25.5f), Color.WHITE.getRGB());

        if (MusicManager.INSTANCE.repeat) {
            Minecraft.getMinecraft().fontRendererObj.drawString("∞", (int) (x + width - 20), (int) (y + height - 25.5f), Color.WHITE.getRGB());
        } else {
            Minecraft.getMinecraft().fontRendererObj.drawString("-", (int) (x + width - 20), (int) (y + height - 25.5f), Color.WHITE.getRGB());
        }

        if (MusicManager.INSTANCE.lyric) {
            Minecraft.getMinecraft().fontRendererObj.drawString("词", (int) (x + 19), (int) (y + height - 25.5f), 0xffffffff);
        } else {
            Minecraft.getMinecraft().fontRendererObj.drawString("词", (int) (x + 19), (int) (y + height - 25.5f), 0xff6b6e71);
        }

        if (MusicManager.INSTANCE.currentTrack != null) {
            if (MusicManager.INSTANCE.getArt(MusicManager.INSTANCE.currentTrack.id) != null) {
                GL11.glPushMatrix();
                RenderUtils.drawImage2(MusicManager.INSTANCE.getArt(MusicManager.INSTANCE.currentTrack.id), x + (width / 2) - 50, y + (height / 2 - 10) - 50, 100, 100 );
                GL11.glPopMatrix();
            }
        }

        //RenderUtils.drawOutlinedRect(x + (width / 2) - 50, y + (height / 2 - 10) - 50, x + (width / 2) + 50, y + (height / 2 - 10) + 50, .5f, Color.WHITE.getRGB());

        //Debug
        //RenderUtils.drawOutlinedRect(x, y, x + width, y + 20, .5f, Color.RED.getRGB()); //标题框
        //RenderUtils.drawOutlinedRect(x + width - 15, y + 5, x + width - 5, y + 15, .5f, Color.RED.getRGB()); //展开侧栏
        //RenderUtils.drawOutlinedRect(x + 5, y + 5, x + 15, y + 15, .5f, Color.RED.getRGB()); //二维码登录
        //RenderUtils.drawOutlinedRect(x + width - 20, y + height - 29, x + width - 10, y + height - 19, .5f, Color.RED.getRGB()); //单曲循环
        //RenderUtils.drawOutlinedRect(x + 10, y + height - 29, x + 20, y + height - 19, .5f, Color.RED.getRGB()); //歌词按钮
        //RenderUtils.drawOutlinedRect(x + (width / 2) - 12, y + height - 36, x + (width / 2) + 12, y + height - 12, .5f, Color.RED.getRGB()); //播放和暂停
        //RenderUtils.drawOutlinedRect(x + 39, y + height - 32, x + 55, y + height - 16, .5f, Color.RED.getRGB()); //上一曲
        //RenderUtils.drawOutlinedRect(x + 96, y + height - 32, x + 112, y + height - 16, .5f, Color.RED.getRGB()); //下一曲

        this.dragWindow(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        if (RenderUtils.isHovering(mouseX, mouseY, x + width - 15, y + 5, x + width - 5, y + 15) && mouseButton == 0) {
            extended = !extended;
        }

        if (mouseButton == 0) {
            //播放/暂停
            if (RenderUtils.isHovering(mouseX, mouseY, x + (width / 2) - 12, y + height - 36, x + (width / 2) + 12, y + height - 12)) {
                if (!MusicManager.INSTANCE.playlist.isEmpty()) {
                    if (MusicManager.INSTANCE.currentTrack == null) {
                        try {
                            MusicManager.INSTANCE.play(MusicManager.INSTANCE.playlist.get(0));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (MusicManager.INSTANCE.getMediaPlayer() != null) {
                            if (MusicManager.INSTANCE.getMediaPlayer().getStatus() == Status.PLAYING) {
                                MusicManager.INSTANCE.getMediaPlayer().pause();
                            } else {
                                MusicManager.INSTANCE.getMediaPlayer().play();
                            }
                        }
                    }
                }
            }

            //上一曲
            if (RenderUtils.isHovering(mouseX, mouseY, x + 39, y + height - 32, x + 55, y + height - 16)) {
                MusicManager.INSTANCE.prev();
            }

            //下一曲
            if (RenderUtils.isHovering(mouseX, mouseY, x + 96, y + height - 32, x + 112, y + height - 16)) {
                MusicManager.INSTANCE.next();
            }

            //歌词按钮
            if (RenderUtils.isHovering(mouseX, mouseY, x + 10, y + height - 29, x + 20, y + height - 19)) {
                MusicManager.INSTANCE.lyric = !MusicManager.INSTANCE.lyric;
            }

            //单曲循环
            if (RenderUtils.isHovering(mouseX, mouseY, x + width - 20, y + height - 29, x + width - 10, y + height - 19)) {
                MusicManager.INSTANCE.repeat = !MusicManager.INSTANCE.repeat;
            }

            //QRCode
            if (RenderUtils.isHovering(mouseX, mouseY, x + 5, y + 5, x + 15, y + 15)) {
                mc.displayGuiScreen(new QRLoginScreen(this));
            }
        }

        if (extended && Math.ceil(sidebarAnimation) >= width + 5) {
            float newX = x + sidebarAnimation;
            float newWidth = x + width + sidebarAnimation;

            if (mouseButton == 0) {
                if (RenderUtils.isHovering(mouseX, mouseY, newWidth - 26, y + 5, newWidth - 7, y + 17) && !this.textField.textString.isEmpty() && MusicManager.INSTANCE.analyzeThread == null) {
                    MusicManager.INSTANCE.analyzeThread = new Thread(() -> {
                        try {
                            this.slots.clear();

                            MusicManager.INSTANCE.playlist = (ArrayList<Track>) CloudMusicAPI.INSTANCE.getPlaylistDetail(this.textField.textString)[1];

                            for (Track t : MusicManager.INSTANCE.playlist) {
                                this.slots.add(new TrackSlot(t));
                            }

                        } catch (Exception ex) {
                            ClientUtils.INSTANCE.displayChatMessage("解析歌单时发生错误!");
                            ex.printStackTrace();
                        }

                        MusicManager.INSTANCE.analyzeThread = null;
                    });

                    MusicManager.INSTANCE.analyzeThread.start();
                }
            }

            //歌曲列表
            if (RenderUtils.isHovering(mouseX, mouseY, newX + 5, y + 20, newWidth - 5, y + height - 4)) {
                float startY = y + 21 + this.scrollAni;
                for (TrackSlot s : slots) {
                    if (startY > y && startY < y + height - 4) {
                        s.click(mouseX, mouseY, mouseButton);
                    }
                    startY += 22;
                }
            }

            this.textField.mouseClicked(mouseX, mouseY, mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }


    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {

        if (extended) {
            this.textField.keyPressed(keyCode);
            this.textField.charTyped(typedChar);
        }

        super.keyTyped(typedChar, keyCode);
    }

    public void dragWindow(int mouseX, int mouseY) {
        if (RenderUtils.isHovering(mouseX, mouseY, x + width - 15, y + 5, x + width - 5, y + 15)) return;

        if (!Mouse.isButtonDown(0) && this.drag) {
            this.drag = false;
        }

        if (this.drag) {
            this.x = mouseX - this.x2;
            this.y = mouseY - this.y2;
        } else if (RenderUtils.isHovering(mouseX, mouseY, x, y, x + width, y + 20) && Mouse.isButtonDown(0)) {
            this.drag = true;
            this.x2 = mouseX - this.x;
            this.y2 = mouseY - this.y;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}