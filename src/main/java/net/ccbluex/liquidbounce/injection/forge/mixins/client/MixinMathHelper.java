package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import net.ccbluex.liquidbounce.features.module.modules.client.FPSPlus;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MathHelper.class)
public class MixinMathHelper {
    @Unique
    private static final float RAD, DEG, SIN_TO_COS;
    @Unique
    private static final int SIN_BITS, SIN_MASK, SIN_MASK2, SIN_COUNT, SIN_COUNT2;
    @Unique
    private static final float radFull, radToIndex;
    @Unique
    private static final float degFull, degToIndex;
    @Unique
    private static final float[] sinFull, sinHalf;

    private static final float PI = 3.1415927f;
    private static final float TWO_PI = PI * 2;
    private static final float HALF_PI = PI / 2;
    private static final float ONE_AND_HALF_PI = PI + HALF_PI;
    private static final double DTWO_PI = Math.PI * 2;

    private static final int SIN_BITS2= 14;
    private static final int SIN_MASK3 = ~(-1 << SIN_BITS2);
    private static final int SIN_COUNT3 = SIN_MASK3 + 1;
    private static final float[] SIN_TABLE2 = new float[SIN_COUNT3];

    private static final float radFull2 = TWO_PI;
    private static final float degFull2 = 360;
    private static final float radToIndex2 = SIN_COUNT3 / radFull2;
    private static final float degToIndex2 = SIN_COUNT3 / degFull2;

    private static final float RAD_TO_DEG = 180 / PI;
    private static final double DRAD_TO_DEG = 180 / Math.PI;
    private static final float DEG_TO_RAD = PI / 180;
    private static final double DDEG_TO_RAD = Math.PI / 180;

    @Shadow
    private static final float[] SIN_TABLE = new float[65536];

    static {
        RAD = (float) Math.PI / 180.0f;
        DEG = 180.0f / (float) Math.PI;
        SIN_TO_COS = (float) (Math.PI * 0.5f);

        SIN_BITS = 12;
        SIN_MASK = ~(-1 << SIN_BITS);
        SIN_MASK2 = SIN_MASK >> 1;
        SIN_COUNT = SIN_MASK + 1;
        SIN_COUNT2 = SIN_MASK2 + 1;

        radFull = (float) (Math.PI * 2.0);
        degFull = (float) (360.0);
        radToIndex = SIN_COUNT / radFull;
        degToIndex = SIN_COUNT / degFull;

        sinFull = new float[SIN_COUNT];
        for (int i = 0; i < SIN_COUNT; i++) {
            sinFull[i] = (float) Math.sin((i + Math.min(1, i % (SIN_COUNT / 4)) * 0.5) / SIN_COUNT * radFull);
        }

        sinHalf = new float[SIN_COUNT2];
        for (int i = 0; i < SIN_COUNT2; i++) {
            sinHalf[i] = (float) Math.sin((i + Math.min(1, i % (SIN_COUNT / 4)) * 0.5) / SIN_COUNT * radFull);
        }

        for(int i = 0; i < SIN_COUNT3; i++) {
            SIN_TABLE2[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT3 * radFull2);
        }

        for(int i = 0; i < 360; i += 90) {
            SIN_TABLE2[(int) (i * degToIndex2) & SIN_MASK3] = (float) Math.sin(i * DEG_TO_RAD);
        }

        int lvt_0_2_;
        for(lvt_0_2_ = 0; lvt_0_2_ < 65536; ++lvt_0_2_) {
            SIN_TABLE[lvt_0_2_] = (float)Math.sin((double)lvt_0_2_ * Math.PI * 2.0 / 65536.0);
        }
    }

    /**
     * @author XeContrast
     * @reason FPS+
     */
    @Overwrite
    public static float sin(float rad) {
        float returnCount;
            switch (FPSPlus.INSTANCE.getModes().toLowerCase()) {
                case "riven":
                    returnCount = sinFull[(int) (rad * radToIndex) & SIN_MASK];
                    break;
                case "half-riven":
                    int index1 = (int) (rad * radToIndex) & SIN_MASK;
                    int index2 = index1 & SIN_MASK2;
                    int mul = ((index1 == index2) ? +1 : -1);
                    returnCount = sinHalf[index2] * mul;
                    break;
                case "bettermathhelper":
                case "libgdx":
                    returnCount = SIN_TABLE2[(int) (rad * radToIndex2) & SIN_MASK3];
                    break;
                default:
                    returnCount = SIN_TABLE[(int)(rad * 10430.378F) & '\uffff'];
                    break;
        }
//            return sinFull[(int) (rad * radToIndex) & SIN_MASK];
        return returnCount;
    }
    /**
     * @author abandenz
     * @reason HowardZHY
     */
    @Overwrite
    public static float cos(float rad) {
        float returnNumber;
            switch (FPSPlus.INSTANCE.getModes().toLowerCase()) {
                case "riven":
                case "half-riven":
                    returnNumber =  sin(rad + SIN_TO_COS);
                    break;
                case "bettermathhelper":
                    returnNumber = SIN_TABLE2[(int) ((rad + PI / 2) * radToIndex2) & SIN_MASK3];
                    break;
                default:
                    returnNumber = SIN_TABLE[(int)(rad * 10430.378F + 16384.0F) & '\uffff'];
                    break;
            }
        return returnNumber;
    }
}
