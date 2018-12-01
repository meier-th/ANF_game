package com.p3212.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;

import java.util.Arrays;

@EnableOAuth2Client
@Configuration
public class GoogleClient {
     private final String clientId = "1089042072438-o7navd9nnvtc15eduuvcseji15d2kjvo.apps.googleusercontent.com";
    private final String clientSecret = "Wa2NxGcqUxNGwDRp7WC18Cnz";
    private final String accessTokenUri = "https://www.googleapis.com/oauth2/v3/token";
    private final String userAuthorizationUri = "https://accounts.google.com/o/oauth2/auth";
    private final String redirectUri = "http://localhost:31480/login/google";
     @Bean
    public OAuth2ProtectedResourceDetails googleResourceDetails(){
        AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);
        details.setAccessTokenUri(accessTokenUri);
        details.setUserAuthorizationUri(userAuthorizationUri);
        details.setPreEstablishedRedirectUri(redirectUri);
        details.setScope(Arrays.asList("email"));
        details.setUseCurrentUri(false);
        return details;
    }
     @Bean
    public OAuth2RestTemplate googleRestTemplate(OAuth2ProtectedResourceDetails googleResourceDetails, OAuth2ClientContext context){
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(googleResourceDetails, context);
        return restTemplate;
    }
} 
