package com.example.SpringVue.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
@Setter
public class SaveUserRequest {

    private String userName;

    private String password;

}