package dev.mg.wannacry.render.postprocess;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.mg.wannacry.render.WannaCryFullscreenQuad;
import dev.mg.wannacry.render.WannaCryRenderPipelines;
import net.minecraft.client.renderer.state.LevelRenderState;

import java.util.OptionalInt;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static dev.mg.wannacry.util.traits.Util.mc;

public class WannaCryShaderESP {
    public static final WannaCryShaderESP INSTANCE = new WannaCryShaderESP();

    public RenderTarget framebuffer;

    private WannaCryShaderESP() {}

    public void init() {
        framebuffer = new TextureTarget(
            "WannaCry ShaderESP",
            mc.getWindow().getWidth(),
            mc.getWindow().getHeight(),
            true
        );
    }

    public void beginRender() {

    }

    public void draw(LevelRenderState levelState, PoseStack matrices) {

    }

    public void drawBlocks(PoseStack matrices) {

    }

    public void render() {

    }

    public void onResized(int width, int height) {
        if (framebuffer != null) framebuffer.resize(width, height);
    }

    @Deprecated
    public void submitVertices() {}
}
