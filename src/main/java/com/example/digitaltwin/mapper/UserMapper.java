package com.example.digitaltwin.mapper;

import com.example.digitaltwin.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);
    int insert(User user);
    int countByUsername(@Param("username") String username);
}
