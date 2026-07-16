package dev.mg.wannacry.font;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public record WannaCryTrueTypeGlyphProviderDefinition(
        Identifier location,
        float size,
        float oversample,
        float shiftX,
        float shiftY,
        String skip,
        int ftLoadFlags,
        int ftRenderMode
) implements GlyphProviderDefinition {

    @Override
    public GlyphProviderType type() {
        return GlyphProviderType.TTF;
    }

    @Override
    public Either<Loader, Reference> unpack() {
        return Either.left(this::load);
    }

    private GlyphProvider load(ResourceManager resourceManager) throws IOException {
        FT_Face face = null;
        ByteBuffer buf = null;

        try {
            InputStream inputStream = resourceManager.open(this.location.withPrefix("font/"));
            try {
                buf = TextureUtil.readResource(inputStream);

                synchronized (FreeTypeUtil.LIBRARY_LOCK) {
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        PointerBuffer pb = stack.mallocPointer(1);
                        FreeTypeUtil.assertError(
                                FreeType.FT_New_Memory_Face(FreeTypeUtil.getLibrary(), buf, 0L, pb),
                                "Initializing font face");
                        face = FT_Face.create(pb.get());
                    }

                    String format = FreeType.FT_Get_Font_Format(face);
                    if (!"TrueType".equals(format) && !"CFF".equals(format) && !"OpenType".equals(format)) {
                        throw new IOException("Font is not TrueType/OpenType, was " + format);
                    }

                    FreeTypeUtil.assertError(
                            FreeType.FT_Select_Charmap(face, FreeType.FT_ENCODING_UNICODE),
                            "Find unicode charmap");

                    return new WannaCryTrueTypeGlyphProvider(
                            buf, face,
                            this.size, this.oversample,
                            this.shiftX, this.shiftY,
                            this.skip,
                            this.ftLoadFlags, this.ftRenderMode);
                }
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            synchronized (FreeTypeUtil.LIBRARY_LOCK) {
                if (face != null) FreeType.FT_Done_Face(face);
            }
            if (buf != null) MemoryUtil.memFree(buf);
            throw e;
        }
    }
}
