package com.anf.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.anf.model.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, String> {
    
}
