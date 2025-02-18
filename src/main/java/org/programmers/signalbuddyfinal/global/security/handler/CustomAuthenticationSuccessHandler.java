package org.programmers.signalbuddyfinal.global.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.security.oauth.CustomOAuth2User;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();
        String redirectUrl = "/";

        if (principal instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) principal;

            if (customUserDetails.getRole().name().contains("ADMIN")) {
                redirectUrl = "/admins";
            }
            request.getSession().setAttribute("user", customUserDetails);
        } else if (principal instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) principal;
            request.getSession().setAttribute("user", customOAuth2User);
        }

        request.getSession().setMaxInactiveInterval(3600);
        response.sendRedirect(redirectUrl);

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
