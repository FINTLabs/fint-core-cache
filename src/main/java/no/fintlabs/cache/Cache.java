package no.fintlabs.cache;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface Cache<T extends Serializable> {
    void put(String key, T object, int[] hashCodes);

    void flush();

    long getLastUpdated();

    int size();

    boolean empty();

    long sizeOfCompressedData();

    Stream<T> stream();

    Stream<T> streamSince(long timestamp);

    Stream<T> streamSlice(int skip, int limit);

    Stream<T> streamSliceSince(long sinceTimeStamp, int skip, int limit);

    Stream<T> streamByHashCode(int hashCode);

    Optional<T> getLastUpdatedByFilter(int hashCode, Predicate<T> predicate);

    void evictOldCacheObjects();

    void setRetentionPeriodInMs(long periodInMs);
}

