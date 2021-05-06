package austeretony.oxygen_notifications.client.gui.notifications;

import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.Textures;
import austeretony.oxygen_core.client.gui.base.block.Text;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.gui.base.button.ImageButton;
import austeretony.oxygen_core.client.gui.base.common.ListEntry;
import austeretony.oxygen_core.client.gui.base.core.Widget;
import austeretony.oxygen_core.common.notification.Notification;
import austeretony.oxygen_core.common.notification.NotificationType;
import austeretony.oxygen_notifications.client.NotificationWrapper;
import austeretony.oxygen_notifications.client.NotificationsManagerClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;

public class NotificationListEntry extends ListEntry<NotificationWrapper> {

    private static final int BUTTON_SIZE = 6;
    private static final Texture CROSS_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.CROSS_ICONS)
            .size(BUTTON_SIZE, BUTTON_SIZE)
            .imageSize(BUTTON_SIZE * 3, BUTTON_SIZE)
            .build();
    private static final Texture NOTE_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.NOTE_ICONS)
            .size(BUTTON_SIZE, BUTTON_SIZE)
            .imageSize(BUTTON_SIZE * 3, BUTTON_SIZE)
            .build();
    private static final Texture CHECK_ICONS_TEXTURE = Texture.builder()
            .texture(Textures.CHECK_ICONS)
            .size(BUTTON_SIZE, BUTTON_SIZE)
            .imageSize(BUTTON_SIZE * 3, BUTTON_SIZE)
            .build();

    private List<String> lines;
    @Nonnull
    private final Text receiveTimeText;
    @Nonnull
    private final Texture iconTexture;

    public NotificationListEntry(@Nonnull NotificationWrapper entry) {
        super(Texts.additional("").decrementScale(.05F), entry);

        Instant receiveTime = Instant.ofEpochMilli(entry.getReceiveTimeMillis());
        receiveTimeText = Texts.additionalDark(OxygenClient.getDateTimeFormatter().format(receiveTime)).decrementScale(.1F);
        iconTexture = Texture.builder()
                .texture(NotificationsManagerClient.getNotificationIcon(entry.getNotification()))
                .size(20, 20)
                .imageSize(20, 20)
                .build();
    }

    private Notification getNotification() {
        return entry.getNotification();
    }

    @Override
    public void init() {
        lines = GUIUtils.splitTextToLines(localize(getNotification().getMessage(), getNotification().getArguments()),
                text.getScale(), getWidth() - 4 - iconTexture.getWidth());
        boolean isNotification = getNotification().getType() == NotificationType.NOTIFICATION;

        addWidget(new ImageButton(2 + iconTexture.getWidth() + 2, getHeight() - BUTTON_SIZE - 2,
                BUTTON_SIZE, BUTTON_SIZE, CROSS_ICONS_TEXTURE,
                localize(isNotification ? "oxygen_notifications.gui.notifications.remove" : "oxygen_notifications.gui.notifications.reject"))
                .setMouseClickListener((x, y, button) -> {
                    if (getNotification().getType() == NotificationType.NOTIFICATION) {
                        NotificationsManagerClient.instance().removeNotificationSynchronized(getNotification().getUUID());
                    } else {
                        NotificationsManagerClient.instance().rejectRequest(getNotification().getUUID());
                    }
                }));
        if (isNotification) {
            if (getAction() != null) {
                addWidget(new ImageButton(2 + iconTexture.getWidth() + 2 + BUTTON_SIZE + 2,
                        getHeight() - BUTTON_SIZE - 2, BUTTON_SIZE, BUTTON_SIZE, NOTE_ICONS_TEXTURE,
                        localize("oxygen_notifications.gui.notifications.check"))
                        .setMouseClickListener((x, y, button) -> executeAction()));
            }
        } else {
            addWidget(new ImageButton(2 + iconTexture.getWidth() + 2 + BUTTON_SIZE + 2,
                    getHeight() - BUTTON_SIZE - 2, BUTTON_SIZE, BUTTON_SIZE, CHECK_ICONS_TEXTURE,
                    localize("oxygen_notifications.gui.notifications.accept"))
                    .setMouseClickListener(
                            (x, y, button) -> NotificationsManagerClient.instance().acceptRequest(getNotification().getUUID())));
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (!isVisible()) return;
        GUIUtils.pushMatrix();
        GUIUtils.translate(getX(), getY());

        GUIUtils.drawRect(0, 0, getWidth(), getHeight(), getColorFromState(fill));

        GUIUtils.colorDef();
        GUIUtils.drawTexturedRect(2, 2, iconTexture);

        if (getNotification().getType() != NotificationType.NOTIFICATION) {
            long now = OxygenClient.getCurrentTimeMillis();
            String secondsLeft = "[" + (int) Math.max(0F, (float) (entry.getReceiveTimeMillis() + getNotification().getExpirationTimeMillis() - now) / 1000F) + "]";
            GUIUtils.drawString(secondsLeft, 2 + iconTexture.getWidth() + 4 + 6 + 4 + 6 + 4, getHeight() - receiveTimeText.getHeight() - 2F,
                    receiveTimeText.getScale(), getColorFromState(receiveTimeText), receiveTimeText.isShadowEnabled());
        }

        GUIUtils.drawString(receiveTimeText.getText(), getWidth() - 4F - receiveTimeText.getWidth(), getHeight() - receiveTimeText.getHeight() - 2F,
                receiveTimeText.getScale(), getColorFromState(receiveTimeText), receiveTimeText.isShadowEnabled());

        if (lines != null && !lines.isEmpty()) {
            float lineHeight = GUIUtils.getTextHeight(1F);

            GUIUtils.pushMatrix();
            GUIUtils.translate(2F + iconTexture.getWidth() + 2F, 2F);
            GUIUtils.scale(text.getScale(), text.getScale());

            for (int i = 0; i < lines.size(); i++) {
                GUIUtils.drawString(lines.get(i), 0, (lineHeight + 2F) * i, getColorFromState(text), text.isShadowEnabled());
            }

            GUIUtils.popMatrix();
        }

        for (Widget widget : getWidgets()) {
            widget.draw(mouseX, mouseY, partialTicks);
        }

        GUIUtils.popMatrix();
    }

    @Nullable
    private Runnable getAction() {
        return OxygenManagerClient.instance().getNotificationsManager().getActionsMap().get(getNotification().getId());
    }

    private void executeAction() {
        Runnable action = getAction();
        if (action != null) {
            action.run();
        }
    }
}
