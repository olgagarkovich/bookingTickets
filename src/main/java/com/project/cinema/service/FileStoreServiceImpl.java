package com.project.cinema.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class FileStoreServiceImpl implements FileStoreService {

    private ObjectMapper objectMapper = new ObjectMapper();

    private Map<Class, ReadWriteLock> readWriteLocks = new ConcurrentHashMap<>();

    @Override
    public <T> Collection<T> read(Class<T> type, TypeReference<Collection<T>> typeReference, String file) {
        Lock lock = getLock(type).readLock();
        lock.lock();
        try (InputStream in = this.getClass().getClassLoader()
                .getResourceAsStream(file)) {
            return objectMapper.readValue(in, typeReference);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> void write(Class<T> type, Collection<T> items, String file) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            Lock lock = getLock(type).writeLock();
            URL url = this.getClass().getClassLoader().getResource(file);
            if (url == null) {
                throw new RuntimeException("Resource not found: " + file);
            }
            lock.lock();
            try (OutputStream out = new FileOutputStream(url.getFile())) {
                objectMapper.writeValue(out, items);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        });
    }

    private ReadWriteLock getLock(Class type) {
        return readWriteLocks.computeIfAbsent(type, (t) -> new ReentrantReadWriteLock());
    }
}
