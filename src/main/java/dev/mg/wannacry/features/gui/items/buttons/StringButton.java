package dev.mg.wannacry.features.gui.items.buttons;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.gui.WannaCryGui;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.models.Timer;
import dev.mg.wannacry.util.render.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import dev.mg.wannacry.util.WannaCrySounds;
import org.lwjgl.glfw.GLFW;

public class StringButton extends Button {
    private static final Timer idleTimer = new Timer();
    private static boolean idle;
    private final Setting<String> setting;
    public boolean isListening;
    private CurrentString currentString = new CurrentString("");

    public StringButton(Setting<String> setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    public static String removeLastChar(String str) {
        if (str != null && !str.isEmpty()) return str.substring(0, str.length() - 1);
        return "";
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        boolean hovering = isHovering(mouseX, mouseY);

        int sh = getSettingItemHeight();
        RenderUtil.rect(context, this.x, this.y, this.x + (float)this.width + 7.4f,
                this.y + sh + 0.5f,
                getState()
                        ? (hovering ? WannaCry.colorManager.getColorWithAlpha(y, gui != null ? gui.topColor.getValue().getAlpha() : 200)
                                    : WannaCry.colorManager.getColorWithAlpha(y, gui != null ? gui.color.getValue().getAlpha() : 180))
                        : (hovering ? brighten(0x11555555, gui != null ? gui.hoverBrightness.getValue() : 40) : 0x11555555));

        float scale = gui != null ? gui.settingTextSize.getValue() : 1.0f;
        String text;
        if (isListening) {
            text = currentString.string() + getIdleSign();
        } else {
            text = (setting.getName().equals("Buttons") ? "Buttons " :
                    (setting.getName().equals("Prefix") ? "Prefix  " + ChatFormatting.GRAY : ""))
                    + setting.getValue();
        }
        drawScaledText(context, text, this.x + 2.3f, settingTextY(), getState() ? -1 : -5592406, scale);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isHovering(mouseX, mouseY))
            mc.getSoundManager().play(SimpleSoundInstance.forUI(WannaCrySounds.UI_CLICK, 1f));
    }

    @Override
    public void onKeyTyped(String typedChar, int keyCode) {
        if (isListening) setString(currentString.string() + typedChar);
    }

    @Override
    public void onKeyPressed(int key) {
        if (isListening) {
            switch (key) {
                case GLFW.GLFW_KEY_ENTER -> enterString();
                case GLFW.GLFW_KEY_BACKSPACE -> setString(removeLastChar(currentString.string()));
            }
        }
    }

    @Override public int getHeight() { return getSettingItemHeight(); }
    @Override public void update() { setHidden(!setting.isVisible()); }

    private void enterString() {
        setting.setValue(currentString.string().isEmpty() ? setting.getDefaultValue() : currentString.string());
        setString("");
        onMouseClick();
    }

    @Override public void toggle() { isListening = !isListening; }
    @Override public boolean getState() { return !isListening; }

    public void setString(String s) { currentString = new CurrentString(s); }

    public static String getIdleSign() {
        if (idleTimer.passedMs(500)) { idle = !idle; idleTimer.reset(); }
        return idle ? "_" : "";
    }

    public record CurrentString(String string) {}
}
