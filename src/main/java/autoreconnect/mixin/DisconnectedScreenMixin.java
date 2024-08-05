package autoreconnect.mixin;

import autoreconnect.AutoReconnect;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DisconnectedScreen.class, priority = 800)
public class DisconnectedScreenMixin extends Screen {
    @Shadow
    @Final
    @Mutable
    private Screen parent;
    @Shadow
    @Final
    private Text reason;

    protected DisconnectedScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;Lnet/minecraft/text/Text;)V")
    private void constructor(Screen parent, Text title, Text reason, Text buttonLabel, CallbackInfo info) {
        if (AutoReconnect.getInstance().isPlayingSingleplayer()) {
            // make back button redirect to SelectWorldScreen instead of MultiPlayerScreen (https://bugs.mojang.com/browse/MC-45602)
            this.parent = new SelectWorldScreen(new TitleScreen());
        }
    }

    @Inject(at = @At("TAIL"), method = "init", cancellable = true)
    private void initTail(CallbackInfo ci) {
        if (reason instanceof MutableText text
            && text.getContent() instanceof TranslatableTextContent translatable
            && translatable.getKey().equals("disconnect.transfer")
        ) {
            ci.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "init")
    private void init(CallbackInfo info) {
        if (AutoReconnect.getInstance().isPlayingSingleplayer()) {
            // change back button text to "Back" instead of "Back to World List" bcs of bug fix above
            AutoReconnect.findBackButton(this).ifPresent(btn -> btn.setMessage(Text.translatable("gui.toWorld")));
        }
    }

    // make this screen closable by pressing escape
    @Inject(at = @At("RETURN"), method = "shouldCloseOnEsc", cancellable = true)
    private void shouldCloseOnEsc(CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(true);
    }

    // actually return to parent screen and not to the title screen
    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(parent);
    }
}
