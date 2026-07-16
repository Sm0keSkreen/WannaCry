package dev.mg.wannacry.font;

import com.mojang.blaze3d.font.*;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Locale;

public class WannaCryTrueTypeGlyphProvider implements GlyphProvider {

    @Nullable
    private ByteBuffer fontMemory;
    @Nullable
    private FT_Face face;
    public final float oversample;
    private final int ftLoadFlags;
    private final int ftRenderMode;

    private final CodepointMap<GlyphEntry> glyphs =
            new CodepointMap<>(GlyphEntry[]::new, GlyphEntry[][]::new);

    public WannaCryTrueTypeGlyphProvider(ByteBuffer byteBuffer, FT_Face ftFace, float size, float oversample,
                                           float shiftX, float shiftY, String skip,
                                           int ftLoadFlags, int ftRenderMode) {
        this.fontMemory = byteBuffer;
        this.face = ftFace;
        this.oversample = oversample;
        this.ftLoadFlags = ftLoadFlags;
        this.ftRenderMode = ftRenderMode;

        IntSet skipSet = new IntArraySet();
        skip.codePoints().forEach(skipSet::add);

        int pixelSize = Math.round(size * oversample);
        FreeType.FT_Set_Pixel_Sizes(ftFace, pixelSize, pixelSize);

        float tx = shiftX * oversample;
        float ty = -shiftY * oversample;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FT_Vector vec = FreeTypeUtil.setVector(FT_Vector.malloc(stack), tx, ty);
            FreeType.FT_Set_Transform(ftFace, null, vec);

            IntBuffer glyphIndexBuffer = stack.mallocInt(1);
            int codepoint = (int) FreeType.FT_Get_First_Char(ftFace, glyphIndexBuffer);

            while (true) {
                int glyphIndex = glyphIndexBuffer.get(0);
                if (glyphIndex == 0) return;

                if (!skipSet.contains(codepoint)) {
                    this.glyphs.put(codepoint, new GlyphEntry(glyphIndex));
                }

                codepoint = (int) FreeType.FT_Get_Next_Char(ftFace, codepoint, glyphIndexBuffer);
            }
        }
    }

    @Nullable
    @Override
    public UnbakedGlyph getGlyph(int codepoint) {
        GlyphEntry entry = this.glyphs.get(codepoint);
        return entry != null ? this.getOrLoadGlyph(codepoint, entry) : null;
    }

    private UnbakedGlyph getOrLoadGlyph(int codepoint, GlyphEntry entry) {
        UnbakedGlyph glyph = entry.glyph;
        if (glyph == null) {
            FT_Face ftFace = this.validateFontOpen();
            synchronized (ftFace) {
                glyph = entry.glyph;
                if (glyph == null) {
                    glyph = this.loadGlyph(codepoint, ftFace, entry.index);
                    entry.glyph = glyph;
                }
            }
        }
        return glyph;
    }

    private UnbakedGlyph loadGlyph(int codepoint, FT_Face ftFace, int glyphIndex) {
        int err = FreeType.FT_Load_Glyph(ftFace, glyphIndex, this.ftLoadFlags);
        if (err != 0) {
            FreeTypeUtil.assertError(err, String.format(Locale.ROOT, "Loading glyph U+%06X", codepoint));
        }

        FT_GlyphSlot slot = ftFace.glyph();
        if (slot == null) {
            throw new NullPointerException(String.format(Locale.ROOT, "Glyph U+%06X not initialized", codepoint));
        }

        float advance = FreeTypeUtil.x(slot.advance());

        FT_Bitmap bmp = slot.bitmap();
        int left = slot.bitmap_left();
        int top  = slot.bitmap_top();
        int w    = bmp.width();
        int h    = bmp.rows();

        if (w <= 0 || h <= 0) {
            return new EmptyGlyph(advance / this.oversample);
        }

        return new Glyph(left, top, w, h, advance, glyphIndex);
    }

    FT_Face validateFontOpen() {
        if (this.fontMemory != null && this.face != null) return this.face;
        throw new IllegalStateException("Provider already closed");
    }

    @Override
    public void close() {
        if (this.face != null) {
            synchronized (FreeTypeUtil.LIBRARY_LOCK) {
                FreeTypeUtil.checkError(FreeType.FT_Done_Face(this.face), "Deleting face");
            }
            this.face = null;
        }
        MemoryUtil.memFree(this.fontMemory);
        this.fontMemory = null;
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return this.glyphs.keySet();
    }

    class Glyph implements UnbakedGlyph {
        final int width;
        final int height;
        final float bearingX;
        final float bearingY;
        final GlyphInfo info;
        final int index;

        Glyph(float left, float top, int w, int h, float advance, int index) {
            this.width    = w;
            this.height   = h;
            this.bearingX = left / WannaCryTrueTypeGlyphProvider.this.oversample;
            this.bearingY = top  / WannaCryTrueTypeGlyphProvider.this.oversample;
            this.info     = GlyphInfo.simple(advance / WannaCryTrueTypeGlyphProvider.this.oversample);
            this.index    = index;
        }

        @Override
        public GlyphInfo info() {
            return this.info;
        }

        @Override
        public BakedGlyph bake(UnbakedGlyph.Stitcher stitcher) {
            return stitcher.stitch(this.info, new GlyphBitmap() {

                @Override public int   getPixelWidth()  { return Glyph.this.width; }
                @Override public int   getPixelHeight() { return Glyph.this.height; }
                @Override public float getOversample()  { return WannaCryTrueTypeGlyphProvider.this.oversample; }
                @Override public float getBearingLeft() { return Glyph.this.bearingX; }
                @Override public float getBearingTop()  { return Glyph.this.bearingY; }
                @Override public boolean isColored()    { return false; }

                @Override
                public void upload(int x, int y, GpuTexture gpuTexture) {
                    FT_Face ftFace = WannaCryTrueTypeGlyphProvider.this.validateFontOpen();
                    synchronized (ftFace) {
                        int err = FreeType.FT_Load_Glyph(ftFace, Glyph.this.index,
                                WannaCryTrueTypeGlyphProvider.this.ftLoadFlags);
                        if (err != 0) {
                            FreeTypeUtil.assertError(err, "Reloading glyph index " + Glyph.this.index);
                        }

                        FT_GlyphSlot slot = ftFace.glyph();
                        if (slot == null) {
                            throw new IllegalStateException("Glyph slot is null for index " + Glyph.this.index);
                        }

                        err = FreeType.FT_Render_Glyph(slot, WannaCryTrueTypeGlyphProvider.this.ftRenderMode);
                        if (err != 0) {
                            FreeTypeUtil.assertError(err, "Rendering glyph index " + Glyph.this.index);
                        }

                        FT_Bitmap bitmap = slot.bitmap();
                        int bw    = bitmap.width();
                        int bh    = bitmap.rows();
                        if (bw <= 0 || bh <= 0) return;

                        int pitch  = bitmap.pitch();
                        ByteBuffer src = bitmap.buffer(pitch * bh);

                        try (NativeImage img = new NativeImage(NativeImage.Format.LUMINANCE, bw, bh, false)) {
                            long dstPtr = img.getPointer();
                            for (int row = 0; row < bh; row++) {
                                long dstRow = dstPtr + (long) row * bw;
                                long srcRow = MemoryUtil.memAddress(src) + (long) row * pitch;
                                MemoryUtil.memCopy(srcRow, dstRow, bw);
                            }
                            RenderSystem.getDevice()
                                    .createCommandEncoder()
                                    .writeToTexture(gpuTexture, img, 0, 0, x, y, bw, bh, 0, 0);
                        }
                    }
                }
            });
        }
    }

    static class GlyphEntry {
        final int index;
        @Nullable volatile UnbakedGlyph glyph;

        GlyphEntry(int index) {
            this.index = index;
        }
    }
}
