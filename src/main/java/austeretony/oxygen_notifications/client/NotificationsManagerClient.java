package austeretony.oxygen_notifications.client;

import austeretony.oxygen_core.client.OxygenManagerClient;
import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.notification.NotificationProviderClient;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.network.packets.server.SPRequestAction;
import austeretony.oxygen_core.common.notification.Notification;
import austeretony.oxygen_core.common.notification.NotificationMode;
import austeretony.oxygen_core.common.notification.NotificationType;
import austeretony.oxygen_core.common.notification.NotificationImpl;
import austeretony.oxygen_core.common.persistent.AbstractPersistentData;
import austeretony.oxygen_core.common.sound.SoundEffects;
import austeretony.oxygen_notifications.common.main.NotificationsMain;
import com.google.common.primitives.Longs;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class NotificationsManagerClient extends AbstractPersistentData implements NotificationProviderClient {

    private static NotificationsManagerClient instance;

    private static final ResourceLocation DEFAULT_REQUEST_ICON = new ResourceLocation(NotificationsMain.MOD_ID,
            "textures/gui/notifications/request_0.png");

    private final Map<UUID, NotificationWrapper> notificationsMap = new HashMap<>();

    @Nullable
    private UUID latestRequestUUID;

    private NotificationsManagerClient() {
        for (int i = 0; i < 10; i++) {
            OxygenClient.registerNotificationIcon(i,
                    new ResourceLocation(NotificationsMain.MOD_ID, "textures/gui/notifications/notification_" + i + ".png"));
        }
        OxygenClient.registerPersistentData(this);
    }

    public void removeExpiredNotifications() {
        Iterator<NotificationWrapper> iterator = notificationsMap.values().iterator();
        while (iterator.hasNext()) {
            NotificationWrapper notificationWrapper = iterator.next();
            if (isRequestExpired(notificationWrapper)) {
                iterator.remove();
                if (latestRequestUUID != null
                        && latestRequestUUID.equals(notificationWrapper.getNotification().getUUID())) {
                    updateLatestRequestUUID();
                }
            }
        }
        notificationsMap.values().removeIf(NotificationsManagerClient::isRequestExpired);
    }

    public static NotificationsManagerClient instance() {
        if (instance == null)
            instance = new NotificationsManagerClient();
        return instance;
    }

    public Map<UUID, NotificationWrapper> getNotificationsMap() {
        return notificationsMap;
    }

    public void clientInitialized() {
        OxygenClient.loadPersistentDataAsync(this);
    }

    public void addNotification(NotificationImpl notification) {
        if (notification.getType() != NotificationType.NOTIFICATION) {
            if (CoreSettings.ENABLE_SOUND_EFFECTS.asBoolean()) {
                MinecraftClient.playUISound(SoundEffects.uiNotificationReceived);
            }
            if (latestRequestUUID == null) {
                latestRequestUUID = notification.getUUID();
            }
        }

        notificationsMap.put(notification.getUUID(),
                new NotificationWrapper(notification, OxygenClient.getCurrentTimeMillis()));
        markChanged();
    }

    public void removeNotificationSynchronized(UUID notificationUUID) {
        removeNotification(notificationUUID);
    }

    private void removeNotification(UUID notificationUUID) {
        notificationsMap.remove(notificationUUID);
        markChanged();
    }

    @Override
    public void acceptLatestRequest() {
        if (latestRequestUUID != null) {
            OxygenMain.network().sendToServer(new SPRequestAction(latestRequestUUID, true));
            removeNotification(latestRequestUUID);
            updateLatestRequestUUID();
        }
    }

    @Override
    public void rejectLatestRequest() {
        if (latestRequestUUID != null) {
            OxygenMain.network().sendToServer(new SPRequestAction(latestRequestUUID, false));
            removeNotification(latestRequestUUID);
            updateLatestRequestUUID();
        }
    }

    private void updateLatestRequestUUID() {
        latestRequestUUID = null;

        NotificationWrapper latest = null;
        for (NotificationWrapper wrapper : notificationsMap.values()) {
            if (wrapper.getNotification().getType() == NotificationType.REQUEST_STANDARD
                    || wrapper.getNotification().getType() == NotificationType.REQUEST_VOTING) {
                if (latest == null
                        || Longs.compare(wrapper.getReceiveTimeMillis(), latest.getReceiveTimeMillis()) == 1) {
                    latest = wrapper;
                }
            }
        }

        if (latest != null) {
            latestRequestUUID = latest.getNotification().getUUID();
        }
    }

    public void acceptRequest(UUID requestUUID) {
        OxygenMain.network().sendToServer(new SPRequestAction(requestUUID, true));
        removeNotification(requestUUID);
        updateLatestRequestUUID();
    }

    public void rejectRequest(UUID requestUUID) {
        OxygenMain.network().sendToServer(new SPRequestAction(requestUUID, false));
        removeNotification(requestUUID);
        updateLatestRequestUUID();
    }

    public static boolean isRequestExpired(NotificationWrapper notification) {
        if (notification.getNotification().getType() == NotificationType.NOTIFICATION) return false;
        return OxygenClient.getCurrentTimeMillis() >= notification.getReceiveTimeMillis()
                + notification.getNotification().getExpirationTimeMillis();
    }

    @Override
    public String getName() {
        return "notifications:notifications_data";
    }

    @Override
    public String getPath() {
        return OxygenClient.getDataFolder() + "/client/players/" + OxygenClient.getClientPlayerUUID()
                + "/notifications/notifications.dat";
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList tagList = new NBTTagList();
        for (NotificationWrapper wrapper : notificationsMap.values()) {
            if (wrapper.getNotification().getType() == NotificationType.NOTIFICATION
                    && wrapper.getNotification().getMode() != NotificationMode.NO_SAVE) {
                tagList.appendTag(wrapper.writeToNBT());
            }
        }
        tagCompound.setTag("notifications_list", tagList);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList tagList = tagCompound.getTagList("notifications_list", 10);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NotificationWrapper notificationWrapper = NotificationWrapper.readFromNBT(tagList.getCompoundTagAt(i));
            notificationsMap.put(notificationWrapper.getNotification().getUUID(), notificationWrapper);
        }
    }

    @Override
    public void reset() {
        notificationsMap.clear();
    }


    @Nonnull
    public static ResourceLocation getNotificationIcon(Notification notification) {
        ResourceLocation iconLocation = OxygenManagerClient.instance().getNotificationsManager().getIconsMap()
                .get(notification.getId());
        if (iconLocation != null) {
            return iconLocation;
        }
        if (notification.getType() != NotificationType.NOTIFICATION) {
            return DEFAULT_REQUEST_ICON;
        }
        return GUIUtils.getMissingTextureLocation();
    }
}
