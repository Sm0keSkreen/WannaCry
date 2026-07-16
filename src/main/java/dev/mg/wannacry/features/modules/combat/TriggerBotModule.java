package dev.mg.wannacry.features.modules.combat;

import dev.mg.wannacry.event.impl.entity.player.TickEvent;
import dev.mg.wannacry.event.impl.input.AttackEvent;
import dev.mg.wannacry.event.system.Subscribe;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.settings.ItemWhitelist;
import dev.mg.wannacry.features.settings.ItemWhitelistHolder;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.MouseSimulation;
import dev.mg.wannacry.util.ShieldUtils;
import dev.mg.wannacry.util.models.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public class TriggerBotModule extends Module implements ItemWhitelistHolder {

    public static Entity overrideTarget = null;

    public static final String WHITELIST_SETTING = "HeldItemWhitelist";

    private static final String DEFAULT_WHITELIST =
            "minecraft:diamond_sword,minecraft:netherite_sword," +
            "minecraft:diamond_axe,minecraft:netherite_axe";

    private final Setting<Boolean> inScreen         = bool("WorkInScreen",          false);
    private final Setting<Boolean> whileUse         = bool("WhileUse",              false);
    private final Setting<Boolean> onLeftClick      = bool("OnLeftClick",           false);
    private final Setting<Boolean> checkShield      = bool("CheckShield",           false);
    private final Setting<Boolean> onlyCritSword    = bool("OnlyCritSword",         false);
    private final Setting<Boolean> onlyCritAxe      = bool("OnlyCritAxe",           false);
    private final Setting<Boolean> prioritizeCrit   = bool("PrioritizeCriticals",   false);
    private final Setting<Boolean> swing            = bool("SwingHand",             true);
    private final Setting<Boolean> whileAscend      = bool("WhileAscending",        false);
    private final Setting<Boolean> clickSimulation  = bool("ClickSimulation",       false);
    private final Setting<Boolean> strayBypass      = bool("StrayBypass",           false);
    private final Setting<Boolean> allEntities      = bool("AllEntities",           false);
    private final Setting<Boolean> useShield        = bool("UseShield",             false);
    private final Setting<Float>   shieldTime       = num( "ShieldTimeMs",          350f, 1f, 1000f);
    private final Setting<Boolean> sticky           = bool("SamePlayer",            false);
    private final Setting<Boolean> predictAttacks   = bool("PredictAttacks",        false);
    private final Setting<Float>   predictVariation = num( "PredictVariation",      0f,   0f, 10f);
    private final Setting<Boolean> enableMaceSwap   = bool("EnableMaceSwap",        false);

    private final Setting<String>  whitelistSetting = str(WHITELIST_SETTING, DEFAULT_WHITELIST);

    private final ItemWhitelist heldItemWhitelist = new ItemWhitelist();

    private final Timer shieldTimer = new Timer();

    private Thread           attackThread;
    private volatile boolean running  = false;
    private volatile boolean isFiring = false;

    private volatile Entity pendingTarget    = null;
    private volatile float  pendingThreshold = 1.0f;

    public TriggerBotModule() {
        super("TriggerBot", "Automatically attacks entities in your crosshair.", Category.CLOSET);

        heldItemWhitelist.deserialize(DEFAULT_WHITELIST);

        shieldTime      .setVisibility(v -> useShield.getValue());
        predictVariation.setVisibility(v -> predictAttacks.getValue());
    }

    @Override
    public ItemWhitelist getWhitelistFor(String settingName) {
        if (WHITELIST_SETTING.equals(settingName)) return heldItemWhitelist;
        return null;
    }

    private void syncWhitelistFromSetting() {
        heldItemWhitelist.deserialize(whitelistSetting.getValue());
    }

    @Override
    public void onLoad() {
        syncWhitelistFromSetting();
    }

    @Override
    public void onEnable() {
        running = true;
        attackThread = new Thread(this::attackLoop, "TriggerBot-Thread");
        attackThread.setDaemon(true);
        attackThread.start();
    }

    @Override
    public void onDisable() {
        running = false;
        isFiring = false;
        pendingTarget = null;
        if (attackThread != null) {
            attackThread.interrupt();
            attackThread = null;
        }
    }

    private void attackLoop() {
        while (running) {
            try {
                poll();
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ignored) {}
        }
    }

    private void poll() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (!inScreen.getValue() && mc.screen != null) return;
        if (onLeftClick.getValue() && !isMouseDown(mc, GLFW.GLFW_MOUSE_BUTTON_LEFT)) return;
        if (!whileUse.getValue() && isMouseDown(mc, GLFW.GLFW_MOUSE_BUTTON_RIGHT)) return;
        if (!whileAscend.getValue() && isAscending(mc)) return;
        if (isFiring) return;

        float baseThreshold = predictAttacks.getValue() ? 0.9f : 1.0f;
        float variation     = predictAttacks.getValue() ? (predictVariation.getValue() / 100.0f) : 0f;
        float threshold     = variation > 0f
                ? baseThreshold + (float) (Math.random() * variation)
                : baseThreshold;
        if (mc.player.getAttackStrengthScale(0f) < threshold) return;

        if (!(mc.hitResult instanceof EntityHitResult hit)) return;
        Entity entity = hit.getEntity();
        if (entity == null) return;

        if (sticky.getValue()) {
            Entity lastHurt = mc.player.getLastHurtMob();
            if (lastHurt != null && entity != lastHurt) return;
        }

        if (!isValidTarget(entity)) return;

        if (entity instanceof Player player
                && checkShield.getValue()
                && ShieldUtils.isShielding(player)) return;

        ItemStack heldItem = mc.player.getMainHandItem();
        if (!heldItemWhitelist.isAllowed(heldItem)) return;

        boolean heldSword = heldItem.is(net.minecraft.tags.ItemTags.SWORDS);
        boolean heldAxe   = heldItem.getItem() instanceof net.minecraft.world.item.AxeItem;

        if (heldSword) {
            if (onlyCritSword.getValue()  && !isFalling(mc))   return;
            if (prioritizeCrit.getValue() && !isCritReady(mc) && !mc.player.onGround()) return;
        }
        if (heldAxe) {
            if (onlyCritAxe.getValue()    && !isFalling(mc))   return;
            if (prioritizeCrit.getValue() && !isCritReady(mc) && !mc.player.onGround()) return;
        }

        tryHit(mc, entity, threshold);
    }

    private void tryHit(Minecraft mc, Entity entity, float threshold) {

        isFiring         = true;
        pendingThreshold = threshold;
        pendingTarget    = entity;
    }

    @Subscribe
    public void onPreTick(TickEvent.Pre event) {
        Entity entity = pendingTarget;
        if (entity == null) return;

        pendingTarget = null;
        float threshold = pendingThreshold;

        if (mc.player == null || mc.gameMode == null) { isFiring = false; return; }

        if (mc.player.getAttackStrengthScale(0f) < threshold) { isFiring = false; return; }

        if (!(mc.hitResult instanceof EntityHitResult currentHit)
                || currentHit.getEntity() != entity) { isFiring = false; return; }

        if (!isValidTarget(entity)) { isFiring = false; return; }

        if (entity instanceof Player player
                && checkShield.getValue()
                && ShieldUtils.isShielding(player)) { isFiring = false; return; }

        if (!heldItemWhitelist.isAllowed(mc.player.getMainHandItem())) { isFiring = false; return; }

        boolean doMaceSwap   = enableMaceSwap.getValue();
        boolean doSwing      = swing.getValue();
        boolean doClick      = clickSimulation.getValue();
        boolean doShield     = useShield.getValue();
        int     shieldHoldMs = shieldTime.getValue().intValue();

        if (doShield
                && mc.player.getOffhandItem().getItem() instanceof ShieldItem
                && mc.player.isBlocking()) {
            MouseSimulation.mouseRelease(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        }

        if (doMaceSwap) {

            TriggerBotModule.overrideTarget = entity;
            try {
                EVENT_BUS.post(new AttackEvent.Pre());
                mc.gameMode.attack(mc.player, entity);
                if (doSwing) mc.player.swing(InteractionHand.MAIN_HAND);
                EVENT_BUS.post(new AttackEvent.Post());
            } finally {
                TriggerBotModule.overrideTarget = null;
            }
        } else {
            mc.gameMode.attack(mc.player, entity);
            if (doSwing) mc.player.swing(InteractionHand.MAIN_HAND);
        }

        isFiring = false;

        if (doClick) MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);

        shieldTimer.reset();

        if (doShield
                && mc.player.getOffhandItem().getItem() == Items.SHIELD
                && !mc.player.isBlocking()) {
            if (!shieldTimer.passedMs(shieldHoldMs)) {
                MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT, shieldHoldMs);
                shieldTimer.reset();
            }
        }
    }

    @Subscribe
    public void onAttackPre(AttackEvent.Pre event) {
        if (isFiring) return;
        if (!isMouseDown(mc, GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            event.cancel();
        }
    }

    private boolean isValidTarget(Entity entity) {
        if (allEntities.getValue()) return entity != null;
        if (strayBypass.getValue() && entity instanceof Zombie) return true;
        return entity instanceof Player;
    }

    private static boolean isMouseDown(Minecraft mc, int button) {
        return GLFW.glfwGetMouseButton(mc.getWindow().handle(), button) == GLFW.GLFW_PRESS;
    }

    private static boolean isAscending(Minecraft mc) {
        return mc.player != null && !mc.player.onGround() && mc.player.getDeltaMovement().y > 0;
    }

    private static boolean isFalling(Minecraft mc) {
        return mc.player != null && !mc.player.onGround() && mc.player.getDeltaMovement().y < 0;
    }

    private static boolean isCritReady(Minecraft mc) {
        return mc.player != null
                && !mc.player.onGround()
                && mc.player.getDeltaMovement().y < 0
                && mc.player.fallDistance > 0;
    }
}
