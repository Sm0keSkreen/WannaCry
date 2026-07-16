package dev.mg.wannacry.font;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.modules.client.FontModule;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.awt.Color;

import static dev.mg.wannacry.util.traits.Util.mc;

public class FontService {

    public final FontLoader          fontLoader       = new FontLoader();
    public final FontRendererProvider rendererProvider = new FontRendererProvider(fontLoader);

    private FontModule fontModule() {
        return WannaCry.moduleManager.getModuleByClass(FontModule.class);
    }

    private boolean isEnabled() {
        FontModule m = fontModule();
        return m != null && m.isEnabled();
    }

    private int darken(int argb, int percent) {
        float factor = 1f - percent / 100f;
        int a = (argb >> 24) & 0xFF;
        int r = (int) (((argb >> 16) & 0xFF) * factor);
        int g = (int) (((argb >>  8) & 0xFF) * factor);
        int b = (int) ((argb         & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int shadowColor(int color) {
        FontModule m = fontModule();
        int darken = m != null ? m.shadowDarken.getValue() : 70;
        return darken(color, darken);
    }

    private static final float SHADOW_TRANSLATE = 0.50f;

    public Font getFont() {
        return rendererProvider.getRenderer(isEnabled());
    }

    public void drawText(GuiGraphics ctx, String text, int x, int y, int color, boolean shadow) {
        Font renderer = getFont();
        if (shadow) {
            var matrices = ctx.pose();
            matrices.pushMatrix();
            matrices.translate(SHADOW_TRANSLATE, SHADOW_TRANSLATE);
            ctx.drawString(renderer, text, x, y, shadowColor(color), false);
            matrices.popMatrix();
        }
        ctx.drawString(renderer, text, x, y, color, renderer.equals(mc.font));
    }

    public void drawText(GuiGraphics ctx, String text, int x, int y, boolean shadow) {
        drawText(ctx, text, x, y, 0xFFFFFFFF, shadow);
    }

    public void drawText(GuiGraphics ctx, Component text, int x, int y, int color, boolean shadow) {
        Font renderer = getFont();
        if (shadow) {
            var matrices = ctx.pose();
            matrices.pushMatrix();
            matrices.translate(SHADOW_TRANSLATE, SHADOW_TRANSLATE);
            ctx.drawString(renderer, text, x, y, shadowColor(color), false);
            matrices.popMatrix();
        }
        ctx.drawString(renderer, text, x, y, color, renderer.equals(mc.font));
    }

    public void drawText(GuiGraphics ctx, Component text, int x, int y, boolean shadow) {
        drawText(ctx, text, x, y, 0xFFFFFFFF, shadow);
    }

    public int getWidth(String text) {
        return getFont().width(text);
    }

    public int getWidth(Component text) {
        return getFont().width(text);
    }

    public int getHeight() {
        if (!isEnabled()) return mc.font.lineHeight;
        FontModule m = fontModule();
        return m != null ? Math.round(m.glyphSize.getValue() * 0.85f) : mc.font.lineHeight;
    }
}
