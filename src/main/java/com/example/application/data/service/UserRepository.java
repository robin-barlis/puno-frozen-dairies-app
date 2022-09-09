package com.example.application.data.service;

import com.example.application.data.entity.AppUser;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<AppUser, Integer> {

    AppUser findByUsername(String username);
    
    @Query(value = "SELECT max(id) FROM AppUser")
    int findLastId();
}