package org.ayfaar.game.controllers;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class Router {
    private final DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @RequestMapping("/")
    public void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        redirectStrategy.setContextRelative(true);
        redirectStrategy.sendRedirect(request, response, "index.html");
    }
}
