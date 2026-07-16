package dev.mg.wannacry.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;

public class WannaCryOutlineCommandQueue extends SubmitNodeStorage {
    private int color;
    private int[] tints;

    public void setColor(int argb) {
        this.color = argb;
    }

    @Override
    public SubmitNodeCollection order(int i) {
        return submitsPerOrder.computeIfAbsent(i, key -> new OutlineBatch(this));
    }

    private class OutlineBatch extends SubmitNodeCollection {
        OutlineBatch(SubmitNodeStorage parent) {
            super(parent);
        }

        @Override
        public void submitShadow(PoseStack ps, float r,
                                 List<EntityRenderState.ShadowPiece> pieces) {}

        @Override
        public void submitNameTag(PoseStack ps, @Nullable Vec3 v, int i,
                                  Component c, boolean b, int j, double d,
                                  CameraRenderState cam) {}

        @Override
        public void submitText(PoseStack ps, float x, float y,
                               FormattedCharSequence seq, boolean ds,
                               Font.DisplayMode dm, int l, int col,
                               int bg, int ol) {}

        @Override
        public void submitFlame(PoseStack ps, EntityRenderState state,
                                Quaternionf q) {}

        @Override
        public void submitLeash(PoseStack ps,
                                EntityRenderState.LeashState ls) {}

        @Override
        public void submitMovingBlock(PoseStack ps,
                                      MovingBlockRenderState state) {}

        public void submitBreakingBlockModel(PoseStack ps,
                                             BlockStateModel m,
                                             long seed, int prog) {}

        @Override
        public void submitCustomGeometry(PoseStack ps, RenderType rt,
                                         CustomGeometryRenderer r) {}

        @Override
        public void submitParticleGroup(ParticleGroupRenderer r) {}

        @Override
        public <S> void submitModel(Model<? super S> model, S state,
                                    PoseStack matrices, RenderType layer,
                                    int light, int overlay, int tintedColor,
                                    @Nullable TextureAtlasSprite sprite,
                                    int outlineColor,
                                    @Nullable ModelFeatureRenderer.CrumblingOverlay crumble) {
            super.submitModel(model, state, matrices, layer,
                              light, overlay, color, sprite, 0, crumble);
        }

        @Override
        public void submitModelPart(ModelPart part, PoseStack matrices,
                                    RenderType layer, int light, int overlay,
                                    @Nullable TextureAtlasSprite sprite,
                                    boolean sheeted, boolean hasGlint,
                                    int tintedColor,
                                    @Nullable ModelFeatureRenderer.CrumblingOverlay crumble,
                                    int i) {
            super.submitModelPart(part, matrices, layer,
                                  light, overlay, sprite,
                                  sheeted, hasGlint, color, crumble, i);
        }

        @Override
        public void submitBlockModel(PoseStack ps, RenderType rt,
                                     BlockStateModel model,
                                     float r, float g, float b,
                                     int light, int overlay, int outlineColor) {
            float cr = ((color >> 16) & 0xFF) / 255f;
            float cg = ((color >> 8)  & 0xFF) / 255f;
            float cb = ( color        & 0xFF) / 255f;
            super.submitBlockModel(ps, rt, model, cr, cg, cb, light, overlay, outlineColor);
        }

        @Override
        public void submitItem(PoseStack ps, ItemDisplayContext ctx,
                               int light, int overlay, int outlineColor,
                               int[] tintLayers, List<BakedQuad> quads,
                               RenderType renderType,
                               ItemStackRenderState.FoilType foilType) {
            if (tints == null || tints[0] != color) {
                tints = new int[]{color, color, color, color};
            }
            super.submitItem(ps, ctx, light, overlay,
                             outlineColor, tints, quads, renderType, foilType);
        }
    }
}
