package com.anf.Repositories;

import com.anf.EntityClasses.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    Optional<User> findUserByVkId(int id);

    Optional<User> findUserByEmail(String email);
    
}
