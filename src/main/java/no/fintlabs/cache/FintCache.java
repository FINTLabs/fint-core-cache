package no.fintlabs.cache;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.cacheObjects.CacheObject;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FintCache<T extends Serializable> implements Cache<T>, Serializable {
    @Getter
    private Map<String, CacheObject<T>> cacheObjects;
    private ListMultimap<Integer, String> hashCodesIndex;
    private ListMultimap<Long, String> lastUpdatedIndex;
    private long lastUpdated;
    private final CacheObjectFactory<T> cacheObjectFactory;
    @Getter
    private final String urn;

    @Setter
    private long retentionPeriodInMs;

    public FintCache(CacheObjectFactory<T> cacheObjectFactory, String urn) {
        this.cacheObjectFactory = cacheObjectFactory;
        this.urn = urn;
        init();
    }

    @Override
    public void flush() {
        init();
    }

    private void init() {
        cacheObjects = new LinkedHashMap<>();
        hashCodesIndex = MultimapBuilder.treeKeys().arrayListValues().build();
        lastUpdatedIndex = MultimapBuilder.treeKeys().arrayListValues().build();
        lastUpdated = 0;
    }

    @Override
    public void put(String key, T object, int[] hashCodes) {
        put(key, object, hashCodes, System.currentTimeMillis());
    }

    public void put(String key, T object, int[] hashCodes, long lastDeliveredTimeInMs) {
        synchronized (cacheObjects) {

            CacheObject<T> newCacheObject = cacheObjectFactory.createCacheObject(object, hashCodes);
            newCacheObject.setLastDelivered(lastDeliveredTimeInMs);

            if (hasEqualElement(key, newCacheObject)) {
                cacheObjects.get(key).setLastDelivered(lastDeliveredTimeInMs);
                return;
            }

            cacheObjects.put(key, newCacheObject);
            Arrays.stream(hashCodes).forEach(hashCode -> hashCodesIndex.put(hashCode, key));
            lastUpdatedIndex.put(newCacheObject.getLastUpdated(), key);

            lastUpdated = System.currentTimeMillis();
        }
    }

    @Override
    public void remove(String key) {
        CacheObject<T> cacheObject = cacheObjects.remove(key);
        if (cacheObject != null) {
            hashCodesIndex.entries().removeIf(entry -> entry.getValue().equals(key));
            lastUpdatedIndex.entries().removeIf(entry -> entry.getValue().equals(key));
        }
    }

    private boolean hasEqualElement(String key, CacheObject<T> object) {
        if (!cacheObjects.containsKey(key)) return false;
        return cacheObjects.get(key).equals(object);
    }

    private Stream<CacheObject<T>> getCacheObjectStream() {
        synchronized (cacheObjects) {
            return ImmutableList.copyOf(cacheObjects.values()).stream();
        }
    }

    private Stream<CacheObject<T>> getCacheObjectStream(long sinceTimeStamp) {
        final List<CacheObject<T>> snapshot;
        synchronized (cacheObjects) {
            snapshot = lastUpdatedIndex.keySet().stream()
                    .filter(ts -> ts > sinceTimeStamp)
                    .flatMap(ts -> lastUpdatedIndex.get(ts).stream())
                    .map(cacheObjects::get)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return snapshot.stream();
    }

    @Override
    public Stream<T> stream() {
        return getCacheObjectStream().map(CacheObject::unboxObject);
    }

    @Override
    public Stream<T> streamSince(long sinceTimeStamp) {
        return getCacheObjectStream(sinceTimeStamp).map(CacheObject::unboxObject);
    }

    @Override
    public Stream<T> streamSlice(int skip, int limit) {
        return getCacheObjectStream().skip(skip).limit(limit).map(CacheObject::unboxObject);
    }

    @Override
    public Stream<T> streamSliceSince(long sinceTimeStamp, int skip, int limit) {
        return getCacheObjectStream(sinceTimeStamp).skip(skip).limit(limit).map(CacheObject::unboxObject);
    }

    @Override
    public Stream<T> streamByHashCode(int hashCode) {
        return hashCodesIndex
                .get(hashCode)
                .stream()
                .map(s -> cacheObjects.get(s))
                .map(CacheObject::unboxObject);
    }

    @Override
    public Optional<T> getLastUpdatedByFilter(int hashCode, Predicate<T> predicate) {
        return hashCodesIndex
                .get(hashCode)
                .stream()
                .map(cacheObjects::get)
                .filter(o -> predicate.test(o.unboxObject()))
                .max(Comparator.comparingLong(CacheObject::getLastUpdated))
                .map(CacheObject::unboxObject);
    }

    @Override
    public long getLastUpdated() {
        synchronized (cacheObjects) {
            long currentLastUpdated = lastUpdated;
            sleepOneMsToPreventUpdatesInSameMs();
            return currentLastUpdated;
        }
    }

    private void sleepOneMsToPreventUpdatesInSameMs() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int size() {
        return cacheObjects.size();
    }

    @Override
    public boolean empty() {
        return cacheObjects.size() == 0;
    }

    @Override
    public long sizeOfCompressedData() {
        return cacheObjects.values().stream().mapToLong(CacheObject::getSize).sum();
    }

    @Scheduled(initialDelay = 900000L, fixedDelay = 900000L)
    public void evictOldCacheObjects() {
        log.debug("Running janitor service");
        if (retentionPeriodInMs <= 0) return;
        long currentTime = System.currentTimeMillis();

        List<Map.Entry<String, CacheObject<T>>> itemsToRemove = cacheObjects
                .entrySet()
                .stream()
                .filter(entrySet -> currentTime - entrySet.getValue().getLastDelivered() > retentionPeriodInMs)
                .collect(Collectors.toList());

        itemsToRemove
                .stream()
                .forEach(entrySet -> {
                            log.info("Remove old object: " + entrySet.getKey());
                            cacheObjects.remove(entrySet.getKey());
                        }
                );
    }
}

