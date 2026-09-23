package com.myorg.cxone.helpers;

public class TestConstants {
    public static final String SUCCESS_AUTH_VALIDATE = "Successfully authenticated to Checkmarx One server!";
    public static final String INVALID_API_KEY_ERROR = "token is malformed: token contains an invalid number of segments";
    public static final String DEFAULT_SUCCESS_VALIDATION_MESSAGE = "Validation should pass";
    public static final String MISSING_URI = "When using client-id and client-secret please provide base-uri or base-auth-uri";
    public static final String FAILED_AUTHENTICATION = "Failed to authenticate - please provide client-id, client-secret and base-uri or apikey";
    public static final String SETUP_GUIDE = "Setup guide: https://checkmarx.com/resource/documents/en/34965-68621-checkmarx-one-cli-quick-start-guide.html";
    public static final String INVALID_STATE_ERROR = "Failed to set property: unknown property or bad value";
    public static final String OUTPUT_PATTERN = "Engines";
    public static final String PROJECT_PATH_ZIP = "src/main/resources/Phoenix-RealtimeGoat.zip";
    public static final String PROJECT_PATH_ZIP_JAVAVUL = "src/main/resources/JavaVulnerableLabE-master.zip";
    public static final String PROJECT_PATH_FOLDER = "src/main/resources/Phoenix-RealtimeGoat";
    public static final String PROJECT_PATH_FOLDER_INVALID = "src/main/resources/JavaVulnerableLabE";
    public static final String GIT_REPO_URL = "https://github.com/vbarhate/JavaVulnerableLabE.git";
    public static final String RELATIVE_DATA_PATH = "src/main/resources/JavaVulnerableLabE-master/src/main/webapp";
    public static final String CX_API_KEY = System.getenv("CX_APIKEY");
    public static final String CX_CLIENT_ID = System.getenv("CX_CLIENT_ID");
    public static final String CX_CLIENT_SECRET = System.getenv("CX_CLIENT_SECRET");
    public static final String SCA_RESOLVER_PATH = System.getenv("SCA_RESOLVER_PATH");
    public static final String SCA_RESOLVER_INVALID_PATH ="src/main/resources/InvalidPath/ScaResolver.exe";
    public static final String ASCA_SQL_INJECTION_FILE_PATH = "src/main/resources/Phoenix-RealtimeGoat/Phoenix-RealtimeGoat/asca/sql_injection.py";
    public static final String OSS_REALTIME_NPM_MANIFEST_FILE_PATH = "src/main/resources/Phoenix-RealtimeGoat/Phoenix-RealtimeGoat/oss/npm/package.json";
    public static final String OSS_REALTIME_GO_MANIFEST_FILE_PATH = "src/main/resources/Phoenix-RealtimeGoat/Phoenix-RealtimeGoat/oss/go/go.mod";
    public static final String OSS_REALTIME_MAVEN_MANIFEST_FILE_PATH = "src/main/resources/Phoenix-RealtimeGoat/Phoenix-RealtimeGoat/oss/maven/pom.xml";
    public static final String OSS_REALTIME_NUGET_MANIFEST_FILE_PATH = "src/main/resources/Phoenix-RealtimeGoat/Phoenix-RealtimeGoat/oss/nuget/packages.config";
    public static final String OSS_REALTIME_PYTHON_MANIFEST_FILE_PATH = "src/main/resources/Phoenix-RealtimeGoat/Phoenix-RealtimeGoat/oss/python/requirements.txt";
    public static final String SECRETS_REALTIME_FILE_PATH = "src/main/resources/Phoenix-RealtimeGoat/Phoenix-RealtimeGoat/secrets/secrets.py";
    public static final String CONTAINERS_REALTIME_FILE_PATH = "src/main/resources/Phoenix-RealtimeGoat/Phoenix-RealtimeGoat/containers/helm/values.yaml";
}
