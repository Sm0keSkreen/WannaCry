package dev.mg.wannacry.features.modules.client;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.event.impl.input.MouseInputEvent;
import dev.mg.wannacry.event.impl.render.Render2DEvent;
import dev.mg.wannacry.event.system.Subscribe;
import dev.mg.wannacry.features.gui.HudEditorScreen;
import dev.mg.wannacry.features.gui.Widget;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import org.joml.Vector2f;

import java.awt.*;

public abstract class HudModule extends Module {
    public final Setting<Vector2f> pos = vec2f("Position", 0.5f, 0.5f);

    public final Setting<Boolean>       gradientEnabled  = bool("Gradient", false);
    public final Setting<Color>         gradientColor1   = color("GradientColor1", 94, 94, 154, 255);
    public final Setting<Color>         gradientColor2   = color("GradientColor2", 180, 94, 154, 255);
    public final Setting<Float>         gradientAngle    = num("GradientAngle", 0.0f, 0.0f, 360.0f);
    public final Setting<Boolean>       gradientAnimated = bool("GradientAnimated", false);
    public final Setting<AnimationType> gradientAnimType = mode("GradientAnimType", AnimationType.PULSE);
    public final Setting<Float>         gradientSpeed    = num("GradientSpeed", 1.0f, 0.1f, 10.0f);

    private float dragX, dragY, width, height;
    private boolean dragging, button;

    public HudModule(String name, String description, float width, float height) {
        super(name, description, Category.HUD);
        this.width = width;
        this.height = height;

        gradientColor1.setVisibility(v -> gradientEnabled.getValue());
        gradientColor2.setVisibility(v -> gradientEnabled.getValue());
        gradientAngle.setVisibility(v -> gradientEnabled.getValue());
        gradientAnimated.setVisibility(v -> gradientEnabled.getValue());
        gradientAnimType.setVisibility(v -> gradientEnabled.getValue() && gradientAnimated.getValue());
        gradientSpeed.setVisibility(v -> gradientEnabled.getValue() && gradientAnimated.getValue());

    }

    public enum AnimationType {

        PULSE,

        BASIC
    }

