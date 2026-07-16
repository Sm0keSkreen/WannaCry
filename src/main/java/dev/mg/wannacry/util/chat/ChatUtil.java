package dev.mg.wannacry.util.chat;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.ducks.render.IChatComponent;
import dev.mg.wannacry.features.commands.Command;
import dev.mg.wannacry.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import static dev.mg.wannacry.util.traits.Util.mc;

public class ChatUtil {
    public static MutableComponent prefix() {
        return withPrefix((Component) null);
    }

    public static MutableComponent withPrefix(String string) {
        return withPrefix(string, new Object[0]);
    }

    public static MutableComponent withPrefix(String string, Object... o) {
        return withPrefix(TextUtil.text(string, o));
    }

    public static MutableComponent withPrefix(Component component) {
        MutableComponent text = Component.empty()
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
                .append(Component.literal("<"))
                .append(getClientNameComponent())
                .append(Component.literal(">"));
        if (component != null) text.append(" ").append(component);
        return text;
    }

    public static void sendMessage(Component message, Signature identifier) {
        sendClientSideMessage(withPrefix(message), identifier);
    }

    public static void sendClientSideMessage(Component message, Signature sig) {
        if (Command.nullCheck()) return;

        IChatComponent chat = (IChatComponent) mc.gui.getChat();
        MessageSignature signature = new MessageSignature(sig.getByteSignature());
        chat.satellite$removeMessage(signature);
        chat.satellite$addMessage(new GuiMessage(mc.gui.getGuiTicks(), message, signature, getMessageTag()));
    }

    public static Component getClientNameComponent() {
        return Component.empty().withColor(WannaCry.colorManager.getColorAsInt()).append("WannaCry");
    }

    private static GuiMessageTag getMessageTag() {
        return new GuiMessageTag(WannaCry.colorManager.getColorAsInt(), null, null, null);
    }
}
