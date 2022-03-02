package no.fintlabs.cache.packing;

import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class PackerFactory {
    private PackerFactory() {

    }

    public static Packer create(String type) {
        switch (type.toUpperCase()) {
            case "UNCOMPRESSED":
                return new SerializationPacker();
            case "DEFLATE":
            default:
                return new CompressingPacker(DeflaterOutputStream::new, InflaterInputStream::new);
        }

    }
}
