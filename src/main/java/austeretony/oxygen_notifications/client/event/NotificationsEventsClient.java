package austeretony.oxygen_notifications.client.event;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.event.OxygenClientInitializedEvent;
import austeretony.oxygen_notifications.client.NotificationsManagerClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class NotificationsEventsClient {

    @SubscribeEvent
    public void onClientInitialized(OxygenClientInitializedEvent event) {
        NotificationsManagerClient.instance().clientInitialized();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (OxygenClient.getCurrentTick() % 20L == 0L) {
                NotificationsManagerClient.instance().removeExpiredNotifications();
            }
        }
    }
}
