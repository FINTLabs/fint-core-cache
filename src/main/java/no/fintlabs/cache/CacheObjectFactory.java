package no.fintlabs.cache;

import no.fintlabs.cache.cacheObjects.CacheObject;
import no.fintlabs.cache.cacheObjects.PojoCacheObject;
import no.fintlabs.cache.cacheObjects.SerializedCacheObject;
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

        switch (packingType) {
            case SERIALIZATION:
                packer = new SerializationPacker();
                break;
            case DEFLATE:
                packer = new CompressingPacker(DeflaterOutputStream::new, InflaterInputStream::new);
                break;
            default:
                packer = null;
                break;
        }
    }

    public CacheObject<T> createCacheObject(T value, int[] hashCodes) {
        switch (packingType) {
            case DEFLATE, SERIALIZATION:
                return new SerializedCacheObject<>(packer, value, hashCodes);
            case POJO:
                return new PojoCacheObject<>(value, hashCodes);
            default:
                throw new UnsupportedOperationException("Can't create cacheObject for type " + packingType);
        }
    }
}
