package com.tw.go.plugin.util;

import com.thoughtworks.go.plugin.api.response.validation.Errors;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class WindowsUNCUrl extends RepoUrl {
    public WindowsUNCUrl(String url, String invalidUser, String invalidPassword) {
        super(url, invalidUser, invalidPassword);
    }

    @Override
    public void validate(Errors errors) {
    }

    @Override
    public void checkConnection(String urlOverride) {
        if (credentialsDetected()) {
            throw new RuntimeException("Username/Password not supported for Windows UNC urls.");
        }
        try {
            URL url = new URL(this.url);
            if (!new File(url.getPath()).exists()) {
                throw new RuntimeException("Invalid path.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getSeparator() {
        return "\\";
    }
}
