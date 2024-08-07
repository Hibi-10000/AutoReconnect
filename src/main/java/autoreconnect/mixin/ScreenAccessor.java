package autoreconnect.mixin;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Invoker("keyPressed")
    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    @SuppressWarnings("UnusedReturnValue")
    @Invoker("addDrawableChild")
    <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement);

    @Invoker("remove")
    void remove(Element child);
}
