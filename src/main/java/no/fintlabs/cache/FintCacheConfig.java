package no.fintlabs.cache;

import no.fintlabs.cache.packing.CompressingPacker;
import no.fintlabs.cache.packing.Packer;
import no.fintlabs.cache.packing.SerializationPacker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Configuration
public class FintCacheConfig {

    @Value("${fint.cache.packer:serialization}")
    private String cachePackerType;

//    @Bean
//    public Packer getPacker() {
//
//        switch (cachePackerType.toUpperCase()) {
//            case "DEFLATE":
//                return new CompressingPacker(DeflaterOutputStream::new, InflaterInputStream::new);
//            default:
//                return new SerializationPacker();
//        }
//
//    }
}
