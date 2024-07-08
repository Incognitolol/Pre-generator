package rip.alpha.pregenerator;

import org.bukkit.ChatColor;
import rip.alpha.pregenerator.impl.PregenAreaTask;
import rip.alpha.pregenerator.impl.PregenWorldTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class PregeneratorListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent event){
        if (PregenWorldTask.ENABLED){
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Server is pre genning");
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (PregenAreaTask.ENABLED) {
            int id = event.getBlock().getTypeId();
            if (id == 8 || id == 9) {
                event.setCancelled(true);
            }
        }
    }
}
