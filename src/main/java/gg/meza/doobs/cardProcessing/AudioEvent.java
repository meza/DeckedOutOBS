package gg.meza.doobs.cardProcessing;

import gg.meza.doobs.DeckedOutOBS;
import gg.meza.doobs.data.CardQueueManager;
import gg.meza.doobs.data.Location;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    public void processBlocks(ClientWorld world) {
        Map<BlockPos, String> jukeboxes = CardCallouts.getCallouts(dungeon);

        jukeboxes.forEach((pos, sound) -> {
            BlockEntity potentialJukebox = world.getBlockEntity(pos);
            if (potentialJukebox == null) {
                return;
            }
            if (potentialJukebox instanceof JukeboxBlockEntity jukebox) {
                BlockState cs = jukebox.getCachedState();
                if (cs.get(JukeboxBlock.HAS_RECORD)) {
                    if (processedIds.contains(sound)) {
                        resetTimer();
                        return;
                    }
                    processedIds.add(sound);
                    queueManager.queueCard(sound);
                    DeckedOutOBS.LOGGER.debug(Text.translatable("decked-out-obs.system.playing_card", sound).getString());
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
        DeckedOutOBS.LOGGER.debug(Text.translatable("decked-out-obs.system.resetting").getString());
    }

}
