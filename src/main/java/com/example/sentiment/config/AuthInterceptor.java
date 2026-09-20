package com.example.sentiment.config;

import com.example.sentiment.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 登录鉴权拦截器
 * 校验请求头 Authorization: Bearer <token>，并把 userId/username 放入 request 属性供 Controller 使用
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行浏览器跨域预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtUtil.parseToken(token);
                request.setAttribute("userId", Long.parseLong(claims.getSubject()));
                request.setAttribute("username", claims.get("username"));
                return true;
            } catch (Exception e) {
                // token 无效或过期，走下方统一未登录返回
            }
        }
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
        return false;
    }
}
