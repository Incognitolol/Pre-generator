package rip.alpha.pregenerator;

import com.khorn.terraincontrol.TerrainControl;
import com.khorn.terraincontrol.events.EventPriority;
import lombok.Getter;
import rip.alpha.libraries.Libraries;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import rip.alpha.libraries.LibrariesPlugin;

@Getter
public class Pregenerator extends JavaPlugin {

    @Getter
    private static Pregenerator instance;

    // Minimum Y level for pregeneration
    private final int minY = 60;

    @Override
    public void onEnable() {
        instance = this;

        // Register command class with the command framework
        LibrariesPlugin.getCommandFramework().registerClass(PregeneratorCommand.class);

        // Register event handler with TerrainControl
        TerrainControl.registerEventHandler(new PregeneratorEventHandler(), EventPriority.CANCELABLE);

        // Register event listener
        getServer().getPluginManager().registerEvents(new PregeneratorListener(), this);
    }

    /**
     * Checks if a block is replaceable.
     *
     * @param block The block to check
     * @return true if the block is replaceable, false otherwise
     */
    public boolean isReplaceable(Block block) {
        if (block.isEmpty()) return false;

        Material material = block.getType();

        // Using a switch statement for cleaner code
        return switch (material) {
            case LOG, LOG_2, LEAVES, LEAVES_2 -> false;
            default -> (material.isSolid() && !material.isTransparent()) || block.isLiquid();
        };
    }
}
