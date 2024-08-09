package autoreconnect.mixin;

import autoreconnect.AutoReconnect;
import autoreconnect.DisconnectedScreenUtil;
import autoreconnect.DisconnectedScreenUtil.DisconnectedScreenTransferring;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin extends Screen implements DisconnectedScreenTransferring {
    @Shadow
    @Mutable
    private @Final Screen parent;
    @Unique
    @Mutable
    private @Final DisconnectedScreenUtil autoreconnect$util;

    protected DisconnectedScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/text/Text;Lnet/minecraft/network/DisconnectionInfo;Lnet/minecraft/text/Text;)V")
    private void constructor(Screen parent, Text title, DisconnectionInfo info, Text buttonLabel, CallbackInfo ci) {
        autoreconnect$util = new DisconnectedScreenUtil(this, this::remove, this::addDrawableChild, super::keyPressed);
        if (AutoReconnect.getInstance().isPlayingSingleplayer()) {
            // make back button redirect to SelectWorldScreen instead of MultiPlayerScreen (https://bugs.mojang.com/browse/MC-45602)
            this.parent = new SelectWorldScreen(new TitleScreen());
        }
    }

    @Unique
    @Override
    public void autoreconnect$setTransferring(boolean transferring) {
        autoreconnect$util.setTransferring(transferring);
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void init(CallbackInfo ci) {
        autoreconnect$util.init();
        if (AutoReconnect.getInstance().isPlayingSingleplayer()) {
            // change back button text to "Back" instead of "Back to World List" bcs of bug fix above
            AutoReconnect.findBackButton(this).ifPresent(
                btn -> btn.setMessage(Text.translatable("gui.toWorld"))
            );
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return autoreconnect$util.keyPressed(keyCode, scanCode, modifiers);
    }

    // make this screen closable by pressing escape
    @Inject(at = @At("RETURN"), method = "shouldCloseOnEsc", cancellable = true)
    private void shouldCloseOnEsc(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    // actually return to parent screen and not to the title screen
    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(parent);
    }
}
