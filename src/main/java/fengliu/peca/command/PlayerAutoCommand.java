package fengliu.peca.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import carpet.utils.CommandHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import fengliu.peca.PecaSettings;
import fengliu.peca.player.IPlayerAuto;
import fengliu.peca.player.PlayerAutoType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;

public class PlayerAutoCommand {

    private static final LiteralArgumentBuilder<
        CommandSourceStack
    > PlayerAutoCmd = literal("playerAuto").requires(player ->
        CommandHelper.canUseCommand(player, PecaSettings.commandPlayerAuto)
    );

    public static void registerAll(
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        // Create a CommandBuildContext for item argument registration
        RegistryAccess registryAccess = RegistryAccess.fromRegistryOfRegistries(
            BuiltInRegistries.REGISTRY
        );
        CommandBuildContext commandBuildContext = CommandBuildContext.simple(
            registryAccess,
            null
        );

        PlayerAutoCmd.then(
            argument("player", StringArgumentType.word())
                .then(
                    literal("stop").executes(context ->
                        IPlayerAuto.setPlayerAutoType(
                            context,
                            PlayerAutoType.STOP
                        )
                    )
                )
                .then(
                    literal("sort").then(
                        argument(
                            "item",
                            ItemArgument.item(commandBuildContext)
                        ).executes(context ->
                            IPlayerAuto.setPlayerAutoType(
                                context,
                                PlayerAutoType.SORT
                            )
                        )
                    )
                )
                .then(
                    literal("craft").then(
                        argument(
                            "slot0",
                            ItemArgument.item(commandBuildContext)
                        ).then(
                            argument(
                                "slot1",
                                ItemArgument.item(commandBuildContext)
                            ).then(
                                argument(
                                    "slot2",
                                    ItemArgument.item(commandBuildContext)
                                ).then(
                                    argument(
                                        "slot3",
                                        ItemArgument.item(commandBuildContext)
                                    ).then(
                                        argument(
                                            "slot4",
                                            ItemArgument.item(
                                                commandBuildContext
                                            )
                                        ).then(
                                            argument(
                                                "slot5",
                                                ItemArgument.item(
                                                    commandBuildContext
                                                )
                                            ).then(
                                                argument(
                                                    "slot6",
                                                    ItemArgument.item(
                                                        commandBuildContext
                                                    )
                                                ).then(
                                                    argument(
                                                        "slot7",
                                                        ItemArgument.item(
                                                            commandBuildContext
                                                        )
                                                    ).then(
                                                        argument(
                                                            "slot8",
                                                            ItemArgument.item(
                                                                commandBuildContext
                                                            )
                                                        ).executes(context ->
                                                            IPlayerAuto.setPlayerAutoType(
                                                                context,
                                                                PlayerAutoType.CRAFT
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
                .then(
                    literal("trading")
                        .executes(context ->
                            IPlayerAuto.setPlayerAutoType(
                                context,
                                PlayerAutoType.TRADING
                            )
                        )
                        .then(
                            literal("from").then(
                                argument(
                                    "start",
                                    IntegerArgumentType.integer(1)
                                )
                                    .executes(context ->
                                        IPlayerAuto.setPlayerAutoType(
                                            context,
                                            PlayerAutoType.TRADING
                                        )
                                    )
                                    .then(
                                        literal("to").then(
                                            argument(
                                                "end",
                                                IntegerArgumentType.integer(1)
                                            ).executes(context ->
                                                IPlayerAuto.setPlayerAutoType(
                                                    context,
                                                    PlayerAutoType.TRADING
                                                )
                                            )
                                        )
                                    )
                            )
                        )
                )
        );

        dispatcher.register(PlayerAutoCmd);
    }
}
