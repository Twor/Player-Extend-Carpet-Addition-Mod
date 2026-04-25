package fengliu.peca.mixin;

import carpet.patches.EntityPlayerMPFake;
import fengliu.peca.PecaSettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEntityMixin extends Entity {

    @Shadow
    private Player followingPlayer;

    @Shadow
    public abstract boolean isAttackable();

    @Shadow
    public ExperienceOrbEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "followNearbyPlayer", at = @At("HEAD"), cancellable = true)
    public void fakePlayerCanNotSurroundExp(CallbackInfo ci) {
        if (
            this.followingPlayer instanceof EntityPlayerMPFake &&
            PecaSettings.fakePlayerCanNotSurroundExp
        ) {
            this.followingPlayer = null;
            ci.cancel();
        }
    }

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    public void fakePlayerCanNotAssimilateExp(Player player, CallbackInfo ci) {
        if (
            player instanceof EntityPlayerMPFake &&
            PecaSettings.fakePlayerCanNotAssimilateExp
        ) {
            ci.cancel();
        }
    }
}
