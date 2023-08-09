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

//    @Query("Select m from Message m where (m.sender = ?1 And m.receiver = ?2) Or " +
//            "(m.sender = ?2 And m.receiver = ?1) And (m.messageID < ?3) Order By m.messageID Desc " +
//            "limit = ?4")
//    List<Message> findPVUpMessages(Profile sender, Profile receiver, Long messageID, int limit);

//    @Query("Select m from Message m where (m.sender = ?1 And m.receiver = ?2) Or " +
//            "(m.sender = ?2 And m.receiver = ?1) And (m.messageID > ?3) Order By m.messageID Desc " +
//            "limit = ?4")
//    List<Message> findPVDownMessages(Profile sender, Profile receiver, Long messageID, int limit);

    @Query("Select m from Message m where m.receiver = :receiver")
    List<Message> findPVTopNMessages(
                                     @Param("receiver") Profile receiver);
}
