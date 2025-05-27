package easy4j.module.security.core;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        /*
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // 从数据库加载用户权限
            Set<Role> roles = user.getRoles();
            List<GrantedAuthority> authorities = new ArrayList<>();

            // 添加角色权限 (ROLE_前缀)
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));

            // 添加细粒度权限
            roles.forEach(role ->
                role.getPermissions().forEach(perm ->
                    authorities.add(new SimpleGrantedAuthority(perm.getName()))
                )
            );

            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, true, true, true,
                authorities
            );
        */

        return null;
    }
}