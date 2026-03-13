package com.sehoprojectmanagerapi.web.dto.workspace.privilege;

import java.util.List;

public record AddSpaceAndProjectMembersRequest(
        List<Long> projectIds,
        List<AddMemberRequest> reqList
) {
}
