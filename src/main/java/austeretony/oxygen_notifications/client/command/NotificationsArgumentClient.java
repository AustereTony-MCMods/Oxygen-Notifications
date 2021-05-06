package austeretony.oxygen_notifications.client.command;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.common.command.CommandArgument;
import austeretony.oxygen_notifications.common.main.NotificationsMain;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class NotificationsArgumentClient implements CommandArgument {

    @Override
    public String getName() {
        return "notifications";
    }

    @Override
    public void process(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length == 1) {
            OxygenClient.openScreenWithDelay(NotificationsMain.SCREEN_ID_NOTIFICATIONS);
        }
    }
}
