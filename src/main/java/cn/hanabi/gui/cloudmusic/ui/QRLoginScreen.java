package cn.hanabi.gui.cloudmusic.ui;

import cn.hanabi.gui.cloudmusic.api.CloudMusicAPI;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import net.ccbluex.liquidbounce.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

public class QRLoginScreen extends GuiScreen {

    public Thread loginProcessThread;
    public String state = "正在等待用户操作...";

    public ScaledResolution res;

    public GuiScreen lastScreen;

    public QRLoginScreen(GuiScreen prevScreen) {
        this.lastScreen = prevScreen;
    }

    @Override
    public void initGui() {
        res = new ScaledResolution(mc);

        this.buttonList.add(new GuiButton(0, res.getScaledWidth() / 2 - 60, 210, 120, 20, "开始"));
        this.buttonList.add(new GuiButton(1, res.getScaledWidth() / 2 - 60, 240, 120, 20, "退出"));

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        String text = "使用二维码登录至网易云音乐";
        Minecraft.getMinecraft().fontRendererObj.drawString(text, res.getScaledWidth() / 2 - (Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2), 30, 0xFFFFFFFF);
        Minecraft.getMinecraft().fontRendererObj.drawString(state, res.getScaledWidth() / 2 - (Minecraft.getMinecraft().fontRendererObj.getStringWidth(state.replaceAll("\247.", "")) / 2), 50, 0xFFFFFFFF);

        if (this.loginProcessThread != null) {
            RenderUtils.drawImage(new ResourceLocation("cloudMusicCache/qrcode"), res.getScaledWidth() / 2 - 64, 70, 128, 128, false);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {

        switch (button.id) {

            case 0:
                this.loginProcessThread = new Thread(() -> {
                    try {
                        this.buttonList.get(0).enabled = false;
                        File fileDir = new File(mc.mcDataDir, ".cache/cookies.txt");

                        if (fileDir.exists()) fileDir.delete();

                        this.state = "正在创建Key...";
                        String key = CloudMusicAPI.INSTANCE.QRKey();
                        System.out.println("Key=" + key);
                        this.state = "生成二维码...";
                        this.createQRImage(new File(mc.mcDataDir, ".cache/qrcode.png"), "https://music.163.com/login?codekey=" + key, 128, "png");

                        boolean needBreak = false;

                        while (!needBreak) {

                            if (!(mc.currentScreen instanceof QRLoginScreen)) needBreak = true;

                            Object[] result = CloudMusicAPI.INSTANCE.QRState(key);
                            int code = (int) result[0];

                            switch (code) {

                                case 800:
                                    this.buttonList.get(0).enabled = true;
                                    state = "二维码已过期, 请重试";
                                    needBreak = true;
                                    break;

                                case 801:
                                    state = "等待用户扫码";
                                    break;

                                case 802:
                                    state = "等待用户授权";
                                    break;

                                case 803:
                                    StringBuilder sb = new StringBuilder();

                                    int size = 0;

                                    for (Cookie c : ((CookieStore) result[1]).getCookies()) {
                                        sb.append(c.getName()).append("=").append(c.getValue()).append(";");
                                        size++;
                                    }

                                    CloudMusicAPI.INSTANCE.cookies = new String[size][2];

                                    for (int i = 0; i < size; ++i) {
                                        Cookie c = ((CookieStore) result[1]).getCookies().get(i);
                                        CloudMusicAPI.INSTANCE.cookies[i][0] = c.getName();
                                        CloudMusicAPI.INSTANCE.cookies[i][1] = c.getValue();
                                    }

                                    FileUtils.writeStringToFile(fileDir, sb.substring(0, sb.toString().length() - 1));
                                    state = "成功登入, Cookie已保存至 " + fileDir.getAbsolutePath() + ", 请妥善保管!";
                                    needBreak = true;
                                default:
                                    break;
                            }
                        }

                    } catch (Exception ex) {
                        this.buttonList.get(0).enabled = true;
                        state = "发生未知错误, 请重试!";
                        ex.printStackTrace();
                    }
                });

                this.loginProcessThread.start();

                break;

            case 1:
                mc.displayGuiScreen(lastScreen);

            default:
                break;
        }

        super.actionPerformed(button);
    }

    @Override
    public void updateScreen() {
        res = new ScaledResolution(mc);
        super.updateScreen();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void createQRImage(File qrFile, String qrCodeText, int size, String fileType) throws WriterException, IOException {
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
        int matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        ImageIO.write(image, fileType, qrFile);
        this.loadImage(qrFile);
    }

    public void loadImage(File file) {
        new Thread(() -> {
            ResourceLocation rl = new ResourceLocation("cloudMusicCache/qrcode");
            IImageBuffer iib = new IImageBuffer() {
                final ImageBufferDownload ibd = new ImageBufferDownload();

                public BufferedImage parseUserSkin(BufferedImage image) {
                    return image;
                }

                @Override
                public void skinAvailable() {
                }
            };

            ThreadDownloadImageData textureArt = new ThreadDownloadImageData(file, null, null, iib);
            Minecraft.getMinecraft().getTextureManager().loadTexture(rl, textureArt);
        }).start();
    }
}
