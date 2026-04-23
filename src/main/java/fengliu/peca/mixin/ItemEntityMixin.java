package fengliu.peca.mixin;

import carpet.patches.EntityPlayerMPFake;
import fengliu.peca.PecaSettings;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    public void fakePlayerCanNotPickUpItem(Player player, CallbackInfo ci){
        if (player instanceof EntityPlayerMPFake && PecaSettings.fakePlayerCanNotPickUpItem){
            ci.cancel();
        }
    }
}
