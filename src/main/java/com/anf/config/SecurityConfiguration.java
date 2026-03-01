package com.anf.config;

import com.anf.service.AuthService;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/** Security configuration – username/password auth only for now. Google OAuth2 login will be
 * added in the auth-google-only refactoring step. */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final DataSource dataSource;
  private final AuthenticationSuccessHandler successHandler;
  private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
  private final AuthService authService;

  private final SimpleUrlAuthenticationFailureHandler failureHandler =
      new SimpleUrlAuthenticationFailureHandler();

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
        .jdbcAuthentication()
        .usersByUsernameQuery("select login, password, true from users where login=?")
        .authoritiesByUsernameQuery("select login, role from user_role where login=?")
        .dataSource(dataSource)
        .passwordEncoder(bCryptPasswordEncoder)
        .and()
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("POST", "GET", "DELETE"));
    config.setAllowCredentials(true);
    config.setAllowedHeaders(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/checkCookies", "/registration", "/confirm").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/").permitAll()
            .anyRequest().authenticated())
        .formLogin(form -> form
            .successHandler(successHandler)
            .failureHandler(failureHandler))
        .logout(logout -> logout
            .deleteCookies("JSESSIONID")
            .logoutSuccessUrl("/logout-success"));

    return http.build();
  }
}
