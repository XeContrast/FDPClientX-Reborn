package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp;

import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.MoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode;
import net.ccbluex.liquidbounce.utils.MovementUtils;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;

public class CubeCraftGround extends SpeedMode {

    public CubeCraftGround() {
        super("CubeCraftGround");
    }
    private final MSTimer timer = new MSTimer();

    @EventTarget
    public void onUpdate() {
        if (mc.getThePlayer().getOnGround()) {
            if (timer.hasTimePassed(155L)) {
                MovementUtils.strafe(2F);
                this.timer.reset();
            }
            else {
                MovementUtils.strafe(0F);
            }
        }
        else {
            mc.getThePlayer().setMotionY(mc.getThePlayer().getMotionY() - 4F);
        }
    }

    @EventTarget
    public void onMotion() {}

    @EventTarget
    public void onMove(MoveEvent event) {}
}
