package funwayguy.esm.ai;

import java.util.ArrayList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityBoat;

public class ESM_EntityAIBoat extends EntityAIBase
{
	EntityLiving host;
	
	public ESM_EntityAIBoat(EntityLiving host)
	{
		this.host = host;
	}
	
	@Override
	public boolean shouldExecute()
	{
		EntityLivingBase target = host.getAttackTarget();
		
		if(host.ridingEntity instanceof EntityBoat && target != null)
		{
			return true;
		} else if(host.handleWaterMovement() && target != null && host.getDistance(target.posX, host.posY, target.posZ) > 16) // Only start if we really need to catch up to someone
		{
			@SuppressWarnings("unchecked")
			ArrayList<EntityBoat> nearBoats = (ArrayList<EntityBoat>)host.worldObj.getEntitiesWithinAABB(EntityBoat.class, host.boundingBox.expand(3D, 3D, 3D));
			
			for(EntityBoat b : nearBoats)
			{
				if(!b.onGround && b.riddenByEntity == null)
				{
					host.mountEntity(b);
					return true;
				}
			}
			host.getEntityData().setBoolean("ESM_BOAT", true);
			EntityBoat boat = new EntityBoat(host.worldObj);
			boat.setPosition(host.posX, host.posY, host.posZ);
			host.worldObj.spawnEntityInWorld(boat);
			host.mountEntity(boat);
			return true;
		}
		
		return false;
	}
	
	@Override
	public void updateTask()
	{
		if(!(host.ridingEntity instanceof EntityBoat))
		{
			return;
		}
		
		EntityBoat boat = (EntityBoat)host.ridingEntity;
		EntityLivingBase target = host.getAttackTarget();
		
		if(boat.onGround || (boat.isCollidedHorizontally && boat.motionX <= 0.25F && boat.motionZ <= 0.25F) || (target != null && host.getDistance(target.posX, target.posY, target.posZ) <= 4))
		{
			host.dismountEntity(boat);
			host.ridingEntity = null;
			// Despawn the boat
			boat.setDead();
			return;
		}
		
		host.moveForward = 1F;
	}
}
