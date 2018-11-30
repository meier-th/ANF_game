package com.p3212.Configurations;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(final HttpServletRequest hsr, final HttpServletResponse hsr1, final AuthenticationException ae) throws IOException {
        hsr1.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
    
}
