/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.FDPClient;
import net.ccbluex.liquidbounce.event.Render2DEvent;
import net.ccbluex.liquidbounce.features.module.modules.client.Animations;
import net.ccbluex.liquidbounce.features.module.modules.client.HUD;
import net.ccbluex.liquidbounce.features.module.modules.client.HotbarSettings;
import net.ccbluex.liquidbounce.features.module.modules.visual.AntiBlind;
import net.ccbluex.liquidbounce.features.module.modules.visual.Crosshair;
import net.ccbluex.liquidbounce.injection.access.StaticStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.GuiStreamIndicator;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.util.Objects;

import static net.ccbluex.liquidbounce.utils.render.CombatRender.drawOnBorderedRect;

@Mixin(GuiIngame.class)
public abstract class MixinGuiInGame extends MixinGui {

    @Shadow
    protected abstract void renderHotbarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer player);

    @Shadow
    @Final
    protected static ResourceLocation widgetsTexPath;

    @Shadow
    @Final
    public GuiPlayerTabOverlay overlayPlayerList;

    @Shadow
    @Final
    protected Minecraft mc;
    @Shadow
    @Final
    protected GuiStreamIndicator streamIndicator;
    @Shadow
    protected int remainingHighlightTicks;
    @Shadow
    protected ItemStack highlightingItemStack;

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    private void renderScoreboard(CallbackInfo callbackInfo) {
        if (Objects.requireNonNull(FDPClient.moduleManager.getModule(HUD.class)).getState())
            callbackInfo.cancel();
    }

    /**
     * @author liulihaocai
     * @reason
     */
    @Overwrite
    protected void renderTooltip(ScaledResolution sr, float partialTicks) {
        final HUD hud = FDPClient.moduleManager.getModule(HUD.class);
        final HotbarSettings HotbarSettings = FDPClient.moduleManager.getModule(HotbarSettings.class);
        final EntityPlayer entityplayer = (EntityPlayer) mc.getRenderViewEntity();

        float tabHope = this.mc.gameSettings.keyBindPlayerList.isKeyDown() ? 1f : 0f;
        final Animations animations = Animations.INSTANCE;
        if(animations.getTabHopePercent() != tabHope) {
            animations.setLastTabSync(System.currentTimeMillis());
            animations.setTabHopePercent(tabHope);
        }
        if (Objects.requireNonNull(hud).getInventoryOnHotbar().get()){
            GlStateManager.pushMatrix();
            int scaledWidth = sr.getScaledWidth();
            int scaledHeight = sr.getScaledHeight();
            GlStateManager.translate((float) scaledWidth / 2 - 90, (float) scaledHeight - 25, 0);
            drawOnBorderedRect(0, 1, 180, -58, 1, new Color(0,0,0,255).getRGB(), new Color(0,0,0,130).getRGB());
            RenderHelper.enableGUIStandardItemLighting();

            int initialSlot = 9;
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 9; column++) {
                    int slot = initialSlot + row * 9 + column;
                    int x = 1 + column * 20;
                    int y = -16 - row * 20;
                    fDP1$renderItem(slot, x, y, mc.thePlayer);
                }
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
        if(animations.getTabPercent() > 0 && tabHope == 0) {
            overlayPlayerList.renderPlayerlist(sr.getScaledWidth(), mc.theWorld.getScoreboard(), mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(0));
        }

        if(Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
            String hotbarType = Objects.requireNonNull(HotbarSettings).getHotbarValue().get();
            Minecraft mc = Minecraft.getMinecraft();
            GlStateManager.resetColor();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(widgetsTexPath);
            float f = this.zLevel;
            this.zLevel = -90.0F;
            GlStateManager.resetColor();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            if (hotbarType.equals("Minecraft")) {
                this.drawTexturedModalRect( sr.getScaledWidth() / 2 - 91, sr.getScaledHeight() - 22, 0, 0, 182, 22);
                this.drawTexturedModalRect(((sr.getScaledWidth() / 2) - 91 + net.ccbluex.liquidbounce.features.module.modules.client.HotbarSettings.INSTANCE.getHotbarEasePos(entityplayer.inventory.currentItem * 20)) - 1, sr.getScaledHeight() - 22 - 1, 0, 22, 24, 22);
            }
            this.zLevel = f;
            RenderHelper.enableGUIStandardItemLighting();
            if(hotbarType.equals("Minecraft")){
                for (int j = 0; j < 9; ++j) {
                    this.renderHotbarItem(j, sr.getScaledWidth() / 2 - 90 + j * 20 + 2, sr.getScaledHeight() - 19, partialTicks, entityplayer);
                }
            }
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
        FDPClient.eventManager.callEvent(new Render2DEvent(partialTicks, StaticStorage.scaledResolution));
    }

    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    private void renderPumpkinOverlay(final CallbackInfo callbackInfo) {
        final AntiBlind antiBlind = FDPClient.moduleManager.getModule(AntiBlind.class);

        if(Objects.requireNonNull(antiBlind).getState() && antiBlind.getPumpkinEffectValue().get())
            callbackInfo.cancel();
    }

    @Inject(method = "renderBossHealth", at = @At("HEAD"), cancellable = true)
    private void renderBossHealth(CallbackInfo callbackInfo) {
        final AntiBlind antiBlind = FDPClient.moduleManager.getModule(AntiBlind.class);
        if (Objects.requireNonNull(antiBlind).getState() && antiBlind.getBossHealthValue().get())
            callbackInfo.cancel();
    }

    @Inject(method = "showCrosshair", at = @At("HEAD"), cancellable = true)
    private void injectCrosshair(CallbackInfoReturnable<Boolean> cir) {
        final Crosshair crossHair = FDPClient.moduleManager.getModule(Crosshair.class);
        if (Objects.requireNonNull(crossHair).getState())
            cir.setReturnValue(false);
    }
    @Unique
    private void fDP1$renderItem(int i, int x, int y , EntityPlayer player) {
        ItemStack itemstack = player.inventory.mainInventory[i];
        if (itemstack != null) {
            mc.getRenderItem().renderItemAndEffectIntoGUI(itemstack, x, y);
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, itemstack, x-1, y-1);
        }
    }
 }
