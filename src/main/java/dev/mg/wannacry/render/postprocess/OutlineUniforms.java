package dev.mg.wannacry.render.postprocess;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.renderer.DynamicUniformStorage;

import java.nio.ByteBuffer;

public class OutlineUniforms {
    private static final int SIZE = new Std140SizeCalculator()
        .putInt()
        .putFloat()
        .putInt()
        .putFloat()
        .get();

    private static final DynamicUniformStorage<Data> STORAGE =
        new DynamicUniformStorage<>("WannaCry - Outline UBO", SIZE, 16);

    private OutlineUniforms() {}

    public static GpuBufferSlice write(int width, float fillOpacity, int shapeMode, float glowMultiplier) {
        return STORAGE.writeUniform(new Data(width, fillOpacity, shapeMode, glowMultiplier));
    }

    public static void flipFrame() {
        STORAGE.endFrame();
    }

    private record Data(int width, float fillOpacity, int shapeMode, float glowMultiplier)
        implements DynamicUniformStorage.DynamicUniform {
        @Override
        public void write(ByteBuffer buf) {
            Std140Builder.intoBuffer(buf)
                .putInt(width)
                .putFloat(fillOpacity)
                .putInt(shapeMode)
                .putFloat(glowMultiplier);
        }
    }
}
