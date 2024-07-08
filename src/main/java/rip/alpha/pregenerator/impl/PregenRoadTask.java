package rip.alpha.pregenerator.impl;

import rip.alpha.pregenerator.Pregenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class PregenRoadTask extends BukkitRunnable {

    private BukkitTask task;
    private final String worldName;
    private final Direction direction;
    private final int width, size;
    private final PregenBlock slab;

    private final int startX, startZ;
    private int currentX, currentY, currentZ;

    private int lastBlockChange = -1;

    private final List<PregenBlock> pregenBlocks;
    private final Random random;

    /**
     * Constructor to initialize the PregenRoadTask.
     *
     * @param worldName    The name of the world where the road is being generated
     * @param currentX     The starting x-coordinate
     * @param currentY     The starting y-coordinate
     * @param currentZ     The starting z-coordinate
     * @param width        The width of the road
     * @param size         The length of the road
     * @param direction    The direction of the road
     * @param slab         The slab block to be used
     * @param pregenBlocks The list of pregeneration blocks
     */
    public PregenRoadTask(String worldName, int currentX, int currentY, int currentZ, int width, int size, Direction direction, PregenBlock slab, List<PregenBlock> pregenBlocks) {
        this.worldName = worldName;
        this.width = width;
        this.startX = currentX;
        this.startZ = currentZ;
        this.currentX = currentX;
        this.currentY = currentY;
        this.currentZ = currentZ;
        this.direction = direction;
        this.slab = slab;
        this.pregenBlocks = pregenBlocks;
        this.random = new Random();
        this.size = size;
    }

    /**
     * Starts the road generation task.
     */
    public void startTask() {
        this.task = Bukkit.getScheduler().runTaskTimer(Pregenerator.getInstance(), this, 2L, 2L);
    }

    @Override
    public void run() {
        if (Math.abs(this.currentX) >= this.size || Math.abs(this.currentZ) >= this.size) {
            this.task.cancel();
            return;
        }

        int blockChange = 0;
        World world = Bukkit.getWorld(this.worldName);
        Block currentHeightBlock = this.getHeightBlock(new Location(world, this.currentX, 0, this.currentZ));

        if (currentHeightBlock != null) {
            int relY = currentHeightBlock.getY();

            if (this.lastBlockChange <= 0 || this.lastBlockChange > 3) {
                if (relY < this.currentY) {
                    this.currentY--;
                    blockChange = -1;

                } else if (relY > this.currentY) {
                    this.currentY++;
                    blockChange = 1;
                }
                this.lastBlockChange = 0;
            }
        }

        for (int offset = -width; offset <= width; offset++) {
            Location location;

            if (this.direction == Direction.SOUTH || this.direction == Direction.NORTH) {
                location = new Location(world, currentX + offset, this.currentY, currentZ);
            } else {
                location = new Location(world, currentX, this.currentY, currentZ + offset);
            }

            location.setY(this.currentY);
            this.clearAbove(location);

            currentHeightBlock = location.getBlock();
            currentHeightBlock.getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
            this.getRandomBlock().setBlock(currentHeightBlock);

            if (slab.getId() != 0) {
                if (this.currentX != startX || this.currentZ != startZ) {
                    if (blockChange != 0) {
                        switch (direction) {
                            case SOUTH -> {
                                if (blockChange == 1) this.slab.setBlock(currentHeightBlock.getRelative(BlockFace.NORTH));
                                else this.slab.setBlock(currentHeightBlock.getRelative(BlockFace.UP));
                            }
                            case NORTH -> {
                                if (blockChange == 1) this.slab.setBlock(currentHeightBlock.getRelative(BlockFace.SOUTH));
                                else this.slab.setBlock(currentHeightBlock.getRelative(BlockFace.UP));
                            }
                            case EAST -> {
                                if (blockChange == 1) this.slab.setBlock(currentHeightBlock.getRelative(BlockFace.WEST));
                                else this.slab.setBlock(currentHeightBlock.getRelative(BlockFace.UP));
                            }
                            case WEST -> {
                                if (blockChange == 1) this.slab.setBlock(currentHeightBlock.getRelative(BlockFace.EAST));
                                else this.slab.setBlock(currentHeightBlock.getRelative(BlockFace.UP));
                            }
                        }
                    }
                }
            }
        }

        this.lastBlockChange++;

        switch (this.direction) {
            case SOUTH -> this.currentZ++;
            case NORTH -> this.currentZ--;
            case EAST -> this.currentX++;
            case WEST -> this.currentX--;
        }
    }

    /**
     * Gets a random block from the pregenBlocks list.
     *
     * @return A random PregenBlock
     */
    private PregenBlock getRandomBlock() {
        return this.pregenBlocks.get(this.random.nextInt(this.pregenBlocks.size()));
    }

    /**
     * Clears blocks above the given location up to the height limit.
     *
     * @param location The starting location
     */
    private void clearAbove(Location location) {
        for (int i = 256; (location.getBlockY() + 1) <= i; i--) {

            Location relLocation = location.clone();
            relLocation.setY(i);
            Block block = relLocation.getBlock();

            int difference = i - location.getBlockY();
            if (difference <= 2 && block.getType() == Material.STEP) continue;
            if (block.getType() != Material.AIR) block.setType(Material.AIR);
        }
    }

    /**
     * Gets the highest block at the given location that matches certain criteria.
     *
     * @param location The location to check
     * @return The highest matching block, or null if none found
     */
    private Block getHeightBlock(Location location) {
        for (int i = 256; i >= Pregenerator.getInstance().getMinY(); i--) {

            Location relLocation = location.clone();
            relLocation.setY(i);
            Block block = relLocation.getBlock();

            if (block.isLiquid()) return block;
            if (this.isRoadMaterial(block)) return block;
            if (this.isTopMaterial(block.getBiome(), block.getType())) return block;
        }
        return null;
    }

    /**
     * Checks if a block is a road material.
     *
     * @param block The block to check
     * @return true if the block is a road material, false otherwise
     */
    private boolean isRoadMaterial(Block block) {
        int id = block.getTypeId();

        byte data = block.getData();

        if (slab.getId() == id && slab.getData() == data) return true;

        for (PregenBlock pregenBlock : this.pregenBlocks) {
            if (id == pregenBlock.getId() && data == pregenBlock.getData()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a block is a top material for a given biome.
     *
     * @param biome    The biome to check
     * @param material The material to check
     * @return true if the material is a top material for the biome, false otherwise
     */
    private boolean isTopMaterial(Biome biome, Material material) {
        return switch (biome) {
            case DESERT -> material == Material.SAND || material == Material.STONE;
            case EXTREME_HILLS -> material == Material.STONE || material == Material.DIRT;
            case HELL -> material == Material.NETHERRACK || material == Material.GRAVEL;
            default -> material == Material.GRASS || material == Material.DIRT || material == Material.SAND;
        };
    }

    public enum Direction {
        NORTH,
        SOUTH,
        EAST,
        WEST;
    }
}
