package autoreconnect.mixin;

import autoreconnect.AutoReconnect;
import autoreconnect.reconnect.SingleplayerReconnectStrategy;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.*;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.objectweb.asm.Opcodes.PUTFIELD;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    public Screen currentScreen;

    @Inject(at = @At("HEAD"), method = "startIntegratedServer")
    private void startIntegratedServer(LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, boolean newWorld, CallbackInfo ci) {
        AutoReconnect.getInstance().setReconnectHandler(new SingleplayerReconnectStrategy(session.getDirectoryName()));
    }

    @Inject(
        at = @At(
            value = "FIELD",
            opcode = PUTFIELD,
            target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"
        ),
        method = "setScreen"
    )
    private void setScreen(Screen newScreen, CallbackInfo ci) {
        AutoReconnect.getInstance().onScreenChanged(currentScreen, newScreen);
    }
}
