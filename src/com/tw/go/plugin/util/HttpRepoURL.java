package com.tw.go.plugin.util;

import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.params.CoreConnectionPNames;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpRepoURL extends RepoUrl {

    public HttpRepoURL(String url, String user, String password) {
        super(url, user, password);
    }

    public static DefaultHttpClient getHttpClient() {
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,5*1000);
        client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(3, false));
        client.setRedirectStrategy(new DefaultRedirectStrategy());
        return client;
    }

    public void validate(ValidationResult validationResult) {
        try {
            doBasicValidations(validationResult);
            URL validatedUrl = new URL(this.url);
            if (!(validatedUrl.getProtocol().startsWith("http"))) {
                validationResult.addError(new ValidationError(REPO_URL, "Invalid URL: Only http is supported."));
            }

            if (StringUtil.isNotBlank(validatedUrl.getUserInfo())) {
                validationResult.addError(new ValidationError(REPO_URL, "User info should not be provided as part of the URL. Please provide credentials using USERNAME and PASSWORD configuration keys."));
            }
            credentials.validate(validationResult);
        } catch (MalformedURLException e) {
            validationResult.addError(new ValidationError(REPO_URL, "Invalid URL : " + url));
        }
    }


    public void checkConnection(String urlOverride) {
        DefaultHttpClient client = getHttpClient();
        if (credentials.provided()) {
            UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(credentials.getUser(), credentials.getPassword());
            //setAuthenticationPreemptive
            client.getCredentialsProvider().setCredentials(AuthScope.ANY, usernamePasswordCredentials);
        }
        HttpGet method = new HttpGet((urlOverride == null) ? url : urlOverride);
        try {
            HttpResponse response = client.execute(method);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException(response.getStatusLine().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
            client.getConnectionManager().shutdown();
        }
    }

    public String getUrlWithBasicAuth() {
        return getUrlWithCreds(url, credentials);
    }

    public static String getUrlWithCreds(String urlStr, Credentials credentials) {
        try {
            URL urlObj = new URL(urlStr);
            StringBuilder sb = new StringBuilder();
            sb.append(urlObj.getProtocol());
            sb.append("://");
            if (credentials.provided()) {
                sb.append(credentials.getUserInfo()).append("@");
            }
            sb.append(urlObj.getHost());
            if(urlObj.getPort() != -1){
                sb.append(":").append(urlObj.getPort());
            }
            sb.append(urlObj.getPath());
            if(urlObj.getQuery() != null)
                sb.append("?").append(urlObj.getQuery());
            if(urlObj.getRef() != null)
                sb.append("#").append(urlObj.getRef());
            if(urlObj.getQuery() == null && urlObj.getRef() == null && !urlObj.getPath().endsWith("/"))
                sb.append("/");
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getUrlStrWithTrailingSlash() {
        String urlStr = getUrlStr();
        if (urlStr.endsWith("/")) return urlStr;
        return urlStr + "/";
    }
}
