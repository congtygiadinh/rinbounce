/*
 * RinBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/rattermc/rinbounce69
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.gui;

import net.ccbluex.liquidbounce.features.module.modules.misc.ChatControl;
import net.ccbluex.liquidbounce.features.module.modules.render.HUD;
import net.ccbluex.liquidbounce.ui.font.Fonts;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.ccbluex.liquidbounce.utils.client.MinecraftInstance.mc;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat {
    @Shadow
    private List<ChatLine> drawnChatLines;

    private final Map<String, Integer> messageCounts = new HashMap<>();

    @Redirect(method = {"getChatComponent", "drawChat"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/FontRenderer;FONT_HEIGHT:I"))
    private int injectFontChat(FontRenderer instance) {
        return HUD.INSTANCE.shouldModifyChatFont() ? Fonts.fontSemibold40.getHeight() : instance.FONT_HEIGHT;
    }

    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private int injectFontChatB(FontRenderer instance, String text, float x, float y, int color) {
        return HUD.INSTANCE.shouldModifyChatFont() ? Fonts.fontSemibold40.drawStringWithShadow(text, x, y, color) : instance.drawStringWithShadow(text, x, y, color);
    }

    @Redirect(method = "getChatComponent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;getStringWidth(Ljava/lang/String;)I"))
    private int injectFontChatC(FontRenderer instance, String text) {
        return HUD.INSTANCE.shouldModifyChatFont() ? Fonts.fontSemibold40.getStringWidth(text) : instance.getStringWidth(text);
    }

    @Inject(method = "printChatMessage", at = @At("HEAD"), cancellable = true)
    public void onPrintChatMessage(IChatComponent chatComponent, CallbackInfo ci) {
        if (chatComponent == null || drawnChatLines == null) {
            return;
        }

        String rawMessage = chatComponent.getUnformattedText().trim();
        if (rawMessage.isEmpty()) {
            return;
        }

        String messageId = rawMessage;

        if (ChatControl.INSTANCE.handleEvents() && ChatControl.INSTANCE.getStackMessage()) {
            int currentCount = messageCounts.getOrDefault(messageId, 0);
            int newCount = currentCount + 1;

            if (newCount > 100) {
                newCount = 1;
                messageCounts.put(messageId, 1);
            } else {
                messageCounts.put(messageId, newCount);
            }

            ChatLine targetLine = null;
            int targetIndex = -1;
            for (int i = 0; i < drawnChatLines.size(); i++) {
                ChatLine line = drawnChatLines.get(i);
                String existingMessage = line.getChatComponent().getUnformattedText().trim();
                String baseMessage = existingMessage.replaceAll(" \\[x\\d+\\]$", "").trim();
                if (baseMessage.equals(rawMessage)) {
                    targetLine = line;
                    targetIndex = i;
                    break;
                }
            }

            String modifiedMessage = rawMessage + (newCount > 1 ? " " + EnumChatFormatting.GRAY + "[x" + newCount + "]" : "");
            ChatComponentText stackedComponent = new ChatComponentText(modifiedMessage);

            if (targetLine != null) {
                drawnChatLines.set(targetIndex, new ChatLine(targetLine.getUpdatedCounter(), stackedComponent, targetLine.getChatLineID()));
                ci.cancel();
            } else if (newCount == 1) {
                return;
            } else {
                drawnChatLines.add(0, new ChatLine(0, stackedComponent, 0));
                ci.cancel();
            }
        }
    }

    @Redirect(method = "setChatLine", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0))
    private int hookNoLengthLimit(List<ChatLine> list) {
        final ChatControl chatControl = ChatControl.INSTANCE;

        if (chatControl.handleEvents() && chatControl.getNoLengthLimit()) {
            return -1;
        }

        return list.size();
    }

    @Inject(method = "clearChatMessages", at = @At("HEAD"), cancellable = true)
    private void hookChatClear(CallbackInfo ci) {
        final ChatControl chatControl = ChatControl.INSTANCE;

        if (chatControl.handleEvents() && chatControl.getNoChatClear()) {
            ci.cancel();
        }
    }
}