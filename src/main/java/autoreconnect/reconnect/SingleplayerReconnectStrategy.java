package autoreconnect.reconnect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.network.chat.Component;

public class SingleplayerReconnectStrategy extends ReconnectStrategy {
    private final String worldName;

    public SingleplayerReconnectStrategy(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public String getName() {
        return worldName;
    }

    /**
     * @see net.minecraft.client.quickplay.QuickPlay#joinSingleplayerWorld(Minecraft, String)
     */
    @Override
    public void reconnect() {
        Minecraft client = Minecraft.getInstance();
        if (!client.getLevelSource().levelExists(getName())) return;
        client.forceSetScreen(new GenericMessageScreen(Component.translatable("selectWorld.data_read")));
        client.createWorldOpenFlows().openWorld(getName(), () -> {});
    }
}
