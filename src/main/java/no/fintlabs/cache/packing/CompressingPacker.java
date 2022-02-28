package no.fintlabs.cache.packing;


import java.io.*;
import java.util.function.Function;

public class CompressingPacker implements Packer {

    private final Function<OutputStream, OutputStream> outputStreamConstructor;
    private final Function<InputStream, InputStream> inputStreamConstructor;

    public CompressingPacker(Function<OutputStream, OutputStream> outputStreamConstructor, Function<InputStream, InputStream> inputStreamConstructor) {
        this.outputStreamConstructor = outputStreamConstructor;
        this.inputStreamConstructor = inputStreamConstructor;
    }

    @Override
    public byte[] pack(Object o) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream oout = new ObjectOutputStream(outputStreamConstructor.apply(out))) {
            oout.writeObject(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    @Override
    public Object unpack(byte[] b) {
        try (ObjectInputStream oin = new ObjectInputStream(inputStreamConstructor.apply(new ByteArrayInputStream(b)))) {
            return oin.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

