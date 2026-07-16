package dev.mg.wannacry.features.modules.client;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.event.impl.ClientEvent;
import dev.mg.wannacry.event.system.Subscribe;
import dev.mg.wannacry.features.commands.Command;
import dev.mg.wannacry.features.gui.WannaCryGui;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.settings.Setting;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

import static dev.mg.wannacry.features.commands.MessageSignatures.GENERAL;

public class ClickGuiModule extends Module {
    private static ClickGuiModule INSTANCE;

    public final Setting<String> prefix = str("Prefix", ".");

    public final Setting<Color> color    = color("Color",    94,  94, 154, 180);
    public final Setting<Color> topColor = color("TopColor", 94,  94, 154, 240);

    public final Setting<Boolean> autoTheme = bool("AutoTheme", true);

    public final Setting<Boolean> gradientEnabled = bool("Gradient", false);
    public final Setting<Color>   gradientColor2  = color("GradientColor2", 180, 60, 220, 180);

    public enum GradientDir { HORIZONTAL, VERTICAL, DIAGONAL }
    public final Setting<GradientDir> gradientDir = mode("GradientDir", GradientDir.HORIZONTAL);

    public final Setting<Boolean> animatedGradient = bool("AnimatedGradient", false);
    public final Setting<Float>   animSpeed        = num("AnimSpeed", 60f, 5f, 360f);

    public final Setting<Boolean> accentBar = bool("AccentBar", true);

    public final Setting<Float> headerTextSize  = num("HeaderTextSize",  1.0f, 0.5f, 2.0f);
    public final Setting<Float> moduleTextSize  = num("ModuleTextSize",  1.0f, 0.5f, 2.0f);
    public final Setting<Float> settingTextSize = num("SettingTextSize", 0.9f, 0.5f, 2.0f);

    public final Setting<Integer> bgAlphaMax = num("BgDimAlpha", 120, 0, 200);

    public final Setting<Integer> panelBodyAlpha = num("PanelBodyAlpha", 119, 0, 255);

    public final Setting<Integer> hoverBrightness = num("HoverBrightness", 40, 0, 80);

    public final Setting<Boolean> showBorder  = bool("ShowBorder",  false);
    public final Setting<Integer> borderAlpha = num("BorderAlpha",  160, 0, 255);

    public final Setting<Color> disabledModuleColor = color("DisabledBgColor",    25,  25,  40,  60);

    public final Setting<Color> disabledTextColor   = color("DisabledTextColor",  160, 160, 175, 255);

    public final Setting<Color> enabledTextColor    = color("EnabledTextColor",   255, 255, 255, 255);

    public final Setting<Integer> moduleHeight  = num("ModuleHeight",  14, 10, 28);

    public final Setting<Integer> settingHeight = num("SettingHeight", 14,  8, 24);

    public final Setting<Integer> panelWidth    = num("PanelWidth",    88, 60, 220);

    public final Setting<Integer> boxWidth      = num("BoxWidth",      320, 180, 600);

    public final Setting<Integer> boxHeight     = num("BoxHeight",     220, 130, 500);

    public final Setting<Integer> catPanelWidth = num("CategoryWidth",  72,  40, 140);

    public final Setting<Boolean> dropShadow   = bool("DropShadow",   true);
    public final Setting<Integer> shadowAlpha  = num("ShadowAlpha",   45,  0, 180);
    public final Setting<Integer> shadowRadius = num("ShadowRadius",   4,  1,  12);

    public final Setting<Boolean> glowEffect   = bool("GlowEffect",   false);
    public final Setting<Color>   glowColor    = color("GlowColor",   100,  80, 220, 100);
    public final Setting<Integer> glowRadius   = num("GlowRadius",     4,   1,  12);

    public final Setting<Boolean> accentGlow = bool("AccentGlow", false);

    public final Setting<Boolean> moduleSheen    = bool("ModuleSheen",   true);
    public final Setting<Integer> sheenAlpha     = num("SheenAlpha",     60,  0, 180);

    public final Setting<Integer> cornerRadius = num("CornerRadius", 0, 0, 3);

    public final Setting<Color>   settingsBgColor = color("SettingsBgColor",  8,  8, 18, 110);

