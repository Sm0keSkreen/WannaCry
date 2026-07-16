package dev.mg.wannacry.features.gui.items.buttons;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.gui.WannaCryGui;
import dev.mg.wannacry.features.gui.WhitelistScreen;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.features.settings.ItemWhitelist;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.WannaCrySounds;
import dev.mg.wannacry.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

import java.awt.*;

public class ItemWhitelistButton extends Button {

    private final Setting<String> setting;
    private final ItemWhitelist   whitelist;

    public ItemWhitelistButton(Setting<String> setting, ItemWhitelist whitelist) {
        super("Whitelist");
        this.setting   = setting;
        this.whitelist = whitelist;
        this.width = 15;
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        boolean hovering = isHovering(mouseX, mouseY);

        int bg;
        if (getState()) {
            if (gui != null && gui.gradientEnabled.getValue()) {
                Color c1 = gui.getGradientColor1();
                Color c2 = gui.getGradientColor2();
                int alpha = hovering ? gui.topColor.getValue().getAlpha() : gui.color.getValue().getAlpha();
                int a1 = withAlpha(c1, alpha), a2 = withAlpha(c2, alpha);
                RenderUtil.gradient(context, (int) this.x, (int) this.y,
                        (int) (this.x + this.width + 7.4f), (int) (this.y + this.height - 0.5f),
                        a1, a1, a2, a2);
            } else {
                int alpha = hovering
                        ? gui != null ? gui.topColor.getValue().getAlpha() : 200
                        : gui != null ? gui.color.getValue().getAlpha()    : 180;
                RenderUtil.rect(context, this.x, this.y,
                        this.x + (float) this.width + 7.4f, this.y + (float) this.height - 0.5f,
                        WannaCry.colorManager.getColorWithAlpha(y, alpha));
            }
        } else {
            bg = hovering ? brighten(0x11555555, gui != null ? gui.hoverBrightness.getValue() : 40) : 0x11555555;
            RenderUtil.rect(context, this.x, this.y,
                    this.x + (float) this.width + 7.4f, this.y + (float) this.height - 0.5f, bg);
        }

        float scale = gui != null ? gui.settingTextSize.getValue() : 1.0f;
        drawScaledText(context, this.getName(), this.x + 2.3f,
                this.y - 1.7f - (float) WannaCryGui.getClickGui().getTextOffset(),
                getState() ? -1 : -5592406, scale);
    }

    @Override
    public boolean getState() { return !whitelist.isEmpty(); }

    @Override
    public void onMouseClick() {
        mc.getSoundManager().play(SimpleSoundInstance.forUI(WannaCrySounds.UI_CLICK, 1f));
        mc.setScreen(new WhitelistScreen(setting, whitelist, mc.screen));
    }

    @Override
    public void update() { this.setHidden(!setting.isVisible()); }
}
