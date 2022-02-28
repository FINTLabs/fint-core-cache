package no.fintlabs.cache.packing;

import org.springframework.util.SerializationUtils;

public class SerializationPacker implements Packer {
    @Override
    public byte[] pack(Object o) {
        return SerializationUtils.serialize(o);
    }

    @Override
    public Object unpack(byte[] b) {
        return SerializationUtils.deserialize(b);
    }
}