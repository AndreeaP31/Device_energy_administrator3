package com.example.demo.dtos.builders;

import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.entities.User;

public class UserBuilder {

    private UserBuilder() {
    }

    public static UserDTO toUserDTO(User User) {
        return new UserDTO(User.getId(), User.getName());
    }

    public static UserDetailsDTO toUserDetailsDTO(User User) {
        return new UserDetailsDTO(User.getId(), User.getName());
    }

    public static User toEntity(UserDetailsDTO userDetailsDTO) {
        return new User(userDetailsDTO.getName());
    }
}
