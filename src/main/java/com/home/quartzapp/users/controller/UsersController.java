package com.home.quartzapp.users.controller;

import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.home.quartzapp.common.config.jwt.JwtProvider;
import com.home.quartzapp.users.dto.AccountDto;
import com.home.quartzapp.users.dto.TokenDto;
import com.home.quartzapp.users.dto.UserDetailsDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UsersController {
    private final JwtProvider jwtProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @RequestMapping(value = "/v1/users/login", method = RequestMethod.POST)
    public ResponseEntity<TokenDto> userLogin(@Valid @RequestBody AccountDto accountDto) {
        UsernamePasswordAuthenticationToken authenticationToken;
        authenticationToken = new UsernamePasswordAuthenticationToken(accountDto.getUsername(), accountDto.getPassword());

        Authentication authentication;

        //get UserDetailsDto and check password
        authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsDto userDetailsDto = (UserDetailsDto)(authentication.getPrincipal());
        String jwt = jwtProvider.createToken(userDetailsDto.getAccountDto());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + jwt);

        return new ResponseEntity<>(new TokenDto(jwt), httpHeaders, HttpStatus.OK);
    }
}
