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

import java.util.function.Predicate;

public class ChainListener implements Listener {
    ScytheHook plugin = ScytheHook.getPlugin(ScytheHook.class);

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        final Player p = e.getPlayer();
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

        // TODO: Cooldownw

        final Location pLoc = p.getEyeLocation();
        final Vector v = pLoc.getDirection();
        final Predicate<Entity> filter = entity -> (entity != p);
        final RayTraceResult r = p.getWorld().rayTraceEntities(pLoc, v, 15, 2, filter);
        if (r == null || r.getHitEntity() == null) {
            p.playSound(pLoc, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
            return;
        }
        final Entity entity = r.getHitEntity();
        final Location eLoc;
        if (entity instanceof LivingEntity) {
            eLoc = ((LivingEntity) entity).getEyeLocation();
        } else {
            eLoc = entity.getLocation();
        }

        // Code to run
        // Sound
        p.playSound(pLoc, Sound.BLOCK_CHAIN_PLACE, 1, 1);

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
                    x + x_interval*i,
                    y + y_interval*i,
                    z + z_interval*i,
                    1,
                    dustOptions);
        }

        // Zoom
        pullEntityToLocation(entity, p.getLocation(), 1.0);

        // TODO: durability
    }

    //better method for pulling COPIED FROM GRAPPLING HOOK
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
}
