package com.example.digitaltwin.mapper;

import com.example.digitaltwin.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);
    User findById(@Param("id") Long id);
    int insert(User user);
    int countByUsername(@Param("username") String username);
    List<User> selectUsers(@Param("username") String username,
                           @Param("role") String role,
                           @Param("status") String status);
    int updateRole(@Param("id") Long id, @Param("role") String role);
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    long countAll();
}
