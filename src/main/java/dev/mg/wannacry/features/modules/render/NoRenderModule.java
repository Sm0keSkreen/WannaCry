package dev.mg.wannacry.features.modules.render;

import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.settings.Setting;

public class NoRenderModule extends Module {

    private static NoRenderModule INSTANCE;

    public final Setting<Boolean> portalOverlay        = bool("Portal Overlay",        false);
    public final Setting<Boolean> spyglassOverlay      = bool("Spyglass Overlay",      false);
    public final Setting<Boolean> nausea               = bool("Nausea",                false);
    public final Setting<Boolean> pumpkinOverlay       = bool("Pumpkin Overlay",       false);
    public final Setting<Boolean> powderedSnowOverlay  = bool("Powdered Snow Overlay", false);
    public final Setting<Boolean> fireOverlay          = bool("Fire Overlay",          false);
    public final Setting<Boolean> liquidOverlay        = bool("Liquid Overlay",        false);
    public final Setting<Boolean> inWallOverlay        = bool("In-Wall Overlay",       false);
    public final Setting<Boolean> vignette             = bool("Vignette",              false);
    public final Setting<Boolean> guiBackground        = bool("GUI Background",        false);
    public final Setting<Boolean> totemAnimation       = bool("Totem Animation",       false);
    public final Setting<Boolean> eatingParticles      = bool("Eating Particles",      false);
    public final Setting<Boolean> enchantGlint         = bool("Enchantment Glint",     false);

    public final Setting<Boolean> bossBar              = bool("Boss Bar",              false);
    public final Setting<Boolean> scoreboard           = bool("Scoreboard",            false);
    public final Setting<Boolean> crosshair            = bool("Crosshair",             false);
    public final Setting<Boolean> title                = bool("Title",                 false);
    public final Setting<Boolean> heldItemName         = bool("Held Item Name",        false);
    public final Setting<Boolean> obfuscation          = bool("Obfuscation",           false);
    public final Setting<Boolean> potionIcons          = bool("Potion Icons",          false);

    public final Setting<Boolean> weather              = bool("Weather",               false);
    public final Setting<Boolean> worldBorder          = bool("World Border",          false);
    public final Setting<Boolean> blindness            = bool("Blindness",             false);
    public final Setting<Boolean> darkness             = bool("Darkness",              false);
    public final Setting<Boolean> fog                  = bool("Fog",                   false);
    public final Setting<Boolean> enchantTableBook     = bool("Enchant Table Book",    false);
    public final Setting<Boolean> signText             = bool("Sign Text",             false);
    public final Setting<Boolean> blockBreakOverlay    = bool("Block Break Overlay",   false);
    public final Setting<Boolean> blockBreakParticles  = bool("Block Break Particles", false);
    public final Setting<Boolean> beaconBeams          = bool("Beacon Beams",          false);
    public final Setting<Boolean> fallingBlocks        = bool("Falling Blocks",        false);
    public final Setting<Boolean> mapMarkers           = bool("Map Markers",           false);
    public final Setting<Boolean> mapContents          = bool("Map Contents",          false);
    public final Setting<Boolean> banners              = bool("Banners",               false);
    public final Setting<Boolean> fireworkExplosions   = bool("Firework Explosions",   false);
    public final Setting<Boolean> hideAllParticles     = bool("Hide All Particles",    false);
    public final Setting<Boolean> textureRotations     = bool("Texture Rotations",     false);

    public final Setting<Boolean> armor                = bool("Armor",                 false);
    public final Setting<Boolean> invisibility         = bool("Invisibility",          false);
    public final Setting<Boolean> glowing              = bool("Glowing",               false);
    public final Setting<Boolean> spawnerEntities      = bool("Spawner Entities",      false);
    public final Setting<Boolean> deadEntities         = bool("Dead Entities",         false);
    public final Setting<Boolean> nametags             = bool("Nametags",              false);

    public NoRenderModule() {
        super("NoRender",
              "Disables rendering of certain things.",
              Category.RENDER);
        INSTANCE = this;
    }

    public static NoRenderModule getInstance() {
        return INSTANCE;
    }
}
