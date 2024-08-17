//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.ccbluex.liquidbounce.memoryfix;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;

public class CapeImageBuffer implements IImageBuffer {
    public ImageBufferDownload imageBufferDownload;
    public final WeakReference<AbstractClientPlayer> playerRef;
    public final ResourceLocation resourceLocation;

    public CapeImageBuffer(AbstractClientPlayer player, ResourceLocation resourceLocation) {
        this.playerRef = new WeakReference(player);
        this.resourceLocation = resourceLocation;
        this.imageBufferDownload = new ImageBufferDownload();
    }

    public BufferedImage func_78432_a(BufferedImage image) {
        return parseCape(image);
    }

    private static BufferedImage parseCape(BufferedImage image) {
        return null;
    }

    public void func_152634_a() {
        AbstractClientPlayer player = (AbstractClientPlayer)this.playerRef.get();
        if (player != null) {
            setLocationOfCape(player, this.resourceLocation);
        }

    }

    private static void setLocationOfCape(AbstractClientPlayer player, ResourceLocation resourceLocation) {
    }

    @Override
    public BufferedImage parseUserSkin(BufferedImage bufferedImage) {
        return null;
    }

    @Override
    public void skinAvailable() {

    }
}