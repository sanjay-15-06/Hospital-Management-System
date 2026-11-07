package com.hospital.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

/**
 * Generic JSON-file repository helper. Writes to a temporary file and then
 * replaces the data file so an interrupted write is less likely to corrupt it.
 */
public class JsonFileStore<T> {
    private final Path file;
    private final ObjectMapper mapper;
    private final TypeReference<List<T>> type;
    private final Function<T, Long> idGetter;

    public JsonFileStore(String filename, ObjectMapper mapper, TypeReference<List<T>> type, Function<T, Long> idGetter) {
        this.file=Paths.get("data", filename);
        this.mapper=mapper; this.type=type; this.idGetter=idGetter;
    }
    public synchronized List<T> findAll() {
        try {
            if (Files.notExists(file)) return new ArrayList<>();
            List<T> items=mapper.readValue(file.toFile(), type);
            return items == null ? new ArrayList<>() : items;
        } catch (IOException e) { throw new IllegalStateException("Could not read " + file + ": " + e.getMessage(), e); }
    }
    public synchronized Optional<T> findById(Long id) {
        return findAll().stream().filter(x -> Objects.equals(idGetter.apply(x), id)).findFirst();
    }
    public synchronized T save(T item) {
        List<T> items=findAll();
        Long currentId=idGetter.apply(item);
        long id=currentId == null ? 0L : currentId;
        if (id==0) {
            long next=items.stream().map(idGetter).filter(Objects::nonNull).mapToLong(Long::longValue).max().orElse(0)+1;
            try { item.getClass().getMethod("setId", Long.class).invoke(item, next); }
            catch (ReflectiveOperationException e) { throw new IllegalStateException("Model must have setId(Long)", e); }
            items.add(item);
        } else {
            boolean replaced=false;
            for(int i=0;i<items.size();i++) if(Objects.equals(idGetter.apply(items.get(i)), id)){items.set(i,item);replaced=true;break;}
            if(!replaced) items.add(item);
        }
        write(items);
        return item;
    }
    public synchronized boolean deleteById(Long id) {
        List<T> items=findAll();
        boolean changed=items.removeIf(x -> Objects.equals(idGetter.apply(x), id));
        if(changed) write(items);
        return changed;
    }
    private void write(List<T> items) {
        try {
            Files.createDirectories(file.getParent());
            Path temp=file.resolveSibling(file.getFileName()+".tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(), items);
            try { Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
            catch(AtomicMoveNotSupportedException ex){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}
        } catch(IOException e){throw new IllegalStateException("Could not save " + file + ": " + e.getMessage(),e);}
    }
}
