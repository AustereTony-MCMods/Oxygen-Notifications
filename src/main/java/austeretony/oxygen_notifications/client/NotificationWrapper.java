package austeretony.oxygen_notifications.client;

import austeretony.oxygen_core.common.notification.NotificationImpl;
import net.minecraft.nbt.NBTTagCompound;

public class NotificationWrapper {

    private final NotificationImpl notification;
    private final long receiveTimeMillis;

    private boolean notified;

    public NotificationWrapper(NotificationImpl notification, long receiveTimeMillis) {
        this.notification = notification;
        this.receiveTimeMillis = receiveTimeMillis;
    }

    public NotificationImpl getNotification() {
        return notification;
    }

    public long getReceiveTimeMillis() {
        return receiveTimeMillis;
    }

    public boolean isPlayerNotified() {
        return notified;
    }

    public void setPlayerNotified(boolean flag) {
        notified = flag;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setTag("notification", notification.writeToNBT());
        tagCompound.setLong("receive_time_millis", receiveTimeMillis);
        tagCompound.setBoolean("notified", notified);
        return tagCompound;
    }

    public static NotificationWrapper readFromNBT(NBTTagCompound tagCompound) {
        NotificationWrapper notification = new NotificationWrapper(NotificationImpl.readFromNBT(tagCompound.getCompoundTag("notification")),
                tagCompound.getLong("receive_time_millis"));
        notification.notified = tagCompound.getBoolean("notified");
        return notification;
    }
}
