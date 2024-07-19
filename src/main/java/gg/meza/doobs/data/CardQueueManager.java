package gg.meza.doobs.data;

import gg.meza.doobs.DeckedOutOBS;
import gg.meza.doobs.server.Session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CardQueueManager {
    private final Map<Session, CardQueue> cardQueues = new ConcurrentHashMap<>();

    public void addQueueForSession(Session session) {
        cardQueues.put(session, new CardQueue());
    }

    public void queueCard(String sound) {
        cardQueues.values().forEach(queue -> queue.addCard(sound));
        DeckedOutOBS.LOGGER.debug("Queued sound: {}", sound);
        DeckedOutOBS.LOGGER.debug("Number of sessions: {}", cardQueues.size());
    }

    public void removeQueueForSession(Session session) {
        cardQueues.remove(session);
    }

    public CardQueue getQueueForSession(Session session) {
        return cardQueues.get(session);
    }
}
