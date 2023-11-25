package com.example.SpringVue.Controller;

import com.example.SpringVue.Request.SaveUserRequest;
import com.example.SpringVue.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;

    }

    @PostMapping("/save")
    public String saveUser(@RequestBody SaveUserRequest saveUserRequest) {

        String userName = userService.addUser(saveUserRequest);

        return userName;
    }

}