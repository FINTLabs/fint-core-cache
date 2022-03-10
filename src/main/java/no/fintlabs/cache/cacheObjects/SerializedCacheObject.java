package no.fintlabs.cache.cacheObjects;

import lombok.Getter;
import lombok.ToString;
import no.fintlabs.cache.packing.Packer;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;

@SuppressWarnings("unchecked")
@Getter
@ToString
public class SerializedCacheObject<T extends Serializable> extends CacheObject<T> {

    private final Packer packer;
    private final byte[] checksum;
    private final byte[] bytes;

    public SerializedCacheObject(Packer packer, T object, int[] hashCodes) {
        super(hashCodes);

        this.packer = packer;
        bytes = packer.pack(object);
        checksum = DigestUtils.sha1(bytes);
    }

    public T unboxObject() {
        return (T) packer.unpack(bytes);
    }

    public int getSize() {
        return bytes.length;
    }

    public String getChecksum() {
        return Hex.encodeHexString(checksum);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof SerializedCacheObject other)) return false;
        return this.getChecksum().equals(other.getChecksum());
    }
}
