package fengliu.peca.mixin;

import fengliu.peca.PecaSettings;
import fengliu.peca.player.PlayerGroup;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin {

    @Inject(
        method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Z)V",
        at = @At("HEAD"),
        cancellable = true
    )
    public void hiddenFakePlayerGroupJoinMessage(
        Component message,
        Function<ServerPlayer, Component> playerMessageFactory,
        boolean overlay,
        CallbackInfo ci
    ) {
        if (!PecaSettings.hiddenFakePlayerGroupJoinMessage) {
            return;
        }

        for (PlayerGroup playerGroup : PlayerGroup.groups) {
            if (
                !message
                    .getString()
                    .toLowerCase()
                    .contains(playerGroup.getName().toLowerCase())
            ) {
                continue;
            }
            ci.cancel();
            return;
        }
    }
}
