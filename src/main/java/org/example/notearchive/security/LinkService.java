package org.example.notearchive.security;

import org.example.notearchive.domain.Link;
import org.example.notearchive.domain.Note;
import org.example.notearchive.repository.LinkRepository;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Component
public class LinkService {
    private final LinkRepository linkRepository;

    public LinkService(final LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public Link generateOneDayLink(Note note) {
        Link link = new Link();
        link.setNote(note);
        link.setLink(UUID.randomUUID().toString());
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        link.setExpiryDate(calendar.getTime());
        linkRepository.save(link);
        return link;
    }
}
