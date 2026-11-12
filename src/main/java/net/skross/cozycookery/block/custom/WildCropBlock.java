package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A wild vegetable plant that spawns in the world (like a flower or a sweet berry bush).
 * Breaking it gives the vegetable + seeds (see its loot table). It can grow on grass and dirt.
 */
public class WildCropBlock extends BushBlock {
    public static final MapCodec<WildCropBlock> CODEC = simpleCodec(WildCropBlock::new);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 10, 14);

    public WildCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos); // wild plants are slightly shifted, like grass
        return SHAPE.move(offset.x, offset.y, offset.z);
    }
}
