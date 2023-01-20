package me.amuazm.scythehook.listeners;

import me.amuazm.scythehook.ScytheHook;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ChainListener implements Listener {
    ScytheHook plugin = ScytheHook.getPlugin(ScytheHook.class);

    private final Map<Player, Long> last_action_map = new HashMap<>();
    private final Map<Player, Long> last_click_map = new HashMap<>();
    private final Map<Player, Integer> task_id_map = new HashMap<>();
    private final Map<Player, Integer> rapid_click_counter_map = new HashMap<>();
    final int action_cooldown_ms = 1500;
    final int rapid_click_delay_ms = 250;

    @EventHandler
    public void multiClickListener(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        final Location pLoc = p.getEyeLocation();
        if (e.getHand() == null) {
            return;
        }
        if (!e.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }
        if (p.getEquipment() == null) {
            return;
        }
        if (!p.getEquipment().getItemInOffHand().getType().equals(Material.CHAIN)) {
            return;
        }
        e.setCancelled(true);

        // Initialise Situation for Player p
        final long last_action = last_action_map.getOrDefault(p, -1L);
        final long current_time = System.currentTimeMillis();
        final long last_click = last_click_map.getOrDefault(p, -1L);
        int rapid_click_counter = rapid_click_counter_map.getOrDefault(p, 1);
        int task_id = task_id_map.getOrDefault(p, -1);

        // Check cooldown
        if (last_action != -1) {
            if ((current_time - last_action) < action_cooldown_ms) {
                p.sendMessage(ChatColor.RED + "Your chain is on cooldown!");

                // Cooldown feedback
                p.playSound(p, Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1, 1);
                return;
            }
        }

        // Check if player has a last_click
        if (last_click != -1) {
            if ((current_time - last_click) < rapid_click_delay_ms) {
                rapid_click_counter += 1;
                Bukkit.getScheduler().cancelTask(task_id);

                // Click feedback
                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1);
            }
        }

        // Execute code based on clicks
        task_id = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            // Get entity to interact with
            final Entity entity = getRayTracedEntity(p);
            final Location eLoc;

            if (entity == null) {
                // No entity feedback
                p.playSound(pLoc, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
                return;
            }

            // Entity Location
            if (entity instanceof LivingEntity livingEntity) {
                eLoc = livingEntity.getEyeLocation();
            } else {
                eLoc = entity.getLocation();
            }

            // Sound
            p.playSound(p, Sound.BLOCK_CHAIN_PLACE, 1, 1);

            // Particles
            chainParticles(p, pLoc, eLoc);

            // Pull
            int clicks = rapid_click_counter_map.get(p);
            switch (clicks) {
                case 1 -> pullEntityToLocation(p, eLoc, 1.0);
                case 2 -> {
                    pullEntityToLocation(p, eLoc, 0.6);
                    pullEntityToLocation(entity, pLoc, 0.6);
                }
                case 3 -> pullEntityToLocation(entity, pLoc, 1.0);
                default -> {
                    // Too much feedback
                    p.playSound(p, Sound.ENTITY_GOAT_HORN_BREAK, 1, 1);
                }
            }

            // For next time
            clicks = 1;
            rapid_click_counter_map.put(p, clicks);
            last_action_map.put(p, current_time);
        }, (long) (rapid_click_delay_ms / (1 / 20.0 * 1000)));

        // For next time
        last_click_map.put(p, current_time);
        rapid_click_counter_map.put(p, rapid_click_counter);
        task_id_map.put(p, task_id);
    }

    public Entity getRayTracedEntity(Player p) {
        final Location pLoc = p.getEyeLocation();
        final Vector v = pLoc.getDirection();
        final Predicate<Entity> filter = entity -> (entity != p);
        final RayTraceResult r;
        // Get ray
        r = p.getWorld().rayTraceEntities(pLoc, v, 50, 2, filter);
        // Get Entity
        return r != null ? r.getHitEntity() : null;
    }

    private void pullEntityToLocation(final Entity entity, Location loc, double multiply) {
        Location entityLoc = entity.getLocation();

        Vector boost = entity.getVelocity();
        boost.setY(0.3);
        entity.setVelocity(boost);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            double g = -0.08;
            double d = loc.distance(entityLoc);
            double t = d;
            // speed = distance / time
            // distance = (100 + 7 * blockstotravel)% * axis distance
            // time = blockstotravel
            double v_x = (1.0 + 0.07 * t) * (loc.getX() - entityLoc.getX()) / t;
            double v_y = (1.0 + 0.03 * t) * (loc.getY() - entityLoc.getY()) / t - 0.5 * g * t;
            double v_z = (1.0 + 0.07 * t) * (loc.getZ() - entityLoc.getZ()) / t;

            Vector v = entity.getVelocity();
            v.setX(v_x);
            v.setY(v_y);
            v.setZ(v_z);
            v.multiply(multiply);
            entity.setVelocity(v);
        }, 1L);
    }

    public void chainParticles(Player p, Location pLoc, Location eLoc) {
        // Particles
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(19, 40, 56), 1.0F);
        double x = pLoc.getX();
        double y = pLoc.getY();
        double z = pLoc.getZ();
        double distance = pLoc.distance(eLoc);
        double x_interval = (eLoc.getX() - x) / distance;
        double y_interval = (eLoc.getY() - y) / distance;
        double z_interval = (eLoc.getZ() - z) / distance;
        double particles_per_block = 2;
        for (double i = 0; i <= distance; i += (1 / particles_per_block)) {
            p.spawnParticle(
                    Particle.REDSTONE,
                    x + x_interval * i,
                    y + y_interval * i,
                    z + z_interval * i,
                    1,
                    dustOptions
            );
        }
    }
}
