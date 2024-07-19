package gg.meza.doobs.data;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CardQueue implements AutoCloseable {
    private final Queue<TimedCard> queue = new ConcurrentLinkedDeque<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public CardQueue() {
        scheduler.scheduleAtFixedRate(this::cleanupExpiredCards, 30, 30, TimeUnit.SECONDS);
    }

    public void addCard(String string) {
        queue.add(new TimedCard(string));
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public String nextCard() {
        TimedCard timedCard = queue.poll();
        return (timedCard != null) ? timedCard.getCard() : null;
    }

    private void cleanupExpiredCards() {
        queue.removeIf(TimedCard::isExpired);
    }
}
