package com.untamedears.PrisonPearl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.topcat.npclib.entity.NPC;
import com.trc202.CombatTag.CombatTag;
import com.trc202.CombatTagEvents.NpcDespawnEvent;
import com.trc202.CombatTagEvents.NpcDespawnReason;
import com.trc202.Containers.PlayerDataContainer;

class CombatTagListener implements Listener {
    final PrisonPearlManager pearlman_;

    public CombatTagListener(final PrisonPearlPlugin plugin,
            final PrisonPearlManager pearlman) {
        this.pearlman_ = pearlman;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onNpcDespawn(NpcDespawnEvent event) {
        if (event.getReason() != NpcDespawnReason.DESPAWN_TIMEOUT) {
            return;
        }
        PlayerDataContainer plrdata = event.getPlayerData();
        String plrname = plrdata.getPlayerName();

        NPC npc = event.getNpc();
        Location loc = npc.getBukkitEntity().getLocation();

        pearlman_.handleNpcDespawn(plrname, loc);
    }
}
