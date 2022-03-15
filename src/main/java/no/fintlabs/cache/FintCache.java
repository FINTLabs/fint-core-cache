package no.fintlabs.cache;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.cacheObjects.CacheObject;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
public class FintCache<T extends Serializable> implements Cache<T>, Serializable {
    @Getter
    private Map<String, CacheObject<T>> cacheObjects;
    private ListMultimap<Integer, String> hashCodesIndex;
    private ListMultimap<Long, String> lastUpdatedIndex;
    private long lastUpdated;
    private final ReentrantLock lock;

    private final CacheObjectFactory<T> cacheObjectFactory;

    public FintCache(CacheObjectFactory<T> cacheObjectFactory) {
        this.cacheObjectFactory = cacheObjectFactory;
        lock = new ReentrantLock();
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
        CacheObject<T> newCacheObject = cacheObjectFactory.createCacheObject(object, hashCodes);
        if (hasElementWithSameChecksum(key, newCacheObject)) return;

        cacheObjects.put(key, newCacheObject);
        Arrays.stream(hashCodes).forEach(hashCode -> hashCodesIndex.put(hashCode, key));
        lastUpdatedIndex.put(newCacheObject.getLastUpdated(), key);

        while(lock.isLocked()){
            log.debug("Lock is on");
        }
        lastUpdated = System.currentTimeMillis();
    }

    private boolean hasElementWithSameChecksum(String key, CacheObject<T> object) {
        if (!cacheObjects.containsKey(key)) return false;
        return cacheObjects.get(key).equals(object);
    }

    private Stream<CacheObject<T>> getCacheObjectStream() {
        return cacheObjects.values().stream();
    }

    private Stream<CacheObject<T>> getCacheObjectStream(long sinceTimeStamp) {
        return Multimaps
                .filterKeys(lastUpdatedIndex, key -> key > sinceTimeStamp)
                .values()
                .stream()
                .map(s -> cacheObjects.get(s));
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
        lock.lock();
        long currentLastUpdated = lastUpdated;
        sleep();
        lock.unlock();
        return currentLastUpdated;
    }

    private void sleep() {
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
}

