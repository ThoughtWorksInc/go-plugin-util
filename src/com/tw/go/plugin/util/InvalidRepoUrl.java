package com.tw.go.plugin.util;

import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;

public class InvalidRepoUrl extends RepoUrl {

    public static final String MESSAGE = "Invalid/Unsupported Repository url";

    public InvalidRepoUrl(String url, String usernameValue, String passwordValue) {
        super("InvalidRepoUrl");
    }

    @Override
    public void validate(ValidationResult errors) {
        doBasicValidations(errors);
        if(errors.isSuccessful())
        errors.addError(new ValidationError(REPO_URL, MESSAGE));
    }

    @Override
    public void checkConnection(String urlOverride) {
        throw new RuntimeException(MESSAGE);
    }
}
