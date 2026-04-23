package fengliu.peca.mixin;

import carpet.patches.EntityPlayerMPFake;
import fengliu.peca.PecaSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BubbleColumnBlock.class)
public class BubbleColumnBlockMixin {

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    public void fakePlayerWillNotAffectedByBubbleColumn(
        BlockState state,
        Level world,
        BlockPos pos,
        Entity entity,
        InsideBlockEffectApplier insideBlockEffectApplier,
        boolean bl,
        CallbackInfo ci
    ) {
        if (
            entity instanceof EntityPlayerMPFake &&
            PecaSettings.fakePlayerWillNotAffectedByBubbleColumn
        ) {
            ci.cancel();
        }
    }
}