    protected Color lerpColor(Color c1, Color c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) (c1.getRed()   + (c2.getRed()   - c1.getRed())   * t);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
        int b = (int) (c1.getBlue()  + (c2.getBlue()  - c1.getBlue())  * t);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * t);
        return new Color(r, g, b, a);
    }

    private float triangleWave(float x) {
        float t = ((x % 2.0f) + 2.0f) % 2.0f;
        return t < 1.0f ? t : 2.0f - t;
    }

    protected float computeGradientT(float normX, float normY) {
        float rad  = (float) (gradientAngle.getValue() * Math.PI / 180.0);
        float cosA = (float) Math.cos(rad);
        float sinA = (float) Math.sin(rad);

        float raw = cosA * normX + sinA * normY;

        float minVal = Math.min(cosA, 0f) + Math.min(sinA, 0f);
        float maxVal = Math.max(cosA, 0f) + Math.max(sinA, 0f);
        float range  = maxVal - minVal;

        if (range < 0.001f) return 0.5f;
        return Math.max(0f, Math.min(1f, (raw - minVal) / range));
    }

    private float getPulseT() {
        float speed = gradientSpeed.getValue();
        return (float) (Math.sin(System.currentTimeMillis() / 1000.0 * speed * Math.PI * 2) * 0.5 + 0.5);
    }

    private float getAnimOffset() {
        float speed = gradientSpeed.getValue();
        return (float) ((System.currentTimeMillis() / 1000.0 * speed) % 2.0);
    }

    protected int getGradientColorAt(float t) {
        if (gradientAnimated.getValue()) {
            if (gradientAnimType.getValue() == AnimationType.PULSE) {
                return lerpColor(gradientColor1.getValue(), gradientColor2.getValue(), getPulseT()).getRGB();
            } else {

                float smooth = triangleWave(t + getAnimOffset());
                return lerpColor(gradientColor1.getValue(), gradientColor2.getValue(), smooth).getRGB();
            }
        }
        return lerpColor(gradientColor1.getValue(), gradientColor2.getValue(), t).getRGB();
    }

    protected void drawGradientText(GuiGraphics ctx, String text, int startX, int y) {
        if (!gradientEnabled.getValue()) return;

        net.minecraft.client.gui.Font font = WannaCry.fontService.getFont();
        int totalWidth = font.width(text);
        if (totalWidth <= 0) {
            ctx.drawString(font, text, startX, y, getGradientColorAt(computeGradientT(0f, 0f)));
            return;
        }

        int curX = startX;
        for (char c : text.toCharArray()) {
            String ch    = String.valueOf(c);
            float  normX = (float) (curX - startX) / totalWidth;
            ctx.drawString(font, ch, curX, y, getGradientColorAt(computeGradientT(normX, 0f)));
            curX += font.width(ch);
        }
    }

    protected void drawGradientText(GuiGraphics ctx, String text, int startX, int y,
                                    float normY, float maxWidth) {
        net.minecraft.client.gui.Font font = WannaCry.fontService.getFont();
        int curX = startX;
        for (char c : text.toCharArray()) {
            String ch    = String.valueOf(c);
            float  normX = maxWidth > 0f ? (float) (curX - startX) / maxWidth : 0f;
            ctx.drawString(font, ch, curX, y, getGradientColorAt(computeGradientT(normX, normY)));
            curX += font.width(ch);
        }
    }

    protected void drawGradientText(GuiGraphics ctx, String text, int startX, int y,
                                    float normY, float maxWidth, float totalPixelHeight, float lineHeight) {
        drawGradientText(ctx, text, startX, y, normY, maxWidth, totalPixelHeight, lineHeight, 1f);
    }

    protected void drawGradientText(GuiGraphics ctx, String text, int startX, int y,
                                    float normY, float maxWidth, float totalPixelHeight, float lineHeight,
                                    float alpha) {
        net.minecraft.client.gui.Font font = WannaCry.fontService.getFont();
        int   curX     = startX;

        float glyphCenterNormY = totalPixelHeight > 0f
                ? (normY * totalPixelHeight + lineHeight * 0.5f) / totalPixelHeight
                : normY;
        for (char c : text.toCharArray()) {
            String ch    = String.valueOf(c);
            float  normX = maxWidth > 0f ? (float) (curX - startX) / maxWidth : 0f;
            int    argb  = getGradientColorAt(computeGradientT(normX, glyphCenterNormY));
            ctx.drawString(font, ch, curX, y, applyAlpha(argb, alpha));
            curX += font.width(ch);
        }
    }

    protected void drawGradientText(GuiGraphics ctx, String text, int startX, int y,
                                    float normY, float maxWidth, float alpha) {
        net.minecraft.client.gui.Font font = WannaCry.fontService.getFont();
        int curX = startX;
        for (char c : text.toCharArray()) {
            String ch    = String.valueOf(c);
            float  normX = maxWidth > 0f ? (float) (curX - startX) / maxWidth : 0f;
            int    argb  = getGradientColorAt(computeGradientT(normX, normY));
            ctx.drawString(font, ch, curX, y, applyAlpha(argb, alpha));
            curX += font.width(ch);
        }
    }

    private int applyAlpha(int argb, float alpha) {
        int a = (int) (((argb >> 24) & 0xFF) * alpha);
        return (argb & 0x00FFFFFF) | (a << 24);
    }

    protected void drawTextGlow(GuiGraphics ctx, String text, int x, int y) { }

    protected void drawTextGlow(GuiGraphics ctx, java.util.List<String> lines,
                                 int x, int startY, int lineHeight) { }

    public float getX() {
        return mc.getWindow().getGuiScaledWidth() * pos.getValue().x();
    }

    public float getY() {
        float heightWithChat = mc.getWindow().getGuiScaledHeight() - 14;
        float baseY = mc.getWindow().getGuiScaledHeight() * pos.getValue().y();
        float combined = baseY + getHeight();
        if (mc.screen instanceof ChatScreen) {
            baseY = Math.min(combined, heightWithChat) - getHeight();
        }
        return baseY;
    }

    @Subscribe
    public void onRender2DHud(Render2DEvent e) {
        render(e);
    }

    @Subscribe
    public void onMouse(MouseInputEvent e) {
        if (!(mc.screen instanceof HudEditorScreen) || nullCheck()) return;

        if (e.getAction() == 0) {
            button = false;
            dragging = false;
            HudEditorScreen.getInstance().currentDragging = null;
        }

        if (e.getAction() == 1 && isHovering()) {
            button = true;
        }
    }

    private float snap(float value, float gridSize) {
        if (gridSize <= 0) return value;
        return Math.round(value / gridSize) * gridSize;
    }

    protected void render(Render2DEvent e) {
        if (!(mc.screen instanceof HudEditorScreen) || nullCheck()) return;

        float x = getX();
        float y = getY();

        if (button) {
            if (!dragging && isHovering() && HudEditorScreen.getInstance().currentDragging == null) {
                dragX = getMouseX() - x;
                dragY = getMouseY() - y;
                dragging = true;
                HudEditorScreen.getInstance().currentDragging = this;
            }

            if (dragging) {
                float rawX = Math.min(Math.max(getMouseX() - dragX, 0),
                        mc.getWindow().getGuiScaledWidth() - width);
                float rawY = Math.min(Math.max(getMouseY() - dragY, 0),
                        mc.getWindow().getGuiScaledHeight() - height);

                float snappedX = snap(rawX, Widget.GRID_SIZE);
                float snappedY = snap(rawY, Widget.GRID_SIZE);

                pos.getValue().x = snappedX / mc.getWindow().getGuiScaledWidth();
                pos.getValue().y = snappedY / mc.getWindow().getGuiScaledHeight();
            }
        } else {
            dragging = false;
        }

        boolean shouldDrawDescription = isHovering() && !HudEditorScreen.getInstance().anyHover;
        if (HudEditorScreen.getInstance().currentDragging != null) {
            shouldDrawDescription = HudEditorScreen.getInstance().currentDragging == this;
        }

        if (shouldDrawDescription) {
            net.minecraft.client.gui.Font font = WannaCry.fontService.getFont();
            int textWidth  = font.width(getName());
            int textHeight = font.lineHeight;
            float textX = x + width + 5;
            if (textX + textWidth > mc.getWindow().getGuiScaledWidth()) {
                textX = x - 5 - textWidth;
            }
            e.getContext().drawString(font, getName(),
                    (int) textX, (int) (y + height / 2f - textHeight / 2f), -1);
            HudEditorScreen.getInstance().anyHover = true;
        }

        RenderUtil.rect(e.getContext(),
                x - 1, y - 1, x + width + 1, y + height + 1,
                WannaCry.colorManager.getColor().getRGB(), 1.0f);
    }

    public int getMouseX() {
        return (int) (mc.mouseHandler.xpos() / mc.getWindow().getGuiScale());
    }

    public int getMouseY() {
        return (int) (mc.mouseHandler.ypos() / mc.getWindow().getGuiScale());
    }

    public void setBounds(float x, float y, float width, float height) {
        this.width = width;
        this.height = height;
        pos.getValue().x = x / mc.getWindow().getGuiScaledWidth();
        pos.getValue().y = y / mc.getWindow().getGuiScaledHeight();
    }

    public boolean isHovering() {
        float x = getX();
        float y = getY();
        int mouseX = getMouseX();
        int mouseY = getMouseY();
        return mouseX >= x - 1 && mouseX <= x + width + 1 &&
                mouseY >= y - 1 && mouseY <= y + height + 1;
    }

    public float getWidth()  { return width; }
    public float getHeight() { return height; }
    public void setWidth(float width)   { this.width  = width; }
    public void setHeight(float height) { this.height = height; }
}
