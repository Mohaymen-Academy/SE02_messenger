package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.ChatType;
import com.mohaymen.model.Message;
import com.mohaymen.model.Profile;
import com.mohaymen.model.Views;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.ProfileRepository;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.SearchService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
public class SearchController {

    private final SearchService searchService;

    private final ProfileRepository profileRepository;

    public SearchController(SearchService searchService, MessageRepository messageRepository, ProfileRepository profileRepository) {
        this.searchService = searchService;
        this.profileRepository = profileRepository;
    }

    @JsonView(Views.ChatDisplay.class)
    @GetMapping("/{chatID}/search")
    public List<Message> searchInChat(@PathVariable Long chatID,
                                      @RequestHeader(name = "Authorization") String token,
                                      @RequestParam(name = "search_entry") String searchEntry) {
        Long userID;
        try {
            userID = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }

        Optional<Profile> profile = profileRepository.findById(chatID);
        if(profile.isPresent()) {
            Profile p = profile.get();
            if(p.getType() == ChatType.USER) {
                return searchService.searchInPv(userID, chatID, searchEntry);
            }
            else {
                return searchService.searchInChat(chatID, searchEntry);
            }
        }
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

}
