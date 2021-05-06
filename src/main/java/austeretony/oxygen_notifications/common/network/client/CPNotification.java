package austeretony.oxygen_notifications.common.network.client;

import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_core.common.network.Packet;
import austeretony.oxygen_core.common.notification.Notification;
import austeretony.oxygen_core.common.notification.NotificationImpl;
import austeretony.oxygen_notifications.client.NotificationsManagerClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;

public class CPNotification extends Packet {

    private Notification notification;

    public CPNotification() {}

    public CPNotification(Notification notification) {
        this.notification = notification;
    }

    @Override
    public void write(ByteBuf buffer, INetHandler netHandler) {
        notification.write(buffer);
    }

    @Override
    public void read(ByteBuf buffer, INetHandler netHandler) {
        final NotificationImpl notification = NotificationImpl.read(buffer);
        MinecraftClient.delegateToClientThread(
                () -> NotificationsManagerClient.instance().addNotification(notification));
    }
}
