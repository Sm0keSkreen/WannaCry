package dev.mg.wannacry.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

public class CustomOutlineVertexConsumerProvider implements MultiBufferSource {
    private final MultiBufferSource.BufferSource immediate =
        MultiBufferSource.immediate(new ByteBufferBuilder(1536));

    @Override
    public VertexConsumer getBuffer(RenderType layer) {
        if (layer.isOutline()) {
            return new PassthroughVertexConsumer(immediate.getBuffer(layer));
        }
        var outline = layer.outline();
        if (outline.isPresent()) {
            return new PassthroughVertexConsumer(immediate.getBuffer(outline.get()));
        }
        return NoopVertexConsumer.INSTANCE;
    }

    public void draw() {
        immediate.endBatch();
    }

    private record PassthroughVertexConsumer(VertexConsumer delegate) implements VertexConsumer {
        @Override public VertexConsumer addVertex(float x, float y, float z) { delegate.addVertex(x, y, z); return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { delegate.setColor(r, g, b, a); return this; }
        @Override public VertexConsumer setColor(int argb) { delegate.setColor(argb); return this; }
        @Override public VertexConsumer setUv(float u, float v) { delegate.setUv(u, v); return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
        @Override public VertexConsumer setLineWidth(float width) { return this; }
    }
}
