package com.example.SpringVue.Exception.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GenericErrorResponse {

    private int status;

    private String message;

    private long timeStamp;

}
