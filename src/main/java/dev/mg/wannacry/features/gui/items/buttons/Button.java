package dev.mg.wannacry.features.gui.items.buttons;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.gui.WannaCryGui;
import dev.mg.wannacry.features.gui.Widget;
import dev.mg.wannacry.features.gui.items.Item;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.util.ColorUtil;
import dev.mg.wannacry.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import dev.mg.wannacry.util.WannaCrySounds;

import java.awt.*;

public class Button extends Item {
    private boolean state;

    public Button(String name) {
        super(name);
        this.height = 15;
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        boolean hovering = isHovering(mouseX, mouseY);
        int vh = getVisualHeight();
        int cr = (gui != null) ? gui.cornerRadius.getValue() : 0;

        int bg = getButtonBg(hovering, gui);
        ClickGuiModule.filledRoundRect(context,
                Math.round(this.x), Math.round(this.y),
                Math.round(this.x + this.width), Math.round(this.y + vh - 0.5f),
                bg, cr);

        if (this.getState()) {
            Color accent = (gui != null) ? gui.getGradientColor1() : WannaCry.colorManager.getAccent();
            int accentInt = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 220).getRGB();
            context.fill(Math.round(this.x), Math.round(this.y),
                         Math.round(this.x) + 2, Math.round(this.y + vh - 0.5f),
                         accentInt);
        }

        if (this.getState() && gui != null && gui.gradientEnabled.getValue()) {
            drawGradientOverlay(context, gui, hovering, vh);
        }

        if (this.getState() && gui != null && gui.moduleSheen.getValue()) {
            int sheenA = gui.sheenAlpha.getValue();
            int sheenCol  = new Color(255, 255, 255, clamp(sheenA)).getRGB();
            int sheenCol0 = new Color(255, 255, 255, 0).getRGB();
            RenderUtil.gradient(context,
                    Math.round(this.x), Math.round(this.y),
                    Math.round(this.x + this.width), Math.round(this.y + 3),
                    sheenCol, sheenCol0, sheenCol0, sheenCol);
        }

