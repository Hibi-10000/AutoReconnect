package autoreconnect.mixin;

import autoreconnect.DisconnectedScreenUtil;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ DisconnectedScreen.class, DisconnectedRealmsScreen.class })
public class DisconnectedScreensMixin extends Screen {
    @Unique
    private final DisconnectedScreenUtil util = new DisconnectedScreenUtil(this);

    protected DisconnectedScreensMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo info) {
        util.init();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return util.keyPressed(keyCode, scanCode, modifiers);
    }
}
