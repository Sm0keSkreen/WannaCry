package dev.mg.wannacry.features.modules.hud;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.event.impl.render.Render2DEvent;
import dev.mg.wannacry.features.modules.client.HudModule;
import dev.mg.wannacry.features.settings.Setting;
import net.minecraft.client.gui.Font;

import java.awt.*;

public class FpsHudModule extends HudModule {
    public final Setting<Color> color = color("Color", 255, 255, 255, 255);

    public FpsHudModule() {
        super("FPS", "Display current FPS", 60, 10);
    }

    @Override
    protected void render(Render2DEvent e) {
        super.render(e);

        String text = "FPS: " + mc.getFps();

        Font font = WannaCry.fontService.getFont();
        int textW = font.width(text);
        int textH = WannaCry.fontService.getHeight();

        drawTextGlow(e.getContext(), text, (int) getX(), (int) getY());

        if (gradientEnabled.getValue()) {
            drawGradientText(e.getContext(), text, (int) getX(), (int) getY());
        } else {
            WannaCry.fontService.drawText(e.getContext(), text,
                    (int) getX(), (int) getY(), color.getValue().getRGB(), false);
        }

        setWidth(textW);
        setHeight(textH);
    }
}