        int textColor = getTextColor(gui);
        float textScale = (gui != null) ? gui.moduleTextSize.getValue() : 1.0f;
        float labelX = this.getState() ? this.x + 5f : this.x + 3f;
        drawScaledText(context, this.getName(),
                labelX,
                this.y - 2.0f - (float) WannaCryGui.getClickGui().getTextOffset(),
                textColor, textScale);
    }

    private int getButtonBg(boolean hovering, ClickGuiModule gui) {
        if (!this.getState()) {

            Color dc = (gui != null) ? gui.disabledModuleColor.getValue() : new Color(25, 25, 40, 60);
            int dcRgb = dc.getRGB();
            if (hovering) {

                Color accent = WannaCry.colorManager.getAccent();
                int accentTint = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 18).getRGB();
                return blend(dcRgb, accentTint);
            }
            return dcRgb;
        }

        Color accent = (gui != null) ? gui.getGradientColor1() : WannaCry.colorManager.getAccent();
        int baseAlpha = hovering ? 75 : 45;
        if (hovering) baseAlpha = (gui != null) ? Math.min(255, gui.color.getValue().getAlpha() / 2 + 30) : 75;
        if (!hovering && gui != null && !gui.gradientEnabled.getValue()) {

            return new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), baseAlpha).getRGB();
        }
        if (gui != null && gui.gradientEnabled.getValue()) {
            Color c1 = gui.getGradientColor1();
            int alpha = hovering ? gui.topColor.getValue().getAlpha() : gui.color.getValue().getAlpha();
            return withAlpha(c1, alpha);
        }
        return new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), baseAlpha).getRGB();
    }

    private static int blend(int dst, int src) {
        int sa = (src >> 24) & 0xFF;
        if (sa == 0) return dst;
        if (sa == 255) return src;
        int da = (dst >> 24) & 0xFF;
        float sf = sa / 255f, df = da / 255f * (1 - sf);
        float af = sf + df;
        if (af < 0.001f) return 0;
        int r = (int)(( ((src >> 16) & 0xFF) * sf + ((dst >> 16) & 0xFF) * df ) / af);
        int g = (int)(( ((src >>  8) & 0xFF) * sf + ((dst >>  8) & 0xFF) * df ) / af);
        int b = (int)(( ( src        & 0xFF) * sf + ( dst        & 0xFF) * df ) / af);
        return (clamp((int)(af * 255)) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private int getTextColor(ClickGuiModule gui) {
        if (gui == null) return this.getState() ? -1 : -5592406;
        Color tc = this.getState() ? gui.enabledTextColor.getValue() : gui.disabledTextColor.getValue();
        return tc.getRGB();
    }

    private void drawGradientOverlay(GuiGraphics context, ClickGuiModule gui,
                                     boolean hovering, int vh) {
        Color c1 = gui.getGradientColor1();
        Color c2 = gui.getGradientColor2();
        int alpha = hovering ? gui.topColor.getValue().getAlpha() : gui.color.getValue().getAlpha();
        int a1 = withAlpha(c1, alpha);
        int a2 = withAlpha(c2, alpha);

        int ix1 = Math.round(this.x);
        int iy1 = Math.round(this.y);
        int ix2 = Math.round(this.x + this.width);
        int iy2 = Math.round(this.y + vh - 0.5f);

        ClickGuiModule.GradientDir dir = gui.gradientDir.getValue();
        if (dir == ClickGuiModule.GradientDir.HORIZONTAL) {
            RenderUtil.gradient(context, ix1, iy1, ix2, iy2, a1, a1, a2, a2);
        } else if (dir == ClickGuiModule.GradientDir.VERTICAL) {
            RenderUtil.gradient(context, ix1, iy1, ix2, iy2, a1, a2, a2, a1);
        } else {
            int mid = withAlpha(ColorUtil.lerp(c1, c2, 0.5f), alpha);
            RenderUtil.gradient(context, ix1, iy1, ix2, iy2, a1, mid, a2, mid);
        }
    }

    protected static void drawScaledText(GuiGraphics context, String text,
                                         float tx, float ty, int color, float scale) {
        net.minecraft.client.gui.Font font = WannaCry.fontService.getFont();
        if (Math.abs(scale - 1.0f) < 0.01f) {
            context.drawString(font, text, (int) tx, (int) ty, color);
        } else {
            var ps = context.pose();
            ps.pushMatrix();
            ps.translate(tx, ty);
            ps.scale(scale, scale);
            context.drawString(font, text, 0, 0, color);
            ps.popMatrix();
        }
    }

    protected int getVisualHeight() {
        return this.height;
    }

    protected static int getSettingItemHeight() {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        return (gui != null) ? gui.settingHeight.getValue() : 14;
    }

    protected float settingTextY() {
        return this.y + getSettingItemHeight() / 2.0f - 3.5f;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.onMouseClick();
        }
    }

    public void onMouseClick() {
        this.state = !this.state;
        this.toggle();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(WannaCrySounds.UI_CLICK, 1f));
    }

    public void toggle() { }

    public boolean getState() { return this.state; }

    @Override
    public int getHeight() { return 14; }

    public boolean isHovering(int mouseX, int mouseY) {
        for (Widget widget : WannaCryGui.getClickGui().getComponents()) {
            if (!widget.drag) continue;
            return false;
        }
        return (float) mouseX >= this.getX() && (float) mouseX <= this.getX() + (float) this.getWidth()
                && (float) mouseY >= this.getY() && (float) mouseY < this.getY() + (float) getVisualHeight();
    }

    protected static int withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), clamp(alpha)).getRGB();
    }

    protected static int brighten(int argb, int amount) {
        int a = (argb >> 24) & 0xFF;
        int r = Math.min(255, ((argb >> 16) & 0xFF) + amount);
        int g = Math.min(255, ((argb >>  8) & 0xFF) + amount);
        int b = Math.min(255, ( argb        & 0xFF) + amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
