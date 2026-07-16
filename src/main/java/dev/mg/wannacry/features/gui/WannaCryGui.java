package dev.mg.wannacry.features.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.Feature;
import dev.mg.wannacry.features.gui.items.Item;
import dev.mg.wannacry.features.gui.items.buttons.ModuleButton;
import dev.mg.wannacry.util.render.ScissorUtil;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.util.render.RenderUtil;
import dev.mg.wannacry.util.traits.Jsonable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WannaCryGui extends Screen implements Jsonable {

    private static WannaCryGui INSTANCE;
    private static Color colorClipboard = null;

    static { INSTANCE = new WannaCryGui(); }

    private final ArrayList<Widget>     widgets    = new ArrayList<>();
    private final List<Module.Category> categories = new ArrayList<>();

    private int boxX = -1, boxY = -1;

    private boolean dragging;
    private int     dragOffX, dragOffY;

    private int selectedCat = 0;

    private Module     sidePanelModule  = null;
    private List<Item> sidePanelItems   = null;
    private float      sidePanelScrollY = 0f;

    private float bgAlpha      = 0f;
    private long  lastRenderNs = System.nanoTime();

    private static final int TITLE_H  = 20;
    private static final int PAD      =  6;
    private static final int TAB_PAD  =  3;
    private static final int TAB_H    = 13;
    private static final int TAB_V_PAD = 2;

    public WannaCryGui() {
        super(Component.literal("WannaCry"));
        INSTANCE = this;
        load();
    }

    private void load() {
        for (Module.Category cat : WannaCry.moduleManager.getCategories()) {
            if (cat == Module.Category.HUD) continue;
            categories.add(cat);
            Widget panel = new Widget(cat.getName(), 0, 0, true);
            WannaCry.moduleManager.stream()
                    .filter(m -> m.getCategory() == cat && !m.hidden)
                    .map(ModuleButton::new)
                    .forEach(panel::addButton);
            widgets.add(panel);
        }
        widgets.forEach(w -> w.getItems().sort(Comparator.comparing(Feature::getName)));
    }

    @Override
    protected void init() {
        bgAlpha      = 0f;
        lastRenderNs = System.nanoTime();
        if (boxX < 0) {
            boxX = (this.width  - boxW()) / 2;
            boxY = (this.height - boxH()) / 2;
        }
    }

    private int boxW() {
        ClickGuiModule g = ClickGuiModule.getInstance();
        return g != null ? g.boxWidth.getValue() : 340;
    }
    private int boxH() {
        ClickGuiModule g = ClickGuiModule.getInstance();
        return g != null ? g.boxHeight.getValue() : 230;
    }
    private int catW() {
        ClickGuiModule g = ClickGuiModule.getInstance();
        return g != null ? g.catPanelWidth.getValue() : 78;
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        if (gui != null) gui.tickHue();

        long now = System.nanoTime();
        float dt = Math.min((now - lastRenderNs) / 1_000_000_000f, 0.1f);
        lastRenderNs = now;
        float targetAlpha = gui != null ? gui.bgAlphaMax.getValue() : 110f;
        bgAlpha += (targetAlpha - bgAlpha) * Math.min(1f, dt * 14f);
        if (bgAlpha > targetAlpha - 0.5f) bgAlpha = targetAlpha;
        ctx.fill(0, 0, ctx.guiWidth(), ctx.guiHeight(),
                new Color(0, 0, 0, (int) bgAlpha).getRGB());

        Item.context = ctx;

        if (dragging) {
            boxX = mouseX + dragOffX;
            boxY = mouseY + dragOffY;
        }

        int bx = boxX, by = boxY, bw = boxW(), bh = boxH(), cw = catW();
        int cr  = gui != null ? gui.cornerRadius.getValue() : 0;
        int divX = bx + cw;

        if (gui != null && gui.dropShadow.getValue()) {
            int sr = gui.shadowRadius.getValue();
            int sa = gui.shadowAlpha.getValue();
            for (int i = sr; i >= 1; i--) {
                float t = 1f - (float)(i - 1) / sr;
                ctx.fill(bx - i, by - i, bx + bw + i, by + bh + i,
                        new Color(0, 0, 0, clamp((int)(sa * t * t))).getRGB());
            }
        }

        if (gui != null && gui.glowEffect.getValue()) {
            int gr = gui.glowRadius.getValue();
            Color gc = gui.accentGlow.getValue()
                    ? gui.getGradientColor1() : gui.glowColor.getValue();
            for (int i = gr; i >= 1; i--) {
                float t = 1f - (float)(i - 1) / gr;
                ctx.fill(bx - i, by - i, bx + bw + i, by + bh + i,
                        new Color(gc.getRed(), gc.getGreen(), gc.getBlue(),
                                  clamp((int)(gc.getAlpha() * t * t))).getRGB());
            }
        }

        Color panelBgColor = WannaCry.colorManager.getPanelBg();
        int mainBg = new Color(panelBgColor.getRed(), panelBgColor.getGreen(),
                               panelBgColor.getBlue(), 245).getRGB();
        ClickGuiModule.filledRoundRect(ctx, bx, by, bx + bw, by + bh, mainBg, cr);

        if (gui != null && gui.showBorder.getValue()) {
            Color c1 = gui.getGradientColor1();
            int bc = new Color(c1.getRed(), c1.getGreen(), c1.getBlue(),
                    gui.borderAlpha.getValue()).getRGB();
            ClickGuiModule.filledRoundRect(ctx,
                    bx - 1, by - 1, bx + bw + 1, by + bh + 1, bc, cr);
            ClickGuiModule.filledRoundRect(ctx, bx, by, bx + bw, by + bh, mainBg, cr);
        }

        renderTitleBar(ctx, gui, bx, by, bw, cr, mouseX, mouseY);

        if (gui != null && gui.accentBar.getValue()) {
            Color c1 = gui.getGradientColor1();
            Color c2 = gui.gradientEnabled.getValue() ? gui.getGradientColor2() : c1;
            RenderUtil.gradient(ctx,
                    bx, by + TITLE_H - 1, bx + bw, by + TITLE_H,
                    withAlpha(c1, 220), withAlpha(c1, 220),
                    withAlpha(c2, 220), withAlpha(c2, 220));
        }

        Color leftBgColor = WannaCry.colorManager.getPanelBg();
        int leftBg = new Color(
                Math.max(0, leftBgColor.getRed()   - 5),
                Math.max(0, leftBgColor.getGreen() - 5),
                Math.max(0, leftBgColor.getBlue()  - 5), 255).getRGB();
        ctx.fill(bx, by + TITLE_H, bx + cw, by + bh, leftBg);

        Color rightBgColor = WannaCry.colorManager.getSettingsBg();
        int rightBg = new Color(rightBgColor.getRed(), rightBgColor.getGreen(),
                                rightBgColor.getBlue(), 245).getRGB();
        ctx.fill(divX + 1, by + TITLE_H, bx + bw, by + bh, rightBg);

        ctx.fill(divX, by + TITLE_H, divX + 1, by + bh,
                new Color(255, 255, 255, 12).getRGB());

        if (selectedCat >= 0 && selectedCat < widgets.size()) {
            int mx = bx + 2;
            int my = by + TITLE_H + 2;
            int mw = cw - 4;
            int mh = bh - TITLE_H - 4;
            widgets.get(selectedCat).drawEmbedded(ctx, mx, my, mw, mh, mouseX, mouseY, delta);
        }

        if (sidePanelModule != null && sidePanelItems != null) {
            int sx = divX + 2;
            int sy = by + TITLE_H + 2;
            int sw = bw - cw - 4;
            int sh = bh - TITLE_H - 4;
            renderSettingsPanel(ctx, gui, sx, sy, sw, sh, mouseX, mouseY);
        } else {

            renderEmptySettingsHint(ctx, gui, divX + 1, by + TITLE_H, bw - cw - 1, bh - TITLE_H);
        }
    }

    private void renderTitleBar(GuiGraphics ctx, ClickGuiModule gui,
                                 int bx, int by, int bw, int cr,
                                 int mouseX, int mouseY) {

        Color titleBgColor = WannaCry.colorManager.getDarkBg();
        int titleBgInt = new Color(titleBgColor.getRed(), titleBgColor.getGreen(),
                                   titleBgColor.getBlue(), 255).getRGB();
        ClickGuiModule.filledRoundRect(ctx, bx, by, bx + bw, by + TITLE_H, titleBgInt, cr);

        net.minecraft.client.gui.Font font = WannaCry.fontService.getFont();
        float scale = gui != null ? gui.headerTextSize.getValue() : 1.0f;
        Color accent = gui != null ? gui.getGradientColor1() : WannaCry.colorManager.getAccent();

        int dotX = bx + PAD;
        int dotY = by + TITLE_H / 2 - 2;
        ctx.fill(dotX, dotY, dotX + 4, dotY + 4,
                new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 220).getRGB());

        drawScaled(ctx, font, "WannaCry",
                bx + PAD + 7, by + TITLE_H / 2f - 4,
                0xFFFFFFFF, scale);

        int tabW    = computeTabWidth(font, scale);
        int totalW  = categories.size() * tabW + (categories.size() - 1) * TAB_PAD;
        int tabsX   = bx + bw - totalW - PAD;
        int tabY    = by + (TITLE_H - TAB_H) / 2;

        for (int i = 0; i < categories.size(); i++) {
            int tx  = tabsX + i * (tabW + TAB_PAD);
            int tx2 = tx + tabW;
            int ty2 = tabY + TAB_H;

            boolean sel     = (i == selectedCat);
            boolean hovered = mouseX >= tx && mouseX <= tx2 && mouseY >= tabY && mouseY <= ty2;

            int r = cr > 0 ? cr : 3;

            if (sel) {

                int activeColor = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 210).getRGB();
                ClickGuiModule.filledRoundRect(ctx, tx, tabY, tx2, ty2, activeColor, r);
            } else {

                int inactiveColor = hovered
                        ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60).getRGB()
                        : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22).getRGB();
                ClickGuiModule.filledRoundRect(ctx, tx, tabY, tx2, ty2, inactiveColor, r);
            }

            String label = categories.get(i).getName();
            int lw = font.width(label);
            int textCol;
            if (sel) {
                textCol = 0xFFFFFFFF;
            } else if (hovered) {
                textCol = new Color(220, 220, 235, 220).getRGB();
            } else {
                Color muted = WannaCry.colorManager.getMutedAccent();
                textCol = new Color(muted.getRed(), muted.getGreen(), muted.getBlue(), 170).getRGB();
            }
            float labelScale = Math.min(scale, 0.85f);
            drawScaled(ctx, font, label,
                    tx + (tabW - (int)(lw * labelScale)) / 2f,
                    tabY + TAB_V_PAD + (TAB_H - TAB_V_PAD * 2) / 2f - 4,
                    textCol, labelScale);
        }
    }

    private int computeTabWidth(net.minecraft.client.gui.Font font, float scale) {
        int maxW = 0;
        for (Module.Category c : categories) maxW = Math.max(maxW, font.width(c.getName()));
        return (int)(maxW * Math.min(scale, 0.85f)) + 10;
    }

    private void renderSettingsPanel(GuiGraphics ctx, ClickGuiModule gui,
                                      int sx, int sy, int sw, int sh,
                                      int mouseX, int mouseY) {
        net.minecraft.client.gui.Font font = WannaCry.fontService.getFont();
        float scale = gui != null ? gui.headerTextSize.getValue() : 1.0f;
        Color accent = gui != null ? gui.getGradientColor1() : WannaCry.colorManager.getAccent();

        int headerH = 16;

        int headerBg = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30).getRGB();
        ctx.fill(sx, sy, sx + sw, sy + headerH, headerBg);

        ctx.fill(sx, sy, sx + 2, sy + headerH,
                new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 230).getRGB());

        drawScaled(ctx, font, sidePanelModule.getName(),
                sx + 6, sy + headerH / 2f - 4, 0xFFFFFFFF, scale * 0.95f);

        ctx.fill(sx, sy + headerH, sx + sw, sy + headerH + 1,
                new Color(255, 255, 255, 14).getRGB());

        int contentY = sy + headerH + 2;
        int contentH = sh - headerH - 2;

        float totalH = 0f;
        for (Item item : sidePanelItems) {
            item.update();
            if (!item.isHidden()) totalH += item.getHeight() + 2;
        }
        float maxScroll = Math.max(0f, totalH - contentH);
        if (sidePanelScrollY > maxScroll) sidePanelScrollY = maxScroll;
        if (sidePanelScrollY < 0f)        sidePanelScrollY = 0f;

        ScissorUtil.enable(ctx, sx, contentY, sx + sw, sy + sh);

        float iy = contentY - sidePanelScrollY;
        for (Item item : sidePanelItems) {
            if (item.isHidden()) continue;
            float ih = item.getHeight();
            if (iy + ih < contentY - 1f) { iy += ih + 2f; continue; }
            if (iy > sy + sh)             break;
            item.setLocation(sx + 1f, iy);
            item.setWidth(sw - 2);
            item.drawScreen(ctx, mouseX, mouseY, 0f);
            iy += ih + 2f;
        }

        ScissorUtil.disable(ctx);

        if (totalH > contentH + 1f) {
            float fraction   = (float) contentH / totalH;
            float thumbH     = Math.max(14f, contentH * fraction);
            float scrollFrac = sidePanelScrollY / Math.max(1f, totalH - contentH);
            int   barX = sx + sw - 2;
            int   barY = contentY + (int)((contentH - thumbH) * scrollFrac);
            int   scrollbarCol = new Color(accent.getRed(), accent.getGreen(),
                                           accent.getBlue(), 80).getRGB();
            ctx.fill(barX, barY, barX + 2, barY + (int) thumbH, scrollbarCol);
        }
    }

    private void renderEmptySettingsHint(GuiGraphics ctx, ClickGuiModule gui,
                                          int rx, int ry, int rw, int rh) {
        net.minecraft.client.gui.Font font = WannaCry.fontService.getFont();
        Color muted = WannaCry.colorManager.getMutedAccent();
        int hintCol = new Color(muted.getRed(), muted.getGreen(), muted.getBlue(), 90).getRGB();
        String hint = "Right-click a module";
        int tw = font.width(hint);
        ctx.drawString(font, hint,
                rx + (rw - tw) / 2,
                ry + rh / 2 - 4,
                hintCol);
    }

    private void drawScaled(GuiGraphics ctx, net.minecraft.client.gui.Font font,
                             String text, float tx, float ty, int col, float scale) {
        if (Math.abs(scale - 1f) < 0.01f) {
            ctx.drawString(font, text, (int) tx, (int) ty, col);
        } else {
            var ps = ctx.pose();
            ps.pushMatrix();
            ps.translate(tx, ty);
            ps.scale(scale, scale);
            ctx.drawString(font, text, 0, 0, col);
            ps.popMatrix();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        int mx = (int) click.x(), my = (int) click.y(), btn = click.button();
        int bx = boxX, by = boxY, bw = boxW(), bh = boxH(), cw = catW();
        int divX = bx + cw;

        if (btn == 0 && mx >= bx && mx <= bx + bw && my >= by && my < by + TITLE_H) {
            net.minecraft.client.gui.Font font = WannaCry.fontService.getFont();
            float scale = ClickGuiModule.getInstance() != null
                    ? ClickGuiModule.getInstance().headerTextSize.getValue() : 1.0f;
            int tabW    = computeTabWidth(font, scale);
            int totalW  = categories.size() * tabW + (categories.size() - 1) * TAB_PAD;
            int tabsX   = bx + bw - totalW - PAD;
            int tabY    = by + (TITLE_H - TAB_H) / 2;
            boolean hitTab = false;

            for (int i = 0; i < categories.size(); i++) {
                int tx = tabsX + i * (tabW + TAB_PAD);
                if (mx >= tx && mx <= tx + tabW && my >= tabY && my <= tabY + TAB_H) {
                    if (selectedCat != i) closeSidePanel();
                    selectedCat = i;
                    hitTab = true;
                    break;
                }
            }

            if (!hitTab) {
                dragging = true;
                dragOffX = bx - mx;
                dragOffY = by - my;
            }
            return true;
        }

        if (mx >= bx && mx <= divX && my >= by + TITLE_H && my <= by + bh) {
            if (selectedCat >= 0 && selectedCat < widgets.size()) {
                widgets.get(selectedCat).embeddedMouseClicked(mx, my, btn);
            }
            return true;
        }

        if (mx >= divX && mx <= bx + bw && my >= by + TITLE_H && my <= by + bh) {
            if (sidePanelItems != null) {
                for (Item item : sidePanelItems) {
                    if (!item.isHidden()) item.mouseClicked(mx, my, btn);
                }
            }
            return true;
        }

        if (btn == 0) closeSidePanel();
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == 0) dragging = false;
        if (selectedCat >= 0 && selectedCat < widgets.size()) {
            widgets.get(selectedCat).embeddedMouseReleased(
                    (int) click.x(), (int) click.y(), click.button());
        }
        if (sidePanelItems != null) {
            for (Item item : sidePanelItems) {
                if (!item.isHidden()) item.mouseReleased(
                        (int) click.x(), (int) click.y(), click.button());
            }
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        int bx = boxX, by = boxY, bw = boxW(), bh = boxH(), cw = catW();
        int divX = bx + cw;

        if (mouseX >= bx && mouseX <= divX
                && mouseY >= by + TITLE_H && mouseY <= by + bh) {
            if (selectedCat >= 0 && selectedCat < widgets.size()) {
                widgets.get(selectedCat).scroll((float) v * 10f);
            }
        }

        if (mouseX >= divX && mouseX <= bx + bw
                && mouseY >= by + TITLE_H && mouseY <= by + bh) {
            sidePanelScrollY = Math.max(0f, sidePanelScrollY - (float) v * 10f);
        }

        return super.mouseScrolled(mouseX, mouseY, h, v);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (selectedCat >= 0 && selectedCat < widgets.size())
            widgets.get(selectedCat).onKeyPressed(input.input());
        if (sidePanelItems != null) {
            for (Item item : sidePanelItems) {
                if (!item.isHidden()) item.onKeyPressed(input.input());
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (selectedCat >= 0 && selectedCat < widgets.size())
            widgets.get(selectedCat).onKeyTyped(input.codepointAsString(), input.modifiers());
        if (sidePanelItems != null) {
            for (Item item : sidePanelItems) {
                if (!item.isHidden()) item.onKeyTyped(input.codepointAsString(), input.modifiers());
            }
        }
        return super.charTyped(input);
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void renderBackground(GuiGraphics ctx, int mouseX, int mouseY, float delta) {

    }

    @Override
    public void onClose() {
        super.onClose();
        closeSidePanel();
        WannaCry.configManager.save();
    }

    @Override public String getFileName() { return "gui.json"; }

    @Override
    public JsonElement toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("boxX",        boxX);
        root.addProperty("boxY",        boxY);
        root.addProperty("selectedCat", selectedCat);
        return root;
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element == null || !element.isJsonObject()) return;
        JsonObject o = element.getAsJsonObject();
        if (o.has("boxX"))        boxX        = o.get("boxX").getAsInt();
        if (o.has("boxY"))        boxY        = o.get("boxY").getAsInt();
        if (o.has("selectedCat")) selectedCat = o.get("selectedCat").getAsInt();
    }

    public void openSidePanel(Module module, List<Item> items) {
        this.sidePanelModule  = module;
        this.sidePanelItems   = items;
        this.sidePanelScrollY = 0f;
    }

    public void closeSidePanel() {
        this.sidePanelModule  = null;
        this.sidePanelItems   = null;
        this.sidePanelScrollY = 0f;
    }

    public Module getSidePanelModule() { return sidePanelModule; }

    public static WannaCryGui getInstance() {
        if (INSTANCE == null) INSTANCE = new WannaCryGui();
        return INSTANCE;
    }

    public static WannaCryGui getClickGui()            { return getInstance(); }
    public final ArrayList<Widget> getComponents()      { return widgets; }
    public int getTextOffset()                          { return -6; }
    public static Color getColorClipboard()             { return colorClipboard; }
    public static void setColorClipboard(Color color)   { colorClipboard = color; }

    private static int withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), clamp(alpha)).getRGB();
    }
    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
