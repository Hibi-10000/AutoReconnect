package autoreconnect.reconnect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.task.RealmsGetServerDetailsTask;

public class RealmsReconnectStrategy extends ReconnectStrategy {
    private final RealmsServer realmsServer;

    public RealmsReconnectStrategy(RealmsServer realmsServer) {
        this.realmsServer = realmsServer;
    }

    @Override
    public String getName() {
        return realmsServer.getName();
    }

    /**
     * @see net.minecraft.client.QuickPlay#startRealms(MinecraftClient, RealmsClient, String)
     */
    @Override
    public void reconnect() {
        TitleScreen titleScreen = new TitleScreen();
        RealmsGetServerDetailsTask realmsGetServerDetailsTask = new RealmsGetServerDetailsTask(titleScreen, realmsServer);
        MinecraftClient.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(titleScreen, realmsGetServerDetailsTask));
    }
}
