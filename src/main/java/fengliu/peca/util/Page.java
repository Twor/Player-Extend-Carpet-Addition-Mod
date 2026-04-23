package fengliu.peca.util;

import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class Page<Type> {

    protected final CommandSourceStack context;
    protected final List<Type> data;
    protected final int dataSize;
    protected final int limit = 5;
    protected int pageIn = 0;
    protected int pageCount;
    protected int pageDataIndex = 1;

    public Page(CommandSourceStack context, List<Type> data) {
        this.context = context;
        this.data = data;
        this.dataSize = data.size();
        this.pageCount = (int) Math.ceil((double) data.size() / this.limit);
    }

    public List<Type> getPageData() {
        int offset = this.limit * this.pageIn;
        int toIndex = offset + this.limit;
        if (toIndex > this.dataSize) {
            toIndex = this.dataSize;
        }
        return this.data.subList(offset, toIndex);
    }

    public abstract List<MutableComponent> putPageData(
        Type pageData,
        int index
    );

    public void look() {
        this.pageDataIndex = this.pageIn * this.limit + 1;
        this.context.sendSystemMessage(
            Component.translatable(
                "peca.info.page.count",
                String.format("%s/%s", this.pageIn + 1, this.pageCount)
            )
        );
        this.getPageData().forEach(data -> {
            this.putPageData(data, this.pageDataIndex).forEach(component ->
                this.context.sendSystemMessage(component)
            );
            this.pageDataIndex++;
        });
        this.context.sendSystemMessage(
            TextClickUtil.runText(
                Component.translatable("peca.info.page.prev"),
                "/peca prev"
            )
                .append(
                    TextClickUtil.runText(
                        Component.translatable("peca.info.page.next"),
                        "/peca next"
                    )
                )
                .append(
                    TextClickUtil.suggestText(
                        Component.translatable("peca.info.page.to"),
                        "/peca to "
                    )
                )
        );
    }

    public void next() {
        if (this.pageIn + 1 < this.pageCount) {
            this.pageIn++;
        }

        this.look();
    }

    public void prev() {
        if (pageIn > 0) {
            this.pageIn--;
        }

        this.look();
    }

    public void to(int toPage) {
        if (this.pageIn + 1 < this.pageCount && toPage > 0) {
            this.pageIn = toPage;
        }

        this.look();
    }
}
