package autoreconnect;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DisconnectedScreenUtil {
    private final Screen screen;
    private final Consumer<AbstractWidget> removeConsumer;
    private final Consumer<AbstractWidget> addDrawableChildConsumer;
    private final IntTernaryPredicate keyPressedPredicate;
    private boolean transferring = false;

    public void setTransferring(boolean transferring) {
        this.transferring = transferring;
    }

    public DisconnectedScreenUtil(
        Screen screen,
        Consumer<AbstractWidget> removeConsumer,
        Consumer<AbstractWidget> addDrawableChildConsumer,
        IntTernaryPredicate keyPressedPredicate
    ) {
        this.screen = screen;
        this.removeConsumer = removeConsumer;
        this.addDrawableChildConsumer = addDrawableChildConsumer;
        this.keyPressedPredicate = keyPressedPredicate;
    }

    private Button reconnectButton;
    private Button cancelButton;
    private Button backButton;
    private boolean shouldAutoReconnect;

    public void init() {
        backButton = autoreconnect.AutoReconnect.findBackButton(screen).orElseThrow(
            () -> new NoSuchElementException("Couldn't find the back button on the disconnect screen")
        );

        shouldAutoReconnect = !transferring && autoreconnect.AutoReconnect.getConfig().hasAttempts();

        LinearLayout reconnectWidget = LinearLayout.horizontal().spacing(4);

        reconnectButton = Button.builder(
            Component.translatable("text.autoreconnect.disconnect.reconnect"),
            btn -> autoreconnect.AutoReconnect.schedule(
                () -> Minecraft.getInstance().execute(this::manualReconnect),
                100,
                TimeUnit.MILLISECONDS
            )
        ).size(0, 20).build();

        reconnectWidget.addChild(reconnectButton);

        // put reconnect (and cancel button) where back button is and push that down
        reconnectWidget.setPosition(backButton.getX(), backButton.getY());
        if (shouldAutoReconnect) {
            reconnectButton.setWidth(backButton.getWidth() - backButton.getHeight() - 4);

            cancelButton = Button.builder(
                Component.literal("✕").withStyle(
                    s -> s.withColor(ChatFormatting.RED)
                ),
                btn -> cancelCountdown()
            ).size(
                backButton.getHeight(),
                backButton.getHeight()
            ).build();

            reconnectWidget.addChild(cancelButton);
        } else {
            reconnectButton.setWidth(backButton.getWidth());
        }
        reconnectWidget.arrangeElements();
        reconnectWidget.visitWidgets(addDrawableChildConsumer);
        backButton.setY(backButton.getY() + backButton.getHeight() + 4);

        if (shouldAutoReconnect) {
            autoreconnect.AutoReconnect.getInstance().startCountdown(this::countdownCallback);
        }
    }

    private void manualReconnect() {
        autoreconnect.AutoReconnect.getInstance().cancelAutoReconnect();
        autoreconnect.AutoReconnect.getInstance().reconnect();
    }

    private void cancelCountdown() {
        AutoReconnect.getInstance().cancelAutoReconnect();
        shouldAutoReconnect = false;
        removeConsumer.accept(cancelButton);
        reconnectButton.active = true; // in case it was deactivated after running out of attempts
        reconnectButton.setMessage(Component.translatable("text.autoreconnect.disconnect.reconnect"));
        reconnectButton.setWidth(backButton.getWidth()); // reset to full width
    }

    private void countdownCallback(int seconds) {
        if (seconds < 0) {
            // indicates that we're out of attempts
            reconnectButton.setMessage(
                Component.translatable("text.autoreconnect.disconnect.reconnect_failed").withStyle(
                    s -> s.withColor(ChatFormatting.RED)
                )
            );
            reconnectButton.active = false;
        } else {
            reconnectButton.setMessage(
                Component.translatable("text.autoreconnect.disconnect.reconnect_in", seconds).withStyle(
                    s -> s.withColor(ChatFormatting.GREEN)
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
            return keyPressedPredicate.test(keyCode, scanCode, modifiers);
        }
    }

    @FunctionalInterface
    public interface IntTernaryPredicate {
        boolean test(int left, int center, int right);
    }

    public interface DisconnectedScreenTransferring {
        void autoreconnect$setTransferring(boolean transferring);
    }
}
