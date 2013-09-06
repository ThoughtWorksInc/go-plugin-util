package com.tw.go.plugin.util;

import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class FileRepoUrl extends RepoUrl {
    public FileRepoUrl(String url, String invalidUser, String invalidPassword) {
        super(url, invalidUser, invalidPassword);
    }

    public FileRepoUrl(String url) {
        super(url);
    }

    @Override
    public void validate(ValidationResult validationResult) {
        try {
            doBasicValidations(validationResult);
            URL validatedUrl = new URL(this.url);
            if (StringUtil.isNotBlank(validatedUrl.getUserInfo())) {
                validationResult.addError(new ValidationError(REPO_URL, "User info invalid for file URL"));
            }
        } catch (MalformedURLException e) {
            validationResult.addError(new ValidationError(REPO_URL, "Invalid URL : " + url));
        }
    }

    public void checkConnection(String urlOverride) {
        if (credentialsDetected()) {
            throw new RuntimeException("File protocol does not support username and/or password.");
        }
        try {
            URL url = new URL(this.url);
            if (!new File(url.getPath()).exists()) {
                throw new RuntimeException("Invalid file path.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
