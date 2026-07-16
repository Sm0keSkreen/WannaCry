package dev.mg.wannacry.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.render.vertex.WannaCryVertexFormats;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class WannaCryRenderPipelines {
    private static final List<RenderPipeline> PIPELINES = new ArrayList<>();

    public static final RenderPipeline POST_OUTLINE = add(
        RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath("wannacry", "pipeline/post/outline"))
            .withVertexFormat(WannaCryVertexFormats.POS2, VertexFormat.Mode.TRIANGLES)
            .withVertexShader(Identifier.fromNamespaceAndPath("wannacry", "shaders/post-process/base.vsh"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("wannacry", "shaders/post-process/outline.fsh"))
            .withSampler("u_Texture")
            .withUniform("PostData", UniformType.UNIFORM_BUFFER)
            .withUniform("OutlineData", UniformType.UNIFORM_BUFFER)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build()
    );

    private static RenderPipeline add(RenderPipeline pipeline) {
        PIPELINES.add(pipeline);
        return pipeline;
    }

    public static void precompile(ResourceManager resources) {
        GpuDevice device = RenderSystem.getDevice();
        if (device == null) {
            WannaCry.LOGGER.warn("[WannaCry] precompile() called before GpuDevice is ready; skipping");
            return;
        }

        for (RenderPipeline pipeline : PIPELINES) {
            device.precompilePipeline(pipeline, (identifier, stage) -> {
                Optional<net.minecraft.server.packs.resources.Resource> opt =
                        resources.getResource(identifier);
                if (opt.isEmpty()) {

                    throw new RuntimeException(
                        "[WannaCry] Shader not found in ResourceManager: " + identifier
                            + " — ensure the satellite JAR is a loaded resource pack.");
                }
                try (var in = opt.get().open()) {
                    return IOUtils.toString(in, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(
                        "[WannaCry] Failed to read shader source: " + identifier, e);
                }
            });
        }
        WannaCry.LOGGER.info("[WannaCry] Shader pipelines precompiled successfully");
    }

    private WannaCryRenderPipelines() {}
}
