package com.example.rangiffler.data.repository;

import com.example.rangiffler.data.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    UserEntity findByUsername(String username);

    List<UserEntity> findByUsernameNot(String username);
}
