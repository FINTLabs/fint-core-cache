package no.fintlabs.cache;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
public class FintCache<T extends Serializable> implements Cache<T>, Serializable {
    @Getter
    private Map<String, CacheObject<T>> cacheObjects;
    private ListMultimap<Integer, String> hashCodesIndex;
    private ListMultimap<Long, String> lastUpdatedIndex;
    private long lastUpdated;

    public FintCache() {
        init();
    }

    @Override
    public void flush() {
        init();
    }

    private void init() {
        cacheObjects = new HashMap<>();
        hashCodesIndex = MultimapBuilder.treeKeys().arrayListValues().build();
        lastUpdatedIndex = MultimapBuilder.treeKeys().arrayListValues().build();
        lastUpdated = 0;
    }

    @Override
    public void put(String key, T object, int[] hashCodes) {
        CacheObject<T> newCacheObject = new CacheObject<>(object, hashCodes);
        if (hasElementWithSameChecksum(key, newCacheObject)) return;

        cacheObjects.put(key, newCacheObject);
        Arrays.stream(hashCodes).forEach(hashCode -> hashCodesIndex.put(hashCode, key));
        lastUpdatedIndex.put(newCacheObject.getLastUpdated(), key);
        lastUpdated = System.currentTimeMillis();
    }

    private boolean hasElementWithSameChecksum(String key, CacheObject<T> object) {
        if (!cacheObjects.containsKey(key)) return false;
        return cacheObjects.get(key).getChecksum().equals(object.getChecksum());
    }

    private Stream<CacheObject<T>> getUncompressedStream() {
        return cacheObjects.values().stream();
    }

    private Stream<CacheObject<T>> getUncompressedStream(long sinceTimeStamp) {
        return Multimaps
                .filterKeys(lastUpdatedIndex, key -> key > sinceTimeStamp)
                .values()
                .stream()
                .map(s -> cacheObjects.get(s));
    }

    @Override
    public Stream<T> stream() {
        return getUncompressedStream().map(CacheObject::decompressObject);
    }

    @Override
    public Stream<T> streamSince(long sinceTimeStamp) {
        return getUncompressedStream(sinceTimeStamp).map(CacheObject::decompressObject);
    }

    @Override
    public Stream<T> streamSlice(int skip, int limit) {
        return getUncompressedStream().skip((long) skip).limit((long) limit).map(CacheObject::decompressObject);
    }

    @Override
    public Stream<T> streamSliceSince(long sinceTimeStamp, int skip, int limit) {
        return getUncompressedStream(sinceTimeStamp).skip((long) skip).limit((long) limit).map(CacheObject::decompressObject);
    }

    @Override
    public Stream<T> streamByHashCode(int hashCode) {
        return hashCodesIndex
                .get(hashCode)
                .stream()
                .map(s -> cacheObjects.get(s))
                .map(CacheObject::decompressObject);
    }

    @Override
    public Optional<T> getLastUpdatedByFilter(int hashCode, Predicate<T> predicate) {
        return hashCodesIndex
                .get(hashCode)
                .stream()
                .map(cacheObjects::get)
                .filter(o -> predicate.test(o.decompressObject()))
                .max(Comparator.comparingLong(CacheObject::getLastUpdated))
                .map(CacheObject::decompressObject);
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
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
        return cacheObjects.values().stream().mapToLong(cacheObjects -> cacheObjects.getSize()).sum();
    }
}

