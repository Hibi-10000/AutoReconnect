package autoreconnect.mixin;

import autoreconnect.AutoReconnect;
import autoreconnect.DisconnectedScreenUtil;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin extends Screen {
    @Shadow
    @Mutable
    private @Final Screen parent;
    @Unique
    @Mutable
    private @Final DisconnectedScreenUtil util;

    protected DisconnectedScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;)V")
    private void constructor(Screen parent, Text title, Text reason, Text buttonLabel, CallbackInfo ci) {
        util = new DisconnectedScreenUtil(this, reason);
        if (AutoReconnect.getInstance().isPlayingSingleplayer()) {
            // make back button redirect to SelectWorldScreen instead of MultiPlayerScreen (https://bugs.mojang.com/browse/MC-45602)
            this.parent = new SelectWorldScreen(new TitleScreen());
        }
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void init(CallbackInfo ci) {
        util.init();
        if (AutoReconnect.getInstance().isPlayingSingleplayer()) {
            // change back button text to "Back" instead of "Back to World List" bcs of bug fix above
            AutoReconnect.findBackButton(this).ifPresent(
                btn -> btn.setMessage(Text.translatable("gui.toWorld"))
            );
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return util.keyPressed(keyCode, scanCode, modifiers);
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
