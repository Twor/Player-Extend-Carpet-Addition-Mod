package fengliu.peca.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;

public class CommandUtil {

    public interface Arg<T> {
        T get() throws CommandSyntaxException;
    }

    public static <T> T getArgOrDefault(Arg<T> arg, T defaultValue) {
        try {
            return arg.get();
        } catch (
            IllegalArgumentException
            | CommandSyntaxException
            | UnsupportedOperationException e
        ) {
            return defaultValue;
        }
    }

    public static void booleanPrintMsg(
        boolean bool,
        MutableComponent text,
        MutableComponent errorText,
        CommandContext<CommandSourceStack> context
    ) {
        if (bool) {
            context.getSource().sendSystemMessage(text);
        } else {
            context.getSource().sendFailure(errorText);
        }
    }
}
