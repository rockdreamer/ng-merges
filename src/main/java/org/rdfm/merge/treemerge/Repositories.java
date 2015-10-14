package org.rdfm.merge.treemerge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.rdfm.merge.ExceptionSerializer;
import org.rdfm.merge.FileDeSerializer;
import org.rdfm.merge.SVNRevisionSerializer;
import org.rdfm.merge.SVNUrlSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

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
public class Repositories implements Map<String, Repository> {
    static final Logger log = LoggerFactory.getLogger(Repositories.class);
    Map<String, Repository> map = new HashMap<>();

    public void loadAllFromJson() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeHierarchyAdapter(Throwable.class, new ExceptionSerializer())
                .registerTypeHierarchyAdapter(SVNURL.class, new SVNUrlSerializer())
                .registerTypeHierarchyAdapter(SVNRevision.class, new SVNRevisionSerializer())
                .registerTypeHierarchyAdapter(File.class, new FileDeSerializer())
                .create();
        try {
            Type collectionType = new TypeToken<Collection<Repository>>() {
            }.getType();
            Collection<Repository> repositories = gson.fromJson(new BufferedReader(new InputStreamReader(
                    ClassLoader.getSystemResourceAsStream("repositories.json"))), collectionType);
            repositories.stream().forEach(e -> map.put(e.getId(), e));
            log.info("Loaded repositories {}", keySet());
        } catch (Exception e) {
            log.error("Cannot read repositories.json", e);
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
    public Repository get(Object key) {
        return map.get(key);
    }

    @Override
    public Repository put(String key, Repository value) {
        return map.put(key, value);
    }

    @Override
    public Repository remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Repository> m) {
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
    public Collection<Repository> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, Repository>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Repositories)
            return ((Repositories) o).map.equals(map);
        return map.equals(o);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }
}
