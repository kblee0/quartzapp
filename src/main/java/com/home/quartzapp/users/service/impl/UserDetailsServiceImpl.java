package com.home.quartzapp.users.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.home.quartzapp.users.dto.AccountDto;
import com.home.quartzapp.users.dto.UserDetailsDto;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountDto accountDto = null;

        if(!StringUtils.hasText(username)) {
            throw new AuthenticationServiceException(username + " not found.");
        }

        // accountDto = selectUserDetails(username);
        // if(accountDto == null) {
        //     throw new UsernameNotFoundException(username)
        // }

        // sample account dto
        accountDto = new AccountDto();

        accountDto.setAccountId("101");
        accountDto.setUsername(username);
        accountDto.setPassword("$2a$04$zOmcafc/4EusYl1m3gqkHethhfAcaBLgUupDV25vxAIMQbLZjnw.y"); // password
        accountDto.setDisplayName("Display:" + username);
        accountDto.setStatus("A");

        List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();

        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        accountDto.setRoles(authorities);

        return new UserDetailsDto(accountDto);
    }
    
}
