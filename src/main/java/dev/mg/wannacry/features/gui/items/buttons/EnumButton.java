package dev.mg.wannacry.features.gui.items.buttons;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.gui.WannaCryGui;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.render.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import dev.mg.wannacry.util.WannaCrySounds;

import java.awt.*;

public class EnumButton extends Button {
    public Setting<Enum<?>> setting;

    public EnumButton(Setting<Enum<?>> setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        boolean hovering = isHovering(mouseX, mouseY);

        int sh = getSettingItemHeight();

        if (gui != null && gui.gradientEnabled.getValue()) {
            Color c1 = gui.getGradientColor1();
            Color c2 = gui.getGradientColor2();
            int alpha = hovering ? gui.topColor.getValue().getAlpha() : gui.color.getValue().getAlpha();
            int a1 = withAlpha(c1, alpha), a2 = withAlpha(c2, alpha);
            RenderUtil.gradient(context, (int)this.x, (int)this.y,
                    (int)(this.x + this.width + 7.4f), (int)(this.y + sh + 0.5f),
                    a1, a1, a2, a2);
        } else {
            int alpha = hovering
                    ? gui != null ? gui.topColor.getValue().getAlpha() : 200
                    : gui != null ? gui.color.getValue().getAlpha() : 180;
            int col = getState()
                    ? (hovering ? WannaCry.colorManager.getColorWithAlpha(y, alpha) : WannaCry.colorManager.getColorWithAlpha(y, alpha))
                    : (hovering ? brighten(0x11555555, gui != null ? gui.hoverBrightness.getValue() : 40) : 0x11555555);
            RenderUtil.rect(context, this.x, this.y, this.x + (float)this.width + 7.4f,
                    this.y + sh + 0.5f, col);
        }

        float scale = gui != null ? gui.settingTextSize.getValue() : 1.0f;
        String label = setting.getName() + " " + ChatFormatting.GRAY + setting.currentEnumName();
        drawScaledText(context, label, this.x + 2.3f, settingTextY(), getState() ? -1 : -5592406, scale);
    }

    @Override public int getHeight() { return getSettingItemHeight(); }
    @Override public void update() { this.setHidden(!this.setting.isVisible()); }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY))
            mc.getSoundManager().play(SimpleSoundInstance.forUI(WannaCrySounds.UI_CLICK, 1f));
    }

    @Override public void toggle() { this.setting.increaseEnum(); }
    @Override public boolean getState() { return true; }
}
