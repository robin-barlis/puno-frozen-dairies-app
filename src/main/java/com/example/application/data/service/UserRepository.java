package com.example.application.data.service;

import com.example.application.data.entity.AppUser;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, UUID> {

    AppUser findByUsername(String username);
}