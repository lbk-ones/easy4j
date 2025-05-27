package easy4j.module.security.core;

import easy4j.module.sauth.core.Easy4jAuth;
import easy4j.module.sauth.user.BaseUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class SecurityEasy4jAuth implements Easy4jAuth {

    @Autowired
    AuthenticationManager authenticationManager;


    @Override
    public void login(String username, String password) {
        // 1. 创建认证请求
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );
//
//        // 2. 认证成功，设置SecurityContext
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        // 3. 生成JWT令牌
//        String jwt = jwtUtils.generateJwtToken(authentication);
//
//        // 4. 获取用户信息
//        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//        List<String> roles = userDetails.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.toList());
    }

    @Override
    public BaseUser getCurrentUser() {
        return null;
    }
}
