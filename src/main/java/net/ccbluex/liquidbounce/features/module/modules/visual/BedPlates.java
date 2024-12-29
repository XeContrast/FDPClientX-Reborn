package net.ccbluex.liquidbounce.features.module.modules.visual;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.features.value.FloatValue;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.ccbluex.liquidbounce.utils.GLUtils;
import net.ccbluex.liquidbounce.utils.timer.TimerUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

@ModuleInfo(name = "BedPlates",category = ModuleCategory.VISUAL)
public class BedPlates extends Module {

    public final FloatValue distance = new FloatValue("Distance",50,10,75);
    public final FloatValue updateRate = new FloatValue("Update Rate",1000, 250, 5000);
    public final FloatValue layers = new FloatValue("Layers",5,1,10);
    private final List<BlockPos> beds = new ArrayList<>();
    private final List<List<Block>> bedBlocks = new ArrayList<>();
    private final List<BlockPos> retardedList = new ArrayList<>();
    private final TimerUtils timer = new TimerUtils();

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        int index = 0;
        if (timer.hasTimeElapsed(updateRate.get())) {
            beds.clear();
            bedBlocks.clear();
            for (int i = 0; i < 8; i++) {
                beds.add(null);
                bedBlocks.add(new ArrayList<>());
            }
            Float radius = distance.get();
            int ind = 0;
            for (Float y = radius; y >= -radius; --y) {
                for (float x = -radius; x <= radius; ++x) {
                    for (float z = -radius; z <= radius; ++z) {
                        if (mc.thePlayer != null && mc.theWorld != null) {
                            BlockPos pos = new BlockPos(mc.thePlayer.posX + (double) x, mc.thePlayer.posY + (double) y, mc.thePlayer.posZ + (double) z);
                            Block bl = mc.theWorld.getBlockState(pos).getBlock();
                            if (retardedList.contains(pos))
                                continue;
                            if (ind < 8) {
                                if (bl.equals(Blocks.bed)) {
                                    boolean found = find(pos.getX(), pos.getY(), pos.getZ(), ind);
                                    if (found) {
                                        retardedList.add(pos.north());
                                        retardedList.add(pos.south());
                                        retardedList.add(pos.east());
                                        retardedList.add(pos.west());
                                        ind++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            timer.reset();
        }

        if (!this.beds.isEmpty()) {

            for (BlockPos blockPos : this.beds) {
                if (blockPos == null)
                    continue;
                if (beds.get(index) != null) {

                    mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);

                    final double[][] vectors = getDoubles(blockPos);

                    float[] projection;
                    final float[] position = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, -1.0F, -1.0F};

                    for (final double[] vec : vectors) {
                        projection = GLUtils.project2D((float) vec[0], (float) vec[1], (float) vec[2], event.getScaledResolution().getScaleFactor());
                        if (projection != null && projection[2] >= 0.0F && projection[2] < 1.0F) {
                            final float pX = projection[0];
                            final float pY = projection[1];
                            position[0] = Math.min(position[0], pX);
                            position[1] = Math.min(position[1], pY);
                            position[2] = Math.max(position[2], pX);
                            position[3] = Math.max(position[3], pY);
                        }
                    }

                    mc.entityRenderer.setupOverlayRendering();
                    float width = bedBlocks.get(index).size() * 20 + 4;
                    final float posX = position[0] - width / 2f;
                    final float posY = position[1];
                    Fonts.font15.drawCenteredString(((int) mc.thePlayer.getDistanceSq(blockPos)) + "m", posX + width / 2, posY + 4, -1);
                    float curX = posX + 4;
                    for (Block block : bedBlocks.get(index)) {
                        ItemStack stack = new ItemStack(block);
                        GlStateManager.pushMatrix();
                        RenderHelper.enableGUIStandardItemLighting();
                        GlStateManager.disableAlpha();
                        GlStateManager.clear(256);
                        mc.getRenderItem().zLevel = -150.0F;
                        GlStateManager.disableLighting();
                        GlStateManager.disableDepth();
                        GlStateManager.disableBlend();
                        GlStateManager.enableLighting();
                        GlStateManager.enableDepth();
                        GlStateManager.disableLighting();
                        GlStateManager.disableDepth();
                        GlStateManager.disableTexture2D();
                        GlStateManager.disableAlpha();
                        GlStateManager.disableBlend();
                        GlStateManager.enableBlend();
                        GlStateManager.enableAlpha();
                        GlStateManager.enableTexture2D();
                        GlStateManager.enableLighting();
                        GlStateManager.enableDepth();
                        mc.getRenderItem().renderItemIntoGUI(stack, (int) curX, (int) (posY + Fonts.font15.getHeight() + 8));
                        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, stack, (int) curX, (int) (posY + Fonts.font15.getHeight() + 8), null);
                        mc.getRenderItem().zLevel = 0.0F;
                        GlStateManager.enableAlpha();
                        RenderHelper.disableStandardItemLighting();
                        GlStateManager.popMatrix();
                        curX += 20;
                    }
                    mc.entityRenderer.setupOverlayRendering();
                    index++;
                }
            }
        }
    }

    private static double[] [] getDoubles(BlockPos blockPos) {
        final double x = blockPos.getX() - mc.getRenderManager().viewerPosX;
        final double y = blockPos.getY() - mc.getRenderManager().viewerPosY;
        final double z = blockPos.getZ() - mc.getRenderManager().viewerPosZ;

        final AxisAlignedBB bb = new AxisAlignedBB(x, y - 1, z, x, y + 1, z);

        return new double[][]{{bb.minX, bb.minY, bb.minZ},
                {bb.minX, bb.maxY, bb.minZ},
                {bb.minX, bb.maxY, bb.maxZ},
                {bb.minX, bb.minY, bb.maxZ},
                {bb.maxX, bb.minY, bb.minZ},
                {bb.maxX, bb.maxY, bb.minZ},
                {bb.maxX, bb.maxY, bb.maxZ},
                {bb.maxX, bb.minY, bb.maxZ}};
    }

    private boolean find(double x, double y, double z, int index) {
        BlockPos bedPos = new BlockPos(x, y, z);
        Block bed = mc.theWorld.getBlockState(bedPos).getBlock();
        bedBlocks.get(index).clear();
        beds.set(index, null);

        if (beds.contains(bedPos)) {
            return false;
        }

        Block[] targetBlocks = {
                Blocks.wool, Blocks.stained_hardened_clay, Blocks.stained_glass, Blocks.planks, Blocks.log, Blocks.log2, Blocks.end_stone, Blocks.obsidian,
                Blocks.bedrock
        }; // BW normal def blocks list ^ ^

        for (int yOffset = 0; yOffset <= layers.get(); ++yOffset) {
            for (int xOffset = (int) -layers.get(); xOffset <= layers.get(); ++xOffset) {
                for (int zOffset = (int) -layers.get(); zOffset <= layers.get(); ++zOffset) {
                    Block blockAtOffset = mc.theWorld.getBlockState(new BlockPos(bedPos.getX() + xOffset, bedPos.getY() + yOffset, bedPos.getZ() + zOffset)).getBlock();

                    for (Block targetBlock : targetBlocks) {
                        if (blockAtOffset.equals(targetBlock) && !bedBlocks.get(index).contains(targetBlock)) {
                            bedBlocks.get(index).add(targetBlock);
                        }
                    }
                }
            }
        }

        if (bed.equals(Blocks.bed)) {
            beds.set(index, bedPos);
            return true;
        }

        return false;
    }
}