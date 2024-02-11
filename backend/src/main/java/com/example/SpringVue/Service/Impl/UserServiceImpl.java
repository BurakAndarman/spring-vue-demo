package com.example.SpringVue.Service.Impl;

import com.example.SpringVue.Dto.NewsApi.TopHeadlines.Article;
import com.example.SpringVue.Dto.NewsApi.TopHeadlines.TopHeadlines;
import com.example.SpringVue.Dto.NewsPreferencesDto;
import com.example.SpringVue.Dto.PlansDto;
import com.example.SpringVue.Dto.TagsDto;
import com.example.SpringVue.Entity.NewsPreferences;
import com.example.SpringVue.Entity.Plans;
import com.example.SpringVue.Entity.PlansTags;
import com.example.SpringVue.Entity.Tags;
import com.example.SpringVue.Exception.DuplicateUsername;
import com.example.SpringVue.Exception.NewsPreferenceNotFound;
import com.example.SpringVue.Exception.UserNotFound;
import com.example.SpringVue.Repo.*;
import com.example.SpringVue.Dto.UserDto;
import com.example.SpringVue.Service.NewsService;
import com.example.SpringVue.Service.UserService;
import com.example.SpringVue.Utils.CacheUtils;
import com.example.SpringVue.Utils.KanbanList;
import com.example.SpringVue.Utils.UserUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDetailsManager userDetailsManager;

    private final NewsService newsService;

    private final NewsPreferencesRepository newsPreferencesRepository;

    private final UserRepository userRepository;

    private final PlansRepository plansRepository;

    private final TagsRepository tagsRepository;

    private final PlansTagsRepository plansTagsRepository;

    private final CacheUtils cacheUtils;

    private final UserUtils userUtils;

    public UserServiceImpl(UserDetailsManager userDetailsManager, NewsService newsService,
                           NewsPreferencesRepository newsPreferencesRepository,
                           UserRepository userRepository, PlansRepository plansRepository,
                           TagsRepository tagsRepository, PlansTagsRepository plansTagsRepository,
                           CacheUtils cacheUtils, UserUtils userUtils){
        this.userDetailsManager = userDetailsManager;
        this.newsService = newsService;
        this.newsPreferencesRepository = newsPreferencesRepository;
        this.userRepository = userRepository;
        this.plansRepository = plansRepository;
        this.tagsRepository = tagsRepository;
        this.plansTagsRepository = plansTagsRepository;
        this.cacheUtils = cacheUtils;
        this.userUtils = userUtils;
    }

    @Override
    public String addUser(UserDto userDto) {

        Optional<com.example.SpringVue.Entity.User> userCheck = userRepository.findById(userDto.getUserName());

        if(userCheck.isPresent()) {
            throw new DuplicateUsername("There is already a user with the same username");
        }

        UserDetails user = User.withDefaultPasswordEncoder()
                                .username(userDto.getUserName())
                                .password(userDto.getPassword())
                                .authorities("REGULAR") // There is only one type of user for now
                                .build();

        userDetailsManager.createUser(user);

        Optional<com.example.SpringVue.Entity.User> userFromDatabase = userRepository.findById(user.getUsername());

        if(userFromDatabase.isPresent()) {
            newsPreferencesRepository.save(new NewsPreferences(user.getUsername(),userFromDatabase.get()));
        }

        return user.getUsername();
    }

    @Override
    public NewsPreferencesDto getNewsPreferences(String userName) {

        Optional<NewsPreferences> newsPreferences = newsPreferencesRepository.findById(userName);

        if(newsPreferences.isEmpty()) {
            throw new NewsPreferenceNotFound("Couldn't find any user preference",userName);
        }

        String language = newsPreferences.get().getLanguage();
        List<String> interestedTopics = new ArrayList<>();

        if(!newsPreferences.get().getInterestedTopics().isEmpty()) {
            interestedTopics.addAll(Arrays.stream(newsPreferences.get().getInterestedTopics().split(",")).toList());
        }

        return new NewsPreferencesDto(language, interestedTopics);
    }

    @Override
    public String updateNewsPreferences(NewsPreferencesDto newsPreferencesDto, String userName) {

        String language = newsPreferencesDto.getLanguage();
        String interestedTopics = "";

        if(!newsPreferencesDto.getInterestedTopics().isEmpty()) {
            interestedTopics += String.join(",", newsPreferencesDto.getInterestedTopics());
        }

        NewsPreferences newsPreferences = new NewsPreferences(
                                            userName,
                                            language,
                                            interestedTopics
                                          );

        newsPreferencesRepository.save(newsPreferences);

        cacheUtils.evictUserNews(userName);

        return "Changes recorded successfully";

    }

    @Override
    public List<PlansDto> getPlans(String userName) {

        Optional<com.example.SpringVue.Entity.User> user = userRepository.findById(userName);

        if(user.isEmpty()) {
            throw new UserNotFound("Invalid username");
        }

        List<Plans> plans = plansRepository.findAllByUser(user.get());

        List<PlansDto> plansDtos = plans.stream().map(userPlan -> {

            List<TagsDto> tagsDtoList = new ArrayList<>();

            if(userPlan.getPlansTags() != null) {
                tagsDtoList.addAll(userPlan.getPlansTags().stream().map(plansTags -> {

                    Tags tag = plansTags.getTags();

                    return new TagsDto(
                        tag.getId(),
                        tag.getName(),
                        tag.getColor()
                    );
                }).toList());
            }


            return new PlansDto(
                   userPlan.getId(),
                   userPlan.getTitle(),
                   userPlan.getContent(),
                   userPlan.getImage(),
                   userPlan.getKanbanList().toString(),
                   tagsDtoList
            );
        }).toList();

        return plansDtos;
    }

    @Transactional
    @Override
    public String savePlans(List<PlansDto> plansDtoList, List<MultipartFile> images, String userName) {

        Optional<com.example.SpringVue.Entity.User> user = userRepository.findById(userName);

        if(user.isEmpty()) {
            throw new UserNotFound("Invalid username");
        }

        plansDtoList.stream().forEach(plansDto -> {
            if(plansDto.isCreated() && !plansDto.isDeleted()) {

                String image = "";
                String imgPublicId = "";

                if(plansDto.getImageIndex() != null) {
                    HashMap<String,String> cloudResponse = userUtils.savePlanImageToCloud(images.get(plansDto.getImageIndex()));
                    image = cloudResponse.get("image_url");
                    imgPublicId = cloudResponse.get("public_id");
                }

                Plans newPlan = plansRepository.save(new Plans(
                        plansDto.getTitle(),
                        plansDto.getContent(),
                        image,
                        imgPublicId,
                        KanbanList.valueOf(plansDto.getKanbanList()),
                        user.get()
                ));

                plansDto.getTags().stream().forEach(tagsDto -> userUtils.createNewPlansTagsRelation(newPlan, tagsDto));

            } else if (plansDto.isChanged() && !plansDto.isDeleted()) {

                Plans oldPlan = plansRepository.findById(plansDto.getId()).get();

                String image = oldPlan.getImage();
                String imgPublicId = oldPlan.getImgPublicId();

                if(plansDto.getImageIndex() != null) {
                    if(!image.isEmpty()) {
                        userUtils.deletePlanImageFromCloud(imgPublicId);
                    }

                    HashMap<String,String> cloudResponse = userUtils.savePlanImageToCloud(images.get(plansDto.getImageIndex()));
                    image = cloudResponse.get("image_url");
                    imgPublicId = cloudResponse.get("public_id");

                } else if(plansDto.getImage().isEmpty() && !image.isEmpty()) {
                    userUtils.deletePlanImageFromCloud(imgPublicId);

                    image = "";
                    imgPublicId = "";
                }

                Set<PlansTags> existingPlansTagsRelations = new HashSet<>();

                if(!plansDto.getTags().isEmpty()) {

                    Collection<Tags> existingOldTags = new ArrayList<>();

                    plansDto.getTags().stream().forEach(tagsDto -> {
                        if(!tagsDto.isCreated()) {
                            existingOldTags.add(tagsRepository.findById(tagsDto.getId()).get());
                        }
                    });

                    existingPlansTagsRelations = plansTagsRepository.getPlansTagsByTagsInAndPlans(existingOldTags,oldPlan);

                }

                Plans updatedPlan = plansRepository.save(new Plans(
                        plansDto.getId(),
                        plansDto.getTitle(),
                        plansDto.getContent(),
                        image,
                        imgPublicId,
                        KanbanList.valueOf(plansDto.getKanbanList()),
                        user.get(),
                        existingPlansTagsRelations
                ));

                plansDto.getTags().stream().forEach(tagsDto -> {
                    if(tagsDto.isCreated()) {
                        userUtils.createNewPlansTagsRelation(updatedPlan, tagsDto);
                    }
                });


            } else if (plansDto.isDeleted() && !plansDto.isCreated()) {

                Plans planToBeDeleted = plansRepository.findById(plansDto.getId()).get();

                if(!planToBeDeleted.getImage().isEmpty()) {
                    userUtils.deletePlanImageFromCloud(planToBeDeleted.getImgPublicId());
                }

                plansRepository.delete(planToBeDeleted);

            }
        });

        return "Changes recorded successfully";
    }

    @Cacheable(value = "userNewsCache", key = "#userName")
    @Override
    public HashMap<String,List<Article>> getUserNews(String userName) {

        log.info("Trying to fetch data from 3rd party api");

        Optional<NewsPreferences> newsPreferences = newsPreferencesRepository.findById(userName);

        if(newsPreferences.isEmpty()) {
            throw new NewsPreferenceNotFound("Couldn't find any user preference",userName);
        }

        NewsPreferences validatedNewsPreferences = newsPreferences.get();

        String preferredLanguage = validatedNewsPreferences.getLanguage();
        Boolean topicsEmpty = validatedNewsPreferences.getInterestedTopics().isEmpty();

        HashMap<String,List<Article>> articlesMap = new HashMap<>();

        if(topicsEmpty) {
            TopHeadlines topHeadlines = newsService.getTopHeadlines(preferredLanguage);

            articlesMap.put("general",topHeadlines.getArticles().stream().limit(12).toList());

            return articlesMap;
        }

        List<String> preferredTopics = Arrays.stream(validatedNewsPreferences.getInterestedTopics().split(",")).toList(); // Splitting comma separated topics

        preferredTopics.stream().forEach(preferredTopic -> {
            TopHeadlines topHeadlines = newsService.getTopHeadlines(preferredTopic,preferredLanguage);

            articlesMap.put(preferredTopic,topHeadlines.getArticles().stream().limit(6).toList());
        });

        return articlesMap;
    }


}
