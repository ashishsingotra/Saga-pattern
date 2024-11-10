package com.ash.repository;

import com.ash.entity.UserTransction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTransactionRepository extends JpaRepository<UserTransction, Integer> {
}
