package dev.mg.wannacry.features.modules.client;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.event.impl.ClientEvent;
import dev.mg.wannacry.event.impl.render.Render2DEvent;
import dev.mg.wannacry.event.system.Subscribe;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationsModule extends Module {

    public enum Type {
        INFO, SUCCESS, WARNING, ERROR;

        public Color getColor() {
            return switch (this) {
                case INFO    -> null;
                case SUCCESS -> new Color( 72, 200, 120, 255);
                case WARNING -> new Color(255, 175,  50, 255);
                case ERROR   -> new Color(215,  60,  60, 255);
            };
        }

        public String getIcon() {
            return switch (this) {
                case INFO    -> "i";
                case SUCCESS -> "+";
                case WARNING -> "!";
                case ERROR   -> "x";
            };
        }

        public String getRetroPrefix() {
            return switch (this) {
                case INFO    -> "[i] ";
                case SUCCESS -> "[+] ";
                case WARNING -> "[!] ";
                case ERROR   -> "[x] ";
            };
        }
    }

    public enum Style {

        TOAST,

        MINIMAL,

        BANNER,

        OUTLINE,

        RETRO;

        float cardH() { return (this == MINIMAL || this == BANNER) ? 20f : 44f; }
    }

    public enum Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;

        public boolean isRight()  { return this == TOP_RIGHT  || this == BOTTOM_RIGHT; }
        public boolean isBottom() { return this == BOTTOM_LEFT || this == BOTTOM_RIGHT; }
    }

    private static final class Entry {
        final String title, message;
        final Type   type;
        final long   spawnMs;

        Entry(String title, String message, Type type) {
            this.title   = title;
            this.message = message;
            this.type    = type;
            this.spawnMs = System.currentTimeMillis();
        }
    }

    private static final float CARD_GAP   =  4f;
    private static final float MARGIN     =  5f;
    private static final float ACCENT_W   =  3f;
    private static final float PROGRESS_H =  2f;
    private static final float ICON_W     = 20f;

    public final Setting<Boolean> moduleToggle = bool("ModuleToggle", true);

    public final Setting<Style>  style      = mode("Style",     Style.TOAST);

    public final Setting<Corner> corner     = mode("Corner",    Corner.BOTTOM_RIGHT);

    public final Setting<Float>  duration   = num("Duration",   3500f,  500f, 12_000f);

    public final Setting<Float>  animSpeed  = num("AnimSpeed",   180f,   50f,    500f);

    public final Setting<Float>  maxToasts  = num("MaxToasts",     5f,    1f,     10f);

    public final Setting<Float>  cardWidth  = num("Width",        190f,  100f,   320f);

    public final Setting<Float>  scale      = num("Scale",         1.0f,  0.5f,    2.0f);

    public final Setting<Float>  opacity    = num("Opacity",      235f,   60f,   255f);

    public final Setting<Boolean> shadow    = bool("Shadow",      true);

    public final Setting<Boolean> showIcons = bool("Icons",       true);

    private static NotificationsModule INSTANCE;
    private final CopyOnWriteArrayList<Entry> entries = new CopyOnWriteArrayList<>();

    public NotificationsModule() {
        super("Notifications",
              "Notifications for activity on the client.",
              Category.CLIENT);
        INSTANCE = this;
    }

    public static NotificationsModule getInstance() { return INSTANCE; }

    public static void push(String title, String message, Type type) {
        NotificationsModule m = INSTANCE;
        if (m == null || !m.isEnabled()) return;
        int max = Math.max(1, m.maxToasts.getValue().intValue());
        while (m.entries.size() >= max) m.entries.remove(0);
        m.entries.add(new Entry(title, message, type));
    }

    public static void push(String title, String message) {
        push(title, message, Type.INFO);
    }

    @Subscribe
    public void onClient(ClientEvent event) {
        if (!moduleToggle.getValue()) return;
        if (event.getType() != ClientEvent.Type.TOGGLE_MODULE) return;
        if (event.getFeature() instanceof ClickGuiModule)      return;
        if (event.getFeature() instanceof NotificationsModule) return;

        boolean on = event.getFeature().isEnabled();
        push(((Module) event.getFeature()).getDisplayName(),
             on ? "Enabled" : "Disabled",
             on ? Type.SUCCESS : Type.ERROR);
    }

    @Override
    public void onDisable() { entries.clear(); }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (entries.isEmpty()) return;

        GuiGraphics ctx    = event.getContext();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        long  now      = System.currentTimeMillis();
        float anim     = animSpeed.getValue();
        float dur      = duration.getValue();
        float totalMs  = anim + dur + anim;
        float s        = scale.getValue();
        float maxAlpha = opacity.getValue();
        Corner c       = corner.getValue();
        Style  st      = style.getValue();

        float localH = st.cardH();
        float localW = (st == Style.BANNER)
                ? (screenW - 2f * MARGIN) / s
                : cardWidth.getValue();

        float Hs = localH * s;
        float Ws = localW * s;

        float yOffset = c.isBottom()
                ? screenH - MARGIN - Hs
                : MARGIN;

        List<Entry> snapshot = new ArrayList<>(entries);
        List<Entry> expired  = new ArrayList<>();

        for (int i = snapshot.size() - 1; i >= 0; i--) {
            Entry e   = snapshot.get(i);
            float age = (float)(now - e.spawnMs);

            if (age >= totalMs) { expired.add(e); continue; }

            float slide = age < anim          ? age / anim
                        : age < anim + dur    ? 1f
                        : 1f - (age - anim - dur) / anim;
            float eased = smoothstep(clamp01(slide));

            int   alpha        = (int) clamp(eased * maxAlpha, 0f, maxAlpha);
            float visibleAge   = Math.max(0f, age - anim);
            float progressFrac = clamp01(1f - visibleAge / dur);

            float xBase, yBase;
            if (st == Style.BANNER) {

                float ySlide = (1f - eased) * (Hs + MARGIN + 2f);
                xBase = MARGIN;
                yBase = c.isBottom() ? yOffset + ySlide : yOffset - ySlide;
            } else {

                float xSlide = (1f - eased) * (Ws + MARGIN + 2f);
                xBase = c.isRight()
                        ? screenW - Ws - MARGIN + xSlide
                        : MARGIN - xSlide;
                yBase = yOffset;
            }

            var pose = ctx.pose();
            pose.pushMatrix();
            pose.translate(xBase, yBase);
            pose.scale(s, s);
            drawStyled(ctx, e, localW, localH, alpha, progressFrac, c, st);
            pose.popMatrix();

            yOffset += c.isBottom() ? -(Hs + CARD_GAP * s) : (Hs + CARD_GAP * s);
        }

        entries.removeAll(expired);
    }

    private void drawStyled(GuiGraphics ctx, Entry e,
                            float W, float H,
                            int alpha, float progressFrac,
                            Corner c, Style st) {
        Color accent = e.type.getColor();
        if (accent == null) accent = WannaCry.colorManager.getAccent();

        switch (st) {
            case TOAST   -> drawToast  (ctx, e, W, H, alpha, progressFrac, c, accent);
            case MINIMAL -> drawMinimal(ctx, e, W, H, alpha, progressFrac, c, accent);
            case BANNER  -> drawBanner (ctx, e, W, H, alpha, progressFrac, c, accent);
            case OUTLINE -> drawOutline(ctx, e, W, H, alpha, progressFrac, c, accent);
            case RETRO   -> drawRetro  (ctx, e, W, H, alpha, progressFrac, c, accent);
        }
    }

    private void drawToast(GuiGraphics ctx, Entry e,
                           float W, float H, int alpha, float progressFrac,
                           Corner c, Color accent) {

        if (shadow.getValue()) {
            RenderUtil.rect(ctx, 3, 3, W + 3, H + 3, argb(0, 0, 0, (int)(alpha * 0.28f)));
        }

        Color bg = WannaCry.colorManager.getPanelBg();
        RenderUtil.rect(ctx, 0, 0, W, H, argb(bg, (int)(alpha * 0.93f)));

        Color sb  = WannaCry.colorManager.getSettingsBg();
        float cx1 = c.isRight() ? ACCENT_W : 0;
        float cx2 = c.isRight() ? W        : W - ACCENT_W;
        RenderUtil.rect(ctx, cx1, 0, cx2, H, argb(sb, (int)(alpha * 0.62f)));

        if (c.isRight()) RenderUtil.rect(ctx, 0,         0, ACCENT_W,     H, argb(accent, alpha));
        else             RenderUtil.rect(ctx, W - ACCENT_W, 0, W,         H, argb(accent, alpha));

        RenderUtil.rect(ctx, cx1, 0, cx2, 1, argb(accent, (int)(alpha * 0.32f)));

        boolean icons    = showIcons.getValue();
        float   iconAreaW = icons ? ICON_W : 0f;
        if (icons) {
            float ix1 = c.isRight() ? ACCENT_W             : W - ACCENT_W - ICON_W;
            float ix2 = c.isRight() ? ACCENT_W + ICON_W    : W - ACCENT_W;
            RenderUtil.rect(ctx, ix1, 0, ix2, H, argb(accent, (int)(alpha * 0.20f)));
            WannaCry.fontService.drawText(ctx, e.type.getIcon(),
                    (int)(ix1 + ICON_W / 2f - 3f),
                    (int)(H / 2f - WannaCry.fontService.getHeight() / 2f),
                    argb(accent, alpha), false);
        }

        float tx = c.isRight() ? ACCENT_W + iconAreaW + 5f : 5f;
        WannaCry.fontService.drawText(ctx, e.title,
                (int) tx, 8,  argb(245, 245, 255, alpha), false);
        WannaCry.fontService.drawText(ctx, e.message,
                (int) tx, 22, argb(160, 160, 175, (int)(alpha * 0.83f)), false);

        float span   = W - ACCENT_W;
        float filled = span * progressFrac;
        int   barClr = argb(accent, (int)(alpha * 0.70f));
        if (c.isRight()) {
            if (filled > 0) RenderUtil.rect(ctx, ACCENT_W, H - PROGRESS_H, ACCENT_W + filled, H, barClr);
        } else {
            if (filled > 0) RenderUtil.rect(ctx, W - ACCENT_W - filled, H - PROGRESS_H, W - ACCENT_W, H, barClr);
        }
    }

    private void drawMinimal(GuiGraphics ctx, Entry e,
                             float W, float H, int alpha, float progressFrac,
                             Corner c, Color accent) {

        Color bg = WannaCry.colorManager.getPanelBg();
        RenderUtil.rect(ctx, 0, 0, W, H, argb(bg, (int)(alpha * 0.28f)));

        float dotSz = 4f;
        float dotY  = (H - dotSz) / 2f;
        float dotX  = c.isRight() ? 5f : W - 9f;
        RenderUtil.rect(ctx, dotX, dotY, dotX + dotSz, dotY + dotSz, argb(accent, alpha));

        int fh = WannaCry.fontService.getHeight();
        int ty = (int)((H - fh) / 2f);

        String sep = "  \u2014  ";
        int tw  = WannaCry.fontService.getWidth(e.title);
        int sw  = WannaCry.fontService.getWidth(sep);

        float tx  = c.isRight() ? dotX + dotSz + 5f : 5f;

        WannaCry.fontService.drawText(ctx, e.title,
                (int) tx, ty, argb(accent, alpha), false);
        WannaCry.fontService.drawText(ctx, sep,
                (int) tx + tw, ty, argb(130, 130, 145, (int)(alpha * 0.70f)), false);
        WannaCry.fontService.drawText(ctx, e.message,
                (int) tx + tw + sw, ty, argb(160, 160, 175, (int)(alpha * 0.83f)), false);

        float filled = (W - 4f) * progressFrac;
        int   barClr = argb(accent, (int)(alpha * 0.55f));
        if (c.isRight()) {
            if (filled > 0) RenderUtil.rect(ctx, 2, H - 1, 2 + filled, H, barClr);
        } else {
            if (filled > 0) RenderUtil.rect(ctx, W - 2 - filled, H - 1, W - 2, H, barClr);
        }
    }

    private void drawBanner(GuiGraphics ctx, Entry e,
                            float W, float H, int alpha, float progressFrac,
                            Corner c, Color accent) {

        Color panelBg   = WannaCry.colorManager.getPanelBg();
        Color accentA   = withAlpha(accent,  (int)(alpha * 0.55f));
        Color panelBgA  = withAlpha(panelBg, (int)(alpha * 0.92f));
        RenderUtil.horizontalGradient(ctx, 0, 0, W, H, accentA, panelBgA);

        if (shadow.getValue()) {
            int shdw = argb(0, 0, 0, (int)(alpha * 0.22f));
            if (c.isBottom()) RenderUtil.rect(ctx, 0, -3, W, 0, shdw);
            else              RenderUtil.rect(ctx, 0, H,  W, H + 3, shdw);
        }

        boolean icons = showIcons.getValue();
        int fh = WannaCry.fontService.getHeight();
        int ty = (int)((H - fh) / 2f);

        float iconX    = 7f;
        float textStart;
        if (icons) {
            WannaCry.fontService.drawText(ctx, e.type.getIcon(),
                    (int) iconX, ty, argb(accent, alpha), false);
            textStart = iconX + WannaCry.fontService.getWidth(e.type.getIcon()) + 7f;
        } else {
            textStart = 10f;
        }

        String sep = "  |  ";
        int    tw  = WannaCry.fontService.getWidth(e.title);
        int    sw  = WannaCry.fontService.getWidth(sep);

        WannaCry.fontService.drawText(ctx, e.title,
                (int) textStart, ty, argb(245, 245, 255, alpha), false);
        WannaCry.fontService.drawText(ctx, sep,
                (int) textStart + tw, ty, argb(150, 150, 165, (int)(alpha * 0.65f)), false);
        WannaCry.fontService.drawText(ctx, e.message,
                (int) textStart + tw + sw, ty, argb(160, 160, 175, (int)(alpha * 0.83f)), false);

        float filled = W * progressFrac;
        int   barClr = argb(accent, (int)(alpha * 0.80f));
        if (c.isBottom()) {
            if (filled > 0) RenderUtil.rect(ctx, 0, 0, filled, PROGRESS_H, barClr);
        } else {
            if (filled > 0) RenderUtil.rect(ctx, 0, H - PROGRESS_H, filled, H, barClr);
        }
    }

    private void drawOutline(GuiGraphics ctx, Entry e,
                             float W, float H, int alpha, float progressFrac,
                             Corner c, Color accent) {

        if (shadow.getValue()) {
            RenderUtil.rect(ctx, 3, 3, W + 3, H + 3, argb(0, 0, 0, (int)(alpha * 0.20f)));
        }

        Color bg = WannaCry.colorManager.getPanelBg();
        RenderUtil.rect(ctx, 0, 0, W, H, argb(bg, (int)(alpha * 0.12f)));

        int borderFull = argb(accent, (int)(alpha * 0.85f));
        int borderDim  = argb(accent, (int)(alpha * 0.40f));

        RenderUtil.rect(ctx, 0,     0,     W,     1, borderFull);
        RenderUtil.rect(ctx, 0,     0,     1,     H, borderDim);
        RenderUtil.rect(ctx, W - 1, 0,     W,     H, borderDim);

        float tx = 8f;
        WannaCry.fontService.drawText(ctx, e.title,
                (int) tx, 8,  argb(245, 245, 255, alpha), false);
        WannaCry.fontService.drawText(ctx, e.message,
                (int) tx, 22, argb(160, 160, 175, (int)(alpha * 0.83f)), false);

        float innerW  = W - 2f;
        float filled  = innerW * progressFrac;
        int   dimLine = argb(accent, (int)(alpha * 0.22f));
        int   barClr  = argb(accent, (int)(alpha * 0.85f));

        RenderUtil.rect(ctx, 1, H - 1, W - 1, H, dimLine);
        if (c.isRight()) {
            if (filled > 0) RenderUtil.rect(ctx, 1, H - 1, 1 + filled, H, barClr);
        } else {
            if (filled > 0) RenderUtil.rect(ctx, W - 1 - filled, H - 1, W - 1, H, barClr);
        }

        RenderUtil.rect(ctx, 0,     0, 3,     3, borderFull);
        RenderUtil.rect(ctx, W - 3, 0, W,     3, borderFull);
    }

    private void drawRetro(GuiGraphics ctx, Entry e,
                           float W, float H, int alpha, float progressFrac,
                           Corner c, Color accent) {

        if (shadow.getValue()) {
            RenderUtil.rect(ctx, 4, 4, W + 4, H + 4, argb(0, 0, 0, (int)(alpha * 0.40f)));
        }

        RenderUtil.rect(ctx, 0, 0, W, H, argb(10, 10, 16, (int)(alpha * 0.94f)));

        RenderUtil.rect(ctx, 0, 0, W, 2, argb(accent, alpha));

        int stripe = argb(accent, (int)(alpha * 0.38f));
        if (c.isRight()) RenderUtil.rect(ctx, 0,         2, ACCENT_W,     H - 2, stripe);
        else             RenderUtil.rect(ctx, W - ACCENT_W, 2, W,         H - 2, stripe);

        float tx = c.isRight() ? ACCENT_W + 5f : 5f;

        String prefix  = e.type.getRetroPrefix();
        int    pw      = WannaCry.fontService.getWidth(prefix);
        int prefixClr  = argb(accent, alpha);
        int titleClr   = argb(210, 225, 210, alpha);

        WannaCry.fontService.drawText(ctx, prefix,  (int) tx,      9,  prefixClr, false);
        WannaCry.fontService.drawText(ctx, e.title, (int) tx + pw, 9,  titleClr,  false);

        int msgClr = argb(
                Math.min(255, accent.getRed()   / 2 + 60),
                Math.min(255, accent.getGreen() / 2 + 60),
                Math.min(255, accent.getBlue()  / 2 + 60),
                (int)(alpha * 0.82f));
        WannaCry.fontService.drawText(ctx, "> " + e.message, (int) tx, 23, msgClr, false);

        float filled  = W * progressFrac;
        int   dimLine = argb(accent, (int)(alpha * 0.18f));
        int   barClr  = argb(accent, (int)(alpha * 0.78f));

        RenderUtil.rect(ctx, 0, H - 2, W, H, dimLine);
        if (c.isRight()) {
            if (filled > 0) RenderUtil.rect(ctx, 0, H - 2, filled, H, barClr);
        } else {
            if (filled > 0) RenderUtil.rect(ctx, W - filled, H - 2, W, H, barClr);
        }
    }

    private static Color withAlpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), clampByte(a));
    }

    private static int argb(Color c, int a) {
        return argb(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    private static int argb(int r, int g, int b, int a) {
        return (clampByte(a) << 24) | (clampByte(r) << 16) | (clampByte(g) << 8) | clampByte(b);
    }

    private static int   clampByte(int v)              { return Math.max(0, Math.min(255, v)); }
    private static float clamp01(float v)               { return Math.max(0f, Math.min(1f, v)); }
    private static float clamp(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }
    private static float smoothstep(float t)            { return t * t * (3f - 2f * t); }
}
