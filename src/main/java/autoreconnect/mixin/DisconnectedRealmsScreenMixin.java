package autoreconnect.mixin;

import autoreconnect.DisconnectedScreenUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
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

    protected DisconnectedRealmsScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void constructor(Screen parent, Component title, Component reason, CallbackInfo ci) {
        autoreconnect$util = new DisconnectedScreenUtil(this, super::removeWidget, super::addRenderableWidget, super::keyPressed);
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
