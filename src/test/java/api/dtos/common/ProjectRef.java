package api.dtos.common;

public class ProjectRef {
    public Integer projectId;
    public String projectName;
    public String archivedProjectName;

    public ProjectRef() {

    }

    public ProjectRef(Integer id, String name, String archived) {
        this.projectId = id;
        this.projectName = name;
        this.archivedProjectName = archived;

    }

}