package austeretony.oxygen_notifications.client.gui.menu;

import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_notifications.client.settings.NotificationsSettings;
import austeretony.oxygen_notifications.common.config.NotificationsConfig;
import austeretony.oxygen_notifications.common.main.NotificationsMain;
import net.minecraft.util.ResourceLocation;

public class NotificationsScreenMenuEntry implements OxygenMenuEntry {

    private static final ResourceLocation ICON = new ResourceLocation(NotificationsMain.MOD_ID,
            "textures/gui/menu/notifications.png");

    @Override
    public int getScreenId() {
        return NotificationsMain.SCREEN_ID_NOTIFICATIONS;
    }

    @Override
    public String getDisplayName() {
        return MinecraftClient.localize("oxygen_notifications.gui.notifications.title");
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public ResourceLocation getIconTexture() {
        return ICON;
    }

    @Override
    public int getKeyCode() {
        return NotificationsConfig.NOTIFICATIONS_SCREEN_KEY_ID.asInt();
    }

    @Override
    public boolean isValid() {
        return NotificationsSettings.ADD_NOTIFICATIONS_SCREEN_TO_OXYGEN_MENU.asBoolean();
    }
}
