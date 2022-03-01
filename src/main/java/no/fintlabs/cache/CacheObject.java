package no.fintlabs.cache;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import no.fintlabs.cache.packing.Packer;
import no.fintlabs.cache.packing.SerializationPacker;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
//import org.apache.commons.codec.binary.Hex;
//import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;

@Getter
@EqualsAndHashCode(of = "checksum")
@ToString
public final class CacheObject<T extends Serializable> implements Serializable {
    // TODO: 26/02/2022 Remove as static field 
    public static Packer PACKER = new SerializationPacker();

    private final byte[] checksum;
    private final long lastUpdated;
    private final byte[] bytes;
    private final int[] hashCodes;

   /* public CacheObject(T obj) {
        this(obj, new int[0]);
    }*/

    public CacheObject(T object, int[] hashCodes) {
        lastUpdated = System.currentTimeMillis();
        bytes = PACKER.pack(object);
        checksum = DigestUtils.sha1(bytes);
        this.hashCodes = hashCodes;
    }

    public T decompressObject() {
        return (T) PACKER.unpack(bytes);
    }

    public int getSize() {
        return bytes.length;
    }

    public String getChecksum() {
        return Hex.encodeHexString(checksum);
    }
}
