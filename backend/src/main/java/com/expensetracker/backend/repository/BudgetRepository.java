package com.expensetracker.backend.repository;

import com.expensetracker.backend.entity.Budget;
import com.expensetracker.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository
        extends JpaRepository<Budget, Long> {

    // Get all budgets belonging to the logged-in user
    List<Budget> findByUser(User user);

    // Find a budget for a particular category
    Optional<Budget> findByUserAndCategory(
            User user,
            String category
    );
}