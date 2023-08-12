package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.Message;
import com.mohaymen.model.Views;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.SearchService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
public class SearchController {

    private final SearchService searchService;

    private final MessageRepository messageRepository;

    public SearchController(SearchService searchService, MessageRepository messageRepository) {
        this.searchService = searchService;
        this.messageRepository = messageRepository;
    }

    @JsonView(Views.ChatDisplay.class)
    @GetMapping("/{chatID}/search")
    public List<Message> searchInChat(@PathVariable Long chatID,
                                     @RequestBody Map<String, Object> request) {
        Long userID;
        String token;
        String searchEntry;
        try {
            token = (String) request.get("jwt");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            searchEntry = (String) request.get("search_entry");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        List<Message> messages = new ArrayList<>();
        for (Long id : searchService.searchInPv(userID, chatID, searchEntry)){
            Optional<Message> message = messageRepository.findById(id);
            message.ifPresent(messages::add);
        }
        Collections.reverse(messages);
        return messages;
    }

}
