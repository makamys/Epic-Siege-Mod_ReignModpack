package funwayguy.esm.ai;

import funwayguy.esm.core.ESM_Settings;
import funwayguy.esm.core.ESM_Utils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class ESM_EntityAIGrief extends EntityAIBase
{
	EntityLiving entityLiving;
	int[] markedLoc;
	int digTick = 0;
	int reachDistSq = 4; // distance squared (I think?) of reach distance  
	int pathRetry = 0;
	
	public ESM_EntityAIGrief(EntityLiving entity)
	{
		this.entityLiving = entity;
	}
	
	@Override
	public boolean shouldExecute()
	{
		/*
		if(this.entityLiving.getRNG().nextInt(4) != 0) // Severely nerfs how many time the next part of the script can run
		{
			return false;
		}
		*/
        
        if (!ESM_Settings.ZombieGriefBlocksAnyTime)
        	if (ESM_Settings.ZombieEnhancementsOnlyWhenSiegeAllowed)
        		if (!ESM_Utils.isSiegeAllowed(entityLiving.worldObj.getWorldTime()))
        			return false;
		
    	// Returns true if something like Iguana Tweaks is nerfing the vanilla picks. This will then cause zombies to ignore the harvestability of blocks when holding picks
    	boolean nerfedPick = !Items.iron_pickaxe.canHarvestBlock(Blocks.stone, new ItemStack(Items.iron_pickaxe));
		int i = MathHelper.floor_double(entityLiving.posX);
		int j = MathHelper.floor_double(entityLiving.posY);
		int k = MathHelper.floor_double(entityLiving.posZ);
		
		if(entityLiving.getAttackTarget() != null)
		{
			return false;
		}
		
		int[] candidate = null;
		ItemStack item = entityLiving.getEquipmentInSlot(0);
		
		for (int l = 0; l < ESM_Settings.ZombieGriefBlocksTriesPerTick; l++) {
			int ii = i + entityLiving.getRNG().nextInt(16) - 8;
			int jj = j + entityLiving.getRNG().nextInt(8) - 4;
			int kk = k + entityLiving.getRNG().nextInt(16) - 8;
			
			Block block = entityLiving.worldObj.getBlock(ii, jj, kk);
			int meta = entityLiving.worldObj.getBlockMetadata(ii, jj, kk);
			String regName = Block.blockRegistry.getNameForObject(block);
			
			if((ESM_Settings.ZombieGriefBlocks.contains(regName) || ESM_Settings.ZombieGriefBlocks.contains(regName + ":" + meta) || (ESM_Settings.ZombieGriefBlocksLightSources && block.getLightValue() > 0)) && block.getBlockHardness(entityLiving.worldObj, ii, jj, kk) >= 0 && !block.getMaterial().isLiquid())
			{
				if(!ESM_Settings.ZombieDiggerTools || ESM_Settings.ZombieGriefBlocksNoTool || (item != null && (item.getItem().canHarvestBlock(block, item) || (item.getItem() instanceof ItemPickaxe && nerfedPick && block.getMaterial() == Material.rock))) || block.getMaterial().isToolNotRequired())
				{
					candidate = new int[]{ii, jj, kk};
					break;
				}
			}
		}
		
		if(candidate == null)
		{
			return false;
		} else
		{
			markedLoc = candidate;
			entityLiving.getNavigator().tryMoveToXYZ(markedLoc[0], markedLoc[1], markedLoc[2], 1D);
			digTick = 0;
			return true;
		}
	}
	
	@Override
	public boolean continueExecuting()
	{
    	// Returns true if something like Iguana Tweaks is nerfing the vanilla picks. This will then cause zombies to ignore the harvestability of blocks when holding picks
    	boolean nerfedPick = !Items.iron_pickaxe.canHarvestBlock(Blocks.stone, new ItemStack(Items.iron_pickaxe));
    	
		if(markedLoc == null || !entityLiving.isEntityAlive() || entityLiving.getAttackTarget() != null)
		{
			markedLoc = null;
			return false;
		}
		
		Block block = entityLiving.worldObj.getBlock(markedLoc[0], markedLoc[1], markedLoc[2]);
		int meta = entityLiving.worldObj.getBlockMetadata(markedLoc[0], markedLoc[1], markedLoc[2]);
		String regName = Block.blockRegistry.getNameForObject(block);
		
		if(block == null || block == Blocks.air || (!ESM_Settings.ZombieGriefBlocks.contains(regName) && !ESM_Settings.ZombieGriefBlocks.contains(regName + ":" + meta) && block.getLightValue() <= 0))
		{
			markedLoc = null;
			return false;
		}
		
		ItemStack item = entityLiving.getEquipmentInSlot(0);
		return !ESM_Settings.ZombieDiggerTools || (item != null && (item.getItem().canHarvestBlock(block, item) || (item.getItem() instanceof ItemPickaxe && nerfedPick && block.getMaterial() == Material.rock))) || block.getMaterial().isToolNotRequired();
	}
	
	@Override
	public void updateTask()
	{
    	// Returns true if something like Iguana Tweaks is nerfing the vanilla picks. This will then cause zombies to ignore the harvestability of blocks when holding picks
    	boolean nerfedPick = !Items.iron_pickaxe.canHarvestBlock(Blocks.stone, new ItemStack(Items.iron_pickaxe));
    	
		if(!continueExecuting())
		{
			digTick = 0;
			return;
		}
		
		if(entityLiving.getDistance(markedLoc[0], markedLoc[1], markedLoc[2]) >= reachDistSq)
		{
			// griefable object is too far, need to move closer
			if(entityLiving.getNavigator().noPath())
			{
				// too far AND can't get a valid path, try and path again
				entityLiving.getNavigator().tryMoveToXYZ(markedLoc[0], markedLoc[1], markedLoc[2], 1D);
				pathRetry++;
			}
			if (pathRetry >= 40)   //   (╯°□°）╯︵ ┻━┻
				markedLoc = null;
			digTick = 0;
			return;
		}
		
		if (!canSeeBlockAt(markedLoc[0], markedLoc[1], markedLoc[2])) {
			markedLoc = null;
			digTick = 0;
			return;
		}
		
		Block block = entityLiving.worldObj.getBlock(markedLoc[0], markedLoc[1], markedLoc[2]);
		digTick++;
		
		float str = AIUtils.getBlockStrength(entityLiving, block, entityLiving.worldObj, markedLoc[0], markedLoc[1], markedLoc[2], !ESM_Settings.ZombieDiggerTools) * (digTick + 1);
		
		if(str >= 1F)
		{
			digTick = 0;
			
			if(markedLoc != null && markedLoc.length >= 3)
			{
				ItemStack item = entityLiving.getEquipmentInSlot(0);
				boolean canHarvest = !ESM_Settings.ZombieDiggerTools || (item != null && (item.getItem().canHarvestBlock(block, item) || (item.getItem() instanceof ItemPickaxe && nerfedPick && block.getMaterial() == Material.rock))) || block.getMaterial().isToolNotRequired();
				entityLiving.worldObj.func_147480_a(markedLoc[0], markedLoc[1], markedLoc[2], canHarvest);
				markedLoc = null;
			} else
			{
				markedLoc = null;
			}
		} else
		{
			if(digTick%5 == 0)
			{
				entityLiving.worldObj.playSoundAtEntity(entityLiving, block.stepSound.getStepResourcePath(), block.stepSound.getVolume() + 1F, block.stepSound.getPitch());
				entityLiving.swingItem();
			}
		}
	}
	
	private boolean canSeeBlockAt(int posX, int posY, int posZ) {
		
		if (isCollidingWithPotentialTarget(posX, posY, posZ))
			return true;
		
		Vec3 thisEntity = Vec3.createVectorHelper(entityLiving.posX, entityLiving.posY + (double)entityLiving.getEyeHeight(), entityLiving.posZ);
		Vec3 target = Vec3.createVectorHelper(posX, posY, posZ);
		MovingObjectPosition mop = entityLiving.worldObj.func_147447_a(thisEntity, target, false, false, true);
		
		if (mop == null)
			return false;
		
		if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			if (mop.blockX == posX && mop.blockY == posY && mop.blockZ == posZ)
				return true;
		
		return false;
	}
	
	private boolean isCollidingWithPotentialTarget(int posX, int posY, int posZ) {
		// entity might be colliding (or arrived) already....
		// ...no need to ray-trace. Allow grief if target is no more than 1 block away from the mob.
		if (isWithinRangeOf(posX, MathHelper.floor_double(entityLiving.posX), 1)) {
			if (isWithinRangeOf(posZ, MathHelper.floor_double(entityLiving.posZ), 1)) {
				if (isWithinRangeOf(posY, MathHelper.floor_double(entityLiving.posY + (double)entityLiving.getEyeHeight()), 1))
					return true;
				// also check from feet-level. I've heard Zombies can kick.
				if (isWithinRangeOf(posY, MathHelper.floor_double(entityLiving.posY), 1))
					return true;
			}
		}
		return false;
	}
	
	private boolean isWithinRangeOf(int from, int to, int range) {
		return (Math.abs(from - to) <= range) ? true : false;
	}
}
