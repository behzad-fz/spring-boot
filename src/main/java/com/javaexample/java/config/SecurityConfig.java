package com.javaexample.java.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.DataSourceFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.reactive.function.client.WebClient;

import javax.sql.DataSource;

import java.sql.Driver;
import java.util.Objects;

import static org.springframework.security.config.Customizer.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private final Environment env;
    private final RsaKeyProperties rsaKeys;

    public SecurityConfig(Environment env, RsaKeyProperties rsaKeys) {
        this.env = env;
        this.rsaKeys = rsaKeys;
    }

    /** In memory user for testing */
//    @Bean
//    public InMemoryUserDetailsManager user() {
//        return new InMemoryUserDetailsManager(
//            User
//                .withUsername("behzad-fz")
//                .password("{noop}password")
//                .authorities("read")
//                .build()
//        );
//    }

    /** Authentication against users stored in DB */
    @Bean
    DataSource dataSource(DataSourceFactory dataSourceFactory) {
        return new EmbeddedDatabaseBuilder()
                .setDataSourceFactory(dataSourceFactory)
                .setType(EmbeddedDatabaseType.H2)
                .addScript(JdbcDaoImpl.DEFAULT_USER_SCHEMA_DDL_LOCATION)
                .build();
    }

    @Bean
    public DataSourceFactory embeddedDataSourceFactory() {
        return new DataSourceFactory() {
            @Override
            public ConnectionProperties getConnectionProperties() {
                return new ConnectionProperties() {

                    @Override
                    public void setUsername(String username) {}

                    @Override
                    public void setPassword(String password) {}

                    @Override
                    public void setUrl(String url) {}

                    @Override
                    public void setDriverClass(Class<? extends Driver> driverClass) {}

                };
            }

            @Override
            public DataSource getDataSource() {
                DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();

                dataSourceBuilder.driverClassName("org.h2.Driver");
                dataSourceBuilder.url(env.getProperty("h2-console.url"));
                dataSourceBuilder.username(env.getProperty("h2-console.username"));
                dataSourceBuilder.password(env.getProperty("h2-console.password"));

                return dataSourceBuilder.build();
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // http.authorizeRequests() is deprecated, replaced by http.authorizeHttpRequests()
        // auth.antMatchers("/h2-console/**").permitAll() deprecated, replaced by auth.requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()

        return http
                .csrf(csrf -> csrf.disable())
                /** in case csrf is enabled and you need to exclude some endpoints from csrf protection */
                    //.csrf(csrf -> csrf.ignoringRequestMatchers(toH2Console()))
                    //.csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
                /** in case csrf is enabled and you need to exclude some endpoints from csrf protection */
                .authorizeHttpRequests(
                    auth -> auth
                        /** in case csrf is enabled and you need to exclude some endpoints from authentication */
                        .requestMatchers(
                                new AntPathRequestMatcher("/h2-console/**"), //could use this => toH2Console() instead of this => new AntPathRequestMatcher("/h2-console/**")
                                new AntPathRequestMatcher("/sign-up")
                        ).permitAll()
                        /** in case csrf is enabled and you need to exclude some endpoints from authentication */
                        .anyRequest()
                        .authenticated()
                )
                /** In case you go StateLESS */
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(withDefaults())

                /** In case you go StateFUL */
                // .formLogin(withDefaults())
                 .headers(headers ->headers.frameOptions().sameOrigin())
                .build();
    }

    @Bean
    public JdbcUserDetailsManager user(DataSource dataSource, PasswordEncoder encoder) {
        UserDetails admin = User.builder()
                .username(Objects.requireNonNull(env.getProperty("admin.username")))
                .password(encoder.encode(Objects.requireNonNull(env.getProperty("admin.password"))))
                .roles("ADMIN")
                .build();

        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);
        jdbcUserDetailsManager.createUser(admin);

        return jdbcUserDetailsManager;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(rsaKeys.publicKey()).privateKey(rsaKeys.privateKey()).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaKeys.publicKey()).build();
    }
}
