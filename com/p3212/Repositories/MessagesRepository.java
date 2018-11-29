package com.p3212.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.p3212.EntityClasses.PrivateMessage;
import com.p3212.EntityClasses.User;
import org.springframework.data.jpa.repository.Modifying;

/**
 * Repository for message entity
 */
@Repository
public interface MessagesRepository extends CrudRepository<PrivateMessage, Integer> {
    @Modifying
    @Query("update PrivateMessage p set isRead = true where p.message_id = :id")
    void setRead(@Param("id") int id);
    
    @Query("select p from PrivateMessage p where p.receiver = :first and p.sender = :second or p.receiver = :second and p.message_id.sender = :first")
    List<PrivateMessage> getAllFromDialog(@Param("first") User first, @Param("second") User second);
    
    @Query("select p from PrivateMessage p where p.receiver = :user and p.isRead = false")
    List<PrivateMessage> getUnreadMessages(@Param("user") User user);
}
