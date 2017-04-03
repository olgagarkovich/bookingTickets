package com.project.cinema.service;

import com.project.cinema.dto.Session;
import com.project.cinema.dto.Ticket;

import java.util.Collection;

public interface TicketService {

    Collection<Ticket> getTicketsBySession(String sessionId);

    Ticket bookTicket(String sessionId, String ticketId);

    void unbookTicket(String ticketId);

    int freeTickets(String session);

}
