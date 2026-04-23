package fengliu.peca.mixin;

import carpet.patches.EntityPlayerMPFake;
import fengliu.peca.PecaSettings;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(
        method = "canBreatheUnderwater",
        at = @At("RETURN"),
        cancellable = true
    )
    public void fakePlayerNotHypoxic(CallbackInfoReturnable<Boolean> cir) {
        if (
            (LivingEntity) (Object) this instanceof EntityPlayerMPFake &&
            PecaSettings.fakePlayerNotHypoxic
        ) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
