package gg.meza.doobs.server;

import gg.meza.doobs.data.CardQueue;

import java.time.Duration;
import java.time.Instant;

public class Session {
    private final String sessionId;
    private final CardQueue cardQueue;
    private Instant lastAccessed;

    public Session(String sessionId) {
        this.sessionId = sessionId;
        this.cardQueue = new CardQueue();
        this.lastAccessed = Instant.now();
    }

    public boolean isExpired() {
        return Duration.between(lastAccessed, Instant.now()).toSeconds() > 30;
    }

    public void updateLastAccessed() {
        this.lastAccessed = Instant.now();
    }

    public String getSessionId() {
        return sessionId;
    }

    public CardQueue getCardQueue() {
        return cardQueue;
    }
}
