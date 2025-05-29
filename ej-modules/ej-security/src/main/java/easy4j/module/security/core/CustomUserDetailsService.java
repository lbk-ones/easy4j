/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.security.core;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService
 *
 * @author bokun.li
 * @date 2025-05
 */
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
