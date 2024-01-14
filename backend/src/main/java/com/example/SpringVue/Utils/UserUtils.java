package com.example.SpringVue.Utils;

import com.example.SpringVue.Dto.PlansDto;
import com.example.SpringVue.Dto.TagsDto;
import com.example.SpringVue.Entity.Plans;
import com.example.SpringVue.Entity.PlansTags;
import com.example.SpringVue.Entity.Tags;
import com.example.SpringVue.Repo.PlansTagsRepository;
import com.example.SpringVue.Repo.TagsRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class UserUtils {

    private final TagsRepository tagsRepository;

    private final PlansTagsRepository plansTagsRepository;

    public UserUtils(TagsRepository tagsRepository, PlansTagsRepository plansTagsRepository) {
        this.tagsRepository = tagsRepository;
        this.plansTagsRepository = plansTagsRepository;
    }

    public void removePlanTagRelation(PlansDto plansDto, Plans plan) {
        Collection<Tags> existingOldTags = new ArrayList<>();

        for (TagsDto tagsDto : plansDto.getTags()) {

            if(!tagsDto.isCreated()) {
                existingOldTags.add(tagsRepository.findById(tagsDto.getId()).get());
            }

        }

        plansTagsRepository.deletePlansTagsByTagsNotInAndPlans(existingOldTags,plan);
    }

    // Checks if newly created tag exists in db. If exists, gets it and if not creates new one. Then adds a relation record.
    public void createPlanTagRelation(Plans plan, TagsDto tagsDto) {

        Optional<Tags> tag = tagsRepository.findFirstByNameAndColor(tagsDto.getName(), tagsDto.getColor());

        Tags tagFromDb;
        if (tag.isEmpty()) {
            tagFromDb = tagsRepository.save(new Tags(
                    tagsDto.getName(),
                    tagsDto.getColor()
            ));
        } else {
            tagFromDb = tag.get();
        }

        plansTagsRepository.save(new PlansTags(
                plan,
                tagFromDb
        ));

    }

}
