package fengliu.peca.util;

import carpet.patches.EntityPlayerMPFake;
import com.mojang.brigadier.context.CommandContext;
import fengliu.peca.PecaSettings;
import fengliu.peca.player.PlayerGroup;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class PlayerUtil {

    public static boolean canSpawn(String name, PlayerList playerManager) {
        AtomicBoolean canSpawn = new AtomicBoolean(true);
        playerManager
            .getPlayers()
            .forEach(player -> {
                if (player.getName().getString().equals(name)) {
                    canSpawn.set(false);
                }
            });
        return canSpawn.get();
    }

    public static boolean canSpawn(
        String name,
        CommandContext<CommandSourceStack> context
    ) {
        return canSpawn(name, context.getSource().getServer().getPlayerList());
    }

    public static boolean canSpawnGroup(
        String nameHand,
        CommandContext<CommandSourceStack> context
    ) {
        if (PecaSettings.groupCanBePlayerLogInSpawn) {
            return true;
        }

        if (PlayerGroup.getGroup(nameHand) != null) {
            return false;
        }

        AtomicBoolean canSpawn = new AtomicBoolean(true);
        context
            .getSource()
            .getServer()
            .getPlayerList()
            .getPlayers()
            .forEach(player -> {
                if (player.getName().getString().contains(nameHand)) {
                    canSpawn.set(false);
                }
            });
        return canSpawn.get();
    }

    public static EntityPlayerMPFake spawn(
        String name,
        Vec3 pos,
        GameType mode,
        CommandContext<CommandSourceStack> context
    ) {
        if (!canSpawn(name, context)) {
            return null;
        }

        if (PecaSettings.fakePlayerGameModeLockSurvive) {
            mode = GameType.SURVIVAL;
        }

        if (mode == null) {
            return spawn(name, pos, context);
        }

        Player sourcePlayer = context.getSource().getPlayer();
        Vec2 facing = context.getSource().getRotation();

        boolean flying = false;
        if (
            sourcePlayer instanceof ServerPlayer player &&
            mode != GameType.SURVIVAL
        ) {
            flying = player.getAbilities().flying;
        }

        if (mode == GameType.SPECTATOR) {
            flying = true;
        }

        // createFake returns boolean in newer versions
        boolean success = EntityPlayerMPFake.createFake(
            name,
            context.getSource().getServer(),
            pos,
            facing.y,
            facing.x,
            context.getSource().getPlayer().level().dimension(),
            mode,
            flying
        );

        if (success) {
            // Get the created player from the player list
            ServerPlayer player = context
                .getSource()
                .getServer()
                .getPlayerList()
                .getPlayerByName(name);
            if (player instanceof EntityPlayerMPFake) {
                return (EntityPlayerMPFake) player;
            }
        }
        return null;
    }

    public static EntityPlayerMPFake spawn(
        String name,
        Vec3 pos,
        CommandContext<CommandSourceStack> context
    ) {
        GameType mode = GameType.CREATIVE;
        Player sourcePlayer = context.getSource().getPlayer();

        if (sourcePlayer instanceof ServerPlayer player) {
            if (!player.isCreative() && !player.isSpectator()) {
                mode = GameType.SURVIVAL;
            } else if (player.isSpectator()) {
                mode = GameType.SPECTATOR;
            }
        }

        return spawn(name, pos, mode, context);
    }

    public static int getItemSlot(ItemStack stack, Inventory inventory) {
        for (
            int slotIndex = 0;
            slotIndex < inventory.getContainerSize();
            slotIndex++
        ) {
            if (!inventory.getItem(slotIndex).is(stack.getItem())) {
                continue;
            }
            return slotIndex;
        }
        return -1;
    }

    public static int getItemSlotAndCount(
        ItemStack stack,
        Inventory inventory
    ) {
        for (
            int slotIndex = 0;
            slotIndex < inventory.getContainerSize();
            slotIndex++
        ) {
            ItemStack slotStack = inventory.getItem(slotIndex);
            if (
                !slotStack.is(stack.getItem()) ||
                slotStack.getCount() < stack.getCount()
            ) {
                continue;
            }
            return slotIndex;
        }
        return -1;
    }
}
