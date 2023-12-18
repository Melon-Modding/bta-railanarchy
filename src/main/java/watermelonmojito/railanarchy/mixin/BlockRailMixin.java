package watermelonmojito.railanarchy.mixin;

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockRail;
import net.minecraft.core.block.logic.RailLogic;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
        value = {BlockRail.class},
        remap = false
)
public abstract class BlockRailMixin extends Block{

    @Shadow @Final private boolean isPowered;

    @Shadow protected abstract boolean func_27044_a(World world, int i, int j, int k, int l, boolean flag, int i1);

    @Shadow protected abstract void func_4031_h(World world, int i, int j, int k, boolean flag);

    public BlockRailMixin(String key, int id, Material material) {
        super(key, id, material);
    }

    @Override
    public boolean canPlaceOnSurfaceOnCondition(World world, int x, int y, int z){
        return true;
    }

    @Inject(method = "renderAsNormalBlock()Z", at = @At("HEAD"), cancellable = true)
    public void renderAsNormalBlockInject(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "canPlaceBlockAt(Lnet/minecraft/core/world/World;III)Z", at = @At("HEAD"), cancellable = true)
    public void CanPlaceBlockAtInject(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    /**
     * @author watermelonmojito
     * @reason fuckinguprails
     */
    @Overwrite
    public void onNeighborBlockChange(World world, int x, int y, int z, int blockId) {
        boolean flag;
        int i1;
        if (world.isClientSide) {
            return;
        }
        int j1 = i1 = world.getBlockMetadata(x, y, z);
        if (this.isPowered) {
            j1 &= 7;
        }
        flag = !world.canPlaceOnSurfaceOfBlock(x, y - 1, z);
        if (j1 == 2 && !world.canPlaceOnSurfaceOfBlock(x + 1, y, z)) {
            flag = true;
        }
        if (j1 == 3 && !world.canPlaceOnSurfaceOfBlock(x - 1, y, z)) {
            flag = true;
        }
        if (j1 == 4 && !world.canPlaceOnSurfaceOfBlock(x, y, z - 1)) {
            flag = true;
        }
        if (j1 == 5 && !world.canPlaceOnSurfaceOfBlock(x, y, z + 1)) {
            flag = true;
        }
        if (flag) {
//            this.dropBlockWithCause(world, EnumDropCause.WORLD, x, y, z, world.getBlockMetadata(x, y, z), null);
//            world.setBlockWithNotify(x, y, z, 0);
        }
        else if (this.id == Block.railPowered.id) {
            boolean flag1 = world.isBlockIndirectlyGettingPowered(x, y, z) || world.isBlockIndirectlyGettingPowered(x, y + 1, z);
            flag1 = flag1 || this.func_27044_a(world, x, y, z, i1, true, 0) || this.func_27044_a(world, x, y, z, i1, false, 0);
            boolean flag2 = false;
            if (flag1 && (i1 & 8) == 0) {
                world.setBlockMetadataWithNotify(x, y, z, j1 | 8);
                flag2 = true;
            } else if (!flag1 && (i1 & 8) != 0) {
                world.setBlockMetadataWithNotify(x, y, z, j1);
                flag2 = true;
            }
            if (flag2) {
                world.notifyBlocksOfNeighborChange(x, y - 1, z, this.id);
                if (j1 == 2 || j1 == 3 || j1 == 4 || j1 == 5) {
                    world.notifyBlocksOfNeighborChange(x, y + 1, z, this.id);
                }
            }
        } else if (blockId > 0 && Block.blocksList[blockId].canProvidePower() && !this.isPowered && RailLogic.getNAdjacentTracks(new RailLogic((BlockRail)(Object)this, world, x, y, z)) == 3) {
            this.func_4031_h(world, x, y, z, false);
        }
    }
}
