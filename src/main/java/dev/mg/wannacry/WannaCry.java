package dev.mg.wannacry;

import dev.mg.wannacry.features.gui.WannaCryGui;
import dev.mg.wannacry.font.FontService;
import dev.mg.wannacry.manager.*;
import dev.mg.wannacry.render.WannaCryFullscreenQuad;
import dev.mg.wannacry.render.WannaCryRenderPipelines;
import dev.mg.wannacry.render.postprocess.WannaCryShaderESP;
import dev.mg.wannacry.util.BuildConfig;
import dev.mg.wannacry.util.WannaCrySounds;
import dev.mg.wannacry.util.TextUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WannaCry implements ModInitializer, ClientModInitializer {
    public static float TIMER = 1f;

    public static final Logger LOGGER = LogManager.getLogger("WannaCry");
    public static ServerManager serverManager;
    public static ColorManager colorManager;
    public static RotationManager rotationManager;
    public static PositionManager positionManager;
    public static HoleManager holeManager;
    public static EventManager eventManager;
    public static SpeedManager speedManager;
    public static CommandManager commandManager;
    public static FriendManager friendManager;
    public static ModuleManager moduleManager;
    public static ConfigManager configManager;
    public static FontService fontService;

    @Override
    public void onInitialize() {
        LOGGER.info("Pre-initializing {} v{}",
                BuildConfig.NAME, BuildConfig.VERSION);
        WannaCrySounds.register();
        configManager = new ConfigManager();
        eventManager = new EventManager();
        serverManager = new ServerManager();
        rotationManager = new RotationManager();
        positionManager = new PositionManager();
        friendManager = new FriendManager();
        colorManager = new ColorManager();
        commandManager = new CommandManager();
        moduleManager = new ModuleManager();
        speedManager = new SpeedManager();
        holeManager = new HoleManager();
        TextUtil.init();
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing {}", BuildConfig.NAME);

        long startTime = System.nanoTime();

        fontService = new FontService();

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return Identifier.fromNamespaceAndPath("wannacry", "pipeline_loader");
                }

                @Override
                public void onResourceManagerReload(ResourceManager manager) {
                    WannaCryRenderPipelines.precompile(manager);
                }
            }
        );

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            WannaCryFullscreenQuad.init();
            WannaCryShaderESP.INSTANCE.init();
        });

        eventManager.init();
        commandManager.init();
        moduleManager.init();
        friendManager.init();

        configManager.addConfig(WannaCryGui.getInstance());

        configManager.load();
        colorManager.init();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> configManager.save()));

        long endTime = System.nanoTime();

        LOGGER.info("Initialized {} in {}ms",
                BuildConfig.NAME, (endTime - startTime) / 1000000.0);
    }
}
