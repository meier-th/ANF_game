package com.anf.config;

import java.util.List;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security configuration – username/password auth and Google OAuth2 login.
 * VK/Telegram integrations have been removed.
 */
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {

  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final DataSource dataSource;
  private final AuthenticationSuccessHandler successHandler;
  private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

  private static final SimpleUrlAuthenticationFailureHandler FAILURE_HANDLER =
      new SimpleUrlAuthenticationFailureHandler();

  @Bean
  public UserDetailsService userDetailsService() {
    var manager = new JdbcUserDetailsManager(dataSource);
    manager.setUsersByUsernameQuery("select login, password, true from users where login=?");
    manager.setAuthoritiesByUsernameQuery("select login, role from user_role where login=?");
    return manager;
  }

  @Bean
  public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
    var provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(bCryptPasswordEncoder);
    return new ProviderManager(provider);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    var config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("POST", "GET", "DELETE"));
    config.setAllowCredentials(true);
    config.setAllowedHeaders(List.of("*"));
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/checkCookies", "/registration", "/confirm", "/oauth2/**", "/login/oauth2/code/google")
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(form -> form.successHandler(successHandler).failureHandler(FAILURE_HANDLER))
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService()))
            .successHandler(successHandler)
            .failureHandler(FAILURE_HANDLER)
        )
        .logout(logout -> logout.deleteCookies("JSESSIONID").logoutSuccessUrl("/logout-success"));

    return http.build();
  }

  @Bean
  public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
    return new DefaultOAuth2UserService(); // Implement custom logic here to save user to DB, etc.
  }
}
