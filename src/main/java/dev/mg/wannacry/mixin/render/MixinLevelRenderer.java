package dev.mg.wannacry.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import dev.mg.wannacry.ducks.render.ILevelRenderer;
import dev.mg.wannacry.event.impl.render.Render3DEvent;
import dev.mg.wannacry.event.impl.render.RenderBlockOutlineEvent;
import dev.mg.wannacry.render.postprocess.WannaCryShaderESP;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.mg.wannacry.util.traits.Util.EVENT_BUS;
import static dev.mg.wannacry.util.traits.Util.mc;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer implements ILevelRenderer {

    @Shadow private RenderTarget entityOutlineTarget;
    @Shadow @Final private LevelTargetBundle targets;

    @Unique private Stack<RenderTarget> satellite$fbStack;
    @Unique private Stack<ResourceHandle<RenderTarget>> satellite$handleStack;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void satellite$init(CallbackInfo ci) {
        satellite$fbStack     = new ObjectArrayList<>();
        satellite$handleStack = new ObjectArrayList<>();
    }

    @Override
    public void satellite$pushEntityOutlineFramebuffer(RenderTarget fb) {
        satellite$fbStack.push(entityOutlineTarget);
        entityOutlineTarget = fb;

        satellite$handleStack.push(targets.entityOutline);
        targets.entityOutline = () -> fb;
    }

    @Override
    public void satellite$popEntityOutlineFramebuffer() {
        entityOutlineTarget   = satellite$fbStack.pop();
        targets.entityOutline = satellite$handleStack.pop();
    }

    @Inject(method = "renderBlockOutline", at = @At("HEAD"), cancellable = true)
    public void satellite$renderBlockOutline(
            MultiBufferSource.BufferSource bufferSource,
            PoseStack poseStack, boolean bl,
            LevelRenderState levelRenderState,
            CallbackInfo ci) {
        if (EVENT_BUS.post(new RenderBlockOutlineEvent())) {
            ci.cancel();
        }
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void satellite$render3D(
            GraphicsResourceAllocator allocator, DeltaTracker tickCounter,
            boolean renderBlockOutline, Camera camera,
            Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix,
            GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky,
            CallbackInfo ci, @Local ProfilerFiller profiler) {

        PoseStack stack = new PoseStack();
        stack.pushPose();
        stack.mulPose(Axis.XP.rotationDegrees(mc.gameRenderer.getMainCamera().xRot()));
        stack.mulPose(Axis.YP.rotationDegrees(mc.gameRenderer.getMainCamera().yRot() + 180f));

        profiler.push("satellite-render-3d");
        EVENT_BUS.post(new Render3DEvent(stack, tickCounter.getGameTimeDeltaPartialTick(true)));
        stack.popPose();
        profiler.pop();
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void satellite$beginRender(
            GraphicsResourceAllocator allocator, DeltaTracker tickCounter,
            boolean renderBlockOutline, Camera camera,
            Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix,
            GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky,
            CallbackInfo ci) {
        WannaCryShaderESP.INSTANCE.beginRender();
    }

    @Inject(method = "submitEntities", at = @At("TAIL"))
    private void satellite$submitEntities(
            PoseStack poseStack,
            LevelRenderState levelRenderState,
            SubmitNodeCollector output,
            CallbackInfo ci) {
        WannaCryShaderESP.INSTANCE.draw(levelRenderState, poseStack);
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void satellite$drawStorageBlocks(
            GraphicsResourceAllocator allocator, DeltaTracker tickCounter,
            boolean renderBlockOutline, Camera camera,
            Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix,
            GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky,
            CallbackInfo ci) {
        WannaCryShaderESP.INSTANCE.drawBlocks(new PoseStack());
    }

    @Inject(method = "resize", at = @At("HEAD"))
    private void satellite$resize(int width, int height, CallbackInfo ci) {
        WannaCryShaderESP.INSTANCE.onResized(width, height);
    }
}
