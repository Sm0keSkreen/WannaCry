package dev.mg.wannacry.features.gui.items.buttons;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.gui.WannaCryGui;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.features.settings.Bind;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.KeyboardUtil;
import dev.mg.wannacry.util.render.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import dev.mg.wannacry.util.WannaCrySounds;
import org.lwjgl.glfw.GLFW;

public class BindButton extends Button {
    private final Setting<Bind> setting;
    public boolean isListening;

    public BindButton(Setting<Bind> setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        boolean hovering = isHovering(mouseX, mouseY);

        int sh = getSettingItemHeight();
        int bg = getState()
                ? (hovering ? brighten(0x11555555, gui != null ? gui.hoverBrightness.getValue() : 40) : 0x11555555)
                : (hovering ? WannaCry.colorManager.getColorWithAlpha(y, gui != null ? gui.topColor.getValue().getAlpha() : 200)
                            : WannaCry.colorManager.getColorWithAlpha(y, gui != null ? gui.color.getValue().getAlpha() : 180));
        RenderUtil.rect(context, this.x, this.y, this.x + (float)this.width + 7.4f,
                this.y + sh + 0.5f, bg);

        float scale = gui != null ? gui.settingTextSize.getValue() : 1.0f;
        String text = isListening ? "Press a Key..."
                : setting.getName() + " " + ChatFormatting.GRAY + KeyboardUtil.getKeyName(setting.getValue());
        drawScaledText(context, text, this.x + 2.3f, settingTextY(), getState() ? -1 : -5592406, scale);
    }

    @Override public int getHeight() { return getSettingItemHeight(); }
    @Override public void update() { setHidden(!setting.isVisible()); }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isListening) {
            if (mouseButton != 0 && mouseButton != 1) {
                setting.setValue(new Bind(-mouseButton - 2));
                onMouseClick();
            }
        } else if (isHovering(mouseX, mouseY)) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(WannaCrySounds.UI_CLICK, 1f));
        }
    }

    @Override
    public void onKeyPressed(int key) {
        if (isListening) {
            Bind bind = new Bind(key);
            if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_ESCAPE)
                bind = new Bind(-1);
            setting.setValue(bind);
            onMouseClick();
        }
    }

    @Override public void toggle() { isListening = !isListening; }
    @Override public boolean getState() { return !isListening; }
}
