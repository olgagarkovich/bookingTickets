package com.project.cinema.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.project.cinema.dto.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionServiceImpl implements SessionService {

    public static final String SESSIONS_DATA_FILE = "data/sessions.json";

    @Autowired
    private FileStoreService fileStoreService;

    private Map<String, Session> sessionsMap = new ConcurrentHashMap<>();

    @Override
    public Collection<Session> getSessions() {
        return sessionsMap.values();
    }

    @Override
    public Session getSession(String id) {
//        return Optional.ofNullable(sessionsMap.get(id)).orElseThrow(ResourceNotFoundException::new);
        Session session = sessionsMap.get(id);
        if(session == null){
            throw new ResourceNotFoundException();
        } else {
            return session;
        }
    }

    @Override
    public Session createSession(Session session) {
        String id = UUID.randomUUID().toString();
        return updateSession(id, session);
    }

    @Override
    public Session updateSession(String id, Session session) {
        session.setId(id);
        sessionsMap.put(id, session);
        fileStoreService.write(Session.class, sessionsMap.values(), SESSIONS_DATA_FILE);
        return session;
    }

    @PostConstruct
    public void initData() throws IOException {
        Collection<Session> sessions = fileStoreService.read(Session.class, new TypeReference<Collection<Session>>(){}, SESSIONS_DATA_FILE);
        sessions.forEach(s -> sessionsMap.put(s.getId(), s));
    }
}
