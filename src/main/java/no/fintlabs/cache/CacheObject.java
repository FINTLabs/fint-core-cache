package no.fintlabs.cache;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import no.fintlabs.cache.packing.Packer;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;

@SuppressWarnings("unchecked")
@Getter
@EqualsAndHashCode(of = "checksum")
@ToString
public final class CacheObject<T extends Serializable> implements Serializable {

    private final Packer packer;
    private final byte[] checksum;
    private final long lastUpdated;
    private final byte[] bytes;
    private final int[] hashCodes;

   /* public CacheObject(T obj) {
        this(obj, new int[0]);
    }*/

    public CacheObject(Packer packer, T object, int[] hashCodes) {
        this.packer = packer;
        lastUpdated = System.currentTimeMillis();
        bytes = packer.pack(object);
        checksum = DigestUtils.sha1(bytes);
        this.hashCodes = hashCodes;
    }

    public T decompressObject() {
        return (T) packer.unpack(bytes);
    }

    public int getSize() {
        return bytes.length;
    }

    public String getChecksum() {
        return Hex.encodeHexString(checksum);
    }
}
