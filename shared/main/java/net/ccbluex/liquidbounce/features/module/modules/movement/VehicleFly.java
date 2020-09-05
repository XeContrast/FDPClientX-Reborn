package net.ccbluex.liquidbounce.features.module.modules.movement;

import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketEntityAction;
import net.ccbluex.liquidbounce.api.minecraft.network.play.client.ICPacketUseEntity;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.FloatValue;

@ModuleInfo(name = "VehicleFly", description = "Allows you to fly while sitting in a vehicle.", category = ModuleCategory.MOVEMENT)
public class VehicleFly extends Module {

    private final FloatValue motion = new FloatValue("Motion", 0.2F, 0.1F, 2F);

    @EventTarget
    public void onUpdate(UpdateEvent event) {
            mc.getNetHandler().addToSendQueue(classProvider.createCPacketPlayerPosition(mc.getThePlayer().getPosX(),
                    mc.getThePlayer().getPosY(), mc.getThePlayer().getPosZ(), mc.getThePlayer().getOnGround()));
            if (mc.getThePlayer().getMovementInput().getJump() && mc.getThePlayer().isRiding()) {
                for (int i = 0; i < 10; i++) {
                    mc.getNetHandler().addToSendQueue(classProvider.createCPacketEntityAction(mc.getThePlayer(), ICPacketEntityAction.WAction.START_RIDING_JUMP));
                    mc.getNetHandler().addToSendQueue(classProvider.createCPacketEntityAction(mc.getThePlayer(),
                            ICPacketEntityAction.WAction.STOP_RIDING_JUMP));
                }
            }
            if(mc.getThePlayer().isRiding()) {
                if(mc.getThePlayer().getMovementInput().getJump() || mc.getGameSettings().getKeyBindJump().getPressed()) {
                    mc.getThePlayer().getRidingEntity().setMotionY(motion.get());
                }
            }
    }
}
