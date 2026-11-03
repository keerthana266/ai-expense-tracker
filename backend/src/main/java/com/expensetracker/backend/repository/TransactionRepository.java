package com.expensetracker.backend.repository;

import com.expensetracker.backend.entity.Transaction;
import com.expensetracker.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findAllByOrderByTransactionTimeDesc();

    List<Transaction> findByUserOrderByTransactionTimeDesc(User user);

    @Query("""
            SELECT t.merchant.name, SUM(t.amount)
            FROM Transaction t
            WHERE t.user = :user
            AND UPPER(t.transactionType) = 'EXPENSE'
            GROUP BY t.merchant.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> getMerchantSummary(User user);

    @Query("""
            SELECT t.category.name, SUM(t.amount)
            FROM Transaction t
            WHERE t.user = :user
            AND UPPER(t.transactionType) = 'EXPENSE'
            GROUP BY t.category.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> getCategorySummary(User user);

    @Query("""
            SELECT SUM(t.amount)
            FROM Transaction t
            WHERE t.user = :user
            AND UPPER(t.transactionType) = 'EXPENSE'
            """)
    Double getTotalExpense(User user);

    @Query("""
            SELECT MAX(t.amount)
            FROM Transaction t
            WHERE t.user = :user
            AND UPPER(t.transactionType) = 'EXPENSE'
            """)
    Double getHighestExpense(User user);

    @Query("""
            SELECT MIN(t.amount)
            FROM Transaction t
            WHERE t.user = :user
            AND UPPER(t.transactionType) = 'EXPENSE'
            """)
    Double getLowestExpense(User user);

    @Query("""
            SELECT COALESCE(AVG(t.amount), 0)
            FROM Transaction t
            WHERE t.user = :user
            AND UPPER(t.transactionType) = 'EXPENSE'
            """)
    Double getAverageExpense(User user);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.user = :user
            AND UPPER(t.category.name) = UPPER(:category)
            AND UPPER(t.transactionType) = 'EXPENSE'
            """)
    Double getTotalByCategory(User user, String category);

    @Query("""
            SELECT MONTH(t.transactionTime),
                   COALESCE(SUM(t.amount), 0.0)
            FROM Transaction t
            WHERE t.user = :user
            AND UPPER(t.transactionType) = 'EXPENSE'
            GROUP BY MONTH(t.transactionTime)
            ORDER BY MONTH(t.transactionTime)
            """)
    List<Object[]> getMonthlyExpensesRaw(User user);

    default List<Object[]> getMonthlyExpenses(User user) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        List<Object[]> raw = getMonthlyExpensesRaw(user);
        List<Object[]> formatted = new ArrayList<>();
        if (raw != null) {
            for (Object[] row : raw) {
                if (row != null && row.length >= 2 && row[0] instanceof Number num) {
                    int m = num.intValue();
                    String monthStr = (m >= 1 && m <= 12) ? months[m - 1] : "M" + m;
                    formatted.add(new Object[]{ monthStr, row[1] });
                }
            }
        }
        return formatted;
    }

    @Query("""
            SELECT t.account.id,
            UPPER(t.transactionType),
            COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.user = :user
            GROUP BY t.account.id, UPPER(t.transactionType)
            """)
    List<Object[]> getAccountTransactionSummary(User user);

    @Query("""
            SELECT t.account.id,
            t.category.name,
            COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.user = :user
            AND UPPER(t.transactionType) = 'EXPENSE'
            GROUP BY t.account.id, t.category.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> getAccountCategorySummary(User user);

    @Query("""
            SELECT t.account.id,
                   MONTH(t.transactionTime),
                   COALESCE(SUM(t.amount), 0.0)
            FROM Transaction t
            WHERE t.user = :user
            AND UPPER(t.transactionType) = 'EXPENSE'
            GROUP BY t.account.id, MONTH(t.transactionTime)
            ORDER BY MONTH(t.transactionTime)
            """)
    List<Object[]> getAccountMonthlyExpenseSummaryRaw(User user);

    default List<Object[]> getAccountMonthlyExpenseSummary(User user) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        List<Object[]> raw = getAccountMonthlyExpenseSummaryRaw(user);
        List<Object[]> formatted = new ArrayList<>();
        if (raw != null) {
            for (Object[] row : raw) {
                if (row != null && row.length >= 3 && row[1] instanceof Number num) {
                    int m = num.intValue();
                    String monthStr = (m >= 1 && m <= 12) ? months[m - 1] : "M" + m;
                    formatted.add(new Object[]{ row[0], monthStr, m, row[2] });
                }
            }
        }
        return formatted;
    }
}
