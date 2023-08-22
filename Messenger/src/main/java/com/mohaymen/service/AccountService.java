package com.mohaymen.service;

import com.mohaymen.model.entity.Account;
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
        Account account = null;
        try {
            account = getAccount(userId);
        } catch (Exception e) {
            System.out.println("گروع و کانال آخرین بازدید ندارند");
        }
        if (account.getProfile().isDeleted())
            return;
        account.setLastSeen(LocalDateTime.now());
        accountRepository.save(account);
    }

    private Account getAccount(Long userId) throws Exception {
        Optional<Account> optionalAccount = accountRepository.findById(userId);
        if (optionalAccount.isEmpty()) throw new Exception("acount not found");
        return optionalAccount.get();
    }

    /**
     * Last seen recently — covers anything between 1 second and 2-3 days.
     * Last seen within a week — between 2-3 and seven days.
     * Last seen within a month — between 6-7 days and a month.
     * Last seen a long time ago — more than a month (this is also always shown to blocked users)
     */


    public String getLastSeen(Long userId) {
        Account account = null;
        try {
            account = getAccount(userId);
        } catch (Exception e) {
            return "Group-Channel";
        }
        if (account.getProfile().isDeleted())
            return "آخرین حضور خیلی وقت پیش ";
        long daysPassed = ChronoUnit.DAYS.between(account.getLastSeen(), LocalDateTime.now());
        long hoursPassed = ChronoUnit.HOURS.between(account.getLastSeen(), LocalDateTime.now());
        long minutesPassed = ChronoUnit.MINUTES.between(account.getLastSeen(), LocalDateTime.now());
        if (account.isLastSeenSetting()) {
            if (daysPassed < 4)
                return "اخیرا دیده شده";
            else if (daysPassed <= 7)
                return "آخرین حضور در یک هفته گذشته";
            else if (daysPassed <= 31)
                return "آخرین حضور در یک ماه گذشته";
            return "آخرین حضور خیلی وقت پیش ";
        }

        if (minutesPassed <= 5)
            return "آنلاین";
        else if (minutesPassed <= 59)
            return "آخرین بازدید "+(minutesPassed - 5)+" دقیقه پیش ";
        else if (hoursPassed < 24)
            return "آخرین بازدید "+(hoursPassed)+" ساعت پیش ";
        else
            return "آخرین بازدید "+(daysPassed)+" روز پیش ";

    }
}
