package dev.mg.wannacry.features.gui;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.features.settings.ItemWhitelist;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.ColorUtil;
import dev.mg.wannacry.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WhitelistScreen extends Screen {

    private static final int PANEL_W      = 260;
    private static final int PANEL_PAD    = 8;
    private static final int HEADER_H     = 14;
    private static final int INPUT_H      = 18;
    private static final int ENTRY_H      = 18;
    private static final int ENTRY_GAP    = 2;
    private static final int MAX_LISTED   = 6;
    private static final int MAX_RESULTS  = 7;
    private static final int BTN_W        = 22;

    private final Setting<String> setting;
    private final ItemWhitelist   whitelist;
    private final Screen          parent;

    private String  searchText    = "";
    private boolean searchFocused = false;
    private int     listScroll    = 0;
    private int     resultScroll  = 0;

    private List<String> cachedResults = new ArrayList<>();
    private String       lastQuery     = null;

    private long    cursorNanos   = System.nanoTime();
    private boolean cursorVisible = true;

    public WhitelistScreen(Setting<String> setting, ItemWhitelist whitelist, Screen parent) {
        super(Component.literal("satellite-whitelist"));
        this.setting   = setting;
        this.whitelist = whitelist;
        this.parent    = parent;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void renderBackground(GuiGraphics ctx, int mx, int my, float delta) {

    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private Color accentColor1() {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        return gui != null ? gui.getGradientColor1() : new Color(94, 94, 154, 255);
    }

    private Color accentColor2() {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        if (gui == null) return accentColor1();
        return gui.gradientEnabled.getValue() ? gui.getGradientColor2() : accentColor1();
    }

    private static int withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(),
                Math.max(0, Math.min(255, alpha))).getRGB();
    }

    private void drawHeader(GuiGraphics ctx, int x1, int y1, int x2, int y2) {
        ClickGuiModule gui = ClickGuiModule.getInstance();
        if (gui == null || !gui.gradientEnabled.getValue()) {
            int col = gui != null ? gui.topColor.getValue().getRGB()
                                  : new Color(94, 94, 154, 220).getRGB();
            ctx.fill(x1, y1, x2, y2, col);
            return;
        }
        Color c1 = gui.getGradientColor1();
        Color c2 = gui.getGradientColor2();
        int   topAlpha = gui.topColor.getValue().getAlpha();
        int   c1a = withAlpha(c1, topAlpha);
        int   c2a = withAlpha(c2, topAlpha);
        ClickGuiModule.GradientDir dir = gui.gradientDir.getValue();
        if (dir == ClickGuiModule.GradientDir.HORIZONTAL) {
            RenderUtil.gradient(ctx, x1, y1, x2, y2, c1a, c1a, c2a, c2a);
        } else if (dir == ClickGuiModule.GradientDir.VERTICAL) {
            RenderUtil.gradient(ctx, x1, y1, x2, y2, c1a, c2a, c2a, c1a);
        } else {
            int mid = withAlpha(ColorUtil.lerp(c1, c2, 0.5f), topAlpha);
            RenderUtil.gradient(ctx, x1, y1, x2, y2, c1a, mid, c2a, mid);
        }
    }

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        Minecraft mc  = Minecraft.getInstance();
        ClickGuiModule gui = ClickGuiModule.getInstance();
        int sw = ctx.guiWidth();
        int sh = ctx.guiHeight();

        if (gui != null) gui.tickHue();

        if (!searchText.equals(lastQuery)) {
            cachedResults = ItemWhitelist.searchItems(searchText);
            lastQuery     = searchText;
            resultScroll  = 0;
        }

        List<String> listed = new ArrayList<>(whitelist.getIds());

        int listedCount  = Math.min(listed.size(),        MAX_LISTED);
        int resultCount  = Math.min(cachedResults.size(), MAX_RESULTS);
        int listedH      = listedCount  * (ENTRY_H + ENTRY_GAP);
        int resultsH     = resultCount  * (ENTRY_H + ENTRY_GAP);

        int panelH = HEADER_H + 2 + 6
                   + INPUT_H + 4 + 1 + 6
                   + 12 + Math.max(listedH, ENTRY_H) + 4
                   + 1 + 6
                   + 12 + Math.max(resultsH, ENTRY_H) + 4
                   + 12 + 6;

        int panelX = (sw - PANEL_W) / 2;
        int panelY = Math.max(10, (sh - panelH) / 2 - 10);

        Color ac1 = accentColor1();
        Color ac2 = accentColor2();
        int bodyAlpha = gui != null ? gui.panelBodyAlpha.getValue() : 0x77;
        int dimAlpha  = gui != null ? gui.bgAlphaMax.getValue()    : 120;

        ctx.fill(0, 0, sw, sh, new Color(0, 0, 0, dimAlpha).getRGB());

        int hx1 = panelX - 1, hy1 = panelY - 1;
        int hx2 = panelX + PANEL_W + 1, hy2 = panelY + HEADER_H;
        drawHeader(ctx, hx1, hy1, hx2, hy2);
        String title = "Item Whitelist: " + setting.getName()
                + "  (" + whitelist.size() + " item" + (whitelist.size() == 1 ? "" : "s") + ")";
        ctx.drawString(WannaCry.fontService.getFont(), title, panelX + PANEL_PAD,
                panelY + (HEADER_H - WannaCry.fontService.getHeight()) / 2, -1, true);

        if (gui == null || gui.accentBar.getValue()) {
            RenderUtil.gradient(ctx, hx1, hy2, hx2, hy2 + 2,
                    withAlpha(ac1, 255), withAlpha(ac1, 255),
                    withAlpha(ac2, 255), withAlpha(ac2, 255));
        }

        RenderUtil.rect(ctx, hx1, hy2 + 2, hx2, panelY + panelH, (bodyAlpha << 24) | 0x000000);

        if (gui != null && gui.showBorder.getValue()) {
            int ba = gui.borderAlpha.getValue();
            int bc = new Color(ac1.getRed(), ac1.getGreen(), ac1.getBlue(), ba).getRGB();
            RenderUtil.rect(ctx, hx1 - 1, hy1 - 1, hx2 + 1, panelY + panelH + 1, bc, 1f);
        }

        int cx = panelX + PANEL_PAD;
        int cy = panelY + HEADER_H + 2 + 6;

        renderSearchBox(ctx, mc, cx, cy, PANEL_W - PANEL_PAD * 2, mx, my);
        cy += INPUT_H + 4;

        RenderUtil.gradient(ctx, panelX + 5, cy, panelX + PANEL_W - 5, cy + 1,
                withAlpha(ac1, 80), withAlpha(ac1, 80),
                withAlpha(ac2, 80), withAlpha(ac2, 80));
        cy += 1 + 6;

        ctx.drawString(WannaCry.fontService.getFont(), "§7Whitelisted (" + listed.size() + ")",
                cx, cy, withAlpha(ac1, 200), false);
        cy += 12;

        if (listed.isEmpty()) {
            int emW = WannaCry.fontService.getWidth("Nothing whitelisted");
            ctx.drawString(WannaCry.fontService.getFont(), "Nothing whitelisted",
                    panelX + (PANEL_W - emW) / 2, cy + 1, 0xFF555566, false);
            cy += ENTRY_H + ENTRY_GAP;
        } else {
            int start = clamp(listScroll, 0, Math.max(0, listed.size() - MAX_LISTED));
            int end   = Math.min(listed.size(), start + MAX_LISTED);
            for (int i = start; i < end; i++) {
                String id   = listed.get(i);
                String name = ItemWhitelist.displayName(id);
                boolean row = hoverRow(mx, my, cx, cy);
                boolean btn = hoverBtnRight(mx, my, cy);

                RenderUtil.rect(ctx, cx, cy, cx + PANEL_W - PANEL_PAD * 2, cy + ENTRY_H,
                        row ? 0x44337733 : 0x22224422);
                RenderUtil.rect(ctx, cx, cy, cx + PANEL_W - PANEL_PAD * 2, cy + ENTRY_H,
                        withAlpha(ac1, 30), 1f);

                int bx = cx + PANEL_W - PANEL_PAD * 2 - BTN_W;
                RenderUtil.rect(ctx, bx, cy, bx + BTN_W, cy + ENTRY_H,
                        btn ? 0xAA882222 : 0x66441111);
                int mW = WannaCry.fontService.getWidth("−");
                ctx.drawString(WannaCry.fontService.getFont(), "−", bx + (BTN_W - mW) / 2,
                        cy + (ENTRY_H - WannaCry.fontService.getHeight()) / 2, 0xFFFF5555, false);

                int iconX = cx + 2;
                int iconY = cy + (ENTRY_H - 16) / 2;
                int textOffsetX = 4;
                if (!ItemWhitelist.FIST_ID.equals(id)) {
                    Item icon = ItemWhitelist.idToItem(id);
                    if (icon != null && icon != Items.AIR) {
                        ctx.renderItem(new ItemStack(icon), iconX, iconY);
                        textOffsetX = 20;
                    }
                }

                String disp = WannaCry.fontService.getFont().plainSubstrByWidth(name, bx - cx - textOffsetX - 2);
                ctx.drawString(WannaCry.fontService.getFont(), disp, cx + textOffsetX,
                        cy + (ENTRY_H - WannaCry.fontService.getHeight()) / 2, 0xFFAAFFAA, false);
                cy += ENTRY_H + ENTRY_GAP;
            }
            if (listed.size() > MAX_LISTED) {
                String scroll = "↑↓  " + (start + 1) + "–" + end + " / " + listed.size();
                ctx.drawString(WannaCry.fontService.getFont(), scroll, cx, cy, 0xFF666688, false);
                cy += WannaCry.fontService.getHeight() + 2;
            }
        }
        cy += 4;

        RenderUtil.gradient(ctx, panelX + 5, cy, panelX + PANEL_W - 5, cy + 1,
                withAlpha(ac1, 80), withAlpha(ac1, 80),
                withAlpha(ac2, 80), withAlpha(ac2, 80));
        cy += 1 + 6;

        ctx.drawString(WannaCry.fontService.getFont(), "§7Add item" + (cachedResults.isEmpty() ? " (no results)" : ""),
                cx, cy, withAlpha(ac1, 200), false);
        cy += 12;

        if (cachedResults.isEmpty() && !searchText.isEmpty()) {
            int nrW = WannaCry.fontService.getWidth("No results.");
            ctx.drawString(WannaCry.fontService.getFont(), "No results.", panelX + (PANEL_W - nrW) / 2, cy + 1, 0xFF555566, false);
            cy += ENTRY_H + ENTRY_GAP;
        } else {
            int rStart = clamp(resultScroll, 0, Math.max(0, cachedResults.size() - MAX_RESULTS));
            int rEnd   = Math.min(cachedResults.size(), rStart + MAX_RESULTS);
            for (int i = rStart; i < rEnd; i++) {
                String id    = cachedResults.get(i);
                String name  = ItemWhitelist.displayName(id);
                boolean inWl = whitelist.contains(id);
                boolean row  = hoverRow(mx, my, cx, cy);
                boolean btn  = hoverBtnRight(mx, my, cy);

                RenderUtil.rect(ctx, cx, cy, cx + PANEL_W - PANEL_PAD * 2, cy + ENTRY_H,
                        row ? (inWl ? 0x33335533 : 0x33333366) : 0x22222244);
                RenderUtil.rect(ctx, cx, cy, cx + PANEL_W - PANEL_PAD * 2, cy + ENTRY_H,
                        withAlpha(ac1, 22), 1f);

                int bx = cx + PANEL_W - PANEL_PAD * 2 - BTN_W;
                RenderUtil.rect(ctx, bx, cy, bx + BTN_W, cy + ENTRY_H,
                        inWl ? 0x55337733 : (btn ? 0x77226622 : 0x33114411));
                String sym = inWl ? "✔" : "+";
                int sW = WannaCry.fontService.getWidth(sym);
                ctx.drawString(WannaCry.fontService.getFont(), sym, bx + (BTN_W - sW) / 2,
                        cy + (ENTRY_H - WannaCry.fontService.getHeight()) / 2,
                        inWl ? 0xFF55FF55 : 0xFFAAFFAA, false);

                int iconX = cx + 2;
                int iconY = cy + (ENTRY_H - 16) / 2;
                int textOffsetX = 4;
                if (!ItemWhitelist.FIST_ID.equals(id)) {
                    Item icon = ItemWhitelist.idToItem(id);
                    if (icon != null && icon != Items.AIR) {
                        ctx.renderItem(new ItemStack(icon), iconX, iconY);
                        textOffsetX = 20;
                    }
                }

                String disp = WannaCry.fontService.getFont().plainSubstrByWidth(name, bx - cx - textOffsetX - 2);
                ctx.drawString(WannaCry.fontService.getFont(), disp, cx + textOffsetX,
                        cy + (ENTRY_H - WannaCry.fontService.getHeight()) / 2,
                        inWl ? 0xFF888888 : 0xFFFFFFFF, false);
                cy += ENTRY_H + ENTRY_GAP;
            }
            if (cachedResults.size() > MAX_RESULTS) {
                String scroll = "↑↓  " + (rStart + 1) + "–" + rEnd + " / " + cachedResults.size();
                ctx.drawString(WannaCry.fontService.getFont(), scroll, cx, cy, 0xFF666688, false);
                cy += WannaCry.fontService.getHeight() + 2;
            }
        }
        cy += 4 + 4;

        String hint = "ESC to close  ·  scroll to browse";
        int hintW = WannaCry.fontService.getWidth(hint);
        ctx.drawString(WannaCry.fontService.getFont(), hint, panelX + (PANEL_W - hintW) / 2, cy, 0xFF555566, false);
    }

    private void renderSearchBox(GuiGraphics ctx, Minecraft mc, int x, int y, int w, int mx, int my) {
        Color ac = accentColor1();
        boolean hov = mx >= x && mx < x + w && my >= y && my < y + INPUT_H;
        int bgCol  = searchFocused ? new Color(ac.getRed() / 6, ac.getGreen() / 6, ac.getBlue() / 6, 200).getRGB()
                                   : new Color(20, 20, 28, 180).getRGB();
        int borCol = searchFocused ? ac.getRGB()
                   : hov            ? withAlpha(ac, 160)
                                    : new Color(60, 60, 80, 180).getRGB();
        RenderUtil.rect(ctx, x, y, x + w, y + INPUT_H, bgCol);
        RenderUtil.rect(ctx, x, y, x + w, y + INPUT_H, borCol, 1f);

        long now = System.nanoTime();
        if (now - cursorNanos > 500_000_000L) { cursorVisible = !cursorVisible; cursorNanos = now; }

        int textY  = y + (INPUT_H - WannaCry.fontService.getHeight()) / 2;
        int innerW = w - 8;

        if (searchText.isEmpty() && !searchFocused) {
            ctx.drawString(WannaCry.fontService.getFont(), "§7Search items…", x + 4, textY, withAlpha(ac, 90), false);
        } else {
            String tail    = WannaCry.fontService.getFont().plainSubstrByWidth(new StringBuilder(searchText).reverse().toString(), innerW);
            String display = new StringBuilder(tail).reverse().toString();
            if (searchFocused && cursorVisible) display = display + "§7|";
            ctx.drawString(WannaCry.fontService.getFont(), display, x + 4, textY, -1, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        int mx = (int) click.x();
        int my = (int) click.y();

        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        List<String> listed = new ArrayList<>(whitelist.getIds());
        int listedCount = Math.min(listed.size(),        MAX_LISTED);
        int resultCount = Math.min(cachedResults.size(), MAX_RESULTS);
        int listedH     = listedCount * (ENTRY_H + ENTRY_GAP);
        int resultsH    = resultCount * (ENTRY_H + ENTRY_GAP);
        int panelH = HEADER_H + 2 + 6 + INPUT_H + 4 + 1 + 6
                   + 12 + Math.max(listedH, ENTRY_H) + 4 + 1 + 6
                   + 12 + Math.max(resultsH, ENTRY_H) + 4 + 12 + 6;
        int panelX = (sw - PANEL_W) / 2;
        int panelY = Math.max(10, (sh - panelH) / 2 - 10);

        int cx = panelX + PANEL_PAD;
        int cy = panelY + HEADER_H + 2 + 6;
        int rowW = PANEL_W - PANEL_PAD * 2;

        searchFocused = mx >= cx && mx < cx + rowW && my >= cy && my < cy + INPUT_H;
        cy += INPUT_H + 4 + 1 + 6;

        cy += 12;
        int start = clamp(listScroll, 0, Math.max(0, listed.size() - MAX_LISTED));
        int end   = Math.min(listed.size(), start + MAX_LISTED);
        for (int i = start; i < end; i++) {
            if (hoverBtnRight(mx, my, cy)) {
                whitelist.remove(listed.get(i));
                persist();
                return true;
            }
            cy += ENTRY_H + ENTRY_GAP;
        }
        if (listed.size() > MAX_LISTED) cy += WannaCry.fontService.getHeight() + 2;
        cy += 4 + 1 + 6;

        cy += 12;
        int rStart = clamp(resultScroll, 0, Math.max(0, cachedResults.size() - MAX_RESULTS));
        int rEnd   = Math.min(cachedResults.size(), rStart + MAX_RESULTS);
        for (int i = rStart; i < rEnd; i++) {
            if (mx >= cx && mx < cx + rowW && my >= cy && my < cy + ENTRY_H) {
                whitelist.toggle(cachedResults.get(i));
                persist();
                return true;
            }
            cy += ENTRY_H + ENTRY_GAP;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmt, double vAmt) {
        if (vAmt < 0) {
            listScroll   = Math.min(listScroll   + 1, Math.max(0, whitelist.size()        - MAX_LISTED));
            resultScroll = Math.min(resultScroll + 1, Math.max(0, cachedResults.size()    - MAX_RESULTS));
        } else if (vAmt > 0) {
            listScroll   = Math.max(0, listScroll   - 1);
            resultScroll = Math.max(0, resultScroll - 1);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        int key = input.input();
        if (key == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchText.isEmpty()) searchText = searchText.substring(0, searchText.length() - 1);
                lastQuery = null;
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (searchFocused) {
            searchText += input.codepointAsString();
            lastQuery = null;
            return true;
        }
        return false;
    }

    private boolean hoverRow(int mx, int my, int cx, int cy) {
        int rowW = PANEL_W - PANEL_PAD * 2;
        return mx >= cx && mx < cx + rowW && my >= cy && my < cy + ENTRY_H;
    }

    private boolean hoverBtnRight(int mx, int my, int cy) {
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        int panelX = (sw - PANEL_W) / 2;
        int cx = panelX + PANEL_PAD;
        int bx = cx + PANEL_W - PANEL_PAD * 2 - BTN_W;
        return mx >= bx && mx < bx + BTN_W && my >= cy && my < cy + ENTRY_H;
    }

    private void persist() {
        setting.setValue(whitelist.serialize());
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
