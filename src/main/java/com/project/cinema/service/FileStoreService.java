package com.project.cinema.service;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Collection;

public interface FileStoreService {

    <T> Collection<T> read(Class<T> type, TypeReference<Collection<T>> typeReference, String file);

    <T> void write(Class<T> type, Collection<T> items, String file);
}
