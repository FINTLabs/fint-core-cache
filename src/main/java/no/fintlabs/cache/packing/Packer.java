package no.fintlabs.cache.packing;

public interface Packer {
    byte[] pack(Object o);
    Object unpack(byte[] b);
}