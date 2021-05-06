package austeretony.oxygen_notifications.client.gui.notifications;

import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.gui.base.core.OxygenScreen;
import austeretony.oxygen_core.client.gui.base.core.Section;
import austeretony.oxygen_core.client.gui.base.core.Workspace;
import austeretony.oxygen_core.client.gui.menu.OxygenMenuEntry;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_notifications.client.gui.menu.NotificationsScreenMenuEntry;
import austeretony.oxygen_notifications.client.settings.NotificationsSettings;
import austeretony.oxygen_notifications.common.main.NotificationsMain;

public class NotificationsScreen extends OxygenScreen {

    public static final OxygenMenuEntry NOTIFICATIONS_SCREEN_ENTRY = new NotificationsScreenMenuEntry();

    private Section section;

    @Override
    public int getScreenId() {
        return NotificationsMain.SCREEN_ID_NOTIFICATIONS;
    }

    @Override
    public Workspace createWorkspace() {
        Workspace workspace = new Workspace(this, 200, 215);
        workspace.setAlignment(Alignment.valueOf(NotificationsSettings.NOTIFICATIONS_SCREEN_ALIGNMENT.asString()), 0, 0);
        return workspace;
    }

    @Override
    public void addSections() {
        getWorkspace().addSection(section = new NotificationsSection(this));
    }

    @Override
    public Section getDefaultSection() {
        return section;
    }

    public static void open() {
        MinecraftClient.displayGuiScreen(new NotificationsScreen());
    }
}
