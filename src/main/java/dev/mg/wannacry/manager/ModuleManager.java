package dev.mg.wannacry.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.event.impl.render.Render2DEvent;
import dev.mg.wannacry.event.impl.render.Render3DEvent;
import dev.mg.wannacry.features.Feature;
import dev.mg.wannacry.features.commands.ModuleCommand;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.features.modules.client.ConfigsModule;
import dev.mg.wannacry.features.modules.client.FontModule;
import dev.mg.wannacry.features.modules.client.HudEditorModule;
import dev.mg.wannacry.features.modules.client.NotificationsModule;
import dev.mg.wannacry.features.modules.combat.AimCorrectionModule;
import dev.mg.wannacry.features.modules.combat.MaceSpoofModule;
import dev.mg.wannacry.features.modules.exploit.ElytraSpeedModule;
import dev.mg.wannacry.features.modules.exploit.WindDelayModule;
import dev.mg.wannacry.features.modules.exploit.XCarryModule;
import dev.mg.wannacry.features.modules.player.TotemModule;
import dev.mg.wannacry.features.modules.combat.TriggerBotModule;
import dev.mg.wannacry.features.modules.hud.FpsHudModule;
import dev.mg.wannacry.features.modules.movement.AutoSprintModule;
import dev.mg.wannacry.features.modules.player.FastBreakModule;
import dev.mg.wannacry.features.modules.player.FastPlaceModule;
import dev.mg.wannacry.features.modules.render.FullbrightModule;
import dev.mg.wannacry.features.modules.render.NoRenderModule;
import dev.mg.wannacry.util.traits.Jsonable;
import dev.mg.wannacry.util.traits.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class ModuleManager implements Jsonable, Util {
    private static final Logger LOGGER = LoggerFactory.getLogger("ModuleManager");

    private final Map<Class<? extends Module>, Module> fastRegistry = new HashMap<>();
    private final List<Module> modules = new ArrayList<>();

    public void init() {

        register(new FpsHudModule());
        register(new HudEditorModule());
        register(new FontModule());
        register(new ClickGuiModule());
        register(new ConfigsModule());

        register(new NotificationsModule());

        register(new AutoSprintModule());

        register(new FastBreakModule());
        register(new FastPlaceModule());
        register(new TotemModule());

        register(new FullbrightModule());
        register(new NoRenderModule());

        register(new ElytraSpeedModule());
        register(new WindDelayModule());
        register(new XCarryModule());

        register(new MaceSpoofModule());
        register(new AimCorrectionModule());
        register(new TriggerBotModule());

        LOGGER.info("Registered {} modules", modules.size());

        for (Module module : modules) {
            WannaCry.commandManager.register(new ModuleCommand(module));
        }

        WannaCry.configManager.addConfig(this);
    }

    public void register(Module module) {
        getModules().add(module);
        fastRegistry.put(module.getClass(), module);
    }

    public List<Module> getModules() {
        return modules;
    }

    public Stream<Module> stream() {
        return getModules().stream();
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModuleByClass(Class<T> clazz) {
        return (T) fastRegistry.get(clazz);
    }

    public Module getModuleByName(String name) {
        return stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Module getModuleByDisplayName(String display) {
        return stream().filter(m -> m.getDisplayName().equalsIgnoreCase(display)).findFirst().orElse(null);
    }

    public List<Module> getModulesByCategory(Module.Category category) {
        return stream().filter(m -> m.getCategory() == category).toList();
    }

    public List<Module.Category> getCategories() {
        return Arrays.asList(Module.Category.values());
    }

    public void onLoad() {
        getModules().forEach(Module::onLoad);
    }

    public void onTick() {
        stream().filter(Feature::isEnabled).forEach(Module::onTick);
    }

    public void onRender2D(Render2DEvent event) {
        stream().filter(Feature::isEnabled).forEach(module -> module.onRender2D(event));
    }

    public void onRender3D(Render3DEvent event) {
        stream().filter(Feature::isEnabled).forEach(module -> module.onRender3D(event));
    }

    public void onUnload() {
        getModules().forEach(EVENT_BUS::unregister);
        getModules().forEach(Module::onUnload);
    }

    public void onKeyPressed(int key) {
        if (key <= 0 || mc.screen != null) return;
        stream().filter(module -> module.getBind().getKey() == key).forEach(Module::toggle);
    }

    public void onMouseClicked(int button) {
        if (mc.screen != null) return;
        int key = -button - 2;
        stream().filter(module -> module.getBind().getKey() == key).forEach(Module::toggle);
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        for (Module module : getModules()) {
            object.add(module.getName(), module.toJson());
        }
        return object;
    }

    @Override
    public void fromJson(JsonElement element) {
        for (Module module : getModules()) {
            module.fromJson(element.getAsJsonObject().get(module.getName()));
        }
    }

    @Override
    public String getFileName() {
        return "modules.json";
    }
}
