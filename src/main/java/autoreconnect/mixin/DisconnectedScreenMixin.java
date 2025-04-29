package autoreconnect.mixin;

import autoreconnect.AutoReconnect;
import autoreconnect.DisconnectedScreenUtil;
import autoreconnect.DisconnectedScreenUtil.DisconnectedScreenTransferring;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
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

    protected DisconnectedScreenMixin(Component title) {
        super(title);
    }

    @Inject(
        at = @At("TAIL"),
        method = "<init>(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/DisconnectionDetails;Lnet/minecraft/network/chat/Component;)V"
    )
    private void constructor(Screen parent, Component title, DisconnectionDetails info, Component buttonLabel, CallbackInfo ci) {
        autoreconnect$util = new DisconnectedScreenUtil(this, super::removeWidget, super::addRenderableWidget, super::keyPressed);
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
                btn -> btn.setMessage(Component.translatable("gui.toWorld"))
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
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }
}
