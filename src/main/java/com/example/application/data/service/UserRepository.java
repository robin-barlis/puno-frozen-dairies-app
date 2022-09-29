package com.example.application.data.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.application.data.entity.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Integer> {

    AppUser findByUsername(String username);
    
    AppUser findByEmailAddress(String emailAddress);
    
    @Query(value = "SELECT max(id) FROM AppUser")
    int findLastId();
}