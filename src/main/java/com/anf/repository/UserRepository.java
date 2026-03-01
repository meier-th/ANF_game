package com.anf.repository;

import com.anf.model.User;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
  Optional<User> findUserByVkId(int id);

  Optional<User> findUserByEmail(String email);
}
