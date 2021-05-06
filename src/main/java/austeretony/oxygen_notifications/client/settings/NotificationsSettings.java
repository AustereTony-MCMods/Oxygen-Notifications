package austeretony.oxygen_notifications.client.settings;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.settings.SettingType;
import austeretony.oxygen_core.client.settings.SettingValue;
import austeretony.oxygen_core.client.settings.gui.SettingWidgets;
import austeretony.oxygen_core.client.settings.gui.alignment.ScreenAlignment;
import austeretony.oxygen_core.common.util.value.ValueType;
import austeretony.oxygen_notifications.common.main.NotificationsMain;

public final class NotificationsSettings {

    public static final SettingValue
            NOTIFICATIONS_SCREEN_ALIGNMENT = OxygenClient.registerSetting(NotificationsMain.MOD_ID, SettingType.INTERFACE, "Notifications", "alignment",
            ValueType.STRING, "notifications_screen_alignment", Alignment.CENTER.toString(), SettingWidgets.screenAlignmentList()),
    NOTIFICATIONS_OVERLAY_ALIGNMENT = OxygenClient.registerSetting(NotificationsMain.MOD_ID, SettingType.INTERFACE, "Notifications", "alignment",
            ValueType.STRING, "notifications_overlay_alignment", Alignment.RIGHT.toString(),
            SettingWidgets.screenAlignmentList(ScreenAlignment.LEFT, ScreenAlignment.RIGHT)),
    NOTIFICATIONS_OVERLAY_VERTICAL_OFFSET = OxygenClient.registerSetting(NotificationsMain.MOD_ID, SettingType.INTERFACE, "Notifications", "misc",
            ValueType.INTEGER, "notifications_overlay_vertical_offset", 0, SettingWidgets.pixelsSizeValueSelector(0, 1000)),

    ADD_NOTIFICATIONS_SCREEN_TO_OXYGEN_MENU = OxygenClient.registerSetting(NotificationsMain.MOD_ID, SettingType.COMMON, "Notifications", "oxygen_menu",
            ValueType.BOOLEAN, "add_notifications_screen", true, SettingWidgets.checkBox());

    private NotificationsSettings() {}

    public static void register() {}
}
