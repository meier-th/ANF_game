package com.p3212.configuration;

import javax.sql.DataSource;

import com.p3212.EntityClasses.User;
import com.p3212.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableOAuth2Sso
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private DataSource dataSource;

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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http
//
//                .authorizeRequests()
//                .antMatchers("/login","/","/callback/").permitAll()
//                .anyRequest().authenticated()
//                .and().csrf().disable();
//        http.csrf().disable()
//                .antMatcher("/characters/**").
//                authorizeRequests()
//                .antMatchers("/registerVk").permitAll()
//                .antMatchers("/authVk").permitAll()
//                .antMatchers("/getVkCode").permitAll()
//                .antMatchers("/").permitAll()
//                .antMatchers("/login").permitAll()
//                .antMatchers("/registration").permitAll()
//                .antMatchers("/admin/**").hasAuthority("ADMIN").anyRequest()
//                .authenticated().and().csrf().disable().formLogin()
//                .loginPage("/login").failureUrl("/login?error=true")
//                .defaultSuccessUrl("/home")
//                .usernameParameter("login")
//                .passwordParameter("password")
//                .and().logout()
//                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
//                .logoutSuccessUrl("/").and().exceptionHandling()
//                .accessDeniedPage("/access-denied");
        http.csrf().disable().
                authorizeRequests()
                .antMatchers("*").permitAll()
                .antMatchers("/registerVk").permitAll()
                .antMatchers("/authVk").permitAll()
                .antMatchers("/getVkCode").permitAll()
                .and().logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/").and().exceptionHandling()
                .accessDeniedPage("/access-denied");
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
    }

    @Bean
    public PrincipalExtractor principalExtractor(UserRepository repo) {
        System.out.println("lol");
        return map -> {
            int id = (int) map.get("user_id");
            if (id == 137651826) return repo.findById("Pr0p1k");
            return null;
        };
    }
}
