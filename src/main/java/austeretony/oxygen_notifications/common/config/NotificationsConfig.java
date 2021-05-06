package austeretony.oxygen_notifications.common.config;

import austeretony.oxygen_core.common.config.AbstractConfig;
import austeretony.oxygen_core.common.config.ConfigValue;
import austeretony.oxygen_core.common.config.ConfigValueUtils;
import austeretony.oxygen_notifications.common.main.NotificationsMain;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class NotificationsConfig extends AbstractConfig {

    public static final ConfigValue
            ENABLE_NOTIFICATIONS_SCREEN_KEY = ConfigValueUtils.getBoolean("client", "enable_notifications_screen_key", true),
            NOTIFICATIONS_SCREEN_KEY_ID = ConfigValueUtils.getInt("client", "notifications_screen_key_id", Keyboard.KEY_N),
            ENABLE_REQUEST_QUICK_KEYS = ConfigValueUtils.getBoolean("client", "enable_request_quick_keys", true),
            ACCEPT_REQUEST_KEY_ID = ConfigValueUtils.getInt("client", "accept_request_key_id", Keyboard.KEY_R),
            REJECT_REQUEST_KEY_ID = ConfigValueUtils.getInt("client", "reject_request_key_id", Keyboard.KEY_X);

    @Override
    public String getDomain() {
        return NotificationsMain.MOD_ID;
    }

    @Override
    public String getVersion() {
        return NotificationsMain.VERSION_CUSTOM;
    }

    @Override
    public String getFileName() {
        return "notifications.json";
    }

    @Override
    public void getValues(List<ConfigValue> values) {
        values.add(ENABLE_NOTIFICATIONS_SCREEN_KEY);
        values.add(NOTIFICATIONS_SCREEN_KEY_ID);
        values.add(ENABLE_REQUEST_QUICK_KEYS);
        values.add(ACCEPT_REQUEST_KEY_ID);
        values.add(REJECT_REQUEST_KEY_ID);
    }
}
