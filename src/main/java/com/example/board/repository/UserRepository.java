package com.example.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.board.entity.UserInfo;

public interface UserRepository extends JpaRepository<UserInfo, Integer> {
}
