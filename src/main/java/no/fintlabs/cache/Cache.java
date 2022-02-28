package no.fintlabs.cache;

import java.io.Serializable;
import java.util.stream.Stream;

public interface Cache<T extends Serializable> {
    void put(String key, T object, int[] hashCodes);

    void flush();

    long getLastUpdated();

    int size();

    long sizeOfCompressedData();

    Stream<T> stream();

    Stream<T> streamSince(long timestamp);

    Stream<T> streamByHashCode(int hashCode);
}

