//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.ccbluex.liquidbounce.memoryfix;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;

public class ResourcePackImageScaler {
    public static final int SIZE = 64;

    public ResourcePackImageScaler() {
    }

    public static BufferedImage scalePackImage(BufferedImage image) throws IOException {
        if (image == null) {
            return null;
        } else {
            System.out.println("Scaling resource pack icon from " + image.getWidth() + " to " + 64);
            BufferedImage smallImage = new BufferedImage(64, 64, 2);
            Graphics graphics = smallImage.getGraphics();
            graphics.drawImage(image, 0, 0, 64, 64, (ImageObserver)null);
            graphics.dispose();
            return smallImage;
        }
    }
}
