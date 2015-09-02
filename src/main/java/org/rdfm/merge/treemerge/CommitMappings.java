package org.rdfm.merge.treemerge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.rdfm.merge.ExceptionSerializer;
import org.rdfm.merge.FileDeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by bantaloukasc on 28/08/15.
 */
public class CommitMappings implements Map<String, CommitMappingList> {
    static final Logger log = LoggerFactory.getLogger(CommitMappings.class);
    Map<String, CommitMappingList> map = new HashMap<>();

    public void loadAllFromJson() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(Throwable.class, new ExceptionSerializer())
                .registerTypeHierarchyAdapter(File.class, new FileDeSerializer())
                .create();

        Path dir = Paths.get("commit-streams");
        if (!dir.toFile().exists()) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path entry : stream) {
                String filename = entry.getFileName().toString();
                String id = filename.replace(".json", "");
                log.info("Loading commit mapping {} from {}", id, filename);
                CommitMappingList commitMappingList = gson.fromJson(new BufferedReader(new InputStreamReader(
                        ClassLoader.getSystemResourceAsStream("repositories.json"))), CommitMappingList.class);
                if (commitMappingList.commitMappings == null) {
                    log.info("commit mappings for {} are empty", filename);
                    continue;
                }
                log.info("Loaded {} commit mappings for {} from {}", commitMappingList.commitMappings.size(), commitMappingList.getBranchHistoryMappingName(), filename);
                map.put(commitMappingList.getBranchHistoryMappingName(), commitMappingList);
            }
        } catch (DirectoryIteratorException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            throw ex.getCause();
        }

    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public CommitMappingList get(Object key) {
        return map.get(key);
    }

    @Override
    public CommitMappingList put(String key, CommitMappingList value) {
        return map.put(key, value);
    }

    @Override
    public CommitMappingList remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends CommitMappingList> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<CommitMappingList> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, CommitMappingList>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CommitMappings)
            return ((CommitMappings) o).map.equals(map);
        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
