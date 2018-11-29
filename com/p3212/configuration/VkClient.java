package com.p3212.configuration;

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
public class VkClient {

    private final String clientId = "6751264";
    private final String clientSecret = "YqVhWS11S17pz670MHzG";
    private final String accessTokenUri = "https://oauth.vk.com/access_token";
    private final String userAuthorizationUri = "https://oauth.vk.com/authorize";
    private final String redirectUri = "http://localhost:31480/login/vk";


    @Bean
    public OAuth2ProtectedResourceDetails vkResourceDetails(){
        AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);
        details.setAccessTokenUri(accessTokenUri);
        details.setUserAuthorizationUri(userAuthorizationUri);
        details.setPreEstablishedRedirectUri(redirectUri);
        details.setUseCurrentUri(false);
        return details;
    }

    @Bean
    public OAuth2RestTemplate vkRestTemplate(OAuth2ProtectedResourceDetails googleResourceDetails, OAuth2ClientContext context){
        return new OAuth2RestTemplate(googleResourceDetails, context);
    }
}