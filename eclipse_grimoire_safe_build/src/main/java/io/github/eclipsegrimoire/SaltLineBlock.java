package io.github.eclipsegrimoire;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

/** Redstone-like connected white dust; it emits no redstone power. */
public class SaltLineBlock extends Block {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");

    public SaltLineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(NORTH,false).setValue(EAST,false).setValue(SOUTH,false).setValue(WEST,false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(NORTH,EAST,SOUTH,WEST); }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level=context.getLevel(); BlockPos pos=context.getClickedPos();
        return defaultBlockState().setValue(NORTH,level.getBlockState(pos.north()).is(this))
            .setValue(EAST,level.getBlockState(pos.east()).is(this))
            .setValue(SOUTH,level.getBlockState(pos.south()).is(this))
            .setValue(WEST,level.getBlockState(pos.west()).is(this));
    }
    @Override public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor,
                                             LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (dir==Direction.DOWN && !canSurvive(state,level,pos)) return Blocks.AIR.defaultBlockState();
        return switch(dir) {
            case NORTH -> state.setValue(NORTH,neighbor.is(this));
            case EAST -> state.setValue(EAST,neighbor.is(this));
            case SOUTH -> state.setValue(SOUTH,neighbor.is(this));
            case WEST -> state.setValue(WEST,neighbor.is(this));
            default -> state;
        };
    }
    @Override public boolean canBeReplaced(BlockState state, Fluid fluid) {
        return fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA);
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Block.box(5,0,5,11,1,11);
        if(state.getValue(NORTH)) shape=Shapes.or(shape, Block.box(6,0,0,10,1,6));
        if(state.getValue(SOUTH)) shape=Shapes.or(shape, Block.box(6,0,10,10,1,16));
        if(state.getValue(EAST)) shape=Shapes.or(shape, Block.box(10,0,6,16,1,10));
        if(state.getValue(WEST)) shape=Shapes.or(shape, Block.box(0,0,6,6,1,10));
        return shape;
    }
}
