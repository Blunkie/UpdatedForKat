package com.example.JadsHelper;

import com.example.EthanApiPlugin.Collections.query.QuickPrayer;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InteractionHelper;
import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.google.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.events.AnimationChanged;

import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.example.EthanApiPlugin.Collections.query.QuickPrayer.*;

@PluginDependency(PacketUtilsPlugin.class)
@PluginDependency(EthanApiPlugin.class)
@PluginDescriptor(
        name = "JadHelper",
        description = "",
        enabledByDefault = false,
        tags = {
                "ethan"
        }
)
public class JadsHelper extends Plugin {
    private static final int MAGIC_ATTACK = 7592;
    private static final int RANGE_ATTACK = 7593;
    private static final int MAGE_PRAY = 12;
    private static final int RANGE_PRAY = 13;
    private static final int MELEE_PRAY = 14;
    private static final Map< QuickPrayer, Integer > prayerMap = new HashMap< >();
    static {
        prayerMap.put(QuickPrayer.PROTECT_FROM_MAGIC, MAGE_PRAY);
        prayerMap.put(QuickPrayer.PROTECT_FROM_MISSILES, RANGE_PRAY);
    }

    @Inject
    Client client;
    @Inject
    MousePackets mousePackets;
    @Inject
    WidgetPackets widgetPackets;
    @Inject
    EthanApiPlugin api;
    @Inject
    MovementPackets movementPackets;
    private boolean forceTab = false;
    private static final int AREA = 9043;

    private QuickPrayer prayer = QuickPrayer.PROTECT_FROM_MELEE;

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!isInFight()) return;
        if (forceTab) {
            System.out.println("forcing tab");
            client.runScript(915, 3);
            forceTab = false;
        }
        if (client.getWidget(5046276) == null) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetAction(client.getWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB), "Setup");
            forceTab = true;
        }
        if (this.shouldPray()) {
            this.handlePrayer();
        }
        if (EthanApiPlugin.isQuickPrayerEnabled()) {
            InteractionHelper.togglePrayer();
        }
        InteractionHelper.togglePrayer();
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged e) {
        if ((e.getActor() == null) || !e.getActor().getName().equalsIgnoreCase("tztok-jad")) {
            return;
        }
        this.prayer = this.getRequiredPrayer(e.getActor().getAnimation());
    }

    private boolean isInFight() {
        return Arrays.stream(client.getMapRegions()).anyMatch(x -> x == AREA);
    }

    private QuickPrayer getRequiredPrayer(int animationId) {
        if (animationId == MAGIC_ATTACK) {
            return QuickPrayer.PROTECT_FROM_MAGIC;
        } else if (animationId == RANGE_ATTACK) {
            return QuickPrayer.PROTECT_FROM_MISSILES;
        }

        return QuickPrayer.PROTECT_FROM_MELEE;
    }

    private boolean shouldPray() {
        return !EthanApiPlugin.isQuickPrayerActive(this.prayer);
    }

    private void handlePrayer() {
        if (shouldPray()) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetActionPacket(1, 5046276, -1, prayerMap.getOrDefault(prayer, MELEE_PRAY));
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned e) {
        if (!isInFight()) {}

    }

}