package dev.mg.wannacry.features.modules.combat;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.event.Stage;
import dev.mg.wannacry.event.impl.ClientEvent;
import dev.mg.wannacry.event.impl.entity.player.TravelEvent;
import dev.mg.wannacry.event.impl.entity.player.UpdateWalkingPlayerEvent;
import dev.mg.wannacry.event.impl.network.PacketEvent;
import dev.mg.wannacry.event.impl.render.Render2DEvent;
import dev.mg.wannacry.event.system.Subscribe;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.models.Angles;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AimCorrectionModule extends Module {

    public enum TargetType { PLAYERS, MOBS, ENTITIES }
    public enum AimMode    { NEAREST, LOWEST_HP }

    private final Setting<TargetType> targets    = mode("Targets",  TargetType.PLAYERS);
    private final Setting<AimMode>    aimMode    = mode("AimAt",    AimMode.NEAREST);
    private final Setting<Boolean>    aimLastHit = bool("AimAtLastHit", false);

    private final Setting<Float> hSpeedMin = num("HorizontalSpeedMin", 120f, 1f, 720f);
    private final Setting<Float> hSpeedMax = num("HorizontalSpeedMax", 120f, 1f, 720f);
    private final Setting<Float> vSpeedMin = num("VerticalSpeedMin",    80f, 1f, 720f);
    private final Setting<Float> vSpeedMax = num("VerticalSpeedMax",    80f, 1f, 720f);

    private final Setting<Boolean> stopH          = bool("StopOnHitboxHorizontal", true);
    private final Setting<Boolean> stopV          = bool("StopOnHitboxVertical",   true);
    private final Setting<Boolean> stopAtPitchLock = bool("StopAtPitchLock",       false);

    private final Setting<Float> rangeMin = num("RangeMin", 6f, 1f, 12f);
    private final Setting<Float> rangeMax = num("RangeMax", 6f, 1f, 12f);

    private float currentHSpeed = 120f;
    private float currentVSpeed =  80f;
    private float currentRange  =   6f;
    private LivingEntity prevTarget = null;

    private LivingEntity lastHitTarget    = null;
    private long         lastFrameNs      = -1L;

    private float prevPlayerYaw   = 0f;
    private float prevPlayerPitch = 0f;

    private boolean returningToCamera = false;

    private volatile boolean windingDown = false;

    private volatile float   camYaw      = 0f;
    private volatile float   camPitch    = 0f;
    private volatile boolean cameraActive = false;

    private Angles syncAngles = null;

    private Angles travelSnapshot = null;

    private Angles walkingSnapshot = null;

    private Angles rotLastSnapshot = null;

    private volatile boolean swapInProgress = false;

    private boolean playerIsMoving = false;

    private float lastSentSwapYaw = Float.NaN;

    public AimCorrectionModule() {
        super("AimCorrection",
              "Silent aim",
              Category.CLOSET);
    }

    public float   getCamYaw()      { return camYaw;      }
    public float   getCamPitch()    { return camPitch;    }
    public boolean isCameraActive() { return cameraActive; }
    public Angles  getSyncAngles()  { return syncAngles;  }

    public boolean isWindingDown()  { return windingDown;  }

    @Override
    public void onEnable() {
        if (nullCheck()) return;
        camYaw            = mc.player.getYRot();
        camPitch          = mc.player.getXRot();
        prevPlayerYaw     = mc.player.getYRot();
        prevPlayerPitch   = mc.player.getXRot();
        cameraActive      = true;
        lastFrameNs       = -1L;
        returningToCamera = false;
        windingDown       = false;
        lastSentSwapYaw   = Float.NaN;
        rollValues();
    }

    @Override
    public void disable() {
        if (!nullCheck() && cameraActive && !windingDown) {
            windingDown = true;

            this.enabled.setValue(false);
            EVENT_BUS.post(new ClientEvent(ClientEvent.Type.TOGGLE_MODULE, this));
            this.onToggle();

        } else {
            windingDown = false;
            super.disable();
        }
    }

    @Override
    public void onDisable() {
        lastHitTarget     = null;
        prevTarget        = null;
        lastFrameNs       = -1L;
        cameraActive      = false;
        returningToCamera = false;
        windingDown       = false;
        syncAngles        = null;
        travelSnapshot    = null;
        walkingSnapshot   = null;
        rotLastSnapshot   = null;
        swapInProgress    = false;
        playerIsMoving    = false;
        lastSentSwapYaw   = Float.NaN;
    }

    @Subscribe
    public void onPacketSend(PacketEvent.Send event) {
        if (!(event.getPacket() instanceof ServerboundInteractPacket pkt)) return;
        if (pkt.action.getType() != ServerboundInteractPacket.ActionType.ATTACK) return;
        Entity entity = mc.level.getEntity(pkt.entityId);
        if (entity instanceof LivingEntity living && living != mc.player) {
            lastHitTarget = living;
        }
    }

    @Subscribe
    public void onTravelPre(TravelEvent.Pre event) {
        if (syncAngles == null || nullCheck()) return;

        net.minecraft.world.phys.Vec3 vel = mc.player.getDeltaMovement();
        playerIsMoving = (vel.x * vel.x + vel.z * vel.z) > 1e-6;
        if (!playerIsMoving) return;

        travelSnapshot = new Angles(mc.player.getYRot(), mc.player.getXRot());
        swapInProgress = true;
        mc.player.setYRot(syncAngles.yRot());
        mc.player.setXRot(syncAngles.xRot());
    }

    @Subscribe
    public void onTravelPost(TravelEvent.Post event) {

        if (travelSnapshot != null && syncAngles == null) {
            mc.player.setYRot(travelSnapshot.yRot());
            mc.player.setXRot(travelSnapshot.xRot());
            travelSnapshot = null;
            swapInProgress = false;
        }

    }

    @Subscribe
    public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
        if (syncAngles == null || nullCheck()) return;

        if (event.getStage() == Stage.PRE && !playerIsMoving) {

            if (!Float.isNaN(lastSentSwapYaw)) {
                float yawDelta = Math.abs(Mth.wrapDegrees(mc.player.getYRot() - lastSentSwapYaw));
                if (yawDelta < 0.002f) {
                    mc.player.setYRot(lastSentSwapYaw + 0.001f);
                }
                lastSentSwapYaw = Float.NaN;
            }

            if (mc.player.positionReminder >= 19) {
                float yawDelta = Math.abs(mc.player.getYRot() - mc.player.yRotLast);
                if (yawDelta < 0.001f) {
                    mc.player.setYRot(mc.player.getYRot() + 0.001f);
                }
            }
        }

        if (!playerIsMoving) return;

        if (event.getStage() == Stage.PRE) {

            if (travelSnapshot != null) {
                walkingSnapshot = travelSnapshot;
                travelSnapshot  = null;
            } else {
                walkingSnapshot = new Angles(mc.player.getYRot(), mc.player.getXRot());
            }
            rotLastSnapshot = new Angles(mc.player.yRotLast,  mc.player.xRotLast);

            float sendYaw = syncAngles.yRot();
            if (!Float.isNaN(lastSentSwapYaw)
                    && Math.abs(Mth.wrapDegrees(sendYaw - lastSentSwapYaw)) < 0.001f) {
                sendYaw = lastSentSwapYaw + 0.001f;
            }
            lastSentSwapYaw = sendYaw;

            mc.player.setYRot(sendYaw);
            mc.player.setXRot(syncAngles.xRot());
        } else if (walkingSnapshot != null) {

            mc.player.setYRot(walkingSnapshot.yRot());
            mc.player.setXRot(walkingSnapshot.xRot());

            if (rotLastSnapshot != null) {
                mc.player.yRotLast = rotLastSnapshot.yRot();
                mc.player.xRotLast = rotLastSnapshot.xRot();
                rotLastSnapshot = null;
            }
            walkingSnapshot = null;

            swapInProgress = false;
        }
    }

    @Subscribe
    public void onRender2D(Render2DEvent event) {
        if (nullCheck()) return;

        long nowNs = System.nanoTime();
        if (lastFrameNs < 0L) {
            lastFrameNs     = nowNs;
            prevPlayerYaw   = mc.player.getYRot();
            prevPlayerPitch = mc.player.getXRot();
            camYaw          = mc.player.getYRot();
            camPitch        = mc.player.getXRot();
            cameraActive    = true;
            return;
        }
        float deltaMs = Math.min((nowNs - lastFrameNs) / 1_000_000f, 100f);
        lastFrameNs = nowNs;

        if (swapInProgress) return;

        float maxYaw   = currentHSpeed * (deltaMs / 1000f);
        float maxPitch = currentVSpeed * (deltaMs / 1000f);

        float mouseDeltaYaw   = Mth.wrapDegrees(mc.player.getYRot() - prevPlayerYaw);
        float mouseDeltaPitch =                  mc.player.getXRot() - prevPlayerPitch;

        camYaw   = Mth.wrapDegrees(camYaw   + mouseDeltaYaw);
        camPitch = Mth.clamp(camPitch + mouseDeltaPitch, -90f, 90f);
        cameraActive = true;

        boolean pitchLocked = stopAtPitchLock.getValue()
                && Math.abs(camPitch) >= 89.9f;

        LivingEntity target = windingDown ? null : findTarget();

        if (target != prevTarget) {
            if (target != null) rollValues();
            prevTarget = target;
        }

        if (target != null && !pitchLocked) {

            returningToCamera = false;

            Vec3 eyePos = mc.player.getEyePosition();
            AABB box    = target.getBoundingBox();
            Vec3 center = box.getCenter();

            Vec3    nearest    = clampToAABB(eyePos, box);
            float[] nearAngles = calcAngle(eyePos, nearest);

            float yawDiff   = Mth.wrapDegrees(nearAngles[0] - mc.player.getYRot());
            float pitchDiff = nearAngles[1] - mc.player.getXRot();

            if (stopH.getValue() || stopV.getValue()) {
                float[] ca  = calcAngle(eyePos, center);
                double  dxz = Math.max(Math.sqrt(
                        (center.x - eyePos.x) * (center.x - eyePos.x) +
                        (center.z - eyePos.z) * (center.z - eyePos.z)), 0.01);
                double  d3  = Math.max(eyePos.distanceTo(center), 0.01);
                float halfW = (float) Math.toDegrees(Math.atan2(target.getBbWidth()  * 0.5, dxz));
                float halfH = (float) Math.toDegrees(Math.atan2(target.getBbHeight() * 0.5, d3));

                if (stopH.getValue() && Math.abs(Mth.wrapDegrees(ca[0] - mc.player.getYRot())) <= halfW) yawDiff   = 0f;
                if (stopV.getValue() && Math.abs(ca[1] - mc.player.getXRot())                  <= halfH) pitchDiff = 0f;
            }

            if (yawDiff != 0f || pitchDiff != 0f) {
                mc.player.setYRot(mc.player.getYRot() + clampAbs(yawDiff,   maxYaw));
                mc.player.setXRot(Mth.clamp(mc.player.getXRot() + clampAbs(pitchDiff, maxPitch), -90f, 90f));
            }

            syncAngles = new Angles(mc.player.getYRot(), mc.player.getXRot());

        } else {

            returningToCamera = true;
            syncAngles = null;

            lastSentSwapYaw = Float.NaN;

            float yawDiff   = Mth.wrapDegrees(camYaw   - mc.player.getYRot());
            float pitchDiff =                  camPitch - mc.player.getXRot();

            boolean arrivedYaw   = Math.abs(yawDiff)   <= maxYaw;
            boolean arrivedPitch = Math.abs(pitchDiff) <= maxPitch;

            if (arrivedYaw && arrivedPitch) {

                mc.player.setYRot(camYaw);
                mc.player.setXRot(camPitch);
                returningToCamera = false;

                if (windingDown) {

                    cameraActive = false;
                    windingDown  = false;
                    EVENT_BUS.unregister(this);
                    this.onDisable();
                } else {

                    cameraActive = false;
                }
            } else {
                mc.player.setYRot(mc.player.getYRot() + clampAbs(yawDiff,   maxYaw));
                mc.player.setXRot(Mth.clamp(mc.player.getXRot() + clampAbs(pitchDiff, maxPitch), -90f, 90f));
            }
        }

        prevPlayerYaw   = mc.player.getYRot();
        prevPlayerPitch = mc.player.getXRot();
    }

    private LivingEntity findTarget() {
        if (aimLastHit.getValue() && lastHitTarget != null) {
            if (lastHitTarget instanceof Player p && WannaCry.friendManager.isFriend(p)) {
                lastHitTarget = null;
            } else if (isValidTarget(lastHitTarget)) {
                return lastHitTarget;
            } else {
                lastHitTarget = null;
            }
        }

        LivingEntity best    = null;
        float        bestVal = Float.MAX_VALUE;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!isValidTarget(living)) continue;

            float val = switch (aimMode.getValue()) {
                case NEAREST   -> living.distanceTo(mc.player);
                case LOWEST_HP -> living.getHealth();
            };

            if (val < bestVal) {
                bestVal = val;
                best    = living;
            }
        }
        return best;
    }

    private boolean isValidTarget(LivingEntity living) {
        if (living == mc.player) return false;
        if (living.isRemoved() || !living.isAlive()) return false;
        if (living.distanceTo(mc.player) > currentRange) return false;
        if (living instanceof Player p && WannaCry.friendManager.isFriend(p)) return false;

        return switch (targets.getValue()) {
            case PLAYERS  -> living instanceof Player;
            case MOBS     -> living instanceof Mob;
            case ENTITIES -> true;
        };
    }

    private static float[] calcAngle(Vec3 from, Vec3 to) {
        double dx    =  (to.x - from.x);
        double dy    = -(to.y - from.y);
        double dz    =  (to.z - from.z);
        double dist  = Math.sqrt(dx * dx + dz * dz);
        float  yaw   = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float  pitch = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(dy, dist)));
        return new float[]{ yaw, pitch };
    }

    private static Vec3 clampToAABB(Vec3 point, AABB box) {
        return new Vec3(
                Mth.clamp(point.x, box.minX, box.maxX),
                Mth.clamp(point.y, box.minY, box.maxY),
                Mth.clamp(point.z, box.minZ, box.maxZ));
    }

    private static float clampAbs(float value, float maxAbs) {
        if (value > 0f) return Math.min( value,  maxAbs);
        if (value < 0f) return Math.max( value, -maxAbs);
        return 0f;
    }

    private static float rollFloat(float a, float b) {
        float lo = Math.min(a, b), hi = Math.max(a, b);
        return (lo == hi) ? lo : lo + (float) (Math.random() * (hi - lo));
    }

    private void rollValues() {
        currentHSpeed = rollFloat(hSpeedMin.getValue(), hSpeedMax.getValue());
        currentVSpeed = rollFloat(vSpeedMin.getValue(), vSpeedMax.getValue());
        currentRange  = rollFloat(rangeMin.getValue(),  rangeMax.getValue());
    }

    @Override
    public String getDisplayInfo() {
        return targets.getValue().name().charAt(0)
             + targets.getValue().name().substring(1).toLowerCase();
    }
}
