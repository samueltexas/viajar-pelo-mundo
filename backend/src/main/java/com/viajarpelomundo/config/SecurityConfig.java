package com.viajarpelomundo.config;

import com.viajarpelomundo.auth.repository.AppUserRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        // BCrypt protege as passwords através de hashing com salt.
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(AppUserRepository users) {
        return email -> {
            var user = users.findByEmail(email)
                .orElseThrow(() ->
                    new UsernameNotFoundException("Invalid credentials")
                );

            return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole())
                .build();
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

        return http
            // API sem sessões de servidor nesta fase.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Configuração para testes locais com HTTP Basic.
            // Antes de publicar, rever CSRF, HTTPS e autenticação.
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/airports",
                    "/api/v1/airports/**",
                    "/api/v1/flights",
                    "/api/v1/flights/**"
                ).permitAll()
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/auth/register"
                ).permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .build();
    }
}
