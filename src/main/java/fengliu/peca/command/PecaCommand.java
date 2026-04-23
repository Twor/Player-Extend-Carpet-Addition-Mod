package fengliu.peca.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fengliu.peca.util.Page;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class PecaCommand {

    private static final LiteralArgumentBuilder<CommandSourceStack> PecaCmd =
        literal("peca");
    private static final Map<UUID, Page<?>> pages = new WeakHashMap<>();

    public static void addPage(ServerPlayer player, Page<?> page) {
        pages.put(player.getUUID(), page);
    }

    public static void registerAll(
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        PecaCmd.then(makePageCommand("next", (context, page) -> page.next()))
            .then(makePageCommand("prev", (context, page) -> page.prev()))
            .then(
                makePageCommand("to", (context, page) ->
                    page.to(IntegerArgumentType.getInteger(context, "page"))
                ).then(argument("page", IntegerArgumentType.integer(0)))
            );

        dispatcher.register(PecaCmd);
    }

    interface runPage {
        void run(CommandContext<CommandSourceStack> context, Page<?> page);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> makePageCommand(
        String name,
        runPage run
    ) {
        return literal(name).executes(context -> {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                return Command.SINGLE_SUCCESS;
            }

            if (!pages.containsKey(player.getUUID())) {
                return Command.SINGLE_SUCCESS;
            }

            run.run(context, pages.get(player.getUUID()));
            return Command.SINGLE_SUCCESS;
        });
    }
}
