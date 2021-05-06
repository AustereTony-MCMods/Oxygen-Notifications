package austeretony.oxygen_notifications.server.notification;

import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.notification.Notification;
import austeretony.oxygen_core.server.notification.NotificationProviderServer;
import austeretony.oxygen_notifications.common.network.client.CPNotification;
import net.minecraft.entity.player.EntityPlayerMP;

public class NotificationProviderImpl implements NotificationProviderServer {

    @Override
    public boolean sendNotification(EntityPlayerMP targetMP, Notification notification) {
        OxygenMain.network().sendTo(new CPNotification(notification), targetMP);
        return true;
    }
}
