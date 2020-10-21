package game.tiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.awt.Point;

import game.world.BIOME;
import game.world.Chunk;
import game.world.World;

public class TileHelperFunctions {
    private static Random r = new Random();

    public static Boolean checkSameNeighbourBiome(BIOME biome, HashMap<Point, Tile> chunk_tiles, Chunk this_chunk,
            int x, int y, int offset_x, int offset_y) {
        int offset_x_16 = offset_x * 16;
        int offset_y_16 = offset_y * 16;
        if (chunk_tiles.containsKey(new Point(x + offset_x_16, y + offset_y_16))) {
            Tile temp_tile = (Tile) chunk_tiles.get(new Point(x + offset_x_16, y + offset_y_16));
            if (biome != temp_tile.biome) {
                // this.biome_bg = temp_tile.biome;
                return false;
            }
        } else {
            float[] arr = World.getHeightMapValuePoint(this_chunk.x + ((x - this_chunk.x) / 16) + offset_x,
                    this_chunk.y + ((y - this_chunk.y) / 16) + offset_y);
            if (biome != getBiomeFromHeightMap(arr)) {
                // this.biome_bg = getBiomeFromHeightMap(arr);
                return false;
            }
        }
        return true;
    }

    public static BIOME getBiomeFromHeightMap(float[] point) {
        float osn = point[0];
        float temp_osn = point[1];
        float moist_osn = point[2];

        return World.getBiome(osn, temp_osn, moist_osn);
    }

    public static BIOME getBestNeighbourBiome(BIOME biome, HashMap<Point, Tile> chunk_tiles, Chunk this_chunk, int x,
            int y, int offset_x, int offset_y) {

        HashMap<BIOME, Integer> biomes = new HashMap<BIOME, Integer>();

        int offset_x_16 = offset_x * 16;
        int offset_y_16 = offset_y * 16;
        if (chunk_tiles.containsKey(new Point(x + offset_x_16, y + offset_y_16))) {
            Tile temp_tile = (Tile) chunk_tiles.get(new Point(x + offset_x_16, y + offset_y_16));
            if (biome != temp_tile.biome) {
                if (biomes.containsKey(temp_tile.biome)) {
                    biomes.put(temp_tile.biome, biomes.get(temp_tile.biome) + 1);
                } else {
                    biomes.put(temp_tile.biome, 0);
                }
            }
        } else {
            float[] arr = World.getHeightMapValuePoint(this_chunk.x + ((x - this_chunk.x) / 16) + offset_x,
                    this_chunk.y + ((y - this_chunk.y) / 16) + offset_y);
            if (biome != getBiomeFromHeightMap(arr)) {
                if (biomes.containsKey(getBiomeFromHeightMap(arr))) {
                    biomes.put(getBiomeFromHeightMap(arr), biomes.get(getBiomeFromHeightMap(arr)) + 1);
                } else {
                    biomes.put(getBiomeFromHeightMap(arr), 0);
                }
            }
        }

        int max = Collections.max(biomes.values());

        ArrayList<BIOME> keys = new ArrayList<>();
        for (HashMap.Entry<BIOME, Integer> entry : biomes.entrySet()) {
            if (entry.getValue() == max) {
                keys.add(entry.getKey());
            }
        }

        if (keys.size() == 1) {
            return keys.get(0);
        } else {
            return keys.get(r.nextInt(keys.size() - 1));
        }
    }
}
