package com.anf.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.RequestContextFilter;

import com.anf.config.filter.GoogleOauthFilter;
import com.anf.config.filter.GoogleRegistrationFilter;
import com.anf.config.filter.VkOauthFilter;
import com.anf.config.filter.VkRegistrationFilter;
import com.anf.service.AuthService;
import com.google.common.collect.ImmutableList;

import lombok.AllArgsConstructor;


@Configuration
@EnableWebSecurity
@EnableOAuth2Client
@AllArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final DataSource dataSource;
    private final AuthenticationSuccessHandler successHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
    @Qualifier("vkRestTemplate")
    private final OAuth2RestTemplate vkRestTemplate;
    private final AuthService authService;
    @Qualifier("googleRestTemplate")
    private final OAuth2RestTemplate googleRestTemplate;

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
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(ImmutableList.of("*")); // * because I dunno my url after forwarding
        corsConfiguration.setAllowedMethods(ImmutableList.of("POST", "GET", "DELETE"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(ImmutableList.of("*"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public VkOauthFilter vkOauthFilter() {
        VkOauthFilter vkOauthFilter = new VkOauthFilter("/login/vk");
        System.out.println("Vk: " + vkRestTemplate.getResource().getClientId());
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

    @Bean
    public GoogleRegistrationFilter googleRegistrationFilter() {
        GoogleRegistrationFilter filter = new GoogleRegistrationFilter("/register/google");
        filter.setRestTemplate(googleRestTemplate);
        filter.setAuthService(authService);
        return filter;
    }

    @Bean
    public GoogleOauthFilter googleOauthFilter() {
        GoogleOauthFilter googleOauthFilter = new GoogleOauthFilter("/login/google");
        System.out.println("Google: " + googleRestTemplate.getResource().getClientId());
        googleOauthFilter.setRestTemplate(googleRestTemplate);
        googleOauthFilter.setAuthService(authService);
        return googleOauthFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // CORS
        http.cors();

        http
                .addFilterAfter(new OAuth2ClientContextFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .addFilterAfter(vkOauthFilter(), OAuth2ClientContextFilter.class)
                .addFilterAfter(new RequestContextFilter(), CsrfFilter.class)
                .httpBasic()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login/vk"));

        http
                .addFilterAfter(new OAuth2ClientContextFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .addFilterAfter(vkRegistrationFilter(), OAuth2ClientContextFilter.class)
                .httpBasic().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/register/vk"));

        http
                .addFilterAfter(new RequestContextFilter(), CsrfFilter.class)
                .httpBasic()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/register/vk"));

        http
                .addFilterAfter(new OAuth2ClientContextFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .addFilterAfter(googleOauthFilter(), OAuth2ClientContextFilter.class)
                .httpBasic()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login/google"));


        http
                .addFilterAfter(new RequestContextFilter(), CsrfFilter.class)
                .httpBasic()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login/google"));

        http
                .addFilterAfter(new OAuth2ClientContextFilter(), AbstractPreAuthenticatedProcessingFilter.class)
                .addFilterAfter(googleRegistrationFilter(), OAuth2ClientContextFilter.class)
                .httpBasic().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/register/google"));

        http
                .addFilterAfter(new RequestContextFilter(), CsrfFilter.class)
                .httpBasic()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/register/google"));

        http.csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .and()
                .authorizeRequests()
                .antMatchers("/confirm").hasAnyAuthority("NEWVK", "NEWGoogle")
                .antMatchers("/checkCookies").permitAll()
                .antMatchers("/registration").permitAll()
                .anyRequest().authenticated()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/").permitAll()
                .and()
                .formLogin()
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .and()
                .logout().deleteCookies("JSESSIONID").logoutSuccessUrl("/logout-success");

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
