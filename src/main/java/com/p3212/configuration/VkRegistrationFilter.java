package com.p3212.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p3212.Repositories.UserRepository;
import com.p3212.Services.AuthService;
import com.p3212.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VkRegistrationFilter extends AbstractAuthenticationProcessingFilter {

    @Autowired
    UserService service;

    private OAuth2RestTemplate restTemplate;

    private AuthService authService;

    VkRegistrationFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        setAuthenticationManager(authenticationManagerNone());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
        try {
            try {

                OAuth2AccessToken accessToken = restTemplate.getAccessToken();

                int vkId = Integer.parseInt(accessToken.getAdditionalInformation().get("user_id").toString());

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("NEWVK"));

                org.springframework.security.core.userdetails.User user = new User("tmp" + vkId, "", authorities);

                System.out.println(user.getUsername());
                return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            } catch (InvalidTokenException e) {
                throw new BadCredentialsException("Could not obtain user details from token", e);
            }
        } catch (OAuth2Exception e) {
            throw new BadCredentialsException("Error token", e);
        }

    }

    public AuthenticationManager authenticationManagerNone() {
        return authentication -> {
            throw new UnsupportedOperationException("No authentication should be done with this AuthenticationManager");
        };
    }

    public OAuth2RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(OAuth2RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}