package fengliu.peca.mixin;

import carpet.patches.EntityPlayerMPFake;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import fengliu.peca.player.IPlayerAuto;
import fengliu.peca.player.PlayerAutoType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 假人每 tick 执行任务
 */
@Mixin(EntityPlayerMPFake.class)
public abstract class FakePlayerAutoMixin
    extends ServerPlayer
    implements IPlayerAuto
{

    private PlayerAutoType autoType = PlayerAutoType.STOP;
    private CommandContext<CommandSourceStack> autoContext;

    public FakePlayerAutoMixin(
        MinecraftServer server,
        ServerLevel world,
        GameProfile profile,
        ClientInformation clientInformation
    ) {
        super(server, world, profile, clientInformation);
    }

    public PlayerAutoType getAutoType() {
        return this.autoType;
    }

    @Override
    public void setAutoType(
        CommandContext<CommandSourceStack> context,
        PlayerAutoType type
    ) {
        this.stopAutoTask();
        this.autoType = type;
        this.autoContext = context;
    }

    @Override
    public void runAutoTask() {
        this.autoType.runTask(
            this.autoContext,
            (EntityPlayerMPFake) (Object) this
        );
    }

    @Override
    public void stopAutoTask() {
        this.autoType.stopTask(
            this.autoContext,
            (EntityPlayerMPFake) (Object) this
        );
    }
}
