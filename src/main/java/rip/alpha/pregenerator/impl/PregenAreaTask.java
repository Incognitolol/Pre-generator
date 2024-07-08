package rip.alpha.pregenerator.impl;

import rip.alpha.pregenerator.Pregenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import rip.alpha.pregenerator.util.location.LocationUtil;
import rip.alpha.pregenerator.util.location.SimpleLocation;

import java.util.*;

public class PregenAreaTask extends BukkitRunnable {

    public static boolean ENABLED = false;

    private final int centerX, centerZ, radius;
    private final String worldName;
    private int currentY = 100;
    private BukkitTask task;
    private final List<PregenBlock> pregenBlocks;
    private final Random random;
    private final int minY;

    private Iterator<SimpleLocation> locationIterator;

    /**
     * Constructor to initialize the PregenAreaTask.
     *
     * @param worldName    The name of the world where the area is being generated
     * @param centerX      The center x-coordinate of the area
     * @param centerZ      The center z-coordinate of the area
     * @param minY         The minimum y-coordinate
     * @param radius       The radius of the area
     * @param pregenBlocks The list of pregeneration blocks
     */
    public PregenAreaTask(String worldName, int centerX, int centerZ, int minY, int radius, List<PregenBlock> pregenBlocks) {
        this.worldName = worldName;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.radius = radius;
        this.minY = minY;
        this.pregenBlocks = pregenBlocks;
        this.random = new Random();
    }

    /**
     * Starts the area generation task.
     */
    public void startTask() {
        this.task = Bukkit.getScheduler().runTaskTimer(Pregenerator.getInstance(), this, 10L, 10L);
    }

    @Override
    public void run() {
        ENABLED = true;

        for (int i = 0; i < 100000; i++) {
            if (this.locationIterator == null || !this.locationIterator.hasNext()) {
                break;
            }

            Location currentLocation = this.locationIterator.next().toBukkit();
            this.locationIterator.remove();
            Block block = currentLocation.getBlock();

            if (block.getLocation().getBlockY() > this.minY) {
                if (block.isEmpty()) continue;

                block.setType(Material.AIR);
            } else {
                PregenBlock pregenBlock = this.getRandomBlock();
                block.getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
                pregenBlock.setBlock(block);
            }
        }

        if (this.locationIterator == null || !this.locationIterator.hasNext()) {
            if (this.minY > this.currentY) {
                ENABLED = false;

                Bukkit.broadcast("Finished", "pregen.debug");
                this.task.cancel();
                return;
            }

            int difference = this.radius + (this.currentY - this.minY);
            SimpleLocation location = new SimpleLocation(this.centerX, currentY, this.centerZ, this.worldName);

            Set<SimpleLocation> outerSet = LocationUtil.getOuterCylinder(location, difference);

            for (SimpleLocation safeLocation : outerSet) {
                Location currentLocation = safeLocation.toBukkit();
                Block block = currentLocation.getBlock();

                if (Pregenerator.getInstance().isReplaceable(block)) {
                    Biome biome = block.getBiome();
                    block.getRelative(BlockFace.DOWN).setType(biome == Biome.HELL ? Material.NETHERRACK : Material.DIRT);
                    block.setType(this.getBlockByBiome(biome));
                }
            }

            Set<SimpleLocation> innerSet = LocationUtil.getCylinder(location, difference);
            innerSet.removeIf(outerSet::contains);
            this.locationIterator = innerSet.iterator();
            this.currentY--;
        }
    }

    /**
     * Gets the block material based on the biome.
     *
     * @param biome The biome to check
     * @return The corresponding block material
     */
    private Material getBlockByBiome(Biome biome) {
        return switch (biome) {
            case DESERT -> Material.SAND;
            case EXTREME_HILLS -> Material.STONE;
            case HELL -> Material.NETHERRACK;
            default -> Material.GRASS;
        };
    }

    /**
     * Gets a random block from the pregenBlocks list.
     *
     * @return A random PregenBlock
     */
    private PregenBlock getRandomBlock() {
        return this.pregenBlocks.get(this.random.nextInt(this.pregenBlocks.size()));
    }
}