package com.anf.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.anf.model.database.User;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
  Optional<User> findUserByVkId(int id);

  Optional<User> findUserByEmail(String email);
}
