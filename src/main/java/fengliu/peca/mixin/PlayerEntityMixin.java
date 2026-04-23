package fengliu.peca.mixin;

import carpet.fakes.ServerPlayerInterface;
import carpet.helpers.EntityPlayerActionPack;
import carpet.patches.EntityPlayerMPFake;
import fengliu.peca.PecaSettings;
import fengliu.peca.player.IPlayerAuto;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow
    public int totalExperience;

    @Shadow
    public int experienceLevel;

    @Final
    public abstract Inventory getInventory();

    protected PlayerEntityMixin(
        EntityType<? extends LivingEntity> entityType,
        Level world
    ) {
        super(entityType, world);
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    public void fakePlayerImmuneDamage(
        ServerLevel level,
        DamageSource source,
        float amount,
        CallbackInfoReturnable<Boolean> cir
    ) {
        boolean isFake = (Player) (Object) this instanceof EntityPlayerMPFake;
        if (isFake) {
            if (PecaSettings.fakePlayerImmuneDamage) {
                cir.cancel();
            } else if (
                this.getHealth() <= amount && PecaSettings.fakePlayerNotDie
            ) {
                this.setHealth(0.1f);
                super.hurtServer(level, source, 0);
                cir.cancel();
            } else if (
                source.is(DamageTypes.ON_FIRE) &&
                PecaSettings.fakePlayerImmuneOnFireDamage
            ) {
                cir.cancel();
            } else if (
                source.is(DamageTypes.IN_FIRE) &&
                PecaSettings.fakePlayerImmuneInFireDamage
            ) {
                cir.cancel();
            } else if (
                source.is(DamageTypes.LAVA) &&
                PecaSettings.fakePlayerImmuneLavaDamage
            ) {
                cir.cancel();
            } else if (
                (source.is(DamageTypes.PLAYER_ATTACK) ||
                    source.is(DamageTypes.PLAYER_EXPLOSION)) &&
                PecaSettings.fakePlayerImmunePlayerDamage
            ) {
                cir.cancel();
            } else if (
                (source.is(DamageTypes.EXPLOSION) ||
                    source.is(DamageTypes.PLAYER_EXPLOSION)) &&
                PecaSettings.fakePlayerImmuneExplosionDamage
            ) {
                cir.cancel();
            } else if (
                source.is(DamageTypes.CRAMMING) &&
                PecaSettings.fakePlayerImmuneCrammingDamage
            ) {
                cir.cancel();
            }
        }
    }

    @Inject(
        method = "getBaseExperienceReward",
        at = @At("RETURN"),
        cancellable = true
    )
    public void fakePlayerDropAllExp(
        ServerLevel level,
        CallbackInfoReturnable<Integer> cir
    ) {
        boolean isFake = (Player) (Object) this instanceof EntityPlayerMPFake;
        if (isFake) {
            if (PecaSettings.fakePlayerDropAllExp) {
                cir.setReturnValue(this.totalExperience);
            } else if (PecaSettings.fakePlayerDropExpNoUpperLimit) {
                cir.setReturnValue(this.experienceLevel * 7);
            }
        }
    }

    private void playerReplaceLowTool() {
        ItemStack mainStack = this.getMainHandItem();
        if (!mainStack.isDamageableItem()) {
            return;
        }

        if (mainStack.getDamageValue() + 10 < mainStack.getMaxDamage()) {
            return;
        }

        Inventory inventory = this.getInventory();
        for (int index = 0; index < 54; index++) {
            ItemStack itemStack = inventory.getItem(index);
            if (!mainStack.is(itemStack.getItem())) {
                continue;
            }

            int slot = inventory.findSlotMatchingItem(itemStack);
            if (slot == inventory.getSelectedSlot()) {
                continue;
            }

            ItemStack copyItem = itemStack.copy();
            if (PecaSettings.fakePlayerDropLowTool) {
                this.drop(mainStack.copy(), false, false);
                mainStack.shrink(1);
                itemStack.shrink(1);
            } else {
                inventory.setItem(slot, mainStack);
            }
            inventory.setItem(inventory.getSelectedSlot(), copyItem);
            return;
        }
        ((ServerPlayerInterface) this).getActionPack().stopAll();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void fakePlayerRunAutoTask(CallbackInfo ci) {
        boolean isFake = (Player) (Object) this instanceof EntityPlayerMPFake;
        if (!isFake) {
            return;
        }

        ((IPlayerAuto) this).runAutoTask();
        if (PecaSettings.fakePlayerReplaceLowTool) {
            playerReplaceLowTool();
        }
    }
}
