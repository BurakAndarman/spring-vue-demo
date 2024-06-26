package com.example.SpringVue.Dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class NewsPreferencesDto {

    private String language;

    private List<String> interestedTopics;

}
