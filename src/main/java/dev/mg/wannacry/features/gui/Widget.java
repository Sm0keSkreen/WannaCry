package dev.mg.wannacry.features.gui;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.Feature;
import dev.mg.wannacry.features.gui.items.Item;
import dev.mg.wannacry.features.gui.items.buttons.Button;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.util.ColorUtil;
import dev.mg.wannacry.util.render.RenderUtil;
import dev.mg.wannacry.util.render.ScissorUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import dev.mg.wannacry.util.WannaCrySounds;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Widget extends Feature {

    public static final int GRID_SIZE = 5;

    protected GuiGraphics context;
    private final List<Item> items = new ArrayList<>();
    public boolean drag;
    private int x;
    private int y;
    private int x2;
    private int y2;
    private int width;
    private int height;
    private boolean open;
    private boolean hidden = false;

    private float animHeight   = 0f;
    private long  lastRenderNanos = System.nanoTime();

    public Widget(String name, int x, int y, boolean open) {
        super(name);
        this.x = x;
        this.y = y;
        this.width  = 88;
        this.height = 18;
        this.open   = open;
        if (open) { animHeight = 9999f; }
    }

    private void drag(int mouseX, int mouseY) {
        if (!this.drag) return;
        int rawX = this.x2 + mouseX;
        int rawY = this.y2 + mouseY;
        this.x = GRID_SIZE > 0 ? Math.round((float) rawX / GRID_SIZE) * GRID_SIZE : rawX;
        this.y = GRID_SIZE > 0 ? Math.round((float) rawY / GRID_SIZE) * GRID_SIZE : rawY;
    }

    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        this.context = context;
        this.drag(mouseX, mouseY);

        ClickGuiModule gui = ClickGuiModule.getInstance();
        if (gui != null) gui.tickHue();

        long now = System.nanoTime();
        float dt = Math.min((now - lastRenderNanos) / 1_000_000_000f, 0.05f);
        lastRenderNanos = now;

        this.width = (gui != null) ? gui.panelWidth.getValue() : 88;

        float targetHeight = this.open ? this.getTotalItemHeight() + 1.0f : 0f;
        tickAnimation(gui, dt, targetHeight);
        boolean drawingItems = animHeight > 0.5f;

        int headerTop    = this.y - 1;
        int headerBottom = this.y + this.height - 6;
        float panelBottom = headerBottom + (drawingItems ? animHeight + 2 : 0);

        float itemClipBottom = panelBottom;

        if (gui != null && gui.dropShadow.getValue()) {
            drawDropShadow(context, gui, headerTop, panelBottom);
        }

        if (gui != null && gui.glowEffect.getValue()) {
            drawGlow(context, gui, headerTop, panelBottom);
        }

        drawHeader(context, mouseX, mouseY, gui, headerTop, headerBottom);

        if (gui != null && gui.accentBar.getValue()) {
            drawAccentBar(context, gui, headerBottom);
        }

        if (gui != null && gui.showBorder.getValue()) {
            int ba  = gui.borderAlpha.getValue();
            Color c1 = gui.getGradientColor1();
            int borderCol = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), ba).getRGB();
            int cr = gui.cornerRadius.getValue();
            ClickGuiModule.filledRoundRect(context,
                    this.x - 1, headerTop - 1,
                    this.x + this.width + 1, (int) panelBottom + 1,
                    borderCol, cr);
        }

        if (drawingItems) {
            int bodyAlpha = (gui != null) ? gui.panelBodyAlpha.getValue() : 0x77;
            int bodyColor = (bodyAlpha << 24) | 0x000000;
            int cr = (gui != null) ? gui.cornerRadius.getValue() : 0;
            ClickGuiModule.filledRoundRect(context,
                    this.x, headerBottom,
                    this.x + this.width, (int) panelBottom,
                    bodyColor, cr);
        }

        drawHeaderText(context, gui);

        ScissorUtil.enable(context, x, headerBottom, x + width, (int) panelBottom);

        if (drawingItems) {
            float y = (float)(this.getY() + this.getHeight()) - 3.0f;
            for (Item item : this.getItems()) {
                if (item.isHidden()) continue;

                if (y >= itemClipBottom) break;
                item.setLocation((float) this.x + 2.0f, y);
                item.setWidth(this.getWidth() - 4);
                item.drawScreen(context, mouseX, mouseY, partialTicks);
                y += (float) item.getHeight() + 2f;
            }
        }

        ScissorUtil.disable(context);
    }

    private void tickAnimation(ClickGuiModule gui, float dt, float target) {
        float speed = 18f;
        animHeight += (target - animHeight) * Math.min(1f, dt * speed);
        if (Math.abs(animHeight - target) < 0.4f) animHeight = target;
    }

    private void drawDropShadow(GuiGraphics ctx, ClickGuiModule gui,
                                int headerTop, float panelBottom) {
        int radius    = gui.shadowRadius.getValue();
        int baseAlpha = gui.shadowAlpha.getValue();
        for (int i = radius; i >= 1; i--) {

            float t = 1f - ((float)(i - 1) / radius);
            int a = (int)(baseAlpha * t * t);
            int col = new Color(0, 0, 0, clamp(a)).getRGB();
            ctx.fill(this.x - i, headerTop - i,
                     this.x + this.width + i, (int) panelBottom + i, col);
        }
    }

    private void drawGlow(GuiGraphics ctx, ClickGuiModule gui,
                          int headerTop, float panelBottom) {
        int radius = gui.glowRadius.getValue();
        Color gc;
        if (gui.accentGlow.getValue()) {
            gc = gui.getGradientColor1();
        } else {
            gc = gui.glowColor.getValue();
        }
        for (int i = radius; i >= 1; i--) {
            float t = 1f - ((float)(i - 1) / radius);
            int a = (int)(gc.getAlpha() * t * t);
            int col = new Color(gc.getRed(), gc.getGreen(), gc.getBlue(), clamp(a)).getRGB();
            ctx.fill(this.x - i, headerTop - i,
                     this.x + this.width + i, (int) panelBottom + i, col);
        }
    }

    private void drawHeader(GuiGraphics ctx, int mouseX, int mouseY,
                            ClickGuiModule gui, int y1, int y2) {
        int x1 = this.x;
        int x2 = this.x + this.width;
        int cr = (gui != null) ? gui.cornerRadius.getValue() : 0;

        if (gui == null || !gui.gradientEnabled.getValue()) {
            int col = ClickGuiModule.getInstance().topColor.getValue().getRGB();
            ClickGuiModule.filledRoundRect(ctx, x1, y1, x2, y2, col, cr);
            return;
        }

        Color c1 = gui.getGradientColor1();
        Color c2 = gui.getGradientColor2();
        int topAlpha = gui.topColor.getValue().getAlpha();
        int c1a = withAlpha(c1, topAlpha);
        int c2a = withAlpha(c2, topAlpha);

        ClickGuiModule.GradientDir dir = gui.gradientDir.getValue();
        if (dir == ClickGuiModule.GradientDir.HORIZONTAL) {
            RenderUtil.gradient(ctx, x1, y1, x2, y2, c1a, c1a, c2a, c2a);
        } else if (dir == ClickGuiModule.GradientDir.VERTICAL) {
            RenderUtil.gradient(ctx, x1, y1, x2, y2, c1a, c2a, c2a, c1a);
        } else {
            int mid = withAlpha(ColorUtil.lerp(c1, c2, 0.5f), topAlpha);
            RenderUtil.gradient(ctx, x1, y1, x2, y2, c1a, mid, c2a, mid);
        }

        if (cr > 0) {

        }
    }

    private void drawAccentBar(GuiGraphics ctx, ClickGuiModule gui, int headerBottom) {
        int x1 = this.x;
        int y1 = headerBottom - 1;
        int x2 = this.x + this.width;
        int y2 = y1 + 2;

        Color c1 = gui.getGradientColor1();
        Color c2 = gui.gradientEnabled.getValue() ? gui.getGradientColor2() : c1;
        RenderUtil.gradient(ctx, x1, y1, x2, y2,
                withAlpha(c1, 255), withAlpha(c1, 255),
                withAlpha(c2, 255), withAlpha(c2, 255));
    }

    private void drawHeaderText(GuiGraphics ctx, ClickGuiModule gui) {
        float scale = (gui != null) ? gui.headerTextSize.getValue() : 1.0f;
        float textX = (float) this.x + 3.0f;
        float textY = (float) this.y - 4.0f - (float) WannaCryGui.getClickGui().getTextOffset();

        net.minecraft.client.gui.Font font = WannaCry.fontService.getFont();
        if (Math.abs(scale - 1.0f) < 0.01f) {
            ctx.drawString(font, this.getName(), (int) textX, (int) textY, -1);
        } else {
            var ps = ctx.pose();
            ps.pushMatrix();
            ps.translate(textX, textY);
            ps.scale(scale, scale);
            ctx.drawString(font, this.getName(), 0, 0, -1);
            ps.popMatrix();
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.x2 = this.x - mouseX;
            this.y2 = this.y - mouseY;
            WannaCryGui.getClickGui().getComponents().forEach(c -> { if (c.drag) c.drag = false; });
            this.drag = true;
            return;
        }
        if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
            this.open = !this.open;

            mc.getSoundManager().play(SimpleSoundInstance.forUI(WannaCrySounds.UI_CLICK, 1f));
            return;
        }
        if (!this.open && animHeight < 0.5f) return;
        this.getItems().forEach(item -> item.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0) this.drag = false;
        if (!this.open && animHeight < 0.5f) return;
        this.getItems().forEach(item -> item.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public void onKeyTyped(String typedChar, int keyCode) {
        if (!this.open) return;
        this.getItems().forEach(item -> item.onKeyTyped(typedChar, keyCode));
    }

    public void onKeyPressed(int key) {
        if (!open) return;
        this.getItems().forEach(item -> item.onKeyPressed(key));
    }

    public void addButton(Button button) { this.items.add(button); }

    public int getX() { return this.x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return this.y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return this.width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return this.height; }
    public void setHeight(int height) { this.height = height; }
    public boolean isHidden() { return this.hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public boolean isOpen() { return this.open; }
    public final List<Item> getItems() { return this.items; }

    public boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth()
                && mouseY >= this.getY() && mouseY <= this.getY() + this.getHeight() - (this.open ? 2 : 0);
    }

    public float getTotalItemHeight() {
        float h = 0f;
        for (Item item : this.getItems()) h += (float) item.getHeight() + 2;
        return h;
    }

    private float scrollY = 0f;

    public void drawEmbedded(GuiGraphics ctx, int px, int py, int pw, int ph,
                              int mouseX, int mouseY, float delta) {
        this.context = ctx;
        this.x = px;

        ClickGuiModule gui = ClickGuiModule.getInstance();

        float totalH   = getTotalItemHeight();
        float maxScroll = Math.max(0f, totalH - ph);
        if (scrollY > maxScroll) scrollY = maxScroll;
        if (scrollY < 0f)        scrollY = 0f;

        ScissorUtil.enable(ctx, px, py, px + pw, py + ph);

        float iy = py - scrollY;
        for (Item item : this.getItems()) {
            if (item.isHidden()) continue;
            float ih = item.getHeight();

            if (iy + ih < py - 1f) { iy += ih + 2f; continue; }

            if (iy > py + ph) break;

            item.setLocation((float) px + 2f, iy);
            item.setWidth(pw - 4);
            item.drawScreen(ctx, mouseX, mouseY, delta);
            iy += ih + 2f;
        }

        ScissorUtil.disable(ctx);

        if (totalH > ph + 1f) {
            float fraction  = (float) ph / totalH;
            float thumbH    = Math.max(14f, ph * fraction);
            float scrollFrac = scrollY / Math.max(1f, totalH - ph);
            int   barX = px + pw - 2;
            int   barY = py + (int)((ph - thumbH) * scrollFrac);
            ctx.fill(barX, barY, barX + 2, barY + (int) thumbH,
                    new Color(255, 255, 255, 55).getRGB());
        }
    }

    public void scroll(float amount) {
        scrollY = Math.max(0f, scrollY - amount);
    }

    public void embeddedMouseClicked(int mouseX, int mouseY, int btn) {
        this.getItems().forEach(item -> item.mouseClicked(mouseX, mouseY, btn));
    }

    public void embeddedMouseReleased(int mouseX, int mouseY, int btn) {
        this.getItems().forEach(item -> item.mouseReleased(mouseX, mouseY, btn));
    }

    protected void drawString(String text, double x, double y, Color color) { drawString(text, x, y, color.hashCode()); }
    protected void drawString(String text, double x, double y, int color) {
        context.drawString(WannaCry.fontService.getFont(), text, (int) x, (int) y, color);
    }

    protected static int withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), clamp(alpha)).getRGB();
    }
    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
