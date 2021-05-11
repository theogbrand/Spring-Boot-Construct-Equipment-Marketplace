package com.leafcutters.antbuildz.security;

import com.leafcutters.antbuildz.models.Role;
import com.leafcutters.antbuildz.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final UserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() //send posts requests without being rejected
                .authorizeRequests()  // 
                .antMatchers("/").permitAll()
                .antMatchers("/login").permitAll()
                .antMatchers("/register").permitAll()
                .antMatchers("/confirm").permitAll()
                .antMatchers("/register/newUser").permitAll()
                .antMatchers("/home/admin/**").hasAuthority("ROLE_ADMIN") //
                .antMatchers("/home/customer/**").hasAuthority(String.valueOf(Role.CUSTOMER))
                .antMatchers("/home/partner/**").hasAuthority(String.valueOf(Role.PARTNER))
                .antMatchers("/", "/home/**", "/js/**", "/css/**").permitAll()
                
                .anyRequest()
                .authenticated().and()
                .formLogin()
                    .loginPage("/login")
                    .permitAll()
                    .defaultSuccessUrl("/home", true)
                .and()
                .logout()
                .logoutUrl("/logout")
                .deleteCookies("JSESSIONID");

    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
//                .withUser("testcustomer").password(passwordEncoder.bCryptPasswordEncoder().encode("customer")).roles(String.valueOf(Role.CUSTOMER))
//                .and()
//                .withUser("testpartner").password(passwordEncoder.bCryptPasswordEncoder().encode("partner")).roles(String.valueOf(Role.PARTNER))
//                .and()
                .withUser("admin").password(passwordEncoder.bCryptPasswordEncoder().encode("admin")).roles("ADMIN");
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder.bCryptPasswordEncoder());
        provider.setUserDetailsService(userService);
        return provider;
    }

}