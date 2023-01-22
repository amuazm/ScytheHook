package me.amuazm.scythehook;

import me.amuazm.scythehook.commands.HoeMobCommand;
import me.amuazm.scythehook.commands.HoeWarCommand;
import me.amuazm.scythehook.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class ScytheHook extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new HoeListener(), this);
        getServer().getPluginManager().registerEvents(new StringListener(), this);
        getServer().getPluginManager().registerEvents(new ShieldListener(), this);
        getServer().getPluginManager().registerEvents(new ChainListener(), this);
        getServer().getPluginManager().registerEvents(new RopeListener(), this);
        getServer().getPluginManager().registerEvents(new PointGunListener(), this);
        getCommand("hoemob").setExecutor(new HoeMobCommand());
        getCommand("hoewar").setExecutor(new HoeWarCommand());
    }

}
