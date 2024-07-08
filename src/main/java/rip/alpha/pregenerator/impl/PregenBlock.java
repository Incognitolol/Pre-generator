package rip.alpha.pregenerator.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;

@Getter
@RequiredArgsConstructor
public class PregenBlock {

    private final int id;
    private final int data;

    /**
     * Sets the block type and data at the specified block location.
     *
     * @param block The block to set
     */
    public void setBlock(Block block) {
        block.setTypeIdAndData(id, (byte) data, false); // last boolean is no physics
    }

    /**
     * Parses a string to create a PregenBlock instance.
     *
     * @param s The string to parse
     * @return A PregenBlock instance, or null if the string is invalid
     */
    public static PregenBlock parse(String s) {
        try {
            if (s.contains(":")) {
                String[] split = s.split(":");

                int id = Integer.parseInt(split[0]);
                int data = Integer.parseInt(split[1]);

                return new PregenBlock(id, data);
            } else {
                int id = Integer.parseInt(s);
                return new PregenBlock(id, 0);
            }
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
