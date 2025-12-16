package com.ex.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.AntPathMatcher;

import com.ex.rider.model.data.RiderLoginSuccessHandler;

// 설정을 담당하는 어노테이션
@Configuration
// Security 설정 활성화
@EnableWebSecurity

@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain riderFilterChain(HttpSecurity http, RiderLoginSuccessHandler successHandler) throws Exception {
        // securityMatcher("/rider/**") : /rider/ 경로와 그 하위 경로에만 적용되도록 범위를 지정
        // authorizeHttpRequests() : 요청 URL에 대한 접근 권한 규칙을 정의
        // anyRequest().permitAll(s) : 현재 체인의 매칭 범위 안에 포함된 모든 요청을 인증/인가 절차 없이 허용
        http
                // 이 체인은 /rider/** 경로에만 적용
                .securityMatcher("/rider/**")

                .authorizeHttpRequests(auth -> {
                    // 해당 범위 내 요청 전부 허용
                    auth.anyRequest().permitAll();
                })

                .formLogin(login -> login
                        // 로그인 페이지 URL (GET 전용)
                        // - 사용자가 로그인 화면을 볼 때 요청하는 경로입니다.
                        // - 컨트롤러/템플릿에 "/rider/login" GET 매핑이 있어야 하며, 이 URL은 반드시 permitAll 이어야 합니다.
                        .loginPage("/rider")

                        // 로그인 처리 URL (POST 전용)
                        // - 실제 인증이 일어나는 엔드포인트입니다. UsernamePasswordAuthenticationFilter 가 가로채어 처리하므로
                        // 보통 별도의 @PostMapping 컨트롤러를 만들지 않습니다(만들면 충돌 위험).
                        // - 폼의 action 과 정확히 일치해야 하며, 이 URL 역시 permitAll 이어야 합니다.
                        .loginProcessingUrl("/rider/login")

                        // 아이디/비밀번호 파라미터명 매핑
                        // - 로그인 폼의 <input name="..."> 와 동일해야 합니다.
                        // - 기본값은 username/password 이지만, 여기선 riderId/password 를 사용합니다.
                        .usernameParameter("riderId")
                        .passwordParameter("password")

                        // <회원의 엔티티중 isTemporary 의 값에 따라 이동 페이지 변경 >

                        // 로그인 성공 후 이동 경로 + alwaysUse 플래그
                        // - defaultSuccessUrl("/rider/", false)
                        // -> false(기본): 로그인 전에 접근하려던 보호 자원(SavedRequest)이 있으면 그곳으로,
                        // 없으면 "/rider/" 로 이동
                        // - defaultSuccessUrl("/rider/", true)
                        // -> true: SavedRequest 가 있어도 무시하고 항상 "/rider/" 로 이동(대시보드 고정 이동 시 유용)
                        // .defaultSuccessUrl("/rider", false)
                        .successHandler(successHandler)

                        // 로그인 실패 시 이동 경로
                        // - 보통 쿼리스트링에 ?error 를 함께 붙여서 화면에서 에러 메시지를 분기 처리합니다.
                        .failureUrl("/rider?error")

                // [선택] 로그인 페이지/처리 URL 모두 익명 접근 허용(편의 메서드)
                // - 이미 authorizeHttpRequests 에서 permitAll 로 열어두었다면 생략 가능.
                // .permitAll()
                )
                .logout(logout -> logout
                        // GET /rider/logout 허용
                        .logoutRequestMatcher(new AntPathRequestMatcher("/rider/logout", "GET"))

                        // 로그아웃 성공 후 이동 경로
                        .logoutSuccessUrl("/rider")

                        // 세션 무효화
                        .invalidateHttpSession(true)

                        // JSESSIONID 쿠키 삭제
                        .deleteCookies("JSESSIONID"))
                // ✅ CSRF: 세션 기반 명시
                .csrf(csrf -> csrf
                        .csrfTokenRepository(new HttpSessionCsrfTokenRepository()));

        ;

        // 개발 중 필요 시 CSRF 비활성화
        // .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 이 체인은 나머지 전체에 적용(전역)
                .securityMatcher("/**")
                .authorizeHttpRequests(a -> a
                        // 전역도 전부 허용 (원하면 authenticated로 변경)
                        .anyRequest().permitAll())
                // 이 설정이 있다면 CSRF토큰 불필요
                // .csrf(csrf -> csrf.disable());
                // 아래의 경로에만 csrf 동작 안하도록(csrf토큰 불필요)
                // .csrf(csrf -> csrf.ignoringRequestMatchers("/product/insert",
                // "/product/uploadFile","/product/summernoteImg"));
                .csrf(csrf -> csrf.ignoringRequestMatchers("/ws/**"));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- 판매자용 SecurityFilterChain ---
    /*
     * @Bean
     * 
     * @Order(3)
     * SecurityFilterChain shopFilterChain(HttpSecurity http) throws Exception {
     * http
     * .securityMatcher("/shop/**") // /shop으로 시작하는 모든 요청
     * .authorizeHttpRequests(auth -> auth
     * .anyRequest().hasAnyRole("SELLER") // SELLER 접근 가능
     * )
     * .formLogin(form -> form
     * .loginPage("/member/login")
     * .loginProcessingUrl("/login")
     * .defaultSuccessUrl("/shop/shopmain", true)
     * .permitAll()
     * )
     * .logout(logout -> logout
     * .logoutUrl("/logout")
     * .logoutSuccessUrl("/")
     * .invalidateHttpSession(true)
     * .deleteCookies("JSESSIONID")
     * )
     * .csrf(csrf -> csrf.disable());
     * 
     * return http.build();
     * }
     */

    // ---관리자용 SecurityFilterChain---
    // @Bean
    // @Order(2)
    // SecurityFilterChain adminFilterChain(HttpSecurity http,
    // @Qualifier("adminDetailsService") UserDetailsService adminDetailsService,
    // PasswordEncoder passwordEncoder) throws Exception {
    // DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    // provider.setUserDetailsService(adminDetailsService);
    // provider.setPasswordEncoder(passwordEncoder);
    // http
    // .authenticationProvider(provider)
    // .securityMatcher("/admin/**")
    // .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
    // .formLogin(login -> login.loginPage("/admin/login").permitAll()
    // .loginProcessingUrl("/admin/loginProc").permitAll()
    // .usernameParameter("username")
    // .passwordParameter("password")
    // .defaultSuccessUrl("/admin/main", true)
    // .failureUrl("/admin/login?error"))
    // .logout(logout -> logout.logoutUrl("/admin/logout")
    // .logoutSuccessUrl("/"));
    // // .csrf(csrf -> csrf.disable());
    // return http.build();
    // }
}
