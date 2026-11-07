package com.viajarpelomundo.config;

import com.viajarpelomundo.auth.repository.AppUserRepository;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
            .cors(cors -> {})
            // API sem sessões de servidor nesta fase.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Apenas para o ambiente local de demonstração.
            // Antes da publicação, implementar autenticação apropriada.
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

            // Mantemos HTTP Basic para a demonstração local,
            // mas sem activar a janela nativa de login do navegador.
            .httpBasic(basic -> basic
                .authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"status\":401,\"message\":\"Invalid or missing credentials\"}"
                    );
                })
            )

            // Resposta consistente quando o acesso é negado.
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        "{\"status\":403,\"message\":\"Access denied\"}"
                    );
                })
            )

            .build();
    }
}
