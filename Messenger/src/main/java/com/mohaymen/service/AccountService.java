package com.mohaymen.service;


import com.mohaymen.model.entity.Account;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public void UpdateLastSeen(Long userId) {
        Account account = getAccount(userId);
        if (account.getProfile().isDeleted())
            return;
        account.setLastSeen(LocalDateTime.now());
        accountRepository.save(account);
    }

    private Account getAccount(Long userId) {
        Optional<Account> optionalAccount = accountRepository.findById(userId);
        if (optionalAccount.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return optionalAccount.get();
    }

    /**
     * Last seen recently — covers anything between 1 second and 2-3 days.
     * Last seen within a week — between 2-3 and seven days.
     * Last seen within a month — between 6-7 days and a month.
     * Last seen a long time ago — more than a month (this is also always shown to blocked users)
     */


    public String getLastSeen(Long userId) throws Exception {
        Account account = getAccount(userId);
        if (account.getProfile().isDeleted())
            return "last Seen a long time ago";
        long daysPassed = ChronoUnit.DAYS.between(account.getLastSeen(), LocalDateTime.now());
        long minutesPassed = ChronoUnit.MINUTES.between(account.getLastSeen(), LocalDateTime.now());
        if (account.isLastSeenSetting()) {
            if (daysPassed < 4)
                return "Last seen recently";
            else if (daysPassed <= 7)
                return "Last seen within a week";
            else if (daysPassed <= 31)
                return "Last seen within a month";
            return "Last seen a long time ago";
        }

        System.out.println(minutesPassed);
        if (minutesPassed <= 5)
            return "Online";
        else if (minutesPassed <= 59)
            return "Last seen " + (account.getLastSeen().getMinute() - 5) + " minutes ago";
        else if (minutesPassed < 1445)
            return "Last seen " + account.getLastSeen().getHour() + "hours ago";
        else
            return "Last seen " + daysPassed + " days ago";

    }
}
