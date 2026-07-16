package dev.mg.wannacry.features.gui;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.modules.client.ClickGuiModule;
import dev.mg.wannacry.util.ColorUtil;
import dev.mg.wannacry.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;

public class ConfigsScreen extends Screen {

    private static ConfigsScreen INSTANCE;

    private static final int PANEL_W     = 230;
    private static final int PANEL_PAD   = 8;
    private static final int INPUT_H     = 18;
    private static final int ENTRY_H     = 22;
    private static final int ENTRY_GAP   = 2;
    private static final int MAX_VISIBLE = 9;
    private static final int BTN_LOAD_W  = 36;
    private static final int BTN_DEL_W   = 30;
    private static final int BTN_GAP     = 4;
    private static final int SAVE_BTN_W  = 38;
    private static final int HEADER_H    = 14;

    private String  inputText     = "";
    private boolean inputFocused  = false;
    private int     scrollOffset  = 0;
    private String  statusMessage = "";
    private long    statusExpiry  = 0L;
    private boolean statusIsError = false;

    private long    cursorNanos   = System.nanoTime();
    private boolean cursorVisible = true;

    private ConfigsScreen() {
        super(Component.literal("satellite-configs"));
    }

    public static ConfigsScreen getInstance() {
        if (INSTANCE == null) INSTANCE = new ConfigsScreen();
        return INSTANCE;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void renderBackground(GuiGraphics ctx, int mx, int my, float delta) {

    }

    @Override
    public void onClose() { Minecraft.getInstance().setScreen(null); }

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

        int dimAlpha = gui != null ? gui.bgAlphaMax.getValue() : 120;
        ctx.fill(0, 0, sw, sh, new Color(0, 0, 0, dimAlpha).getRGB());

        List<String> configs = WannaCry.configManager.listConfigs();
        int listCount = Math.min(configs.size(), MAX_VISIBLE);
        int listH     = listCount * (ENTRY_H + ENTRY_GAP);

        int panelH = HEADER_H + 2 + 6 + INPUT_H + 10 + 1 + 6 + Math.max(listH, 12) + 8 + 10 + 6;

        int panelX = (sw - PANEL_W) / 2;
        int panelY = Math.max(10, (sh - panelH) / 2 - 20);

        Color ac1 = accentColor1();
        Color ac2 = accentColor2();

        int hx1 = panelX - 1;
        int hy1 = panelY - 1;
        int hx2 = panelX + PANEL_W + 1;
        int hy2 = panelY + HEADER_H;
        drawHeader(ctx, hx1, hy1, hx2, hy2);
        ctx.drawString(WannaCry.fontService.getFont(), "Configs",
                panelX + PANEL_PAD, panelY + (HEADER_H - WannaCry.fontService.getHeight()) / 2, -1, true);

        if (gui == null || gui.accentBar.getValue()) {
            RenderUtil.gradient(ctx,
                    hx1, hy2, hx2, hy2 + 2,
                    withAlpha(ac1, 255), withAlpha(ac1, 255),
                    withAlpha(ac2, 255), withAlpha(ac2, 255));
        }

        int bodyAlpha = gui != null ? gui.panelBodyAlpha.getValue() : 0x77;
        RenderUtil.rect(ctx, hx1, hy2 + 2, hx2, panelY + panelH, (bodyAlpha << 24) | 0x000000);

        if (gui != null && gui.showBorder.getValue()) {
            int ba = gui.borderAlpha.getValue();
            int bc = new Color(ac1.getRed(), ac1.getGreen(), ac1.getBlue(), ba).getRGB();
            RenderUtil.rect(ctx, hx1 - 1, hy1 - 1, hx2 + 1, panelY + panelH + 1, bc, 1f);
        }

        int cx = panelX + PANEL_PAD;
        int cy = panelY + HEADER_H + 2 + 6;

        int inputW = PANEL_W - PANEL_PAD * 2 - SAVE_BTN_W - BTN_GAP;
        renderInputField(ctx, mc, cx, cy, inputW, mx, my);

        int saveBtnX  = cx + inputW + BTN_GAP;
        boolean canSave   = !inputText.isBlank();
        boolean saveHover = mx >= saveBtnX && mx < saveBtnX + SAVE_BTN_W
                         && my >= cy       && my < cy + INPUT_H;
        int saveBgCol;
        if (!canSave) {
            saveBgCol = new Color(35, 35, 40, 150).getRGB();
        } else if (saveHover) {
            saveBgCol = withAlpha(ac1, 220);
        } else {
            saveBgCol = new Color(
                    Math.max(0, ac1.getRed()   - 30),
                    Math.max(0, ac1.getGreen() - 30),
                    Math.max(0, ac1.getBlue()  - 30), 170).getRGB();
        }
        RenderUtil.rect(ctx, saveBtnX, cy, saveBtnX + SAVE_BTN_W, cy + INPUT_H, saveBgCol);
        RenderUtil.rect(ctx, saveBtnX, cy, saveBtnX + SAVE_BTN_W, cy + INPUT_H,
                withAlpha(ac1, 130), 1f);
        int saveTW = WannaCry.fontService.getWidth("Save");
        ctx.drawString(WannaCry.fontService.getFont(), "Save",
                saveBtnX + (SAVE_BTN_W - saveTW) / 2, cy + (INPUT_H - WannaCry.fontService.getHeight()) / 2,
                canSave ? -1 : 0xFF444455, true);

        cy += INPUT_H + 10;

        RenderUtil.gradient(ctx,
                panelX + 5, cy, panelX + PANEL_W - 5, cy + 1,
                withAlpha(ac1, 100), withAlpha(ac1, 100),
                withAlpha(ac2, 100), withAlpha(ac2, 100));
        cy += 1 + 6;

        if (configs.isEmpty()) {
            int emptyTW = WannaCry.fontService.getWidth("No configs saved");
            ctx.drawString(WannaCry.fontService.getFont(), "No configs saved",
                    panelX + (PANEL_W - emptyTW) / 2, cy + 1, 0xFF666688, false);
            cy += 12;
        } else {
            int btnColX  = panelX + PANEL_W - PANEL_PAD - BTN_LOAD_W - BTN_GAP - BTN_DEL_W;
            int nameMaxW = (PANEL_W - PANEL_PAD * 2) - BTN_LOAD_W - BTN_GAP - BTN_DEL_W - BTN_GAP - 4;
            int hoverB   = gui != null ? gui.hoverBrightness.getValue() : 40;

            for (int i = scrollOffset; i < Math.min(scrollOffset + MAX_VISIBLE, configs.size()); i++) {
                String cfg      = configs.get(i);
                int    ey       = cy + (i - scrollOffset) * (ENTRY_H + ENTRY_GAP);
                boolean rowHover = mx >= panelX + PANEL_PAD && mx < panelX + PANEL_W - PANEL_PAD
                                && my >= ey && my < ey + ENTRY_H;

                int add = rowHover ? hoverB : 0;
                RenderUtil.rect(ctx, panelX + PANEL_PAD, ey,
                        panelX + PANEL_W - PANEL_PAD, ey + ENTRY_H,
                        new Color(Math.min(255, add), Math.min(255, add),
                                  Math.min(255, add + 18), Math.min(255, bodyAlpha + 30)).getRGB());
                RenderUtil.rect(ctx, panelX + PANEL_PAD, ey,
                        panelX + PANEL_W - PANEL_PAD, ey + ENTRY_H, withAlpha(ac1, 55), 1f);

                ctx.drawString(WannaCry.fontService.getFont(), WannaCry.fontService.getFont().plainSubstrByWidth(cfg, nameMaxW),
                        panelX + PANEL_PAD + 4, ey + (ENTRY_H - WannaCry.fontService.getHeight()) / 2, -1, false);

                int loadX = btnColX;
                boolean lHov = mx >= loadX && mx < loadX + BTN_LOAD_W
                            && my >= ey + 2 && my < ey + ENTRY_H - 2;
                int lb = lHov ? hoverB : 0;
                RenderUtil.rect(ctx, loadX, ey + 2, loadX + BTN_LOAD_W, ey + ENTRY_H - 2,
                        new Color(Math.min(255, ac1.getRed()   / 2 + lb),
                                  Math.min(255, ac1.getGreen() / 2 + lb),
                                  Math.min(255, ac1.getBlue()  / 2 + lb),
                                  lHov ? 210 : 160).getRGB());
                RenderUtil.rect(ctx, loadX, ey + 2, loadX + BTN_LOAD_W, ey + ENTRY_H - 2,
                        withAlpha(ac1, lHov ? 200 : 130), 1f);
                int loadTW = WannaCry.fontService.getWidth("Load");
                ctx.drawString(WannaCry.fontService.getFont(), "Load",
                        loadX + (BTN_LOAD_W - loadTW) / 2,
                        ey + (ENTRY_H - WannaCry.fontService.getHeight()) / 2,
                        lHov ? -1 : 0xFFCCCCFF, false);

                int delX = loadX + BTN_LOAD_W + BTN_GAP;
                boolean dHov = mx >= delX && mx < delX + BTN_DEL_W
                            && my >= ey + 2 && my < ey + ENTRY_H - 2;
                RenderUtil.rect(ctx, delX, ey + 2, delX + BTN_DEL_W, ey + ENTRY_H - 2,
                        (dHov ? new Color(160, 40, 40, 210) : new Color(100, 20, 20, 175)).getRGB());
                RenderUtil.rect(ctx, delX, ey + 2, delX + BTN_DEL_W, ey + ENTRY_H - 2,
                        new Color(200, 60, 60, dHov ? 200 : 150).getRGB(), 1f);
                int delTW = WannaCry.fontService.getWidth("Del");
                ctx.drawString(WannaCry.fontService.getFont(), "Del",
                        delX + (BTN_DEL_W - delTW) / 2,
                        ey + (ENTRY_H - WannaCry.fontService.getHeight()) / 2,
                        dHov ? -1 : 0xFFFF8888, false);
            }

            cy += listH;

            if (configs.size() > MAX_VISIBLE) {
                int sbX   = panelX + PANEL_W - PANEL_PAD + 1;
                int maxSc = configs.size() - MAX_VISIBLE;
                float ratio = (float) scrollOffset / maxSc;
                int thumbH  = Math.max(14, listH * MAX_VISIBLE / configs.size());
                int thumbY  = cy - listH + (int) ((listH - thumbH) * ratio);
                RenderUtil.rect(ctx, sbX, cy - listH, sbX + 3, cy, withAlpha(ac1, 40));
                RenderUtil.rect(ctx, sbX, thumbY, sbX + 3, thumbY + thumbH, withAlpha(ac1, 210));
            }
        }

        cy += 8;

        if (!statusMessage.isEmpty() && System.currentTimeMillis() < statusExpiry) {
            int col = statusIsError ? 0xFFFF7070 : 0xFF70FF90;
            int stW = WannaCry.fontService.getWidth(statusMessage);
            ctx.drawString(WannaCry.fontService.getFont(), statusMessage, panelX + (PANEL_W - stW) / 2, cy, col, true);
        } else {
            String hint = "ESC to close";
            int hintW = WannaCry.fontService.getWidth(hint);
            ctx.drawString(WannaCry.fontService.getFont(), hint, panelX + (PANEL_W - hintW) / 2, cy, 0xFF666688, false);
        }
    }

