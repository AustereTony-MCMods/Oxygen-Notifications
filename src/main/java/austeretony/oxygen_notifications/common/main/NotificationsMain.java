package austeretony.oxygen_notifications.common.main;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.command.CommandOxygenClient;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuHelper;
import austeretony.oxygen_core.common.api.OxygenCommon;
import austeretony.oxygen_core.common.main.OxygenMain;
import austeretony.oxygen_core.common.util.MinecraftCommon;
import austeretony.oxygen_core.server.api.OxygenServer;
import austeretony.oxygen_notifications.client.NotificationsManagerClient;
import austeretony.oxygen_notifications.client.command.NotificationsArgumentClient;
import austeretony.oxygen_notifications.client.event.NotificationsEventsClient;
import austeretony.oxygen_notifications.client.gui.notifications.NotificationsScreen;
import austeretony.oxygen_notifications.client.gui.overlay.NotificationsOverlay;
import austeretony.oxygen_notifications.client.settings.NotificationsSettings;
import austeretony.oxygen_notifications.common.config.NotificationsConfig;
import austeretony.oxygen_notifications.common.network.client.CPNotification;
import austeretony.oxygen_notifications.server.notification.NotificationProviderImpl;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = NotificationsMain.MOD_ID,
        name = NotificationsMain.NAME,
        version = NotificationsMain.VERSION,
        dependencies = "required-after:oxygen_core@[0.12.0,);",
        certificateFingerprint = "@FINGERPRINT@",
        updateJSON = NotificationsMain.VERSIONS_FORGE_URL)
public class NotificationsMain {

    public static final String
            MOD_ID = "oxygen_notifications",
            NAME = "Oxygen: Notifications",
            VERSION = "0.12.0",
            VERSION_CUSTOM = VERSION + ":beta:0",
            VERSIONS_FORGE_URL = "https://raw.githubusercontent.com/AustereTony-MCMods/Oxygen-Notifications/info/versions.json";

    //oxygen module index
    public static final int MODULE_INDEX = 1;

    //screen id
    public static final int SCREEN_ID_NOTIFICATIONS = 10;

    //key binding id
    public static final int
            KEYBINDING_ID_OPEN_NOTIFICATION_MENU = 10,
            KEYBINDING_ID_ACCEPT_REQUEST = 11,
            KEYBINDING_ID_REJECT_REQUEST = 12;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OxygenCommon.registerConfig(new NotificationsConfig());
        if (event.getSide() == Side.CLIENT) {
            CommandOxygenClient.registerArgument(new NotificationsArgumentClient());
            OxygenClient.registerKeyBind(
                    KEYBINDING_ID_OPEN_NOTIFICATION_MENU,
                    "key.oxygen_notifications.open_notifications_menu",
                    OxygenMain.KEY_BINDINGS_CATEGORY,
                    NotificationsConfig.NOTIFICATIONS_SCREEN_KEY_ID::asInt,
                    NotificationsConfig.ENABLE_NOTIFICATIONS_SCREEN_KEY::asBoolean,
                    true,
                    () -> OxygenClient.openScreen(SCREEN_ID_NOTIFICATIONS));
            OxygenClient.registerKeyBind(
                    KEYBINDING_ID_ACCEPT_REQUEST,
                    "key.oxygen_notifications.accept_request",
                    OxygenMain.KEY_BINDINGS_CATEGORY,
                    NotificationsConfig.ACCEPT_REQUEST_KEY_ID::asInt,
                    NotificationsConfig.ENABLE_REQUEST_QUICK_KEYS::asBoolean,
                    false,
                    NotificationsManagerClient.instance()::acceptLatestRequest);
            OxygenClient.registerKeyBind(
                    KEYBINDING_ID_REJECT_REQUEST,
                    "key.oxygen_notifications.reject_request",
                    OxygenMain.KEY_BINDINGS_CATEGORY,
                    NotificationsConfig.REJECT_REQUEST_KEY_ID::asInt,
                    NotificationsConfig.ENABLE_REQUEST_QUICK_KEYS::asBoolean,
                    false,
                    NotificationsManagerClient.instance()::rejectLatestRequest);
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        initNetwork();
        OxygenServer.registerNotificationsProvider(new NotificationProviderImpl());
        if (event.getSide() == Side.CLIENT) {
            OxygenClient.registerNotificationProvider(NotificationsManagerClient.instance());
            MinecraftCommon.registerEventHandler(new NotificationsEventsClient());
            MinecraftCommon.registerEventHandler(new NotificationsOverlay());
            NotificationsSettings.register();
            OxygenMenuHelper.addMenuEntry(NotificationsScreen.NOTIFICATIONS_SCREEN_ENTRY);
            OxygenClient.registerScreen(SCREEN_ID_NOTIFICATIONS, NotificationsScreen::open);
        }
    }

    private static void initNetwork() {
        OxygenMain.network().registerPacket(CPNotification.class);
    }
}
