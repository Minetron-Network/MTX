package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.block.BlockFadeEvent;
import cn.nukkit.event.block.BlockSpreadEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.level.generator.object.BlockManager;
import cn.nukkit.level.generator.object.legacytree.LegacyTallGrass;
import cn.nukkit.level.particle.BoneMealParticle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.random.NukkitRandom;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class BlockGrassBlock extends BlockDirt {
    public static final BlockProperties PROPERTIES = new BlockProperties(GRASS_BLOCK);

    @Override
    @NotNull
    public BlockProperties getProperties() {
        return PROPERTIES;
    }

    public BlockGrassBlock(BlockState blockstate) {
        super(blockstate);
    }

    @Override
    public double getResistance() {
        return 0.6;
    }

    @Override
    public String getName() {
        return "Grass Block";
    }

    @Override
    public boolean onActivate(@NotNull Item item, Player player, BlockFace blockFace, float fx, float fy, float fz) {
        if (!this.up().canBeReplaced()) {
            return false;
        }

        if (item.isFertilizer()) {
            if (player != null && (player.gamemode & 0x01) == 0) {
                item.count--;
            }
            this.level.addParticle(new BoneMealParticle(this));
            BlockManager blockManager = new BlockManager(this.level);
            LegacyTallGrass.growGrass(blockManager, this, new NukkitRandom());
            blockManager.applyBlockUpdate();
            return true;
        } else if (item.isHoe()) {
            item.useOn(this);
            this.getLevel().setBlock(this, Block.get(BlockID.FARMLAND));
            if (player != null) {
                player.getLevel().addSound(player, Sound.USE_GRASS);
            }
            return true;
        } else if (item.isShovel()) {
            item.useOn(this);
            this.getLevel().setBlock(this, Block.get(BlockID.GRASS_PATH));
            if (player != null) {
                player.getLevel().addSound(player, Sound.USE_GRASS);
            }
            return true;
        }

        return false;
    }


    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (!this.up().canBeReplaced()) {
                BlockFadeEvent ev = new BlockFadeEvent(this, Block.get(BlockID.DIRT));
                Server.getInstance().getPluginManager().callEvent(ev);
                if (!ev.isCancelled()) {
                    this.getLevel().setBlock(this, ev.getNewState());
                    return type;
                }
            }

            ThreadLocalRandom random = ThreadLocalRandom.current();
            int x = random.nextInt((int) this.x - 1, (int) this.x + 2);
            int y = random.nextInt((int) this.y - 3, (int) this.y + 2);
            int z = random.nextInt((int) this.z - 1, (int) this.z + 2);
            Block block = this.getLevel().getBlock(new Vector3(x, y, z));

            if (block.getId().equals(Block.DIRT)) {
                if (block.up().canBeReplaced()) {
                    BlockSpreadEvent ev = new BlockSpreadEvent(block, this, Block.get(BlockID.GRASS_BLOCK));
                    Server.getInstance().getPluginManager().callEvent(ev);
                    if (!ev.isCancelled()) {
                        this.getLevel().setBlock(block, ev.getNewState());
                    }
                }
            }
            return type;
        }
        return 0;
    }

    @Override
    public boolean canSilkTouch() {
        return true;
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{Block.get(DIRT).toItem()};
    }

    @Override
    public boolean isFertilizable() {
        return true;
    }
}