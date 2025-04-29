package autoreconnect.mixin;

import autoreconnect.DisconnectedScreenUtil.DisconnectedScreenTransferring;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonNetworkHandlerMixin {
    @Shadow
    protected boolean isTransferring;

    @Redirect(at = @At(value = "NEW", target = "net/minecraft/client/gui/screens/DisconnectedScreen"), method = "createDisconnectScreen")
    private DisconnectedScreen createDisconnectScreen(Screen parent, Component title, DisconnectionDetails info) {
        DisconnectedScreen screen = new DisconnectedScreen(parent, title, info);
        ((DisconnectedScreenTransferring) screen).autoreconnect$setTransferring(this.isTransferring);
        return screen;
    }
}