    public final Setting<Boolean> settingsSep     = bool("SettingsSep",      true);

    private float hueOffset = 0f;
    private long  lastHueNanos = System.nanoTime();

    public ClickGuiModule() {
        super("ClickGui", "Opens the ClickGui", Module.Category.CLIENT);
        setBind(GLFW.GLFW_KEY_RIGHT_SHIFT);
        INSTANCE = this;
    }

    @Subscribe
    public void onSettingChange(ClientEvent event) {
        if (event.getType() == ClientEvent.Type.SETTING_UPDATE && event.getSetting().getFeature().equals(this)) {
            if (event.getSetting().equals(this.prefix)) {
                WannaCry.commandManager.setCommandPrefix(this.prefix.getPlannedValue());
                Command.sendMessage("Prefix set to {global} %s", GENERAL, WannaCry.commandManager.getCommandPrefix());
            }
            if (event.getSetting().equals(this.color)) {
                WannaCry.colorManager.setAutoTheme(this.autoTheme.getValue());
                WannaCry.colorManager.setColor(this.color.getPlannedValue());
            }
            if (event.getSetting().equals(this.autoTheme)) {
                WannaCry.colorManager.setAutoTheme(this.autoTheme.getPlannedValue());
                if (this.autoTheme.getPlannedValue()) {
                    WannaCry.colorManager.setColor(this.color.getValue());
                }
            }
        }
    }

    @Override
    public void onEnable() {
        if (nullCheck()) return;
        mc.setScreen(WannaCryGui.getClickGui());
    }

    @Override
    public void onLoad() {
        WannaCry.colorManager.setAutoTheme(this.autoTheme.getValue());
        WannaCry.colorManager.setColor(this.color.getValue());
        WannaCry.commandManager.setCommandPrefix(this.prefix.getValue());
    }

    @Override
    public void onTick() {
        if (!(ClickGuiModule.mc.screen instanceof WannaCryGui)) {
            this.disable();
        }
        tickHue();
    }

    public Color getGradientColor1() {
        if (!animatedGradient.getValue()) return color.getValue();
        return shiftHue(color.getValue(), hueOffset);
    }

    public Color getGradientColor2() {
        if (!animatedGradient.getValue()) return gradientColor2.getValue();
        return shiftHue(gradientColor2.getValue(), hueOffset);
    }

    public void tickHue() {
        long now = System.nanoTime();
        float dt = Math.min((now - lastHueNanos) / 1_000_000_000f, 0.1f);
        lastHueNanos = now;
        if (animatedGradient.getValue()) {
            hueOffset = (hueOffset + animSpeed.getValue() * dt) % 360f;
        }
    }

    public static void filledRoundRect(net.minecraft.client.gui.GuiGraphics ctx,
                                       int x1, int y1, int x2, int y2,
                                       int color, int radius) {
        if (radius <= 0) {
            ctx.fill(x1, y1, x2, y2, color);
            return;
        }
        int r = Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2));

        ctx.fill(x1 + r, y1,     x2 - r, y2,     color);

        ctx.fill(x1,     y1 + r, x1 + r, y2 - r, color);
        ctx.fill(x2 - r, y1 + r, x2,     y2 - r, color);

        for (int i = 0; i < r; i++) {
            int step = r - i - 1;
            ctx.fill(x1 + step, y1 + i,     x1 + step + 1, y1 + i + 1, color);
            ctx.fill(x2 - step - 1, y1 + i, x2 - step,     y1 + i + 1, color);
            ctx.fill(x1 + step, y2 - i - 1, x1 + step + 1, y2 - i,     color);
            ctx.fill(x2 - step - 1, y2 - i - 1, x2 - step, y2 - i,     color);
        }
    }

    private Color shiftHue(Color base, float hueDeltaDeg) {
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        float newHue = (hsb[0] + hueDeltaDeg / 360f) % 1f;
        if (newHue < 0) newHue += 1f;
        Color shifted = Color.getHSBColor(newHue, hsb[1], hsb[2]);
        return new Color(shifted.getRed(), shifted.getGreen(), shifted.getBlue(), base.getAlpha());
    }

    public static ClickGuiModule getInstance() { return INSTANCE; }
}
