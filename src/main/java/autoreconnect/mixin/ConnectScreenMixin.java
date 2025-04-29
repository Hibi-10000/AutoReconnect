package autoreconnect.mixin;

import autoreconnect.AutoReconnect;
import autoreconnect.reconnect.MultiplayerReconnectStrategy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConnectScreen.class)
public class ConnectScreenMixin {
    @Inject(at = @At("HEAD"), method = "connect(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;Lnet/minecraft/client/multiplayer/ServerData;Lnet/minecraft/client/multiplayer/TransferState;)V")
    private void connect(Minecraft client, ServerAddress address, ServerData serverInfo, TransferState cookieStorage, CallbackInfo ci) {
        if (serverInfo == null) return;
        AutoReconnect.getInstance().setReconnectHandler(new MultiplayerReconnectStrategy(serverInfo));
    }
}
