package autoreconnect.reconnect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public class MultiplayerReconnectStrategy extends ReconnectStrategy {
    private final ServerData serverInfo;

    public MultiplayerReconnectStrategy(ServerData serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public String getName() {
        return serverInfo.name;
    }

    /**
     * @see net.minecraft.client.quickplay.QuickPlay#joinMultiplayerWorld(Minecraft, String)
     */
    @Override
    public void reconnect() {
        ConnectScreen.startConnecting(
            new JoinMultiplayerScreen(new TitleScreen()),
            Minecraft.getInstance(),
            ServerAddress.parseString(serverInfo.ip),
            serverInfo,
            false,
            null
        );
    }
}
