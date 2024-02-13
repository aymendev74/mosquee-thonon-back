package org.mosqueethonon.concurrent;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LockManager {

    private final Map<String, Lock> locks = new ConcurrentHashMap<>();
    public static final String LOCK_INSCRIPTIONS = "LOCK_INSCRIPTIONS";

    public Lock getLock(String resourceName) {
        locks.putIfAbsent(resourceName, new ReentrantLock());
        return locks.get(resourceName);
    }

}
