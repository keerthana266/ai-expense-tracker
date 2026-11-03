package com.expensetracker.backend.repository;

import com.expensetracker.backend.entity.Account;
import com.expensetracker.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUserAndPrimaryAccountTrue(User user);

    List<Account> findByUser(User user);

}