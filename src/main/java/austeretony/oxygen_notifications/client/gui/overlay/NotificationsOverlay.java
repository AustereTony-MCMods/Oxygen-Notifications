package austeretony.oxygen_notifications.client.gui.overlay;

import austeretony.oxygen_core.client.api.OxygenClient;
import austeretony.oxygen_core.client.gui.base.Alignment;
import austeretony.oxygen_core.client.gui.base.GUIUtils;
import austeretony.oxygen_core.client.gui.base.block.Texture;
import austeretony.oxygen_core.client.settings.CoreSettings;
import austeretony.oxygen_core.client.util.MinecraftClient;
import austeretony.oxygen_core.common.notification.NotificationMode;
import austeretony.oxygen_core.common.notification.NotificationType;
import austeretony.oxygen_core.common.util.MathUtils;
import austeretony.oxygen_notifications.client.NotificationWrapper;
import austeretony.oxygen_notifications.client.NotificationsManagerClient;
import austeretony.oxygen_notifications.client.settings.NotificationsSettings;
import austeretony.oxygen_notifications.common.config.NotificationsConfig;
import austeretony.oxygen_notifications.common.main.NotificationsMain;
import com.google.common.primitives.Longs;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class NotificationsOverlay {

    private RequestEntry currRequestEntry;
    private final Set<NotificationEntry> notificationsQueue = new TreeSet<>();

    private ScaledResolution sr;

    private static final int MAX_NOTIFICATIONS_DISPLAY_AMOUNT = 4;

    private static final Comparator<NotificationWrapper> RECEIVE_TIME_COMPARATOR =
            (e1, e2) -> Longs.compare(e1.getReceiveTimeMillis(), e2.getReceiveTimeMillis());

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            update();
        }
    }

    @SubscribeEvent
    public void onOverlayRendering(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL && !MinecraftClient.hasActiveGUI()) {
            draw(event.getPartialTicks());
        }
    }

    private void update() {
        EntityPlayer player = MinecraftClient.getPlayer();
        if (player == null || player.ticksExisted % 20 != 0) return;
        sr = GUIUtils.getScaledResolution();

        List<NotificationWrapper> notificationsSorted = new ArrayList<>(NotificationsManagerClient.instance()
                .getNotificationsMap().values());
        notificationsSorted.sort(RECEIVE_TIME_COMPARATOR);

        Iterator<NotificationWrapper> iterator = notificationsSorted.iterator();
        while (iterator.hasNext()) {
            NotificationWrapper notificationWrapper = iterator.next();
            if (notificationWrapper != null) {
                if (notificationWrapper.getNotification().getType() == NotificationType.REQUEST_STANDARD) {
                    if (currRequestEntry == null) {
                        currRequestEntry = new RequestEntry(notificationWrapper);
                    }
                } else if (notificationsQueue.size() < MAX_NOTIFICATIONS_DISPLAY_AMOUNT && !notificationWrapper.isPlayerNotified()) {
                    NotificationEntry entry = new NotificationEntry(notificationWrapper);
                    if (!notificationsQueue.contains(entry)) {
                        notificationWrapper.setPlayerNotified(true);
                        notificationsQueue.add(entry);

                        if (notificationWrapper.getNotification().getMode() == NotificationMode.OVERLAY_ONLY) {
                            iterator.remove();
                        }
                    }
                }
            }
        }

        if (currRequestEntry != null && NotificationsManagerClient.instance().getNotificationsMap()
                .get(currRequestEntry.wrapper.getNotification().getUUID()) == null) {
            currRequestEntry.setState(RequestState.POP_OUT);
        }

        if (currRequestEntry != null && currRequestEntry.update()) {
            currRequestEntry = null;
        }
        notificationsQueue.removeIf(NotificationEntry::update);
    }

    private void draw(float partialTicks) {
        int verticalOffset = NotificationsSettings.NOTIFICATIONS_OVERLAY_VERTICAL_OFFSET.asInt();

        if (currRequestEntry != null) {
            int x = currRequestEntry.alignment == Alignment.RIGHT ? sr.getScaledWidth() - currRequestEntry.getScaledWidth() - 4 : 4;
            int y = sr.getScaledHeight() - (sr.getScaledHeight() / 2) - currRequestEntry.getScaledHeight() - 4;
            currRequestEntry.draw(x, y + verticalOffset, partialTicks);
        }

        int index = 0;
        for (NotificationEntry entry : notificationsQueue) {
            int x = entry.alignment == Alignment.RIGHT ? sr.getScaledWidth() - entry.getScaledWidth() - 4 : 4;
            int offset = CoreSettings.SCALE_OVERLAY.asFloat() > 1F ? 2 : 1;
            int y = sr.getScaledHeight() - (sr.getScaledHeight() / 2) + index * (entry.getScaledHeight() + offset);

            entry.draw(x, y + verticalOffset, partialTicks);
            index++;
        }
    }

    private static class RequestEntry {

        final NotificationWrapper wrapper;
        final Alignment alignment;
        final List<String> lines;

        RequestState state = RequestState.POP_UP;
        long stateChangeMillis;

        final int width = 162, height = 24;
        final Texture iconTexture;
        final String typeStr, handleStr;

        boolean remove;

        public RequestEntry(NotificationWrapper wrapper) {
            this.wrapper = wrapper;
            alignment = Alignment.valueOf(NotificationsSettings.NOTIFICATIONS_OVERLAY_ALIGNMENT.asString());

            String message = wrapper.getNotification().getMessage();
            String[] args = wrapper.getNotification().getArguments();
            lines = GUIUtils.splitTextToLines(MinecraftClient.localize(message, args),
                    CoreSettings.SCALE_TEXT_OVERLAY.asFloat(), 130);

            iconTexture = Texture.builder()
                    .texture(NotificationsManagerClient.getNotificationIcon(wrapper.getNotification()))
                    .size(20, 20)
                    .imageSize(20, 20)
                    .build();
            stateChangeMillis = OxygenClient.getCurrentTimeMillis();

            typeStr = wrapper.getNotification().getType().getDisplayName();

            if (NotificationsConfig.ENABLE_REQUEST_QUICK_KEYS.asBoolean()) {
                KeyBinding acceptKeyBinding = OxygenClient.getKeyBinding(NotificationsMain.KEYBINDING_ID_ACCEPT_REQUEST);
                String acceptKeyStr = GUIUtils.getKeyDisplayString(acceptKeyBinding.getKeyCode());
                KeyBinding rejectKeyBinding = OxygenClient.getKeyBinding(NotificationsMain.KEYBINDING_ID_REJECT_REQUEST);
                String rejectKeyStr = GUIUtils.getKeyDisplayString(rejectKeyBinding.getKeyCode());

                handleStr = MinecraftClient.localize(wrapper.getNotification().getType() == NotificationType.REQUEST_STANDARD ?
                                "oxygen_notifications.gui.overlay.notifications.handle_request_keys" :
                                "oxygen_notifications.gui.overlay.notifications.handle_request_voting_keys",
                        acceptKeyStr, rejectKeyStr);
            } else {
                handleStr = MinecraftClient.localize("oxygen_notifications.gui.overlay.notifications.handle_request_commands");
            }
        }

        boolean update() {
            return remove;
        }

        void updateState() {
            if (remove) return;
            long now = OxygenClient.getCurrentTimeMillis();
            boolean next = now >= stateChangeMillis + (state == RequestState.DISPLAY
                    ? wrapper.getNotification().getExpirationTimeMillis() : state.durationMillis);
            remove = next && state == RequestState.values()[RequestState.values().length - 1];
            if (!remove && next) {
                state = RequestState.values()[state.ordinal() + 1];
                stateChangeMillis = now;
            }
        }

        void setState(RequestState newState) {
            state = newState;
            stateChangeMillis = OxygenClient.getCurrentTimeMillis();
        }

        void draw(float x, float y, float partialTicks) {
            updateState();
            if (remove) return;

            long now = OxygenClient.getCurrentTimeMillis();
            int scaledWidth = getScaledWidth();

            float progress = MathUtils.clamp((now - stateChangeMillis + partialTicks) / state.durationMillis, 0F, 1F);
            if (state == RequestState.POP_UP) {
                x += alignment == Alignment.RIGHT ? scaledWidth * (1F - progress) : -scaledWidth * (1F - progress);
            } else if (state == RequestState.POP_OUT) {
                x += alignment == Alignment.RIGHT ? scaledWidth * progress : -scaledWidth * progress;
            }

            GUIUtils.pushMatrix();
            GUIUtils.translate(x, y);
            float scale = CoreSettings.SCALE_OVERLAY.asFloat();
            GUIUtils.scale(scale, scale);

            GUIUtils.drawRect(0, 0, width, height, CoreSettings.COLOR_ELEMENT_ENABLED.asInt());

            GUIUtils.colorDef();
            GUIUtils.drawTexturedRect(2, 2, iconTexture);

            float textScale = CoreSettings.SCALE_TEXT_OVERLAY.asFloat() - .05F;
            float specsScale = CoreSettings.SCALE_TEXT_OVERLAY.asFloat();
            int specsColor = CoreSettings.COLOR_OVERLAY_TEXT_BASE.asInt();
            float specsStrHeight = GUIUtils.getTextHeight(specsScale);
            GUIUtils.drawString(typeStr, 2F + iconTexture.getWidth() + 4F, 5 - specsStrHeight, specsScale, specsColor, false);

            float lineHeight = GUIUtils.getTextHeight(1F);
            if (!lines.isEmpty()) {
                GUIUtils.pushMatrix();
                GUIUtils.translate(2F + iconTexture.getWidth() + 2F, 7F);
                GUIUtils.scale(textScale, textScale);

                for (int i = 0; i < lines.size(); i++) {
                    GUIUtils.drawString(lines.get(i), 0, (lineHeight + 2F) * i, CoreSettings.COLOR_OVERLAY_TEXT_ADDITIONAL.asInt(), false);
                }

                GUIUtils.popMatrix();
            }

            String secondsLeft = "[" + (int) Math.max(0F, (float) (wrapper.getReceiveTimeMillis()
                    + wrapper.getNotification().getExpirationTimeMillis() - now) / 1000F) + "]";
            GUIUtils.drawString(secondsLeft, 2F + iconTexture.getWidth() + 4F, height - specsStrHeight - 1F, specsScale, specsColor, false);
            GUIUtils.drawString(handleStr, 2F + iconTexture.getWidth() + 4F + 14F, height - specsStrHeight - 1F, specsScale, specsColor, false);

            GUIUtils.popMatrix();
        }

        int getScaledWidth() {
            return (int) (width * CoreSettings.SCALE_OVERLAY.asFloat());
        }

        int getScaledHeight() {
            return (int) (height * CoreSettings.SCALE_OVERLAY.asFloat());
        }
    }

    private enum RequestState {

        POP_UP(300L),
        DISPLAY(0L),
        POP_OUT(300L);

        final long durationMillis;

        RequestState(long durationMillis) {
            this.durationMillis = durationMillis;
        }
    }

    private static class NotificationEntry implements Comparable<NotificationEntry> {

        final NotificationWrapper wrapper;
        final Alignment alignment;
        final long popUpTimeMillis;
        final List<String> lines;

        NotificationState state = NotificationState.POP_UP;
        long stateChangeMillis;

        final int width = 130, height = 24;
        final Texture iconTexture;

        boolean remove;

        public NotificationEntry(NotificationWrapper wrapper) {
            this.wrapper = wrapper;
            alignment = Alignment.valueOf(NotificationsSettings.NOTIFICATIONS_OVERLAY_ALIGNMENT.asString());

            String message = wrapper.getNotification().getMessage();
            String[] args = wrapper.getNotification().getArguments();
            lines = GUIUtils.splitTextToLines(MinecraftClient.localize(message, args),
                    CoreSettings.SCALE_TEXT_OVERLAY.asFloat(), 98);

            iconTexture = Texture.builder()
                    .texture(NotificationsManagerClient.getNotificationIcon(wrapper.getNotification()))
                    .size(20, 20)
                    .imageSize(20, 20)
                    .build();
            popUpTimeMillis = stateChangeMillis = OxygenClient.getCurrentTimeMillis();
        }

        boolean update() {
            return remove;
        }

        void updateState() {
            if (remove) return;
            long now = OxygenClient.getCurrentTimeMillis();
            boolean next = now >= stateChangeMillis + state.durationMillis;
            remove = next && state == NotificationState.values()[NotificationState.values().length - 1];
            if (!remove && next) {
                state = NotificationState.values()[state.ordinal() + 1];
                stateChangeMillis = now;
            }
        }

        void draw(float x, float y, float partialTicks) {
            updateState();
            if (remove) return;

            long now = OxygenClient.getCurrentTimeMillis();
            if (state == NotificationState.POP_UP) {
                int scaledWidth = getScaledWidth();
                float progress = MathUtils.clamp((now - stateChangeMillis + partialTicks) / state.durationMillis, 0F, 1F);
                x += alignment == Alignment.RIGHT ? scaledWidth * (1F - progress) : -scaledWidth * (1F - progress);
            }

            GUIUtils.pushMatrix();
            GUIUtils.translate(x, y);
            float scale = CoreSettings.SCALE_OVERLAY.asFloat();
            GUIUtils.scale(scale, scale);

            float textScale = CoreSettings.SCALE_TEXT_OVERLAY.asFloat();
            float alpha = 1F;
            if (state == NotificationState.FADE_OUT) {
                alpha = 1F - MathUtils.clamp((now - stateChangeMillis + partialTicks) / state.durationMillis, 0F, 1F);
            }

            GUIUtils.drawRect(0, 0, width, height,
                    GUIUtils.scaleAlpha(CoreSettings.COLOR_ELEMENT_ENABLED.asInt(), alpha));

            GUIUtils.enableBlend();
            GUIUtils.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GUIUtils.color(1F, 1F, 1F, alpha);

            GUIUtils.drawTexturedRect(2, 2, iconTexture);

            GUIUtils.disableBlend();

            if (!lines.isEmpty() && alpha > .1F) {
                float lineHeight = GUIUtils.getTextHeight(1F);

                GUIUtils.pushMatrix();
                GUIUtils.translate(2F + iconTexture.getWidth() + 2F, 3F);
                GUIUtils.scale(textScale, textScale);

                GUIUtils.enableBlend();
                GUIUtils.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                for (int i = 0; i < lines.size(); i++) {
                    GUIUtils.drawString(lines.get(i), 0, (lineHeight + 2F) * i,
                            GUIUtils.scaleAlpha(CoreSettings.COLOR_OVERLAY_TEXT_BASE.asInt(), alpha), false);
                }

                GUIUtils.disableBlend();

                GUIUtils.popMatrix();
            }

            GUIUtils.popMatrix();
        }

        int getScaledWidth() {
            return (int) (width * CoreSettings.SCALE_OVERLAY.asFloat());
        }

        int getScaledHeight() {
            return (int) (height * CoreSettings.SCALE_OVERLAY.asFloat());
        }

        @Override
        public int compareTo(NotificationEntry other) {
            return Longs.compare(other.popUpTimeMillis, popUpTimeMillis);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NotificationEntry that = (NotificationEntry) o;
            return wrapper.getNotification().getUUID().equals(that.wrapper.getNotification().getUUID());
        }

        @Override
        public int hashCode() {
            return Objects.hash(wrapper.getNotification().getUUID());
        }
    }

    private enum NotificationState {

        POP_UP(200L),
        DISPLAY(5000L),
        FADE_OUT(1000L);

        final long durationMillis;

        NotificationState(long durationMillis) {
            this.durationMillis = durationMillis;
        }
    }
}
