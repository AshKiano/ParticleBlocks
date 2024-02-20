package com.ashkiano.particleblocks;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class ParticleListCommandExecutor implements CommandExecutor {
    private final ParticleBlocks plugin;

    public ParticleListCommandExecutor(ParticleBlocks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Tento příkaz může být použit pouze hráči.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("particlelist")) {
            if (!player.hasPermission("particleblocks.list")) {
                player.sendMessage(ChatColor.RED + "Nemáte oprávnění k tomuto příkazu.");
                return true;
            }

            List<Map<?, ?>> particleList = plugin.getConfig().getMapList("particles");

            if (particleList.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Neexistují žádné uložené particle efekty.");
                return true;
            }

            for (Map<?, ?> particleMap : particleList) {
                int id = (Integer) particleMap.get("ID");
                String effectType = (String) particleMap.get("effect-type");
                String worldName = (String) particleMap.get("world");
                String particleType = (String) particleMap.get("particle-type");
                double x = (Double) particleMap.get("x");
                double y = (Double) particleMap.get("y");
                double z = (Double) particleMap.get("z");

                player.sendMessage(ChatColor.GREEN + "ID: " + id + ", Efekt: " + effectType + ", Particle: " + particleType + ", Svět: " + worldName + ", Lokace: [" + x + ", " + y + ", " + z + "]");
            }

            return true;
        }

        return false;
    }


}