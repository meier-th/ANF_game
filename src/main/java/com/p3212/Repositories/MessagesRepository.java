package com.p3212.Repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.p3212.EntityClasses.PrivateMessage;
import com.p3212.EntityClasses.User;

/**
 * Repository for message entity
 */
@Repository
public interface MessagesRepository extends CrudRepository<PrivateMessage, Integer> {
    @Query("update PrivateMessage p set isRead = true where p.receiver = :receiver AND p.sender = :sender AND p.message_id.sendingDate = :date")
    void setRead(@Param("sender") User sender, @Param("receiver") User receiver, @Param("date") Date date);
    
    @Query("select p from PrivateMessage p where p.receiver = :first and p.sender = :second or p.receiver = :second and p.message_id.sender = :first")
    List<PrivateMessage> getAllFromDialog(@Param("first") User first, @Param("second") User second);
    
    @Query("select p from PrivateMessage p where p.receiver = :user and p.isRead = false")
    List<PrivateMessage> getUnreadMessages(@Param("user") User user);
}
