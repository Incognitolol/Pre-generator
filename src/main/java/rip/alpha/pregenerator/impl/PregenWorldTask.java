package rip.alpha.pregenerator.impl;


import net.minecraft.server.v1_8_R3.ChunkProviderServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import rip.alpha.libraries.util.TimeUtil;
import rip.alpha.pregenerator.Pregenerator;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PregenWorldTask extends BukkitRunnable {

    public static boolean ENABLED = false;

    private final World world;
    private final int size;
    private final ChunkProviderServer chunkProviderServer;

    private final int maxX;
    private final int maxZ;
    private final int total;

    private int currentX;
    private double progress = 0;

    private long startTime = -1;
    private BukkitTask task;
    private final Consumer<Boolean> consumer;

    /**
     * Constructor to initialize the PregenWorldTask.
     *
     * @param world    The world in which chunks are being pregenerated
     * @param size     The size of the area to pregenerate
     * @param consumer A callback to execute when the task is complete
     */
    public PregenWorldTask(World world, int size, Consumer<Boolean> consumer) {
        this.world = world;
        CraftWorld craftWorld = (CraftWorld) this.world;
        this.chunkProviderServer = craftWorld.getHandle().chunkProviderServer;

        this.size = size;
        this.maxX = this.size >> 4;
        this.maxZ = this.size >> 4;
        this.total = (maxX * 2) * (maxZ * 2);
        this.currentX = -maxX;
        this.consumer = consumer;
    }

    /**
     * Starts the pregeneration task.
     */
    public void startTask() {
        this.startTime = System.currentTimeMillis();
        this.task = Bukkit.getScheduler().runTaskTimer(Pregenerator.getInstance(), this, 10L, 10L);
    }

    @Override
    public void run() {
        if (this.currentX >= this.maxX) {

            ENABLED = false;

            if (this.task != null) {
                long diff = System.currentTimeMillis() - this.startTime;

                System.out.println("It took: " + TimeUtil.formatTime(diff) + " to generate " + this.size);
                this.task.cancel();
                this.task = null;

                consumer.accept(true);
            }
            return;
        }

        ENABLED = true;

        List<Long> timeList = new ArrayList<>();
        for (int z = -maxZ; z < maxZ; z++) {
            try {
                long startTime = System.currentTimeMillis();

                Chunk chunk = this.getChunkAt(this.currentX, z);
                chunk.load(true);

                this.populateChunk(this.currentX, z);
                chunk.unload(true);

                long diff = System.currentTimeMillis() - startTime;
                this.progress++;

                timeList.add(diff);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Couldn't load chunk at " + currentX + " " + z);
            }
        }

        // Calculate median time taken for chunk operations to estimate progress
        long timeTaken = -1;

        if (timeList.size() > 2) {
            timeList.sort(Long::compare);
            timeTaken = timeList.get(timeList.size() / 2);
        }

        // Print progress and estimated time to complete
        if (timeTaken != -1) {
            System.out.println("Progress: %" + ((progress / total) * 100D));
            System.out.println("ETA: " + TimeUtil.formatTime((long) ((total - progress) * timeTaken)));
        }

        this.currentX++;
    }

    /**
     * Retrieves the chunk at the specified coordinates.
     *
     * @param chunkX The x-coordinate of the chunk
     * @param chunkZ The z-coordinate of the chunk
     * @return The chunk at the specified coordinates
     */
    private Chunk getChunkAt(int chunkX, int chunkZ) {
        return this.world.getChunkAt(chunkX, chunkZ);
    }

    /**
     * Populates the chunk at the specified coordinates by loading surrounding chunks.
     *
     * @param x The x-coordinate of the chunk
     * @param z The z-coordinate of the chunk
     */
    private void populateChunk(int x, int z) {
        for (int cx = x - 1; cx <= x + 1; cx++) {
            for (int cz = z - 1; cz <= z + 1; cz++) {
                this.chunkProviderServer.getChunkAt(cx, cz);
            }
        }
        this.chunkProviderServer.getChunkAt(this.chunkProviderServer, x, z);
    }
}