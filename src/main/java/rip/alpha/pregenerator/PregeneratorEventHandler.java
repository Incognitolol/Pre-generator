package rip.alpha.pregenerator;

import com.khorn.terraincontrol.LocalWorld;
import com.khorn.terraincontrol.customobjects.CustomObject;
import com.khorn.terraincontrol.events.EventHandler;
import com.khorn.terraincontrol.generator.resource.Resource;
import rip.alpha.pregenerator.impl.PregenWorldTask;

import java.util.Random;

public class PregeneratorEventHandler extends EventHandler {

    /**
     * Determines if a custom object can spawn at the given coordinates in the world.
     *
     * @param object      The custom object to potentially spawn
     * @param world       The local world in which the object may spawn
     * @param x           The x-coordinate for spawning
     * @param y           The y-coordinate for spawning
     * @param z           The z-coordinate for spawning
     * @param isCancelled Indicates if the spawn event is cancelled
     * @return true if the custom object can spawn, false otherwise
     */
    public boolean canCustomObjectSpawn(CustomObject object, LocalWorld world, int x, int y, int z, boolean isCancelled) {
        if (PregenWorldTask.ENABLED) {
            // Get absolute coordinates to check boundaries
            int currentX = Math.abs(x);
            int currentZ = Math.abs(z);

            // Check if within 3000x3000 boundary
            boolean withinBoundary = currentX < 3000 && currentZ < 3000;

            // If within boundary and the object can spawn as a tree, apply additional conditions
            if (withinBoundary && object.canSpawnAsTree()) {
                return currentX > 200 || currentZ > 200;
            }

            return withinBoundary;
        }
        return false;
    }

    /**
     * Processes resources in the given chunk of the world.
     *
     * @param resource        The resource to process
     * @param world           The local world in which the resource is processed
     * @param random          The random number generator
     * @param villageInChunk  Indicates if a village is present in the chunk
     * @param chunkX          The x-coordinate of the chunk
     * @param chunkZ          The z-coordinate of the chunk
     * @param isCancelled     Indicates if the resource process event is cancelled
     * @return true if the resource can be processed, false otherwise
     */
    public boolean onResourceProcess(Resource resource, LocalWorld world, Random random, boolean villageInChunk, int chunkX, int chunkZ, boolean isCancelled) {
        // Calculate absolute coordinates of the chunk's position
        int currentX = Math.abs(chunkX << 4);
        int currentZ = Math.abs(chunkZ << 4);

        // Check if within 3000x3000 boundary
        return currentX < 3000 && currentZ < 3000;
    }
}