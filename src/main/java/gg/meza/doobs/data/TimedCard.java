package gg.meza.doobs.data;

import java.time.Instant;

public class TimedCard {
    private final String card;
    private final long timestamp;

    public TimedCard(String card) {
        this.card = card;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public String getCard() {
        return card;
    }

    public boolean isExpired() {
        return Instant.now().toEpochMilli() - timestamp > 20000; // 20 seconds
    }
}
