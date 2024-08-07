package autoreconnect;

import autoreconnect.mixin.ScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class DisconnectedScreenUtil {
    private final Screen screen;
    private final Text reason;
    public DisconnectedScreenUtil(Screen screen, Text reason) {
        this.screen = screen;
        this.reason = reason;
    }

    private ButtonWidget reconnectButton, cancelButton, backButton;
    private boolean shouldAutoReconnect;

    public void init() {
        backButton = AutoReconnect.findBackButton(screen).orElseThrow(
            () -> new NoSuchElementException("Couldn't find the back button on the disconnect screen")
        );

        if (reason.getContent() instanceof TranslatableTextContent translatable
            && translatable.getKey().equals("disconnect.transfer")
        ) {
            shouldAutoReconnect = false;
        } else {
            shouldAutoReconnect = AutoReconnect.getConfig().hasAttempts();
        }

        reconnectButton = ButtonWidget.builder(
            Text.translatable("text.autoreconnect.disconnect.reconnect"),
            btn -> AutoReconnect.schedule(
                () -> MinecraftClient.getInstance().execute(this::manualReconnect),
                100,
                TimeUnit.MILLISECONDS
            )
        ).dimensions(0, 0, 0, 20).build();

        // put reconnect (and cancel button) where back button is and push that down
        reconnectButton.setX(backButton.getX());
        reconnectButton.setY(backButton.getY());
        if (shouldAutoReconnect) {
            reconnectButton.setWidth(backButton.getWidth() - backButton.getHeight() - 4);

            cancelButton = ButtonWidget.builder(
                Text.literal("✕").styled(
                    s -> s.withColor(Formatting.RED)
                ),
                btn -> cancelCountdown()
            ).dimensions(
                backButton.getX() + backButton.getWidth() - backButton.getHeight(),
                backButton.getY(),
                backButton.getHeight(),
                backButton.getHeight()
            ).build();

            ((ScreenAccessor) screen).addDrawableChild(cancelButton);
        } else {
            reconnectButton.setWidth(backButton.getWidth());
        }
        ((ScreenAccessor) screen).addDrawableChild(reconnectButton);
        backButton.setY(backButton.getY() + backButton.getHeight() + 4);

        if (shouldAutoReconnect) {
            AutoReconnect.getInstance().startCountdown(this::countdownCallback);
        }
    }

    private void manualReconnect() {
        AutoReconnect.getInstance().cancelAutoReconnect();
        AutoReconnect.getInstance().reconnect();
    }

    private void cancelCountdown() {
        AutoReconnect.getInstance().cancelAutoReconnect();
        shouldAutoReconnect = false;
        ((ScreenAccessor) screen).remove(cancelButton);
        reconnectButton.active = true; // in case it was deactivated after running out of attempts
        reconnectButton.setMessage(Text.translatable("text.autoreconnect.disconnect.reconnect"));
        reconnectButton.setWidth(backButton.getWidth()); // reset to full width
    }

    private void countdownCallback(int seconds) {
        if (seconds < 0) {
            // indicates that we're out of attempts
            reconnectButton.setMessage(
                Text.translatable("text.autoreconnect.disconnect.reconnect_failed").styled(
                    s -> s.withColor(Formatting.RED)
                )
            );
            reconnectButton.active = false;
        } else {
            reconnectButton.setMessage(
                Text.translatable("text.autoreconnect.disconnect.reconnect_in", seconds).styled(
                    s -> s.withColor(Formatting.GREEN)
                )
            );
        }
    }

    // cancel auto reconnect when pressing escape, higher priority than exiting the screen
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && shouldAutoReconnect) {
            cancelCountdown();
            return true;
        } else {
            return ((ScreenAccessor) screen).keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
