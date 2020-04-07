package de.bruss.demontoo.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        if (request.getServletPath().startsWith("/api") || request.getServletPath().startsWith("/management") || request.getServletPath().startsWith("/service") || request.getServletPath().startsWith("/currentUser")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            response.sendRedirect("/login");
        }
    }

}