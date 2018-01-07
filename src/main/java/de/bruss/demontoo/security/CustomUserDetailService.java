package de.bruss.demontoo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(CustomUserDetailService.class);

    @Override
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
        return new User("eins-gmbh", "$2a$10$pL9iqEmafi2f6/b3a1OrAeQevF5NNZUhR7zz/9CE2rg4YDqqxHEae", true, true, true, true, getAuthorities());

    }

    private Collection<? extends GrantedAuthority> getAuthorities() {
        return getGrantedAuthorities();
    }

    private static List<GrantedAuthority> getGrantedAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ADMIN"));
        return authorities;
    }

}
