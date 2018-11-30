package com.p3212.configuration;

import javax.sql.DataSource;

import com.p3212.EntityClasses.User;
import com.p3212.Repositories.UserRepository;

import javax.servlet.Filter;

import com.p3212.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.RequestContextFilter;

@Configuration
@EnableWebSecurity
@EnableOAuth2Client
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuthenticationSuccessHandler successHandler;

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    private final SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

    @Autowired
    private OAuth2RestTemplate vkRestTemplate;

    @Autowired
    private AuthService authService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.
                jdbcAuthentication()
                .usersByUsernameQuery("select login, password, true from users where login=?")
                .authoritiesByUsernameQuery("select login, role from user_role where login=?")
                .dataSource(dataSource)
                .passwordEncoder(bCryptPasswordEncoder);
    }

    @Bean
    public VkOauthFilter vkOauthFilter() {
        VkOauthFilter vkOauthFilter = new VkOauthFilter("/login/vk");
        vkOauthFilter.setRestTemplate(vkRestTemplate);
        vkOauthFilter.setAuthService(authService);
        return vkOauthFilter;
    }

    @Bean
    public VkRegistrationFilter vkRegistrationFilter() {
        VkRegistrationFilter vrf = new VkRegistrationFilter("/register/vk");
        vrf.setRestTemplate(vkRestTemplate);
        vrf.setAuthService(authService);
        return vrf;
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterAfter(new OAuth2ClientContextFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .addFilterAfter(vkOauthFilter(), OAuth2ClientContextFilter.class)
                .httpBasic()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login/vk"));

        http
                .addFilterAfter(new RequestContextFilter(), CsrfFilter.class)
                .httpBasic()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login/vk"));

        http.addFilterAfter(new OAuth2ClientContextFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .addFilterAfter(vkRegistrationFilter(), OAuth2ClientContextFilter.class)
                .httpBasic().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/register/vk"));

        http
                .addFilterAfter(new RequestContextFilter(), CsrfFilter.class)
                .httpBasic()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/register/vk"));

        http.csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .authorizeRequests()
                .antMatchers("/users").authenticated()
                .antMatchers("/registerVk").permitAll()
                .antMatchers("/authVk").permitAll()
                .antMatchers("/getVkCode").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/login/vk").permitAll()
                .antMatchers("/registration").permitAll()
                .antMatchers("/me").authenticated()
                .antMatchers("/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .and()
                .logout().deleteCookies("JSESSIONID");

    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
