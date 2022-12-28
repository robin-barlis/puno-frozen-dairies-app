package com.example.application.data.service;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.application.data.entity.PasswordReset;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Integer> {

    PasswordReset findByToken(String token);
}