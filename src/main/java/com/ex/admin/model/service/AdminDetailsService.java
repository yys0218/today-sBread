// package com.ex.admin.model.service;

// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;

// import com.ex.admin.model.repository.AdminRepository;
// import com.ex.member.model.data.MemberDTO;

// import lombok.RequiredArgsConstructor;

// @Service("adminDetailsService")
// @RequiredArgsConstructor
// public class AdminDetailsService implements UserDetailsService {

//     private final AdminRepository adminRepository;

//     @Override
//     public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
//         MemberDTO admin = adminRepository.adminLogin(memberId)
//             .orElseThrow(() -> new UsernameNotFoundException("관리자 계정을 찾을 수 없습니다."));

//         return User
//                 .withUsername(admin.getMemberId())
//                 .password(admin.getMemberPw())
//                 .roles("ADMIN") // ROLE_ADMIN 권한 부여
//                 .build();
//     }
// }

