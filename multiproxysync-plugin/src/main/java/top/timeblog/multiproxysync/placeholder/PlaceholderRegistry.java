package top.timeblog.multiproxysync.placeholder;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import top.timeblog.multiproxysync.MultiProxySync;

public class PlaceholderRegistry {
    private final MultiProxySync plugin;
    private Expansion expansion;

    public PlaceholderRegistry(MultiProxySync plugin) {
        this.plugin = plugin;
    }

    public void register() {
        this.expansion = Expansion.builder("multiproxysync")
                .globalPlaceholder("global_player_count", (ctx, queue) ->
                        Tag.selfClosingInserting(
                                Component.text(plugin.getCore().getAllServerPlayerCount())
                        )
                )
                .build();

        this.expansion.register();
    }
}