    private void renderInputField(GuiGraphics ctx, Minecraft mc,
                                  int x, int y, int w, int mx, int my) {
        boolean hovered = mx >= x && mx < x + w && my >= y && my < y + INPUT_H;
        Color ac = accentColor1();

        int borderCol = inputFocused ? ac.getRGB()
                      : hovered      ? withAlpha(ac, 160)
                                     : new Color(60, 60, 80, 180).getRGB();
        int bgCol = inputFocused
                ? new Color(ac.getRed() / 6, ac.getGreen() / 6, ac.getBlue() / 6, 200).getRGB()
                : new Color(20, 20, 28, 180).getRGB();

        RenderUtil.rect(ctx, x, y, x + w, y + INPUT_H, bgCol);
        RenderUtil.rect(ctx, x, y, x + w, y + INPUT_H, borderCol, 1f);

        long now = System.nanoTime();
        if (now - cursorNanos > 500_000_000L) { cursorVisible = !cursorVisible; cursorNanos = now; }

        int textY  = y + (INPUT_H - WannaCry.fontService.getHeight()) / 2;
        int innerW = w - 8;

        if (inputText.isEmpty() && !inputFocused) {
            ctx.drawString(WannaCry.fontService.getFont(), "Config name\u2026", x + 4, textY, withAlpha(ac, 90), false);
        } else {
            String tail    = WannaCry.fontService.getFont().plainSubstrByWidth(new StringBuilder(inputText).reverse().toString(), innerW);
            String display = new StringBuilder(tail).reverse().toString();
            if (inputFocused && cursorVisible) display = display + "|";
            ctx.drawString(WannaCry.fontService.getFont(), display, x + 4, textY, -1, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        Minecraft mc = Minecraft.getInstance();
        int mx = (int) click.x();
        int my = (int) click.y();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        List<String> configs = WannaCry.configManager.listConfigs();
        int listCount = Math.min(configs.size(), MAX_VISIBLE);
        int listH     = listCount * (ENTRY_H + ENTRY_GAP);
        int panelH    = HEADER_H + 2 + 6 + INPUT_H + 10 + 1 + 6 + Math.max(listH, 12) + 8 + 10 + 6;
        int panelX    = (sw - PANEL_W) / 2;
        int panelY    = Math.max(10, (sh - panelH) / 2 - 20);

        int cx = panelX + PANEL_PAD;
        int cy = panelY + HEADER_H + 2 + 6;

        int inputW = PANEL_W - PANEL_PAD * 2 - SAVE_BTN_W - BTN_GAP;
        inputFocused = mx >= cx && mx < cx + inputW && my >= cy && my < cy + INPUT_H;

        int saveBtnX = cx + inputW + BTN_GAP;
        if (mx >= saveBtnX && mx < saveBtnX + SAVE_BTN_W && my >= cy && my < cy + INPUT_H) {
            doSave();
            return true;
        }

        cy += INPUT_H + 10 + 1 + 6;

        int btnColX = panelX + PANEL_W - PANEL_PAD - BTN_LOAD_W - BTN_GAP - BTN_DEL_W;
        for (int i = scrollOffset; i < Math.min(scrollOffset + MAX_VISIBLE, configs.size()); i++) {
            String cfg = configs.get(i);
            int    ey  = cy + (i - scrollOffset) * (ENTRY_H + ENTRY_GAP);

            int loadX = btnColX;
            if (mx >= loadX && mx < loadX + BTN_LOAD_W && my >= ey + 2 && my < ey + ENTRY_H - 2) {
                WannaCry.configManager.loadConfig(cfg);
                setStatus("Loaded: " + cfg, false);
                return true;
            }
            int delX = loadX + BTN_LOAD_W + BTN_GAP;
            if (mx >= delX && mx < delX + BTN_DEL_W && my >= ey + 2 && my < ey + ENTRY_H - 2) {
                WannaCry.configManager.deleteConfig(cfg);
                setStatus("Deleted: " + cfg, false);
                int newMax = Math.max(0, configs.size() - 1 - MAX_VISIBLE);
                if (scrollOffset > newMax) scrollOffset = newMax;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmt, double vAmt) {
        List<String> configs = WannaCry.configManager.listConfigs();
        int maxScroll = Math.max(0, configs.size() - MAX_VISIBLE);
        if (vAmt < 0) scrollOffset = Math.min(scrollOffset + 1, maxScroll);
        else if (vAmt > 0) scrollOffset = Math.max(scrollOffset - 1, 0);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        int key = input.input();
        if (key == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
        if (inputFocused) {
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (!inputText.isEmpty()) inputText = inputText.substring(0, inputText.length() - 1);
                return true;
            }
            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) { doSave(); return true; }
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (inputFocused) {
            String ch = input.codepointAsString();
            if (ch.matches("[a-zA-Z0-9 _\\-]") && inputText.length() < 48) inputText += ch;
            return true;
        }
        return false;
    }

    private void doSave() {
        String name = inputText.trim();
        if (name.isEmpty()) { setStatus("Enter a name first", true); return; }
        WannaCry.configManager.saveConfig(name);
        setStatus("Saved: " + name, false);
        inputText = ""; inputFocused = false;
    }

    private void setStatus(String msg, boolean error) {
        statusMessage = msg; statusIsError = error;
        statusExpiry = System.currentTimeMillis() + 3000L;
    }
}
