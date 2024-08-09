package autoreconnect.mixin;

import autoreconnect.DisconnectedScreenUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedRealmsScreen.class)
public class DisconnectedRealmsScreenMixin extends RealmsScreen {
    @Unique
    @Mutable
    private @Final DisconnectedScreenUtil autoreconnect$util;

    protected DisconnectedRealmsScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void constructor(Screen parent, Text title, Text reason, CallbackInfo ci) {
        autoreconnect$util = new DisconnectedScreenUtil(this, this::remove, this::addDrawableChild, super::keyPressed);
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void init(CallbackInfo ci) {
        autoreconnect$util.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return autoreconnect$util.keyPressed(keyCode, scanCode, modifiers);
    }
}
