package fengliu.peca.player;

import static fengliu.peca.util.CommandUtil.getArgOrDefault;

import carpet.patches.EntityPlayerMPFake;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fengliu.peca.PecaMod;
import fengliu.peca.mixin.MerchantMenuAccessor;
import fengliu.peca.util.PlayerUtil;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

/**
 * 假人自动任务类型
 */
public enum PlayerAutoType {
    /**
     * 停止
     */
    STOP(
        (context, player) -> {
            return;
        },
        (context, player) -> {
            return;
        }
    ),

    /**
     * 分类
     */
    SORT(
        (context, player) -> {
            PecaMod.LOGGER.info(player.containerMenu.toString());
            if (
                !(player.containerMenu instanceof ShulkerBoxMenu) &&
                !(player.containerMenu instanceof ChestMenu) &&
                !(player.containerMenu instanceof DispenserMenu) &&
                !(player.containerMenu instanceof HopperMenu)
            ) {
                return;
            }

            Item item = ItemArgument.getItem(context, "item").getItem();
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack itemStack = inventory.getItem(i);
                if (itemStack.isEmpty()) {
                    continue;
                }

                if (!itemStack.is(item)) {
                    player.drop(itemStack.copy(), false, false);
                    itemStack.setCount(0);
                    continue;
                }

                for (
                    int index = 0;
                    index < player.containerMenu.slots.size();
                    index++
                ) {
                    ItemStack slotStack = player.containerMenu
                        .getSlot(index)
                        .getItem();
                    if (!slotStack.isEmpty()) {
                        continue;
                    }

                    player.containerMenu.setItem(index, 0, itemStack.copy());
                    itemStack.setCount(0);
                }
            }
        },
        (context, player) -> {
            if (!(player.containerMenu instanceof InventoryMenu)) {
                player.containerMenu.removed(player);
            }
        }
    ),

    /**
     * 合成
     */
    CRAFT(
        (context, player) -> {
            if (
                !(player.containerMenu instanceof CraftingMenu) ||
                player.level().getServer() == null
            ) {
                return;
            }

            Inventory inventory = player.getInventory();
            FakeCraftingInventory fakeCraftingInventory =
                new FakeCraftingInventory();

            for (int index = 0; index < 9; index++) {
                ItemStack stack = ItemArgument.getItem(context, "slot" + index)
                    .getItem()
                    .getDefaultInstance();
                if (stack.isEmpty()) {
                    continue;
                }

                int slot = PlayerUtil.getItemSlot(stack, inventory);
                if (slot == -1) {
                    return;
                }

                fakeCraftingInventory.setItem(index, inventory.getItem(slot));
            }

            Level level = player.level();
            ItemStack stack = ItemStack.EMPTY;

            int maxIterations = 64;
            int iterationCount = 0;
            while (iterationCount < maxIterations) {
                Optional<RecipeHolder<CraftingRecipe>> optional = (
                    (ServerLevel) player.level()
                ).getServer()
                    .getRecipeManager()
                    .getRecipeFor(
                        RecipeType.CRAFTING,
                        fakeCraftingInventory.asCraftInput(),
                        level
                    );
                if (!optional.isPresent()) {
                    return;
                }

                ItemStack craftStack = optional
                    .get()
                    .value()
                    .assemble(
                        fakeCraftingInventory.asCraftInput(),
                        level.registryAccess()
                    );
                if (!craftStack.is(stack.getItem()) && !stack.isEmpty()) {
                    return;
                }
                stack = craftStack;
                player.drop(craftStack, false, false);
                for (
                    int craftingIndex = 0;
                    craftingIndex < fakeCraftingInventory.getContainerSize();
                    craftingIndex++
                ) {
                    fakeCraftingInventory.getItem(craftingIndex).shrink(1);
                }
                iterationCount++;
            }
        },
        (context, player) -> {
            return;
        }
    ),

    /**
     * 交易
     */
    TRADING(
        (context, player) -> {
            if (!(player.containerMenu instanceof MerchantMenu merchant)) {
                return;
            }

            MerchantOffers tradeList = merchant.getOffers();
            int size = tradeList.size();
            int from =
                getArgOrDefault(
                    () -> IntegerArgumentType.getInteger(context, "start"),
                    1
                ) -
                1;
            int to = getArgOrDefault(
                () -> IntegerArgumentType.getInteger(context, "end"),
                size
            );

            if (from < 0) {
                from = 0;
            } else if (from > size) {
                from = size;
            }

            if (to > size) {
                to = size;
            } else if (to < 0) {
                to = 0;
            }

            Inventory inventory = player.getInventory();
            for (MerchantOffer trade : tradeList.subList(from, to)) {
                if (trade.isOutOfStock()) {
                    continue;
                }

                ItemStack firstBuyItem = trade.getCostA();
                ItemStack secondBuyItem = trade.getCostB();

                int firstSlot = PlayerUtil.getItemSlotAndCount(
                    firstBuyItem,
                    inventory
                );
                if (firstSlot == -1) {
                    continue;
                }

                int secondSlot = -1;
                if (!secondBuyItem.isEmpty()) {
                    secondSlot = PlayerUtil.getItemSlotAndCount(
                        secondBuyItem,
                        inventory
                    );
                    if (secondSlot == -1) {
                        continue;
                    }
                }

                inventory.getItem(firstSlot).shrink(firstBuyItem.getCount());
                if (secondSlot != -1) {
                    inventory
                        .getItem(secondSlot)
                        .shrink(secondBuyItem.getCount());
                }

                player.drop(trade.getResult().copy(), false, false);
                ((MerchantMenuAccessor) merchant).peca$getTrader().notifyTrade(
                    trade
                );
            }
        },
        (context, player) -> {}
    );

    interface AutoTask {
        void run(
            CommandContext<CommandSourceStack> context,
            EntityPlayerMPFake player
        );
    }

    private final AutoTask task;
    private final AutoTask stopTask;

    /**
     * 自动任务类型
     *
     * @param task     执行
     * @param stopTask 停止
     */
    PlayerAutoType(AutoTask task, AutoTask stopTask) {
        this.task = task;
        this.stopTask = stopTask;
    }

    public void runTask(
        CommandContext<CommandSourceStack> context,
        EntityPlayerMPFake player
    ) {
        this.task.run(context, player);
    }

    public void stopTask(
        CommandContext<CommandSourceStack> context,
        EntityPlayerMPFake player
    ) {
        this.stopTask.run(context, player);
    }
}
