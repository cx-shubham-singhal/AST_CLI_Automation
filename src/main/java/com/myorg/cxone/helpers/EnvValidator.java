package com.myorg.cxone.helpers;

public class EnvValidator {

    private static final String[] REQUIRED_VARS = {
            "CX_BASE_URI",
            "CX_BASE_AUTH_URI",
            "CX_TENANT",
            "CX_APIKEY"
    };

    public static void validate() {
        for (String var : REQUIRED_VARS) {
            if (System.getenv(var) == null || System.getenv(var).isEmpty()) {
                throw new RuntimeException("Environment variable '" + var + "' is not set!");
            }
        }
    }
}
