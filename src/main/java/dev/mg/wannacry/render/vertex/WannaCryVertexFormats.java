package dev.mg.wannacry.render.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;

public abstract class WannaCryVertexFormats {

    public static final VertexFormat POS2 = VertexFormat.builder()
        .add("Position", WannaCryVertexFormatElements.POS2)
        .build();

    private WannaCryVertexFormats() {}
}
