package com.project.cinema.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.project.cinema.dto.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class TicketServiceImpl implements TicketService {

    public static final String TICKETS_DATA_FILE = "data/tickets.json";

    private Map<String, Collection<Ticket>> ticketsBySession = new ConcurrentHashMap<>();
    private Map<String, Ticket> ticketsById = new ConcurrentHashMap<>();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    @Autowired
    private FileStoreService fileStoreService;

    @Autowired
    private SessionService sessionService;

    @Override
    public Collection<Ticket> getTicketsBySession(String sessionId) {
        readWriteLock.readLock().lock();
        try {
            return ticketsBySession.getOrDefault(sessionId, Collections.emptyList());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public Ticket bookTicket(String sessionId, String user) {
        readWriteLock.writeLock().lock();
        try {
            if (freeTickets(sessionId) == 0) {
                throw new RuntimeException("No free tickets for session id: " + sessionId);
            }
            String id = UUID.randomUUID().toString();
            Ticket ticket = new Ticket(sessionId, id);
            saveTicket(ticket);
            fileStoreService.write(Ticket.class, ticketsById.values(), TICKETS_DATA_FILE);
            return ticket;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public void unbookTicket(String ticketId) {
        readWriteLock.writeLock().lock();
        try {
            Ticket ticket = ticketsById.remove(ticketId);
            ticketsBySession.get(ticket.getSessionId()).remove(ticket);
            fileStoreService.write(Ticket.class, ticketsById.values(), TICKETS_DATA_FILE);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private void saveTicket(Ticket ticket) {
        ticketsBySession.computeIfAbsent(ticket.getSessionId(), (c) -> new ArrayList()).add(ticket);
        ticketsById.put(ticket.getId(), ticket);
    }

    @Override
    public int freeTickets(String sessionId) {
        readWriteLock.readLock().lock();
        try {
            return sessionService.getSession(sessionId).getPlaceCount() - ticketsBySession.getOrDefault(sessionId, Collections.emptyList()).size();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @PostConstruct
    public void initData() throws IOException {
        Collection<Ticket> tickets = fileStoreService.read(Ticket.class, new TypeReference<Collection<Ticket>>() {
        }, TICKETS_DATA_FILE);
        tickets.forEach(this::saveTicket);
    }
}
