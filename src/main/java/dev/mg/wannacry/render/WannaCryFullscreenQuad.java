package dev.mg.wannacry.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import dev.mg.wannacry.render.vertex.WannaCryVertexFormats;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WannaCryFullscreenQuad {
    public static GpuBuffer vbo;
    public static GpuBuffer ibo;

    private WannaCryFullscreenQuad() {}

    public static void init() {

        ByteBuffer vertices = ByteBuffer.allocateDirect(4 * 2 * Float.BYTES)
            .order(ByteOrder.nativeOrder());
        putVec2(vertices, -1f, -1f);
        putVec2(vertices, -1f,  1f);
        putVec2(vertices,  1f,  1f);
        putVec2(vertices,  1f, -1f);
        vertices.flip();

        ByteBuffer indices = ByteBuffer.allocateDirect(6 * Integer.BYTES)
            .order(ByteOrder.nativeOrder());
        indices.asIntBuffer().put(new int[]{0, 1, 2, 0, 2, 3});
        indices.limit(6 * Integer.BYTES);

        vbo = WannaCryVertexFormats.POS2.uploadImmediateVertexBuffer(vertices);
        ibo = WannaCryVertexFormats.POS2.uploadImmediateIndexBuffer(indices);
    }

    private static void putVec2(ByteBuffer buf, float x, float y) {
        buf.putFloat(x);
        buf.putFloat(y);
    }
}
