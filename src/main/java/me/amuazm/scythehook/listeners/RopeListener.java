package me.amuazm.scythehook.listeners;

import me.amuazm.scythehook.Point;
import me.amuazm.scythehook.ScytheHook;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public class RopeListener implements Listener {
    ScytheHook plugin = ScytheHook.getPlugin(ScytheHook.class);
    private boolean active = false;
    private final double bounce = 0.9;
    private final double gravity = -0.1;
    private final double friction = 0.99;

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
        if (!p.getEquipment().getItemInOffHand().getType().equals(Material.LADDER)) {
            return;
        }
        if (active) {
            active = false;
            return;
        }
        e.setCancelled(true);

        final Location pLoc = p.getEyeLocation();
        final Location bLoc;
        final Vector v = pLoc.getDirection();
        final RayTraceResult r;
        // Hook to blocks
        // Get ray
        r = p.getWorld().rayTraceBlocks(pLoc, v, 100);
        if (r == null || r.getHitBlock() == null) {
            // No ray feedback
            p.playSound(pLoc, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1, 1);
            return;
        }
        // Get location to anchor to
        bLoc = r.getHitBlock().getLocation();
        bLoc.add(0.5, 0.5, 0.5);

        // Rope
        active = !active;
        if (active) {

            // Points
            java.util.Vector<Point> points = new java.util.Vector<>();
            double x = pLoc.getX();
            double y = pLoc.getY();
            double z = pLoc.getZ();
            double distance = pLoc.distance(bLoc);
            double x_interval = (bLoc.getX() - x) / distance;
            double y_interval = (bLoc.getY() - y) / distance;
            double z_interval = (bLoc.getZ() - z) / distance;
            double particles_per_block = 2;
            for (double i = 0; i <= distance; i += (1 / particles_per_block)) {
                points.add(
                        new Point(
                                new Location(
                                        p.getWorld(),
                                        x + x_interval * i,
                                        y + y_interval * i,
                                        z + z_interval * i
                                ),
                                new Location(
                                        p.getWorld(),
                                        x + x_interval * i + 0.5,
                                        y + y_interval * i + 0.1,
                                        z + z_interval * i + 0.5
                                )
                        )
                );
            }

            // Looped task
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!active) {
                        // Code to run after reaching inactive
                        p.sendMessage("Ending.");

                        // Exit task
                        this.cancel();
                        return;
                    }

                    // Code to run on loop while active
                    p.sendMessage("Running.");
                    // Update Points
                    for (Point point : points) {
                        double vx = point.getCurrentX() - point.getOldX();
                        double vy = point.getCurrentY() - point.getOldY();
                        double vz = point.getCurrentZ() - point.getOldZ();
                        vx *= friction;
                        vy *= friction;
                        vz *= friction;
                        point.setOld(point.getCurrent().clone());
                        point.getCurrent().add(vx, vy, vz);
                        point.getCurrent().add(0, gravity, 0);
                        // point.getCurrent().multiply(friction);

                        World pWorld = p.getWorld();
                        Block pointBlock = pWorld.getBlockAt(point.getCurrent());
                        if (pointBlock.getType() != Material.AIR) {
                            point.setCurrentY(pointBlock.getY() + 1);
                            point.setOldY(point.getCurrentY() + vy * bounce);
                        }
                    }
                    // Particles / Render
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0F);
                    for (Point point : points) {
                        p.spawnParticle(
                                Particle.REDSTONE,
                                point.getCurrent(),
                                1,
                                dustOptions
                        );
                    }
                }
            }.runTaskTimer(plugin, 0, 2);
        }
    }
}
