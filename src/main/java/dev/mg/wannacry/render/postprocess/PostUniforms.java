package dev.mg.wannacry.render.postprocess;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.renderer.DynamicUniformStorage;

import java.nio.ByteBuffer;

public class PostUniforms {
    private static final int SIZE = new Std140SizeCalculator()
        .putVec2()
        .putFloat()
        .get();

    private static final DynamicUniformStorage<Data> STORAGE =
        new DynamicUniformStorage<>("WannaCry - Post UBO", SIZE, 16);

    private PostUniforms() {}

    public static GpuBufferSlice write(float sizeX, float sizeY, float time) {
        return STORAGE.writeUniform(new Data(sizeX, sizeY, time));
    }

    public static void flipFrame() {
        STORAGE.endFrame();
    }

    private record Data(float sizeX, float sizeY, float time)
        implements DynamicUniformStorage.DynamicUniform {
        @Override
        public void write(ByteBuffer buf) {
            Std140Builder.intoBuffer(buf)
                .putVec2(sizeX, sizeY)
                .putFloat(time);
        }
    }
}
