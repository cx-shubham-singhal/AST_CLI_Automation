package PageObjects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanInfo {
    private String scanId;
    private String projectId;
    private String projectName;
    private String status;
    private String branch;
    private String type;
    private String engines;

    // Constructor
    public ScanInfo(String scanId, String projectId, String projectName, String status, String branch, String type, String engines) {
        this.scanId = scanId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.status = status;
        this.branch = branch;
        this.type = type;
        this.engines = engines;
    }

    // Getters
    public String getScanId() { return scanId; }
    public String getProjectId() { return projectId; }
    public String getProjectName() { return projectName; }
    public String getStatus() { return status; }
    public String getBranch() { return branch; }
    public String getType() { return type; }
    public String getEngines() { return engines; }

    @Override
    public String toString() {
        return "ScanInfo{" +
                "scanId='" + scanId + '\'' +
                ", projectId='" + projectId + '\'' +
                ", projectId='" + projectName + '\'' +
                ", status='" + status + '\'' +
                ", branch='" + branch + '\'' +
                ", type='" + type + '\'' +
                ", engines='" + engines + '\'' +
                '}';
    }

}