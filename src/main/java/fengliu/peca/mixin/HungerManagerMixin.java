package fengliu.peca.mixin;

import carpet.patches.EntityPlayerMPFake;
import fengliu.peca.PecaSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class HungerManagerMixin {

    @Shadow
    private int tickTimer;

    @Inject(method = "tick", at = @At("HEAD"))
    public void fakePlayerNotHunger(ServerPlayer player, CallbackInfo ci) {
        if (
            player instanceof EntityPlayerMPFake &&
            PecaSettings.fakePlayerNotHunger
        ) {
            this.tickTimer = 0;
        }
    }
}
