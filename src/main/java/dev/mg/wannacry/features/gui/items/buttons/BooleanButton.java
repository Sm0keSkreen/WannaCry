package dev.mg.wannacry.features.gui.items.buttons;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.gui.WannaCryGui;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.ColorUtil;
import dev.mg.wannacry.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import dev.mg.wannacry.util.WannaCrySounds;

import java.awt.*;

public class BooleanButton extends Button {
    private final Setting<Boolean> setting;

    public BooleanButton(Setting<Boolean> setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        boolean hovering  = isHovering(mouseX, mouseY);
        float barW = (float) this.width + 7.4f;
        int cr = (gui != null) ? gui.cornerRadius.getValue() : 0;

        int sh = getSettingItemHeight();

        if (getState()) {
            if (gui != null && gui.gradientEnabled.getValue()) {
                Color c1 = gui.getGradientColor1();
                Color c2 = gui.getGradientColor2();
                int alpha = hovering ? gui.topColor.getValue().getAlpha() : gui.color.getValue().getAlpha();
                int a1 = withAlpha(c1, alpha), a2 = withAlpha(c2, alpha);
                RenderUtil.gradient(context,
                        (int) this.x, (int) this.y,
                        (int) (this.x + barW), (int) (this.y + sh + 0.5f),
                        a1, a1, a2, a2);
            } else {
                int alpha = hovering
                        ? (gui != null ? gui.topColor.getValue().getAlpha() : 200)
                        : (gui != null ? gui.color.getValue().getAlpha()    : 180);
                ClickGuiModule.filledRoundRect(context,
                        (int) this.x, (int) this.y,
                        (int) (this.x + barW), (int) (this.y + sh + 0.5f),
                        WannaCry.colorManager.getColorWithAlpha(y, alpha), cr);
            }
        } else {

            Color sbg = (gui != null) ? gui.settingsBgColor.getValue() : new Color(8, 8, 18, 110);
            int bg = hovering ? brighten(sbg.getRGB(), (gui != null) ? gui.hoverBrightness.getValue() : 40) : sbg.getRGB();
            ClickGuiModule.filledRoundRect(context,
                    (int) this.x, (int) this.y,
                    (int) (this.x + barW), (int) (this.y + sh + 0.5f),
                    bg, cr);
        }

        float scale = (gui != null) ? gui.settingTextSize.getValue() : 1.0f;

        int txtColor = getState() ? -1 : -5592406;
        if (gui != null) {
            Color tc = getState() ? gui.enabledTextColor.getValue() : gui.disabledTextColor.getValue();
            txtColor = tc.getRGB();
        }
        drawScaledText(context, this.getName(), this.x + 2.3f, settingTextY(), txtColor, scale);
    }

    @Override public int getHeight() { return getSettingItemHeight(); }
    @Override public void update() { this.setHidden(!this.setting.isVisible()); }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(WannaCrySounds.UI_CLICK, 1f));
        }
    }

    @Override public void toggle()         { this.setting.setValue(!this.setting.getValue()); }
    @Override public boolean getState()    { return this.setting.getValue(); }
}
