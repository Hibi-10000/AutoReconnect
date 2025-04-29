package autoreconnect.mixin;

import autoreconnect.AutoReconnect;
import autoreconnect.reconnect.SingleplayerReconnectStrategy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.objectweb.asm.Opcodes.PUTFIELD;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Shadow
    public Screen screen;

    @Inject(at = @At("HEAD"), method = "doWorldLoad")
    private void startIntegratedServer(LevelStorageSource.LevelStorageAccess session, PackRepository dataPackManager, WorldStem saveLoader, boolean newWorld, CallbackInfo ci) {
        AutoReconnect.getInstance().setReconnectHandler(new SingleplayerReconnectStrategy(session.getLevelId()));
    }

    @Inject(
        at = @At(
            value = "FIELD",
            opcode = PUTFIELD,
            target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;"
        ),
        method = "setScreen"
    )
    private void setScreen(Screen newScreen, CallbackInfo ci) {
        AutoReconnect.getInstance().onScreenChanged(this.screen, newScreen);
    }
}
