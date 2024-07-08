package rip.alpha.pregenerator;


import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.command.annotation.Wildcard;
import rip.alpha.pregenerator.impl.PregenBlock;
import rip.alpha.pregenerator.impl.PregenRoadTask;
import rip.alpha.pregenerator.impl.PregenAreaTask;
import rip.alpha.pregenerator.impl.PregenWorldTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class PregeneratorCommand {

    @CommandUsage("<World> <Size>")
    @Command(names = {"pregen world"}, permission = "op")
    public static void pregenCommand(CommandSender sender, World world, int size) {
        // Only allow players to execute this command
        if (sender instanceof Player) {
            // Execute the pregeneration task
            new PregenWorldTask(world, size, aBoolean ->
                    new PregenWorldTask(world, size, aBoolean2 ->
                            new PregenWorldTask(world, size, aBoolean3 ->
                                    System.out.println("!!!!!!!!!!!!!!!!!!DONE EVERYTHING!!!!!!!!!!!!!!!!!!!!!!!")
                            ).startTask()
                    ).startTask()
            ).startTask();
        }
    }

    @CommandUsage("<Radius> (Material)")
    @Command(names = {"pregen area"}, permission = "op")
    public static void cleanupSpawn(Player player, int radius, @Wildcard String materials) {
        Location location = player.getLocation();
        int centerX = location.getBlockX();
        int centerZ = location.getBlockZ();

        String[] split = materials.split(" ");
        List<PregenBlock> blocks = new ArrayList<>();
        for (String string : split) {
            PregenBlock pregenBlock = PregenBlock.parse(string);
            if (pregenBlock == null) {
                player.sendMessage(string + " is an invalid block data, ex: 17:14");
                return;
            }
            blocks.add(pregenBlock);
        }

        int currentY = location.getBlockY();
        new PregenAreaTask(location.getWorld().getName(), centerX, centerZ, currentY, radius, blocks).startTask();
    }

    @CommandUsage("<Direction> <Width> <Size> <Road Materials> (Materials)")
    @Command(names = {"pregen road"}, permission = "op")
    public static void cleanupRoad(Player player, String directionName, int width, int size, String material, @Wildcard String materials) {
        try {
            PregenRoadTask.Direction direction = PregenRoadTask.Direction.valueOf(directionName.toUpperCase());
            Location location = player.getLocation();
            String worldName = location.getWorld().getName();

            String[] split = materials.split(" ");
            List<PregenBlock> blocks = new ArrayList<>();
            for (String string : split) {
                PregenBlock pregenBlock = PregenBlock.parse(string);
                if (pregenBlock == null) {
                    player.sendMessage(string + " is an invalid block data, ex: 17:14");
                    return;
                }
                blocks.add(pregenBlock);
            }

            PregenBlock pregenBlock = PregenBlock.parse(material);
            if (pregenBlock == null) {
                player.sendMessage(material + " is an invalid block data, ex: 17:14");
                return;
            }

            new PregenRoadTask(worldName, location.getBlockX(), location.getBlockY(), location.getBlockZ(), width, size, direction, pregenBlock, blocks).startTask();
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid direction. Please use a valid direction.");
        } catch (Exception e) {
            player.sendMessage("An error occurred while processing your command.");
        }
    }
}