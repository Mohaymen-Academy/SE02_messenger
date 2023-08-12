package com.mohaymen.repository;

import com.mohaymen.model.Message;
import com.mohaymen.model.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByReceiverAndMessageIDLessThanOrderByTimeDesc(Profile receiver,
                                                            Long messageID,
                                                            Pageable pageable);

    List<Message> findByReceiverAndMessageIDGreaterThanOrderByTimeDesc(Profile receiver,
                                                               Long messageID,
                                                               Pageable pageable);

    List<Message> findTopNByReceiverAndMessageIDOrderByTimeDesc(Profile receiver,
                                                        Long messageID,
                                                        Pageable pageable,
                                                        int limit);

    @Query("Select m from Message m where ((m.sender = :sender And m.receiver = :receiver) " +
            "OR (m.sender = :receiver And m.receiver = :sender)) And (m.messageID < :messageID) " +
            "ORDER BY m.messageID DESC " +
            "LIMIT :limit")
    List<Message> findPVUpMessages(Profile sender, Profile receiver, Long messageID, int limit);

    @Query("Select m from Message m where ((m.sender = :sender And m.receiver = :receiver) " +
            "OR (m.sender = :receiver And m.receiver = :sender)) And (m.messageID > :messageID) " +
            "ORDER BY m.messageID ASC " +
            "LIMIT :limit")
    List<Message> findPVDownMessages(Profile sender, Profile receiver, Long messageID, int limit);

    @Query("Select m from Message m where ((m.sender = :sender AND m.receiver = :receiver) " +
            "OR (m.sender = :receiver AND m.receiver = :sender)) " +
            "ORDER BY m.messageID DESC " +
            "LIMIT :limit")
    List<Message> findPVTopNMessages(@Param("sender") Profile sender,
                                     @Param("receiver") Profile receiver,
                                     @Param("limit") int limit);

    int countByReceiverAndMessageIDGreaterThan(Profile receiver, Long messageID);

    int countBySenderAndReceiverAndMessageIDGreaterThan(Profile sender, Profile receiver, Long messageID);

    Message findTopBySenderAndReceiverOrderByMessageIDDesc(Profile sender, Profile receiver);

    Message findTopByReceiverOrderByMessageIDDesc(Profile receiver);

    Message findFirstBySenderAndReceiver(Profile sender, Profile receiver);

    Message findFirstByReceiver(Profile receiver);

    List<Message> findBySenderAndReceiverAndMessageIDBetween(Profile sender,
                                                             Profile receiver,
                                                             Long minMessageIDAmount,
                                                             Long MaxMessageIDAmount);

    List<Message> findByReceiverAndMessageIDBetween(Profile receiver,
                                                    Long minMessageIDAmount,
                                                    Long MaxMessageIDAmount);
}
