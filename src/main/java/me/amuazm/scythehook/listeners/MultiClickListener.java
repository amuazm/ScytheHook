package me.amuazm.scythehook.listeners;

import me.amuazm.scythehook.ScytheHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;

public class MultiClickListener implements Listener {
    ScytheHook plugin = ScytheHook.getPlugin(ScytheHook.class);

    private final Map<Player, Long> last_click_map = new HashMap<>();
    private final Map<Player, Integer> task_id_map = new HashMap<>();
    private final Map<Player, Integer> rapid_click_counter_map = new HashMap<>();
    final int delay_ms = 250;

    @EventHandler
    public void multiClickListener(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getHand() == null) {
            return;
        }
        if (!e.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }

        // Initialise Situation for Player
        final long current_time = System.currentTimeMillis();
        final long last_click = last_click_map.getOrDefault(p, -1L);
        int rapid_click_counter = rapid_click_counter_map.getOrDefault(p, 1);
        int task_id = task_id_map.getOrDefault(p, -1);

        // Check if player has a last_click
        if (last_click != -1) {
            if ((current_time - last_click) < delay_ms) {
                rapid_click_counter += 1;
                Bukkit.getScheduler().cancelTask(task_id);
            }
        }

        // Execute code based on clicks
        task_id = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            int clicks = rapid_click_counter_map.get(p);
            switch (clicks) {
                case 1 -> Bukkit.broadcastMessage("Single Click");
                case 2 -> Bukkit.broadcastMessage("Double Click");
                case 3 -> Bukkit.broadcastMessage("Triple Click");
                default -> Bukkit.broadcastMessage("A lot");
            }
            clicks = 1;
            rapid_click_counter_map.put(p, clicks);
        }, (long) (delay_ms / (1 / 20.0 * 1000)));

        // For next time
        last_click_map.put(p, current_time);
        rapid_click_counter_map.put(p, rapid_click_counter);
        task_id_map.put(p, task_id);
    }
}
