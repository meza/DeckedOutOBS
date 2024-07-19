package gg.meza.doobs.data;

import gg.meza.doobs.DeckedOutOBS;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class CardQueue {
    private final Queue<TimedCard> queue = new ConcurrentLinkedDeque<>();

    public CardQueue() {
        DeckedOutOBS.scheduler.scheduleWithFixedDelay(this::cleanupExpiredCards, 30, 30, TimeUnit.SECONDS);
    }

    public void addCard(String string) {
        queue.add(new TimedCard(string));
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
