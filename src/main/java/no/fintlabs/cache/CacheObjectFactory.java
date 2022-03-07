package no.fintlabs.cache;

import no.fintlabs.cache.packing.CompressingPacker;
import no.fintlabs.cache.packing.Packer;
import no.fintlabs.cache.packing.PackingTypes;
import no.fintlabs.cache.packing.SerializationPacker;

import java.io.Serializable;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class CacheObjectFactory<T extends Serializable> {

    private Packer packer;
    private PackingTypes packingType;

    public CacheObjectFactory(PackingTypes packingType) {
        this.packingType = packingType;
        this.packer = packer;

        switch (packingType) {
            case DEFLATE:
                packer = new CompressingPacker(DeflaterOutputStream::new, InflaterInputStream::new);
            case SERIALIZATION:
                packer = new SerializationPacker();
        }
    }

    public CacheObject<T> createCacheObject(T value, int[] hashCodes) {
        switch (packingType) {
            case DEFLATE, SERIALIZATION:
                return new CacheObject<>(packer, value, hashCodes);
            default:
                throw new UnsupportedOperationException("Can't create cacheObject for type " + packingType);
        }

    }
}
