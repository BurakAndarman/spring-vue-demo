package com.example.SpringVue.Service;

import com.example.SpringVue.Dto.NewsApi.TopHeadlines.Article;
import com.example.SpringVue.Dto.Request.UpdateNewsPreferencesRequest;
import com.example.SpringVue.Dto.Request.SavePlansRequest;
import com.example.SpringVue.Dto.Request.SaveUserRequest;
import com.example.SpringVue.Dto.Response.GetNewsPreferencesResponse;
import com.example.SpringVue.Dto.Response.GetPlansResponse;

import java.util.List;

public interface UserService {

    String addUser(SaveUserRequest saveUserRequest);

    List<Article> getUserNews(String userName);

    GetNewsPreferencesResponse getNewsPreferences(String userName);

    String updateNewsPreferences(UpdateNewsPreferencesRequest updateNewsPreferencesRequest, String userName);

    GetPlansResponse getPlans(String userName);

    String savePlans(SavePlansRequest savePlansRequest, String userName);

}
