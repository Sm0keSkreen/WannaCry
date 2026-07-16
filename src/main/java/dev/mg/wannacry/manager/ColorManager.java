package dev.mg.wannacry.manager;

import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.util.ColorUtil;

import java.awt.*;

public class ColorManager {
    private Color color = new Color(0, 0, 255, 180);

    private boolean autoTheme = true;

    private boolean applyingTheme = false;

    public void init() {
        ClickGuiModule ui = ClickGuiModule.getInstance();
        setColor(ui.color.getValue());
    }

    public Color getColor() { return this.color; }

    public void setColor(Color color) {
        this.color = color;
        if (autoTheme) applyAutoTheme(color);
    }

    public boolean isAutoTheme()          { return autoTheme; }
    public void    setAutoTheme(boolean v){ autoTheme = v;    }

    private void applyAutoTheme(Color base) {
        if (applyingTheme) return;
        applyingTheme = true;
        try {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        if (gui == null) return;

        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        float h = hsb[0], s = hsb[1], b = hsb[2];

        Color accent      = base;
        Color accentLight = hsb(h, s, clampF(b + 0.22f));
        Color accentDim   = hsb(h, clampF(s * 0.6f), clampF(b * 0.55f));

        Color titleBg     = hsb(h, clampF(s * 0.4f), clampF(b * 0.12f));

        Color panelBg     = hsb(h, clampF(s * 0.25f), 0.09f);

        Color settingsBg  = hsb(h, clampF(s * 0.18f), 0.13f);

        Color disabledBg  = hsb(h, clampF(s * 0.08f), 0.11f);

        Color textEnabled  = new Color(245, 245, 255, 255);

        Color textDisabled = hsb(h, 0.08f, 0.60f);

        gui.topColor.setValue(withAlpha(titleBg, 235));

        gui.color.setValue(withAlpha(accent, base.getAlpha()));

        Color complement = hsb((h + 0.14f) % 1f, clampF(s * 0.9f), clampF(b * 0.85f));
        gui.gradientColor2.setValue(withAlpha(complement, base.getAlpha()));

        gui.panelBodyAlpha.setValue(clampI((int)(0.88f * 255)));
        gui.settingsBgColor.setValue(withAlpha(settingsBg, 180));

        gui.disabledModuleColor.setValue(withAlpha(disabledBg, 70));
        gui.disabledTextColor.setValue(textDisabled);
        gui.enabledTextColor.setValue(textEnabled);

        gui.glowColor.setValue(withAlpha(accentLight, 110));
        gui.accentGlow.setValue(true);

        gui.borderAlpha.setValue(180);
        } finally {
            applyingTheme = false;
        }
    }

    public Color getAccent()       { return color; }

    public Color getAccentLight() {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb(hsb[0], hsb[1], clampF(hsb[2] + 0.22f));
    }

    public Color getDarkBg() {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb(hsb[0], clampF(hsb[1] * 0.35f), clampF(hsb[2] * 0.12f));
    }

    public Color getPanelBg() {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb(hsb[0], clampF(hsb[1] * 0.20f), 0.09f);
    }

    public Color getSettingsBg() {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb(hsb[0], clampF(hsb[1] * 0.15f), 0.13f);
    }

    public Color getMutedAccent() {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return hsb(hsb[0], clampF(hsb[1] * 0.55f), clampF(hsb[2] * 0.55f));
    }

    public int getColorAsInt()             { return this.color.getRGB(); }
    public int getColorAsIntFullAlpha()    { return new Color(color.getRed(), color.getGreen(), color.getBlue(), 255).getRGB(); }
    public int getColorWithAlpha(float offset, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
    }

    public int getGradientColor(float t, int alpha) {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        if (gui == null || !gui.gradientEnabled.getValue()) {
            return alpha < 0 ? this.color.getRGB()
                    : new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
        }
        Color c1 = gui.getGradientColor1();
        Color c2 = gui.getGradientColor2();
        Color lerped = ColorUtil.lerp(c1, c2, t);
        int a = alpha < 0 ? lerped.getAlpha() : alpha;
        return new Color(lerped.getRed(), lerped.getGreen(), lerped.getBlue(), a).getRGB();
    }

    public int getGradientColorAtY(float itemY, float totalHeight, int alpha) {
        float t = (totalHeight <= 0) ? 0f : Math.max(0f, Math.min(1f, itemY / totalHeight));
        return getGradientColor(t, alpha);
    }

    private static Color hsb(float h, float s, float b) {
        return Color.getHSBColor(h, s, b);
    }

    private static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), clampI(alpha));
    }

    private static float clampF(float v) { return Math.max(0f, Math.min(1f, v)); }
    private static int   clampI(int   v) { return Math.max(0,  Math.min(255, v)); }
}
