package api.mappers;

import api.dtos.common.ProjectRef;

import java.util.ArrayList;
import java.util.List;

public final class DvprRequestMapper {

    private DvprRequestMapper() {
        // prevent instantiation
    }

    public static List<ProjectRef> buildProjectRefs(
            String projectIdsRaw,
            String projectNamesRaw) {

        String[] projectIds = projectIdsRaw.split("\\s*,\\s*");
        String[] projectNames = projectNamesRaw.split("\\s*,\\s*");

        if (projectIds.length != projectNames.length) {
            throw new IllegalArgumentException(
                    "Mismatch between projectIds and projectNames. " +
                            "Ids: " + projectIds.length +
                            ", Names: " + projectNames.length
            );
        }

        List<ProjectRef> projects = new ArrayList<>();

        for (int i = 0; i < projectIds.length; i++) {

            projects.add(new ProjectRef(
                    Integer.parseInt(projectIds[i]),
                    projectNames[i],
                    projectNames[i]
            ));
        }
        return projects;
    }
}

