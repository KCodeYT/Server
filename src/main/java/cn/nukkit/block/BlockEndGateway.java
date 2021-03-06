package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.Identifier;

import static cn.nukkit.block.BlockIds.AIR;

/**
 * @author PikyCZ
 */
public class BlockEndGateway extends BlockSolid {

    public BlockEndGateway(Identifier id) {
        super(id);
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public boolean isBreakable(Item item) {
        return false;
    }

    @Override
    public float getHardness() {
        return -1;
    }

    @Override
    public float getResistance() {
        return 18000000;
    }

    @Override
    public int getLightLevel() {
        return 15;
    }

    @Override
    public boolean hasEntityCollision() {
        return true;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.BLACK_BLOCK_COLOR;
    }

    @Override
    public Item toItem() {
        return Item.get(AIR, 0, 0);
    }

}
