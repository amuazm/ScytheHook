package me.amuazm.scythehook.listeners;

import me.amuazm.scythehook.Point;
import me.amuazm.scythehook.ScytheHook;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class PointGunListener implements Listener {
    ScytheHook plugin = ScytheHook.getPlugin(ScytheHook.class);
    private boolean active = false;
    private final double bounce = 0.9;
    private final double friction = 0.99;
    private final double gravity = -0.05;

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
        if (!p.getEquipment().getItemInOffHand().getType().equals(Material.REDSTONE_TORCH)) {
            return;
        }
        e.setCancelled(true);
        if (active) {
            active = false;
            return;
        }

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
            points.add(new Point(
                    pLoc,
                    pLoc.clone().subtract(pLoc.getDirection().normalize().multiply(0.4))
            ));

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
                    World pWorld = p.getWorld();
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

                        // Block collision detection
                        // point = new Point(
                        //         new Location(
                        //                 pWorld,
                        //                 794.54,
                        //                 9.89,
                        //                 974.85
                        //
                        //         ),
                        //         new Location(
                        //                 pWorld,
                        //                 794.66,
                        //                 10.01,
                        //                 974.72
                        //         )
                        // );
                        RayTraceResult r2 = pWorld.rayTraceBlocks(point.getOld(), point.getVector(), point.getVector().length());
                        if (r2 != null && r2.getHitBlock() != null && r2.getHitBlockFace() != null) {
                            Block b = r2.getHitBlock();
                            BoundingBox bb = b.getBoundingBox();

                            Bukkit.broadcastMessage("\n" + b.getType());
                            Bukkit.broadcastMessage("" + r2.getHitBlockFace());
                            Bukkit.broadcastMessage("" + point);

                            switch (r2.getHitBlockFace()) {
                                case UP -> {
                                    // Reflects a point (point.getCurrentY()) across a threshold (bb.getMaxY())
                                    point.setCurrentY(point.getCurrentY() + 2 * (bb.getMaxY() - point.getCurrentY()));
                                    point.setOldY(point.getCurrentY() + vy * bounce);
                                    // point.setOldY(point.getOldY() + (bb.getMaxY() - point.getOldY()) + 0.9 * (bb.getMaxY() - point.getOldY()));
                                }
                                case DOWN -> {
                                    point.setCurrentY(point.getCurrentY() + 2 * (bb.getMinY() - point.getCurrentY()));
                                    point.setOldY(point.getCurrentY() + vy * bounce);
                                }
                                case WEST -> {
                                    point.setCurrentX(point.getCurrentX() + 2 * (bb.getMinX() - point.getCurrentX()));
                                    point.setOldX(point.getCurrentX() + vx * bounce);
                                }
                                case NORTH -> {
                                    point.setCurrentZ(point.getCurrentZ() + 2 * (bb.getMinZ() - point.getCurrentZ()));
                                    point.setOldZ(point.getCurrentZ() + vz * bounce);
                                }
                                case EAST -> {
                                    point.setCurrentX(point.getCurrentX() + 2 * (bb.getMaxX() - point.getCurrentX()));
                                    point.setOldX(point.getCurrentX() + vx * bounce);
                                }
                                case SOUTH -> {
                                    point.setCurrentZ(point.getCurrentZ() + 2 * (bb.getMaxZ() - point.getCurrentZ()));
                                    point.setOldZ(point.getCurrentZ() + vz * bounce);
                                }
                                default -> Bukkit.broadcastMessage("It hit none?");
                            }
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
            }.runTaskTimer(plugin, 0, 1);
        }
    }
}
