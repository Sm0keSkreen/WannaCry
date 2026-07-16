package dev.mg.wannacry.features.gui.items.buttons;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.gui.WannaCryGui;
import dev.mg.wannacry.features.gui.Widget;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.ColorUtil;
import dev.mg.wannacry.util.render.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class Slider extends Button {
    private final Number min;
    private final Number max;
    public Setting<Number> setting;

    public Slider(Setting<Number> setting) {
        super(setting.getName());
        this.setting = setting;
        this.min = setting.getMin();
        this.max = setting.getMax();
        this.width = 15;
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        this.dragSetting(mouseX, mouseY);

        ClickGuiModule gui = ClickGuiModule.getInstance();
        boolean hovering = isHovering(mouseX, mouseY);
        float barW = (float)this.width + 7.4f;

        int sh = getSettingItemHeight();

        int trackBg = hovering ? brighten(0x11555555, gui != null ? gui.hoverBrightness.getValue() : 40) : 0x11555555;
        RenderUtil.rect(context, this.x, this.y, this.x + barW, this.y + sh + 0.5f, trackBg);

        float fillX = (setting.getValue()).floatValue() <= min.floatValue()
                ? this.x : this.x + barW * partialMultiplier();

        if (gui != null && gui.gradientEnabled.getValue()) {
            Color c1 = gui.getGradientColor1();
            Color c2 = gui.getGradientColor2();
            int alpha = hovering ? gui.topColor.getValue().getAlpha() : gui.color.getValue().getAlpha();

            float endT = partialMultiplier();
            Color cEnd = ColorUtil.lerp(c1, c2, endT);
            int a1 = withAlpha(c1, alpha), aEnd = withAlpha(cEnd, alpha);
            RenderUtil.gradient(context, (int)this.x, (int)this.y, (int)fillX, (int)(this.y + sh + 0.5f),
                    a1, a1, aEnd, aEnd);
        } else {
            int alpha = hovering
                    ? gui != null ? gui.topColor.getValue().getAlpha() : 200
                    : gui != null ? gui.color.getValue().getAlpha() : 180;
            RenderUtil.rect(context, this.x, this.y, fillX, this.y + sh + 0.5f,
                    WannaCry.colorManager.getColorWithAlpha(y, alpha));
        }

        float scale = gui != null ? gui.settingTextSize.getValue() : 1.0f;
        String label = this.getName() + " " + ChatFormatting.GRAY
                + (setting.getValue() instanceof Float ? setting.getValue()
                   : Double.valueOf((setting.getValue()).doubleValue()));
        drawScaledText(context, label, this.x + 2.3f, settingTextY(), -1, scale);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) this.setSettingFromX(mouseX);
    }

    @Override
    public boolean isHovering(int mouseX, int mouseY) {
        for (Widget widget : WannaCryGui.getClickGui().getComponents()) {
            if (!widget.drag) continue;
            return false;
        }
        return (float)mouseX >= this.getX() && (float)mouseX <= this.getX() + (float)this.getWidth() + 8.0f
                && (float)mouseY >= this.getY() && (float)mouseY < this.getY() + getSettingItemHeight() + 1f;
    }

    @Override public int getHeight() { return getSettingItemHeight(); }
    @Override public void update() { this.setHidden(!this.setting.isVisible()); }

    private void dragSetting(int mouseX, int mouseY) {
        if (this.isHovering(mouseX, mouseY) && GLFW.glfwGetMouseButton(mc.getWindow().handle(), 0) == 1)
            this.setSettingFromX(mouseX);
    }

    private void setSettingFromX(int mouseX) {
        float barW = (float)this.width + 7.4f;
        float percent = Math.max(0f, Math.min(1f, ((float)mouseX - this.x) / barW));
        if (setting.getValue() instanceof Double) {
            double minD = min.doubleValue(), maxD = max.doubleValue();
            double raw = minD + (maxD - minD) * percent;
            double halfPx = 0.5 * (maxD - minD) / barW;
            if (minD < 0 && maxD > 0 && Math.abs(raw) < halfPx) { setting.setValue(0.0); return; }
            double step = stepSize(minD, maxD);
            double result = Math.max(minD, Math.min(maxD, Math.round(raw / step) * step));
            setting.setValue(result);
        } else if (setting.getValue() instanceof Float) {
            float minF = min.floatValue(), maxF = max.floatValue();
            float raw = minF + (maxF - minF) * percent;
            float halfPx = 0.5f * (maxF - minF) / barW;
            if (minF < 0 && maxF > 0 && Math.abs(raw) < halfPx) { setting.setValue(0.0f); return; }
            float step = (float)stepSize(minF, maxF);
            float result = Math.max(minF, Math.min(maxF, Math.round(raw / step) * step));
            setting.setValue(result);
        } else if (setting.getValue() instanceof Integer) {
            int minI = min.intValue(), maxI = max.intValue();
            setting.setValue(Math.max(minI, Math.min(maxI, minI + (int)((float)(maxI - minI) * percent))));
        }
    }

    private double stepSize(double min, double max) {
        double range = max - min;
        if (range <= 10.0)  return 0.01;
        if (range <= 100.0) return 0.1;
        return 1.0;
    }

    private float partialMultiplier() {
        float range = max.floatValue() - min.floatValue();
        if (range == 0) return 0;
        return (setting.getValue().floatValue() - min.floatValue()) / range;
    }
}
