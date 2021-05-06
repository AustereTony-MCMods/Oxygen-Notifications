package austeretony.oxygen_notifications.client.gui.notifications;

import austeretony.oxygen_core.client.gui.base.Texts;
import austeretony.oxygen_core.client.gui.base.background.Background;
import austeretony.oxygen_core.client.gui.base.block.Text;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.list.ScrollableList;
import austeretony.oxygen_core.client.gui.base.text.TextLabel;
import austeretony.oxygen_core.client.gui.util.OxygenGUIUtils;
import austeretony.oxygen_notifications.client.NotificationWrapper;
import austeretony.oxygen_notifications.client.NotificationsManagerClient;
import com.google.common.primitives.Longs;

import javax.annotation.Nonnull;
import java.util.*;

public class NotificationsSection extends Section {

    private ScrollableList<NotificationWrapper> notificationsList;
    private TextLabel noPendingNotificationsLabel;

    @Nonnull
    private List<NotificationWrapper> cache = new ArrayList<>();

    private static final Comparator<NotificationWrapper> RECEIVE_TIME_COMPARATOR =
            (e1, e2) -> Longs.compare(e1.getReceiveTimeMillis(), e2.getReceiveTimeMillis());
    private static final Comparator<NotificationWrapper> TYPE_COMPARATOR =
            (e1, e2) -> e2.getNotification().getType().compareTo(e1.getNotification().getType());

    public NotificationsSection(@Nonnull NotificationsScreen screen) {
        super(screen, "", true);
    }

    @Override
    public void init() {
        addWidget(new Background.UnderlinedTitle(this));
        addWidget(new TextLabel(4, 12, Texts.title("oxygen_notifications.gui.notifications.title")));

        addWidget(notificationsList = new ScrollableList<>(4, 16, 8, 200 - 4 - 4, 24));

        Text text = Texts.additionalDark("oxygen_notifications.gui.notifications.no_pending_notifications");
        addWidget(noPendingNotificationsLabel = new TextLabel((int) ((getWidth() - text.getWidth()) / 2F), 26, text));
    }

    @Override
    public void update() {
        super.update();

        List<NotificationWrapper> notifications = new ArrayList<>(NotificationsManagerClient.instance()
                .getNotificationsMap().values());
        if (!cache.equals(notifications)) {
            notificationsList.clear();

            notifications.sort(RECEIVE_TIME_COMPARATOR);
            notifications.sort(TYPE_COMPARATOR);

            for (NotificationWrapper wrapper : notifications) {
                notificationsList.addElement(new NotificationListEntry(wrapper));
            }
        }

        cache = notifications;
        noPendingNotificationsLabel.setVisible(cache.isEmpty());
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        OxygenGUIUtils.closeScreenOnKeyPress(getScreen(), keyCode);
        super.keyTyped(typedChar, keyCode);
    }
}
