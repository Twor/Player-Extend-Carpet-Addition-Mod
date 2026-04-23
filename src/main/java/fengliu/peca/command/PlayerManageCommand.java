package fengliu.peca.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import carpet.patches.EntityPlayerMPFake;
import carpet.utils.CommandHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fengliu.peca.PecaSettings;
import fengliu.peca.player.sql.PlayerData;
import fengliu.peca.player.sql.PlayerSql;
import fengliu.peca.util.CommandUtil;
import fengliu.peca.util.Page;
import fengliu.peca.util.TextClickUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class PlayerManageCommand {

    private static final LiteralArgumentBuilder<
        CommandSourceStack
    > PlayerManageCmd = literal("playerManage").requires(player ->
        CommandHelper.canUseCommand(player, PecaSettings.commandPlayerManage)
    );

    public static void registerAll(
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        PlayerManageCmd.then(
            literal("list").executes(c -> find(c, PlayerSql::readPlayer))
        )
            .then(
                literal("clone").then(
                    argument("purpose", StringArgumentType.string())
                        .executes(PlayerManageCommand::clonePlayer)
                        .then(
                            literal("to").then(
                                argument(
                                    "name",
                                    StringArgumentType.string()
                                ).executes(PlayerManageCommand::clonePlayer)
                            )
                        )
                        .then(
                            literal("in").then(
                                argument(
                                    "gamemode",
                                    GameModeArgument.gameMode()
                                )
                                    .executes(PlayerManageCommand::clonePlayer)
                                    .then(
                                        literal("to").then(
                                            argument(
                                                "name",
                                                StringArgumentType.string()
                                            ).executes(
                                                PlayerManageCommand::clonePlayer
                                            )
                                        )
                                    )
                            )
                        )
                )
            )
            .then(
                literal("find")
                    .then(
                        argument("name", StringArgumentType.string())
                            .executes(c -> find(c, PlayerSql::readPlayer))
                            .then(makeFindAtCommand())
                            .then(makeFindInCommand())
                            .then(makeFindInDimensionCommand())
                    )
                    .then(
                        literal("gamemode").then(
                            argument("gamemode", GameModeArgument.gameMode())
                                .executes(c -> find(c, PlayerSql::readPlayer))
                                .then(makeFindIsCommand())
                                .then(makeFindAtCommand())
                                .then(makeFindInDimensionCommand())
                        )
                    )
                    .then(
                        literal("pos").then(
                            argument("pos", Vec3Argument.vec3())
                                .executes(c -> find(c, PlayerSql::readPlayer))
                                .then(
                                    literal("inside").then(
                                        argument(
                                            "offset",
                                            IntegerArgumentType.integer(0)
                                        )
                                            .executes(c ->
                                                find(c, PlayerSql::readPlayer)
                                            )
                                            .then(makeFindIsCommand())
                                            .then(makeFindInCommand())
                                            .then(makeFindInDimensionCommand())
                                    )
                                )
                        )
                    )
                    .then(
                        literal("dimension").then(
                            argument("dimension", DimensionArgument.dimension())
                                .executes(c -> find(c, PlayerSql::readPlayer))
                                .then(makeFindIsCommand())
                                .then(makeFindAtCommand())
                                .then(makeFindInCommand())
                        )
                    )
            )
            .then(
                argument("player", EntityArgument.players())
                    .then(
                        literal("save").then(
                            argument(
                                "purpose",
                                StringArgumentType.string()
                            ).executes(PlayerManageCommand::save)
                        )
                    )
                    .then(literal("info").executes(PlayerManageCommand::info))
            )
            .then(
                literal("id").then(
                    argument("id", LongArgumentType.longArg(0))
                        .then(
                            literal("info").executes(
                                PlayerManageCommand::infoId
                            )
                        )
                        .then(
                            literal("delete").executes(
                                PlayerManageCommand::delete
                            )
                        )
                        .then(
                            literal("execute")
                                .executes(PlayerManageCommand::execute)
                                .then(
                                    literal("add").then(
                                        argument(
                                            "command",
                                            StringArgumentType.string()
                                        ).executes(
                                            PlayerManageCommand::executeAdd
                                        )
                                    )
                                )
                                .then(
                                    literal("del").then(
                                        argument(
                                            "index",
                                            IntegerArgumentType.integer(1)
                                        ).executes(
                                            PlayerManageCommand::executeDel
                                        )
                                    )
                                )
                                .then(
                                    literal("set").then(
                                        argument(
                                            "index",
                                            IntegerArgumentType.integer(1)
                                        ).then(
                                            argument(
                                                "command",
                                                StringArgumentType.string()
                                            ).executes(
                                                PlayerManageCommand::executeSet
                                            )
                                        )
                                    )
                                )
                                .then(
                                    literal("clear").executes(
                                        PlayerManageCommand::executeClear
                                    )
                                )
                        )
                )
            );

        dispatcher.register(PlayerManageCmd);
    }

    private static LiteralArgumentBuilder<
        CommandSourceStack
    > makeFindIsCommand() {
        return literal("is").then(
            argument("name", StringArgumentType.string()).executes(c ->
                find(c, PlayerSql::readPlayer)
            )
        );
    }

    private static LiteralArgumentBuilder<
        CommandSourceStack
    > makeFindAtCommand() {
        return literal("at").then(
            argument("pos", Vec3Argument.vec3())
                .executes(c -> find(c, PlayerSql::readPlayer))
                .then(
                    literal("inside").then(
                        argument(
                            "offset",
                            IntegerArgumentType.integer(0)
                        ).executes(c -> find(c, PlayerSql::readPlayer))
                    )
                )
        );
    }

    private static LiteralArgumentBuilder<
        CommandSourceStack
    > makeFindInCommand() {
        return literal("in").then(
            argument("gamemode", GameModeArgument.gameMode()).executes(c ->
                find(c, PlayerSql::readPlayer)
            )
        );
    }

    private static LiteralArgumentBuilder<
        CommandSourceStack
    > makeFindInDimensionCommand() {
        return literal("in").then(
            argument("dimension", DimensionArgument.dimension()).executes(c ->
                find(c, PlayerSql::readPlayer)
            )
        );
    }

    private static String getLoggedText(
        CommandSourceStack context,
        PlayerData playerData
    ) {
        String loggedText;
        net.minecraft.server.level.ServerPlayer player = context
            .getServer()
            .getPlayerList()
            .getPlayerByName(playerData.name());
        if (player == null) {
            loggedText = "peca.info.command.player.not.logged";
        } else if (!(player instanceof carpet.patches.EntityPlayerMPFake)) {
            loggedText = "peca.info.command.player.not.fake";
        } else {
            loggedText = "peca.info.command.player.logged";
        }
        return loggedText;
    }

    public static class PlayerPage extends Page<PlayerData> {

        public PlayerPage(CommandSourceStack context, List<PlayerData> data) {
            super(context, data);
        }

        @Override
        public List<MutableComponent> putPageData(
            PlayerData pageData,
            int index
        ) {
            List<MutableComponent> texts = new ArrayList<>();
            texts.add(
                Component.literal(String.format("[%s] ", index))
                    .append(pageData.name())
                    .append(" - ")
                    .append(
                        Component.translatable(
                            getLoggedText(this.context, pageData)
                        )
                    )
                    .append(" ")
                    .append(
                        Component.literal(
                            pageData.dimension().getPath() + " "
                        ).setStyle(Style.EMPTY.withColor(0xAAAAAA))
                    )
                    .append(
                        Component.literal(
                            String.format(
                                "x: %d y: %d z:%d",
                                (int) pageData.pos().x,
                                (int) pageData.pos().y,
                                (int) pageData.pos().z
                            )
                        ).setStyle(Style.EMPTY.withColor(0x00AAAA))
                    )
            );
            texts.add(
                Component.translatable(
                    "peca.info.command.player.purpose",
                    "§6" + pageData.purpose()
                )
                    .append(
                        TextClickUtil.runText(
                            Component.translatable(
                                "peca.info.command.player.spawn"
                            ),
                            String.format(
                                "/player %s spawn at %g %g %g facing %g %g in %s in %s",
                                pageData.name(),
                                pageData.pos().x,
                                pageData.pos().y,
                                pageData.pos().z,
                                pageData.yaw(),
                                pageData.pitch(),
                                pageData.dimension().getPath(),
                                pageData.gamemode().getName()
                            )
                        )
                    )
                    .append(
                        TextClickUtil.runText(
                            Component.translatable(
                                "peca.info.command.player.kill"
                            ),
                            String.format("/player %s kill", pageData.name())
                        )
                    )
                    .append(
                        TextClickUtil.runText(
                            Component.translatable(
                                "peca.info.command.player.info"
                            ),
                            String.format(
                                "/playerManage id %s info",
                                pageData.id()
                            )
                        )
                    )
                    .append(
                        TextClickUtil.runText(
                            Component.translatable(
                                "peca.info.command.player.delete"
                            ),
                            String.format(
                                "/playerManage id %s delete",
                                pageData.id()
                            )
                        )
                    )
            );
            return texts;
        }
    }

    private static int clonePlayer(CommandContext<CommandSourceStack> context) {
        String purpose = StringArgumentType.getString(context, "purpose");
        if (purpose.isEmpty()) {
            context
                .getSource()
                .sendFailure(
                    Component.translatable(
                        "peca.info.command.error.not.bot.purpose"
                    )
                );
            return -1;
        }

        PlayerData data = PlayerData.fromPlayer(
            context.getSource().getPlayer()
        );
        String name = CommandUtil.getArgOrDefault(
            () -> StringArgumentType.getString(context, "name"),
            data.name()
        );
        GameType gamemode = CommandUtil.getArgOrDefault(
            () -> GameModeArgument.getGameMode(context, "gamemode"),
            data.gamemode()
        );

        CommandUtil.booleanPrintMsg(
            PlayerSql.savePlayer(
                new PlayerData(
                    data.id(),
                    name,
                    data.dimension(),
                    data.pos(),
                    data.yaw(),
                    data.pitch(),
                    gamemode,
                    data.flying(),
                    data.execute(),
                    data.purpose(),
                    data.createTime(),
                    data.createPlayerUuid(),
                    data.lastModifiedTime(),
                    data.lastModifiedPlayerUuid()
                ),
                context.getSource().getPlayer(),
                purpose
            ),
            Component.translatable("peca.info.command.player.save", name),
            Component.translatable("peca.info.command.player.save", name),
            context
        );
        return Command.SINGLE_SUCCESS;
    }

    interface Find {
        List<PlayerData> run(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException;
    }

    private static int find(
        CommandContext<CommandSourceStack> context,
        Find find
    ) {
        List<PlayerData> lists = null;
        try {
            lists = find.run(context);
        } catch (CommandSyntaxException e) {
            context
                .getSource()
                .sendSystemMessage(
                    Component.translatable(
                        "peca.info.command.player.find.empty"
                    )
                );
            return -1;
        }

        if (lists.isEmpty()) {
            context
                .getSource()
                .sendSystemMessage(
                    Component.translatable(
                        "peca.info.command.player.find.empty"
                    )
                );
            return -1;
        }

        Page<?> page = new PlayerPage(context.getSource(), lists);
        PecaCommand.addPage(
            Objects.requireNonNull(context.getSource().getPlayer()),
            page
        );
        page.look();
        return Command.SINGLE_SUCCESS;
    }

    private static int save(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException {
        String purpose = StringArgumentType.getString(context, "purpose");
        if (purpose.isEmpty()) {
            context
                .getSource()
                .sendFailure(
                    Component.translatable(
                        "peca.info.command.error.not.bot.purpose"
                    )
                );
            return -1;
        }

        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        CommandUtil.booleanPrintMsg(
            PlayerSql.savePlayer(
                player,
                context.getSource().getPlayer(),
                purpose
            ),
            Component.translatable(
                "peca.info.command.player.save",
                player.getName()
            ),
            Component.translatable(
                "peca.info.command.player.save",
                player.getName()
            ),
            context
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int delete(CommandContext<CommandSourceStack> context) {
        long id = LongArgumentType.getLong(context, "id");
        CommandUtil.booleanPrintMsg(
            PlayerSql.deletePlayer(id),
            Component.translatable("peca.info.command.player.delete.info", id),
            Component.translatable("peca.info.command.error.player.delete", id),
            context
        );
        return Command.SINGLE_SUCCESS;
    }

    private static void printInfo(
        CommandContext<CommandSourceStack> context,
        PlayerData playerData
    ) {
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.name",
                    playerData.name() +
                        " - " +
                        Component.translatable(
                            getLoggedText(context.getSource(), playerData)
                        ).getString()
                )
            );
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.dimension",
                    playerData.dimension().getPath()
                ).setStyle(Style.EMPTY.withColor(0x00AAAA))
            );
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.pos",
                    playerData.pos().x,
                    playerData.pos().y,
                    playerData.pos().z
                ).setStyle(Style.EMPTY.withColor(0x00AAAA))
            );
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.pos.overworld",
                    playerData.pos().x * 8,
                    playerData.pos().y * 8,
                    playerData.pos().z * 8
                ).setStyle(Style.EMPTY.withColor(0x00AAAA))
            );
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.yaw",
                    playerData.yaw()
                ).setStyle(Style.EMPTY.withColor(0x00AAAA))
            );
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.pitch",
                    playerData.pitch()
                ).setStyle(Style.EMPTY.withColor(0x00AAAA))
            );
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.gamemode",
                    playerData.gamemode().getName()
                ).setStyle(Style.EMPTY.withColor(0x00AAAA))
            );
        if (playerData.id() == -1) {
            return;
        }

        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.execute.info",
                    playerData
                        .execute()
                        .toString()
                        .replace("[", "§3[")
                        .replace("]", "§3]")
                        .replace(",", "§3,")
                        .replace("\"", "§6\"")
                )
            );
        ServerPlayer createPlayer = context
            .getSource()
            .getServer()
            .getPlayerList()
            .getPlayer(playerData.createPlayerUuid());
        if (createPlayer != null) {
            context
                .getSource()
                .sendSystemMessage(
                    Component.translatable(
                        "peca.info.command.player.create.player",
                        createPlayer.getName()
                    ).setStyle(Style.EMPTY.withColor(0x00AAAA))
                );
        }
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.create.player.uuid",
                    playerData.createPlayerUuid()
                ).setStyle(Style.EMPTY.withColor(0x00AAAA))
            );
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.create.time",
                    playerData.createTime()
                ).setStyle(Style.EMPTY.withColor(0x00AAAA))
            );
        ServerPlayer lastModifiedPlayer = context
            .getSource()
            .getServer()
            .getPlayerList()
            .getPlayer(playerData.lastModifiedPlayerUuid());
        if (lastModifiedPlayer != null) {
            context
                .getSource()
                .sendSystemMessage(
                    Component.translatable(
                        "peca.info.command.player.last.modified.player",
                        lastModifiedPlayer.getName()
                    ).setStyle(Style.EMPTY.withColor(0x00AAAA))
                );
        }
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.last.modified.player.uuid",
                    playerData.lastModifiedPlayerUuid()
                ).setStyle(Style.EMPTY.withColor(0x00AAAA))
            );
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.last.modified.time",
                    playerData.lastModifiedTime()
                ).setStyle(Style.EMPTY.withColor(0x00AAAA))
            );
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.purpose",
                    playerData.purpose()
                )
            );
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.id",
                    playerData.id()
                ).setStyle(Style.EMPTY.withColor(0x00AAAA))
            );
        context
            .getSource()
            .sendSystemMessage(
                Component.translatable(
                    "peca.info.command.player.data.info"
                ).setStyle(Style.EMPTY.withColor(0xFF5555))
            );
        context
            .getSource()
            .sendSystemMessage(
                TextClickUtil.suggestText(
                    Component.translatable(
                        "peca.info.command.player.spawn.suggest"
                    ),
                    String.format(
                        "/player %s spawn at %g %g %g facing %g %g in %s in %s",
                        playerData.name(),
                        playerData.pos().x,
                        playerData.pos().y,
                        playerData.pos().z,
                        playerData.yaw(),
                        playerData.pitch(),
                        playerData.dimension().getPath(),
                        playerData.gamemode().getName()
                    )
                )
                    .append(
                        TextClickUtil.runText(
                            Component.translatable(
                                "peca.info.command.player.execute"
                            ),
                            String.format(
                                "/playerManage id %s execute",
                                playerData.id()
                            )
                        )
                    )
                    .append(
                        TextClickUtil.runText(
                            Component.translatable(
                                "peca.info.command.player.stop"
                            ),
                            String.format("/player %s stop", playerData.name())
                        )
                    )
                    .append(
                        TextClickUtil.runText(
                            Component.translatable(
                                "peca.info.command.player.kill"
                            ),
                            String.format("/player %s kill", playerData.name())
                        )
                    )
                    .append(
                        TextClickUtil.runText(
                            Component.translatable(
                                "peca.info.command.player.delete"
                            ),
                            String.format(
                                "/playerManage id %s delete",
                                playerData.id()
                            )
                        )
                    )
            );
    }

    private static int info(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        if (!(player instanceof EntityPlayerMPFake)) {
            context.getSource().sendFailure(Component.translatable(""));
            return -1;
        }

        printInfo(
            context,
            PlayerData.fromPlayer(EntityArgument.getPlayer(context, "player"))
        );
        return Command.SINGLE_SUCCESS;
    }

    interface Execute {
        JsonArray run(JsonArray executeArray);
    }

    private static int setExecute(
        CommandContext<CommandSourceStack> context,
        Execute execute,
        Component text
    ) {
        long id = LongArgumentType.getLong(context, "id");
        PlayerData playerData = PlayerSql.readPlayer(id);
        JsonArray newArray = execute.run(playerData.execute());
        if (newArray == null) {
            return Command.SINGLE_SUCCESS;
        }

        if (!PlayerSql.executeUpdate(id, newArray)) {
            context.getSource().sendFailure(text);
            return -1;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int infoId(CommandContext<CommandSourceStack> context) {
        long id = LongArgumentType.getLong(context, "id");
        PlayerData playerData = PlayerSql.readPlayer(id);
        if (playerData == null) {
            context
                .getSource()
                .sendFailure(
                    Component.translatable(
                        "peca.info.command.player.find.empty"
                    )
                );
            return -1;
        }
        printInfo(context, playerData);
        return Command.SINGLE_SUCCESS;
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        long id = LongArgumentType.getLong(context, "id");
        PlayerData playerData = PlayerSql.readPlayer(id);
        if (playerData == null) {
            context
                .getSource()
                .sendFailure(
                    Component.translatable(
                        "peca.info.command.player.find.empty"
                    )
                );
            return -1;
        }

        JsonArray executeArray = playerData.execute();
        if (executeArray.isEmpty()) {
            context
                .getSource()
                .sendSystemMessage(
                    Component.translatable("peca.info.command.execute.empty")
                );
            return 1;
        }

        context
            .getSource()
            .sendSystemMessage(
                Component.translatable("peca.info.command.execute.list")
            );
        for (int i = 0; i < executeArray.size(); i++) {
            String command = executeArray.get(i).getAsString();
            context
                .getSource()
                .sendSystemMessage(
                    Component.literal(String.format("%d: %s", i + 1, command))
                );
        }
        return 1;
    }

    private static int executeAdd(CommandContext<CommandSourceStack> context) {
        String command = StringArgumentType.getString(context, "command");
        if (!command.startsWith("/player")) {
            context
                .getSource()
                .sendFailure(
                    Component.translatable(
                        "peca.info.command.error.execute.add.starts"
                    )
                );
            return -1;
        }

        return setExecute(
            context,
            executeArray -> {
                executeArray.add(command);
                return executeArray;
            },
            Component.translatable("peca.info.command.error.execute.add")
        );
    }

    private static int executeDel(CommandContext<CommandSourceStack> context) {
        int index = IntegerArgumentType.getInteger(context, "index") - 1;
        return setExecute(
            context,
            executeArray -> {
                if (index > executeArray.size()) {
                    return executeArray;
                }
                executeArray.remove(index);
                return executeArray;
            },
            Component.translatable("peca.info.command.error.execute.del")
        );
    }

    private static int executeSet(CommandContext<CommandSourceStack> context) {
        int index = IntegerArgumentType.getInteger(context, "index") - 1;
        String command = StringArgumentType.getString(context, "command");
        if (!command.startsWith("/player")) {
            context
                .getSource()
                .sendFailure(
                    Component.translatable(
                        "peca.info.command.error.execute.add.starts"
                    )
                );
            return -1;
        }

        return setExecute(
            context,
            executeArray -> {
                if (index > executeArray.size()) {
                    return executeArray;
                }
                executeArray.set(index, new JsonPrimitive(command));
                return executeArray;
            },
            Component.translatable("peca.info.command.error.execute.set")
        );
    }

    private static int executeClear(
        CommandContext<CommandSourceStack> context
    ) {
        return setExecute(
            context,
            executeArray -> JsonParser.parseString("[]").getAsJsonArray(),
            Component.translatable("peca.info.command.error.execute.clear")
        );
    }
}
