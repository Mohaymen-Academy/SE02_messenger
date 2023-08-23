package com.mohaymen.repository;

import com.mohaymen.model.entity.Update;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UpdateRepository extends JpaRepository<Update, Long> {

    List<Update> findByIdGreaterThan(Long id);

    Update findTopByChatIdOrderByIdDesc(String chatId);

    @Query(value = "SELECT u FROM Update u WHERE u.id > :updateId AND (u.messageId, u.updateType) IN " +
            "(SELECT DISTINCT u.messageId, u.updateType FROM Update u)", nativeQuery = true)
    List<Update> findUpdates(@Param("updateId") Long updateId);
}
