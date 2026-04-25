package fengliu.peca.player;

import java.util.Collections;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public class FakeCraftingInventory
    extends SimpleContainer
    implements CraftingContainer
{

    private final NonNullList<ItemStack> stacks;
    private final int width;
    private final int height;

    public FakeCraftingInventory() {
        super(9);
        this.width = 3;
        this.height = 3;
        this.stacks = NonNullList.withSize(9, ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.stacks) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= this.getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return this.stacks.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(this.stacks, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.stacks, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
        this.setChanged();
    }

    @Override
    public void setChanged() {
        // 容器内容变化时的处理
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        Collections.fill(this.stacks, ItemStack.EMPTY);
    }

    // CraftingContainer 接口方法
    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    public void copy(List<ItemStack> stacks) {
        for (
            int index = 0;
            index < this.stacks.size() && index < stacks.size();
            index++
        ) {
            this.stacks.set(index, stacks.get(index).copy());
        }
        this.setChanged();
    }
}
