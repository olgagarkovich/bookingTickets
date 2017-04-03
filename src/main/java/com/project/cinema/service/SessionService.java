package com.project.cinema.service;

import com.project.cinema.dto.Session;

import java.util.Collection;
import java.util.List;

public interface SessionService {

    Collection<Session> getSessions();

    Session getSession(String id);

    Session createSession(Session session);

    Session updateSession(String id, Session session);

}
