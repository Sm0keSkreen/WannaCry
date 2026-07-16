package dev.mg.wannacry.font;

import com.mojang.blaze3d.font.GlyphProvider;
import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.modules.client.FontModule;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.GlyphStitcher;
import net.minecraft.resources.Identifier;
import org.lwjgl.util.freetype.FreeType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static dev.mg.wannacry.util.traits.Util.mc;

public class FontLoader {

    private FontSet storage;
    private int       currentSize       = -1;
    private int       currentOversample = -1;
    private FontType  lastFont          = null;
    private int       currentFlags      = -1;
    private int       currentRenderMode = -1;
    private float     currentShiftX     = Float.NaN;
    private float     currentShiftY     = Float.NaN;

    public void init() {
        FontModule fontModule = WannaCry.moduleManager.getModuleByClass(FontModule.class);
        if (fontModule == null) return;

        int      newSize      = fontModule.glyphSize.getValue();
        int      newOversample = fontModule.oversample.getValue();
        FontType selectedFont = fontModule.fontType.getValue();
        float    shiftX       = fontModule.shiftX.getValue();
        float    shiftY       = fontModule.shiftY.getValue();

        int renderMode = switch (fontModule.antialiasMode.getValue()) {
            case LIGHT -> FreeType.FT_RENDER_MODE_LIGHT;
            default    -> FreeType.FT_RENDER_MODE_NORMAL;
        };

        int flags = FreeType.FT_LOAD_DEFAULT | (renderMode << 16);
        if (fontModule.autoHint.getValue()) flags |= FreeType.FT_LOAD_FORCE_AUTOHINT;

        if (storage != null
                && currentSize       == newSize
                && currentOversample == newOversample
                && selectedFont      == lastFont
                && currentFlags      == flags
                && currentRenderMode == renderMode
                && Float.compare(currentShiftX, shiftX) == 0
                && Float.compare(currentShiftY, shiftY) == 0) {
            return;
        }

        WannaCryTrueTypeGlyphProviderDefinition definition =
                new WannaCryTrueTypeGlyphProviderDefinition(
                        Identifier.fromNamespaceAndPath("wannacry", selectedFont.getFileName()),
                        newSize, newOversample,
                        shiftX, shiftY,
                        "",
                        flags, renderMode);

        try {
            GlyphProvider font = definition.unpack().orThrow().load(mc.getResourceManager());

            GlyphStitcher stitcher = new GlyphStitcher(
                    mc.getTextureManager(),
                    Identifier.fromNamespaceAndPath("wannacry", selectedFont.getFileName() + "_storage"));

            storage = new FontSet(stitcher);
            storage.reload(
                    List.of(new GlyphProvider.Conditional(font, FontOption.Filter.ALWAYS_PASS)),
                    Collections.emptySet());

            currentSize       = newSize;
            currentOversample = newOversample;
            currentFlags      = flags;
            currentRenderMode = renderMode;
            currentShiftX     = shiftX;
            currentShiftY     = shiftY;
            lastFont          = selectedFont;

        } catch (IOException e) {
            WannaCry.LOGGER.error("[FontLoader] Failed to load font '{}': {}", selectedFont.getFileName(), e.getMessage());
            storage           = null;
            currentSize       = -1;
            currentOversample = -1;
            lastFont          = null;
            currentFlags      = -1;
            currentRenderMode = -1;
            currentShiftX     = Float.NaN;
            currentShiftY     = Float.NaN;
        }
    }

    public FontSet getStorage()           { return storage; }
    public int     getCurrentSize()       { return currentSize; }
    public int     getCurrentOversample() { return currentOversample; }
}
