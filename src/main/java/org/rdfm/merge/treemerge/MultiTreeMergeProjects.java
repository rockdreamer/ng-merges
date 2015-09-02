package org.rdfm.merge.treemerge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.rdfm.merge.ExceptionSerializer;
import org.rdfm.merge.FileDeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by bantaloukasc on 28/08/15.
 */
public class MultiTreeMergeProjects implements Map<String, MultiTreeMergeProject> {
    static final Logger log = LoggerFactory.getLogger(Repositories.class);
    Map<String, MultiTreeMergeProject> map = new HashMap<>();

    public void loadAllFromJson() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(Throwable.class, new ExceptionSerializer())
                .registerTypeHierarchyAdapter(File.class, new FileDeSerializer())
                .create();
        try {
            Type collectionType = new TypeToken<Collection<MultiTreeMergeProject>>() {
            }.getType();
            Collection<MultiTreeMergeProject> repositories = gson.fromJson(new BufferedReader(new InputStreamReader(
                    ClassLoader.getSystemResourceAsStream("multi-tree-projects.json"))), collectionType);
            repositories.stream().forEach(e -> map.put(e.getName(), e));
            log.info("Loaded multi-tree projects {}", keySet());
        } catch (Exception e) {
            log.error("Cannot read multi-tree-projects.json", e);
            throw e;
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
    public MultiTreeMergeProject get(Object key) {
        return map.get(key);
    }

    @Override
    public MultiTreeMergeProject put(String key, MultiTreeMergeProject value) {
        return map.put(key, value);
    }

    @Override
    public MultiTreeMergeProject remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends MultiTreeMergeProject> m) {
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
    public Collection<MultiTreeMergeProject> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, MultiTreeMergeProject>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MultiTreeMergeProjects)
            return ((MultiTreeMergeProjects) o).map.equals(map);
        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
