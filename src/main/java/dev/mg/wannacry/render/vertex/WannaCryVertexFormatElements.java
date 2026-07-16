package dev.mg.wannacry.render.vertex;

import com.mojang.blaze3d.vertex.VertexFormatElement;

public abstract class WannaCryVertexFormatElements {

    public static final VertexFormatElement POS2 =
        VertexFormatElement.register(nextFreeId(), 0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 2);

    private WannaCryVertexFormatElements() {}

    private static int nextFreeId() {
        int id = 0;
        while (VertexFormatElement.byId(id) != null) {
            id++;
            if (id >= 32) throw new RuntimeException("Too many mods registering VertexFormatElements");
        }
        return id;
    }
}
