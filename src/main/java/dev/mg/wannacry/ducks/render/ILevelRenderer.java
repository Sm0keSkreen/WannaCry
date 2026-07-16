package dev.mg.wannacry.ducks.render;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface ILevelRenderer {
    void satellite$pushEntityOutlineFramebuffer(RenderTarget framebuffer);
    void satellite$popEntityOutlineFramebuffer();
}
