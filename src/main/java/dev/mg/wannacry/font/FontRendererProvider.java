package dev.mg.wannacry.font;

import dev.mg.wannacry.mixin.render.gui.MixinFont;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.network.chat.FontDescription;

import static dev.mg.wannacry.util.traits.Util.mc;

public class FontRendererProvider {

    private final FontLoader fontLoader;
    private Font cachedRenderer;
    private int  cachedSize       = -1;
    private int  cachedOversample = -1;

    public FontRendererProvider(FontLoader fontLoader) {
        this.fontLoader = fontLoader;
    }

    public Font getRenderer(boolean enabled) {
        if (!enabled) {

            cachedRenderer  = null;
            cachedSize      = -1;
            cachedOversample = -1;
            return mc.font;
        }

        fontLoader.init();

        if (cachedRenderer != null
                && cachedSize       == fontLoader.getCurrentSize()
                && cachedOversample == fontLoader.getCurrentOversample()) {
            return cachedRenderer;
        }

        EffectGlyph rectangle = ((MixinFont) mc.font).getProvider().effect();

        cachedRenderer = new Font(new Font.Provider() {
            @Override
            public GlyphSource glyphs(FontDescription font) {
                return fontLoader.getStorage().source(true);
            }

            @Override
            public EffectGlyph effect() {
                return rectangle;
            }
        });

        cachedSize       = fontLoader.getCurrentSize();
        cachedOversample = fontLoader.getCurrentOversample();

        return cachedRenderer;
    }
}
