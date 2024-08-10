package autoreconnect.mixin;

import autoreconnect.DisconnectedScreenUtil.DisconnectedScreenTransferring;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientCommonNetworkHandlerMixin {
    @Shadow
    protected boolean transferring;

    @Redirect(at = @At(value = "NEW", target = "net/minecraft/client/gui/screen/DisconnectedScreen"), method = "createDisconnectedScreen")
    private DisconnectedScreen createDisconnectedScreen(Screen parent, Text title, DisconnectionInfo info) {
        DisconnectedScreen screen = new DisconnectedScreen(parent, title, info);
        ((DisconnectedScreenTransferring) screen).autoreconnect$setTransferring(this.transferring);
        return screen;
    }
}
