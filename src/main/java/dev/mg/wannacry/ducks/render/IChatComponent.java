package dev.mg.wannacry.ducks.render;

import net.minecraft.client.GuiMessage;
import net.minecraft.network.chat.MessageSignature;

public interface IChatComponent {
    void satellite$addMessage(GuiMessage message);
    void satellite$removeMessage(MessageSignature signature);
}
