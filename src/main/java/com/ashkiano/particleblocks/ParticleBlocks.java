package com.ashkiano.particleblocks;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class ParticleBlocks extends JavaPlugin implements TabCompleter {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        Bukkit.getScheduler().runTaskTimer(this, this::spawnParticles, 0, 20);

        Metrics metrics = new Metrics(this, 19557);

        this.getLogger().info("Thank you for using the ParticleBlocks plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://donate.ashkiano.com");

        this.getCommand("setparticle").setTabCompleter(this);
        this.getCommand("getparticle");
        this.getCommand("particlelist").setExecutor(new ParticleListCommandExecutor(this));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;

        // Kontrola oprávnění
        if (!player.hasPermission("particleblocks.command")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("setparticle")) {
            if (args.length == 2) {
                Location loc = player.getLocation();
                String particleName = args[0].toUpperCase();
                String effectName = args[1].toLowerCase();

                // Kontrola platnosti jména particlu
                try {
                    Particle.valueOf(particleName);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + "Invalid particle name!");
                    return true;
                }

                if (!Arrays.asList(
                        "explosion", "ring", "spiral",
                        "vertical_ring", "heart", "wave",
                        "helix", "orb", "star",
                        "galaxy", "fire_ring", "double_ring",
                        "random_burst"
                ).contains(effectName)) {
                    player.sendMessage(ChatColor.RED + "Invalid effect name! Choose from the available effects.");
                    return true;
                }

                List<Map<String, Object>> particleList = (List<Map<String, Object>>) getConfig().getList("particles", new ArrayList<>());
                Map<String, Object> particleData = new HashMap<>();
                int newId = generateUniqueId(particleList);
                particleData.put("ID", newId);
                particleData.put("world", loc.getWorld().getName());
                particleData.put("x", loc.getX());
                particleData.put("y", loc.getY());
                particleData.put("z", loc.getZ());
                particleData.put("particle-type", particleName);
                particleData.put("effect-type", effectName);
                particleList.add(particleData);
                getConfig().set("particles", particleList);
                saveConfig();
                player.sendMessage(ChatColor.GREEN + "Particle location, type, and effect set!");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /setparticle <particleName> <effectName>");
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("delparticle")) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /delparticle <ID>");
                return true;
            }

            int idToDelete;
            try {
                idToDelete = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid ID format. Please enter a valid number.");
                return true;
            }

            List<Map<String, Object>> particleList = (List<Map<String, Object>>) getConfig().getList("particles", new ArrayList<>());
            boolean found = false;
            Iterator<Map<String, Object>> iterator = particleList.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> currentEffect = iterator.next();
                if (currentEffect.containsKey("ID") && (int) currentEffect.get("ID") == idToDelete) {
                    iterator.remove();
                    found = true;
                    break;
                }
            }

            if (found) {
                getConfig().set("particles", particleList);
                saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Successfully deleted particle effect with ID " + idToDelete + ".");
            } else {
                sender.sendMessage(ChatColor.RED + "No particle effect found with ID " + idToDelete + ".");
            }

            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("setparticle")) {
            if (args.length == 1) {
                String inputParticle = args[0].toUpperCase();
                List<String> matches = new ArrayList<>();

                // Seznam particlů, které potřebují více informací.
                List<Particle> excludedParticles = Arrays.asList(
                        Particle.BLOCK_DUST,
                        Particle.BLOCK_CRACK,
                        Particle.FALLING_DUST,
                        Particle.ITEM_CRACK
                        // Přidejte další particly sem, pokud je třeba.
                );

                for (Particle particle : Particle.values()) {
                    if (!excludedParticles.contains(particle) && particle.name().startsWith(inputParticle)) {
                        matches.add(particle.name().toLowerCase());
                    }
                }

                return matches;
            } else if (args.length == 2) {
                List<String> effects = Arrays.asList(
                        "explosion", "ring", "spiral",
                        "vertical_ring", "heart", "wave",
                        "helix", "orb", "star",
                        "galaxy", "fire_ring", "double_ring",
                        "random_burst"
                );
                String inputEffect = args[1].toLowerCase();

                return effects.stream()
                        .filter(effect -> effect.startsWith(inputEffect))
                        .collect(Collectors.toList());
            }
        }
        return null;
    }

    private void spawnParticles() {
        List<Map<String, Object>> particleList = (List<Map<String, Object>>) getConfig().getList("particles", new ArrayList<>());

        for (Map<String, Object> particleData : particleList) {
            String worldName = (String) particleData.get("world");
            double x = (double) particleData.get("x");
            double y = (double) particleData.get("y");
            double z = (double) particleData.get("z");
            String particleName = (String) particleData.get("particle-type");
            String effectName = (String) particleData.get("effect-type");

            World world = Bukkit.getWorld(worldName);
            Location loc = new Location(world, x, y, z);
            Particle particleType = Particle.valueOf(particleName);

            switch (effectName) {
                case "explosion":
                    for (int i = 0; i < 100; i++) {
                        double offset = (i / 100.0) * 2 * Math.PI;
                        double offsetX = Math.cos(offset);
                        double offsetY = Math.sin(offset);
                        world.spawnParticle(particleType, loc, 1, offsetX, offsetY, 0, 0);
                    }
                    break;

                case "ring":
                    for (int i = 0; i < 100; i++) {
                        double offset = (i / 100.0) * 2 * Math.PI;
                        double offsetX = 2 * Math.cos(offset);
                        double offsetZ = 2 * Math.sin(offset);
                        world.spawnParticle(particleType, loc.add(offsetX, 0, offsetZ), 1, 0, 0, 0, 0);
                        loc.subtract(offsetX, 0, offsetZ); // Reset location for the next particle
                    }
                    break;

                case "spiral":
                    for (int i = 0; i < 100; i++) {
                        double offset = (i / 50.0) * 2 * Math.PI;  // Adjusted to make the spiral tighter
                        double offsetX = Math.cos(offset);
                        double offsetZ = Math.sin(offset);
                        world.spawnParticle(particleType, loc.add(offsetX, i * 0.05, offsetZ), 1, 0, 0, 0, 0);
                        loc.subtract(offsetX, i * 0.05, offsetZ); // Reset location for the next particle
                    }
                    break;

                case "vertical_ring":
                    for (int i = 0; i < 100; i++) {
                        double offset = (i / 50.0) * 2 * Math.PI;
                        double offsetX = Math.cos(offset);
                        double offsetY = 2 * Math.sin(offset);
                        world.spawnParticle(particleType, loc.add(offsetX, offsetY, 0), 1, 0, 0, 0, 0);
                        loc.subtract(offsetX, offsetY, 0); // Reset location for the next particle
                    }
                    break;

                case "heart":
                    for (double t = 0; t < 2 * Math.PI; t += 0.01) {
                        double xHeart = 16 * Math.pow(Math.sin(t), 3);
                        double yHeart = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
                        world.spawnParticle(particleType, loc.add(xHeart, yHeart, 0), 1);
                        loc.subtract(xHeart, yHeart, 0);
                    }
                    break;

                case "wave":
                    for (double i = 0; i < 2 * Math.PI; i += 0.1) {
                        double offsetY = Math.sin(i);
                        world.spawnParticle(particleType, loc.add(i, offsetY, 0), 1, 0, 0, 0, 0);
                        loc.subtract(i, offsetY, 0);
                    }
                    break;

                case "helix":
                    for (int i = 0; i < 100; i++) {
                        double offset = (i / 50.0) * 2 * Math.PI;
                        double offsetX = Math.cos(offset);
                        double offsetZ = Math.sin(offset);
                        double offsetY = i * 0.03;
                        world.spawnParticle(particleType, loc.add(offsetX, offsetY, offsetZ), 1);
                        world.spawnParticle(particleType, loc.add(-offsetX, offsetY, -offsetZ), 1);
                        loc.subtract(offsetX, offsetY, offsetZ);
                        loc.add(offsetX, offsetY, offsetZ); // Reset location for the next particle
                    }
                    break;

                case "orb":
                    for (double i = 0; i < Math.PI; i += 0.1) {
                        double offsetX = Math.sin(i);
                        double offsetY = Math.cos(i);
                        double offsetZ = Math.sin(i);
                        world.spawnParticle(particleType, loc.add(offsetX, offsetY, offsetZ), 1, 0, 0, 0, 0);
                        loc.subtract(offsetX, offsetY, offsetZ); // Reset location for the next particle
                    }
                    break;

                case "star":
                    for (int i = 0; i < 5; i++) {
                        double angle = 2 * Math.PI / 5 * i;
                        double nextAngle = 2 * Math.PI / 5 * (i + 2);  // Jump by 2 for star shape
                        double[] coords = {Math.cos(angle) * 2, Math.sin(angle) * 2, Math.cos(nextAngle) * 2, Math.sin(nextAngle) * 2};
                        world.spawnParticle(particleType, loc.add(coords[0], coords[1], 0), 1);
                        world.spawnParticle(particleType, loc.add(coords[2], coords[3], 0), 1);
                    }
                    break;

                case "galaxy":
                    for (int i = 0; i < 300; i++) {
                        double angle = 0.1 * i;
                        double radius = 0.01 * angle;
                        double xc = radius * Math.cos(angle);
                        double zc = radius * Math.sin(angle);
                        world.spawnParticle(particleType, loc.add(xc, 0, zc), 1);
                    }
                    break;

                case "fire_ring":
                    for (int i = 0; i < 100; i++) {
                        double angle = i / 50.0 * 2 * Math.PI;
                        double xb = 2 * Math.cos(angle);
                        double zb = 2 * Math.sin(angle);
                        world.spawnParticle(particleType, loc.add(xb, 0, zb), 1, 0, 0.5, 0, 0);
                        loc.subtract(xb, 0, zb);
                    }
                    break;

                case "double_ring":
                    for (int i = 0; i < 100; i++) {
                        double angle = i / 50.0 * 2 * Math.PI;
                        double xInner = 1.5 * Math.cos(angle);
                        double zInner = 1.5 * Math.sin(angle);
                        double xOuter = 2.5 * Math.cos(angle);
                        double zOuter = 2.5 * Math.sin(angle);
                        world.spawnParticle(particleType, loc.add(xInner, 0, zInner), 1);
                        world.spawnParticle(particleType, loc.add(xOuter, 0, zOuter), 1);
                        loc.subtract(xInner, 0, zInner);
                        loc.subtract(xOuter, 0, zOuter);
                    }
                    break;

                case "random_burst":
                    for (int i = 0; i < 50; i++) {
                        double xa = (Math.random() * 2 - 1) * 3;  // Random value between -3 and 3
                        double ya = (Math.random() * 2 - 1) * 3;
                        double za = (Math.random() * 2 - 1) * 3;
                        world.spawnParticle(particleType, loc.add(xa, ya, za), 1);
                        loc.subtract(xa, ya, za);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    int generateUniqueId(List<Map<String, Object>> particleList) {
        int newID = 1;
        Set<Integer> existingIDs = new HashSet<>();

        for (Map<String, Object> particle : particleList) {
            if (particle.containsKey("ID")) {
                existingIDs.add((Integer) particle.get("ID"));
            }
        }

        while (existingIDs.contains(newID)) {
            newID++;
        }

        return newID;
    }

}
