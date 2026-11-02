package shopflow.w07;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class InventoryStore {
    private static final int VERSION = 1;
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private record StoreFile(Integer version, Map<String, Integer> stock) {}

    public void save(Map<String, Integer> stock, Path file) {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, MAPPER.writeValueAsString(new StoreFile(VERSION, new TreeMap<>(stock))));
        } catch (IOException e) {
            throw new StoreException("Could not write store file: " + file, e);
        }
    }

    public Map<String, Integer> load(Path file) {
        String json;
        try {
            json = Files.readString(file);
        } catch (NoSuchFileException e) {
            throw new StoreException("Store file not found: " + file, e);
        } catch (IOException e) {
            throw new StoreException("Could not read store file: " + file, e);
        }

        StoreFile parsed;
        try {
            parsed = MAPPER.readValue(json, StoreFile.class);
        } catch (JsonProcessingException e) {
            throw new StoreException("Store file is corrupt: " + file, e);
        }

        if (parsed == null || parsed.version() == null || parsed.version() != VERSION) {
            throw new StoreException("Store file has unsupported version "
                    + (parsed == null ? null : parsed.version()) + ": " + file);
        }
        if (parsed.stock() == null || parsed.stock().values().stream().anyMatch(q -> q == null || q < 0)) {
            throw new StoreException("Store file is corrupt: " + file);
        }
        return parsed.stock();
    }
}
