package funwayguy.esm.core;

import java.lang.reflect.Array;
import java.util.HashSet;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class BlockAndMeta {
    public final Block block;
    public final int meta;
    
    public BlockAndMeta(Block block, int meta) {
        this.block = block;
        this.meta = meta;
    }
    
    //public static BlockAndMeta[] buildList(String listName, ArrayList<String> blockListRaw) {
    //	return buildList(listName, blockListRaw.toArray(new String[blockListRaw.size()]));
    //}
    
    public static BlockAndMeta[] buildList(String listName, String[] blockListRaw) {
        HashSet<BlockAndMeta> blockList = new HashSet<BlockAndMeta>();

        ESM.log.info("Building " + listName + "...");
        
        for (String blockName : blockListRaw) {
            blockName = blockName.trim();
            if (!blockName.equals("")) {
                String[] blockId = blockName.split(":");
                if (Array.getLength(blockId) != 2 && Array.getLength(blockId) != 3 ) {
                	ESM.log.warn(" - Invalid block (bad length of " + Array.getLength(blockId) + "): " + blockName);
                    continue;
                }
                if (blockId[0] == null || blockId[1] == null) {
                	ESM.log.warn(" - Invalid block (parse/format error): " + blockName);
                    continue;
                }
                Block block = GameRegistry.findBlock(blockId[0], blockId[1]);
                if (block == null) {
                	ESM.log.warn(" - Skipping missing block: " + blockName);
                    continue;
                }
                int meta = -1;
                if (Array.getLength(blockId) == 3) {
                    try {
                        meta = Integer.parseInt(blockId[2]);
                        if (meta < 0 || meta > 15)
                            throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                    	ESM.log.warn(" - Meta value invalid : '" + blockId[2] + "', must be a number between 0 and 15");
                        continue;
                    }
                }
                blockList.add(new BlockAndMeta(block, meta));
            }
        }

        ESM.log.info("...added " + blockList.size() + " blocks to " + listName);
        
        
        return blockList.toArray(new BlockAndMeta[blockList.size()]);
    }
    
    public static boolean isInBlockAndMetaList(Block block, int meta, BlockAndMeta[] list) {
		if (list != null) {
			for (BlockAndMeta blockAndMeta : list) {
				if (Block.isEqualTo(blockAndMeta.block, block)) {
                    if (blockAndMeta.meta == -1 || blockAndMeta.meta == meta) {
                    	return true;
                    }
                }
			}
		}
		return false;
	}
}