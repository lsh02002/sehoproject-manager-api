// com.sehoprojectmanagerapi.config.mapper.TagMapper (수동 매핑)
package com.sehoprojectmanagerapi.config.mapper;

import com.sehoprojectmanagerapi.repository.tag.Tag;
import com.sehoprojectmanagerapi.web.dto.tag.TagResponse;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    public TagResponse toResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .projectId(tag.getProject() != null ? tag.getProject().getId() : null)
                .name(tag.getName())
                .description(tag.getDescription())
                .build();
    }
}
