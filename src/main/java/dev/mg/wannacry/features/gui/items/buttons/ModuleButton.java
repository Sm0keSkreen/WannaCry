package dev.mg.wannacry.features.gui.items.buttons;

import dev.mg.wannacry.features.gui.WannaCryGui;
import dev.mg.wannacry.features.gui.items.Item;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.features.settings.Bind;
import dev.mg.wannacry.features.settings.ItemWhitelist;
import dev.mg.wannacry.features.settings.ItemWhitelistHolder;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.WannaCrySounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton extends Button {

    private final Module module;
    private List<Item> items = new ArrayList<>();

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;
        this.initSettings();
    }

    @SuppressWarnings("unchecked")
    public void initSettings() {
        ArrayList<Item> newItems = new ArrayList<>();

        if (!this.module.getSettings().isEmpty()) {
            for (Setting<?> setting : this.module.getSettings()) {
                if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled")) {
                    newItems.add(new BooleanButton((Setting<Boolean>) setting));
                }
                if (setting.getValue() instanceof Bind
                        && !setting.getName().equalsIgnoreCase("Keybind")
                        && !this.module.getName().equalsIgnoreCase("Hud")) {
                    newItems.add(new BindButton((Setting<Bind>) setting));
                }
                if (setting.getValue() instanceof String || setting.getValue() instanceof Character) {
                    if (setting.getName().equalsIgnoreCase("displayName")) continue;
                    if (this.module instanceof ItemWhitelistHolder holder) {
                        ItemWhitelist wl = holder.getWhitelistFor(setting.getName());
                        if (wl != null) {
                            newItems.add(new ItemWhitelistButton((Setting<String>) setting, wl));
                            continue;
                        }
                    }
                    newItems.add(new StringButton((Setting<String>) setting));
                }
                if (setting.isColorSetting()) {
                    newItems.add(new ColorButton((Setting<java.awt.Color>) setting));
                    continue;
                }
                if (setting.isNumberSetting() && setting.hasRestriction()) {
                    newItems.add(new Slider((Setting<Number>) setting));
                    continue;
                }
                if (!setting.isEnumSetting()) continue;
                newItems.add(new EnumButton((Setting<Enum<?>>) setting));
            }
        }
        newItems.add(new BindButton((Setting<Bind>) this.module.getSettingByName("Keybind")));
        this.items = newItems;
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(context, mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!this.items.isEmpty()) {
            if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
                WannaCryGui gui = WannaCryGui.getClickGui();
                if (gui.getSidePanelModule() == this.module) {
                    gui.closeSidePanel();
                } else {
                    gui.openSidePanel(this.module, this.items);
                }
                mc.getSoundManager().play(SimpleSoundInstance.forUI(WannaCrySounds.UI_CLICK, 1f));
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        super.mouseReleased(mouseX, mouseY, releaseButton);
    }

    @Override
    public void onKeyTyped(String typedChar, int keyCode) {
        super.onKeyTyped(typedChar, keyCode);
    }

    @Override
    public void onKeyPressed(int key) {
        super.onKeyPressed(key);
    }

    @Override
    public int getHeight() {
        return getModuleHeight();
    }

    @Override
    protected int getVisualHeight() {
        return getModuleHeight();
    }

    private int getModuleHeight() {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        return (gui != null) ? gui.moduleHeight.getValue() : 14;
    }

    public Module getModule() { return this.module; }

    @Override
    public void toggle() { this.module.toggle(); }

    @Override
    public boolean getState() { return this.module.isEnabled(); }
}
