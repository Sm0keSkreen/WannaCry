package dev.mg.wannacry.features.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.event.impl.ClientEvent;
import dev.mg.wannacry.event.impl.render.Render2DEvent;
import dev.mg.wannacry.event.impl.render.Render3DEvent;
import dev.mg.wannacry.features.Feature;
import dev.mg.wannacry.features.commands.Command;
import dev.mg.wannacry.features.commands.MessageSignatures;
import dev.mg.wannacry.features.settings.Bind;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.manager.ConfigManager;
import dev.mg.wannacry.util.traits.Jsonable;
import dev.mg.wannacry.util.traits.Toggleable;
import net.minecraft.ChatFormatting;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

public class Module extends Feature implements Jsonable, Toggleable {
    private final String description;
    private final Category category;

    public final Setting<Boolean> enabled = bool("Enabled", false);
    public final Setting<Bind> bind = key("Keybind", new Bind(GLFW_KEY_UNKNOWN));
    public final Setting<String> displayName;

    public boolean hidden;

    public Module(String name, String description, Category category) {
        super(name);
        this.displayName = str("DisplayName", name);
        this.description = description;
        this.category = category;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onToggle() {
    }

    public void onLoad() {
    }

    public void onTick() {
    }

    public void onRender2D(Render2DEvent event) {
    }

    public void onRender3D(Render3DEvent event) {
    }

    public void onUnload() {
    }

    public String getDisplayInfo() {
        return null;
    }

    public void enable() {
        this.enabled.setValue(true);
        EVENT_BUS.register(this);
        EVENT_BUS.post(new ClientEvent(ClientEvent.Type.TOGGLE_MODULE, this));
        this.onToggle();
        this.onEnable();
    }

    public void disable() {
        this.enabled.setValue(false);
        EVENT_BUS.unregister(this);
        EVENT_BUS.post(new ClientEvent(ClientEvent.Type.TOGGLE_MODULE, this));
        this.onToggle();
        this.onDisable();
    }

    public String getDisplayName() {
        return this.displayName.getValue();
    }

    public void setDisplayName(String name) {
        Module module = WannaCry.moduleManager.getModuleByDisplayName(name);
        Module originalModule = WannaCry.moduleManager.getModuleByName(name);
        if (module == null && originalModule == null) {
            Command.sendMessage("%, name: %s, has been renamed to: %s", MessageSignatures.GENERAL,
                    getDisplayName(), getName(), name);
            this.displayName.setValue(name);
            return;
        }

        Command.sendMessage("{red} A module of this name already exists.", MessageSignatures.GENERAL);
    }

    @Override
    public boolean isEnabled() {
        return enabled.getValue();
    }

    @Override
    public boolean isToggled() {
        return isEnabled();
    }

    public String getDescription() {
        return this.description;
    }

    public Category getCategory() {
        return this.category;
    }

    public String getInfo() {
        return null;
    }

    public Bind getBind() {
        return this.bind.getValue();
    }

    public void setBind(int key) {
        this.bind.setValue(new Bind(key));
    }

    public String getFullArrayString() {
        return this.getDisplayName() + ChatFormatting.GRAY + (this.getDisplayInfo() != null ? " [" + ChatFormatting.WHITE + this.getDisplayInfo() + ChatFormatting.GRAY + "]" : "");
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        for (Setting<?> setting : getSettings()) {
            try {
                if (setting.getValue() instanceof Bind keyBind) {
                    object.addProperty(setting.getName(), keyBind.getKey());
                } else if (setting.getValue() instanceof java.awt.Color color) {
                    object.addProperty(setting.getName(), color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha());
                } else if (setting.getValue() instanceof Vector2f pos) {
                    object.addProperty(setting.getName(), pos.x() + "," + pos.y());
                } else {
                    object.addProperty(setting.getName(), setting.getValueAsString());
                }
            } catch (Throwable e) {
                WannaCry.LOGGER.error("Failed to create JSON field", e);
            }
        }
        return object;
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element == null || element.isJsonNull()) return;
        JsonObject object = element.getAsJsonObject();

        for (Setting<?> setting : getSettings()) {
            if (setting == this.enabled) continue;
            try {
                JsonElement settingElement = object.get(setting.getName());
                if (settingElement != null && !settingElement.isJsonNull()) {
                    ConfigManager.setValueFromJson(this, setting, settingElement);
                }
            } catch (Throwable throwable) {
                WannaCry.LOGGER.error("Failed to load from JSON", throwable);
            }
        }

        if (object.has("Enabled")) {
            boolean shouldBeEnabled = Boolean.parseBoolean(object.get("Enabled").getAsString());
            if (shouldBeEnabled && !isEnabled()) {
                enable();
            } else if (!shouldBeEnabled && isEnabled()) {
                disable();
            }
        }

        onLoad();
    }

    public enum Category {
        CLOSET("Closet"),
        EXPLOIT("Exploit"),
        CLIENT("Client"),
        HUD("Hud"),
        RENDER("Render");

        private final String name;

        Category(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }
}
