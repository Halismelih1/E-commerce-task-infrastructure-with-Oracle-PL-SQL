package com.example.demo.service;


import com.example.demo.dto.UserRegisterRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.entities.Users;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private HikariDataSource dataSource;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<Users> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }


    //Oracle PL/SQL prosedürleri çağır
    public void registerUser(UserRegisterRequest userRegisterRequest) {
        String sql = "{ call RegisterPackage.RegisterUser(?, ?, ?, ?, ?, ?) }";
        jdbcTemplate.update(sql, userRegisterRequest.getUsername(), userRegisterRequest.getUserPassword(),userRegisterRequest.getFirstname(),userRegisterRequest.getLastname(),userRegisterRequest.getEmail(),userRegisterRequest.getPhone());
    }

    //login şuan için çalışmıyor bakılacak
    public boolean loginUser(String email, String password) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "{ ? = call LoginPackage.LoginUser(?, ?) }";
            try (CallableStatement callableStatement = connection.prepareCall(sql)) {
                callableStatement.registerOutParameter(1, Types.NUMERIC); // NUMERIC türüne kayıt oluştur
                callableStatement.setString(2, email);
                callableStatement.setString(3, password);
                callableStatement.execute();
                int loginResult = callableStatement.getInt(1); // NUMERIC değeri INT olarak al
                return loginResult == 1; // 1 başarılı girişi, 0 başarısız girişi temsil eder
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to login: " + e.getMessage(), e);
        }
    }



    public void resetPassword(String email, String oldPassword, String newPassword) {
        String sql = "{ call ChangePasswordPackage.resetPassword(?, ?, ?) }";
        jdbcTemplate.update(sql, email, oldPassword, newPassword);
    }

}
