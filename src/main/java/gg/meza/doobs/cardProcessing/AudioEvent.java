package gg.meza.doobs.cardProcessing;

import gg.meza.doobs.DeckedOutOBS;
import gg.meza.doobs.data.CardQueueManager;
import gg.meza.doobs.data.Location;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AudioEvent {
    private final Set<String> processedIds = new HashSet<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> futureTask = null;
    private final CardQueueManager queueManager;
    private final Location dungeon;

    public AudioEvent(CardQueueManager queue, Location dungeon) {
        this.queueManager = queue;
        this.dungeon = dungeon;
    }

    public void processBlocks(ClientLevel world) {
        Map<BlockPos, String> jukeboxes = CardCallouts.getCallouts(dungeon);

        jukeboxes.forEach((pos, sound) -> {
            BlockEntity potentialJukebox = world.getBlockEntity(pos);
            if (potentialJukebox == null) {
                return;
            }
            if (potentialJukebox instanceof JukeboxBlockEntity jukebox) {
                BlockState cs = jukebox.getBlockState();
                if (cs.getValue(JukeboxBlock.HAS_RECORD)) {
                    if (processedIds.contains(sound)) {
                        resetTimer();
                        return;
                    }
                    processedIds.add(sound);
                    queueManager.queueCard(sound);
                    DeckedOutOBS.LOGGER.debug(Component.translatable("decked-out-obs.system.playing_card", sound).getString());
                }
            }
        });

    }

    private void resetTimer() {
        if (futureTask != null) {
            futureTask.cancel(false);
        }
        futureTask = scheduler.schedule(this::resetProcessedIds, 5, TimeUnit.SECONDS);
    }

    public void resetProcessedIds() {
        processedIds.clear();
        DeckedOutOBS.LOGGER.debug(Component.translatable("decked-out-obs.system.resetting").getString());
    }

}
