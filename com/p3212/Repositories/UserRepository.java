package com.p3212.Repositories;

import com.p3212.EntityClasses.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    Optional<User> findUserByVkId(int id);
}
