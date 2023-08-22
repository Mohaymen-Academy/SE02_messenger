package com.mohaymen.repository;

import com.mohaymen.model.entity.Update;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UpdateRepository extends JpaRepository<Update, Long> {

    List<Update> findByIdGreaterThan(Long id);

    Update findTopByChatIdOrderByIdDesc(String chatId);

}
