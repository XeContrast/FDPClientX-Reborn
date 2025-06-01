package net.ccbluex.liquidbounce.features.module.modules.movement;

import kotlin.jvm.JvmField;
import net.ccbluex.liquidbounce.api.minecraft.potion.PotionType;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.Rotation;
import net.ccbluex.liquidbounce.utils.RotationUtils;
import net.ccbluex.liquidbounce.value.BoolValue;
import net.ccbluex.liquidbounce.value.ListValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ModuleInfo(name = "Sprint", description = "Automatically sprints all the time.", category = ModuleCategory.MOVEMENT)
public class Sprint extends Module {

    @JvmField
    @NotNull
    public final BoolValue allDirectionsValue = new BoolValue("OMNI", false);
    @JvmField
    @NotNull
    public final BoolValue blindnessValue = new BoolValue("Blindness", true);
    @JvmField
    @NotNull
    public final BoolValue foodValue = new BoolValue("Food", true);
    @JvmField
    @NotNull
    public final BoolValue checkServerSide = new BoolValue("CheckServerSide", false);
    @JvmField
    @NotNull
    public final BoolValue checkServerSideGround = new BoolValue("CheckServerSideOnlyGround", false);

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!MovementUtils.isMoving() || mc.getThePlayer().isSneaking() ||
                (blindnessValue.get() && mc.getThePlayer().isPotionActive(classProvider.getPotionEnum(PotionType.BLINDNESS))) ||
        (foodValue.get() && !(mc.getThePlayer().getFoodStats().getFoodLevel() > 6.0F || mc.getThePlayer().getCapabilities().getAllowFlying()))
                || (checkServerSide.get() && (mc.getThePlayer().getOnGround() || !checkServerSideGround.get())
                        && !allDirectionsValue.get() && RotationUtils.targetRotation != null &&
                RotationUtils.getRotationDifference(new Rotation(mc.getThePlayer().getRotationYaw(), mc.getThePlayer().getRotationPitch())) > 30)) {
            mc.getThePlayer().setSprinting(false);
            return;
        }

        mc2.player.setSprinting(true);

        if(allDirectionsValue.get()) {
            if(mc.getThePlayer().getMovementInput().getMoveForward() >= 0.8F) {
                mc.getThePlayer().setSprinting(true);
            }
        }
    }

    @Nullable
    public String getTag() {
        return (Boolean)this.allDirectionsValue.get() ? "OMNI" : null;
    }
}
