package funwayguy.esm.ai;

import funwayguy.esm.core.ESM_Settings;
import funwayguy.esm.core.ESM_Utils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;

public class ESM_EntityAIDemolition extends EntityAIBase
{
	public EntityLiving host;
	
	public ESM_EntityAIDemolition(EntityLiving host)
	{
		this.host = host;
	}
	
	@Override
	public boolean shouldExecute()
	{
		if (ESM_Settings.ZombieEnhancementsOnlyWhenSiegeAllowed)
			if (!ESM_Utils.isSiegeAllowed(host.worldObj.getWorldTime()))
				return false;
		return host.getAttackTarget() != null && host.getAttackTarget().getDistanceToEntity(host) < 3F && host.getHeldItem() != null && host.getHeldItem().getItem() == Item.getItemFromBlock(Blocks.tnt);
	}
	
	@Override
	public boolean continueExecuting()
	{
		return false;
	}
	
	@Override
	public void startExecuting()
	{
		host.setCurrentItemOrArmor(0, null);
		EntityTNTPrimed tnt = new EntityTNTPrimed(host.worldObj, host.posX, host.posY, host.posZ, host);
		host.worldObj.spawnEntityInWorld(tnt);
	}
}
