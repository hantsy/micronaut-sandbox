package com.example;

import io.micronaut.data.jdbc.runtime.JdbcOperations;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import jakarta.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Singleton
@RequiredArgsConstructor
public class CustomerDaoWithJdbcOperations implements CustomerDao {
    public static final Function<ResultSet, Customer> MAPPING_FUNCTION = (rs) -> {
        try {
            var id = rs.getObject("id", UUID.class);
            var name = rs.getString("name");
            var age = rs.getInt("age");
            var street = rs.getString("street");
            var city = rs.getString("city");
            var zip = rs.getString("zip");
            var version = rs.getLong("version");
            return new Customer(id, name, age, new Address(street, city, zip), version);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    };

    private final JdbcOperations jdbcOperations;

    @Override
    @Transactional
    public List<Customer> findAll() {
        var sql = "SELECT * FROM customers ";
        return jdbcOperations.prepareStatement(sql, statement -> {
            var rs = statement.executeQuery();
            var customers = new ArrayList<Customer>();
            while (rs.next()) {
                customers.add(MAPPING_FUNCTION.apply(rs));
            }
            return customers;
        });
    }

    @Override
    @Transactional
    public Optional<Customer> findById(UUID id) {
        var sql = "SELECT *  FROM  customers WHERE id=? ";
        return jdbcOperations.prepareStatement(sql, statement -> {
            statement.setObject(1, id);
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.ofNullable(MAPPING_FUNCTION.apply(resultSet));
            }
            return Optional.ofNullable(null);
        });
    }

    @Override
    @Transactional
    public UUID save(Customer data) {
        var sql = "INSERT INTO customers(name, age, street, city, zip) VALUES (?, ?, ?, ?, ?) RETURNING id ";

        return jdbcOperations.prepareStatement(sql, stmt -> {
            try {
                stmt.setString(1, data.name());
                stmt.setInt(2, data.age());
                stmt.setString(3, data.address().street());
                stmt.setString(4, data.address().city());
                stmt.setString(5, data.address().zip());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("id");
            }
            return null;
        });
    }

    @Override
    @Transactional
    public Integer deleteAll() {
        var sql = "DELETE  FROM customers";
        return jdbcOperations.prepareStatement(sql, PreparedStatement::executeUpdate);
    }

    @Override
    @Transactional
    public Integer deleteById(UUID id) {
        var sql = "DELETE FROM customers WHERE id=?";
        return jdbcOperations.prepareStatement(sql, stmt -> {
            try {
                stmt.setObject(1, id);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return stmt.executeUpdate();
        });
    }
}
