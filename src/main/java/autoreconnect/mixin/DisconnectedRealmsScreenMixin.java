package autoreconnect.mixin;

import autoreconnect.DisconnectedScreenUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedRealmsScreen.class)
public class DisconnectedRealmsScreenMixin extends Screen {
    @Unique
    @Mutable
    private @Final DisconnectedScreenUtil util;

    protected DisconnectedRealmsScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void constructor(Screen parent, Text title, Text reason, CallbackInfo ci) {
        util = new DisconnectedScreenUtil(this, null, this::remove, this::addDrawableChild, super::keyPressed);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        util.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return util.keyPressed(keyCode, scanCode, modifiers);
    }
}
