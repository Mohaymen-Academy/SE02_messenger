package com.mohaymen.repository;

import com.mohaymen.model.Message;
import com.mohaymen.model.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderAndReceiverAndIdLessThanOrderByTimeAsc(Profile sender,
                                                                      Profile receiver,
                                                                      Long messageID,
                                                                      Pageable pageable);

    List<Message> findBySenderAndReceiverAndIdGreaterThanOrderByTimeAsc(Profile sender,
                                                                     Profile receiver,
                                                                     Long messageID,
                                                                     Pageable pageable);

    List<Message> findTopNBySenderAndReceiverAndIdOrderByTimeAsc(Profile sender,
                                     Profile receiver,
                                     Long messageID,
                                     Pageable pageable,
                                     int limit);
}
