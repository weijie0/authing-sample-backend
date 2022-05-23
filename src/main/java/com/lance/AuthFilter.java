package com.lance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lance.BaseResponse;
import com.lance.UserInfo;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest)request;

        String uri = servletRequest.getRequestURI();
        String authorization = servletRequest.getHeader("authorization");
        HttpServletResponse servletResponse = (HttpServletResponse)response;
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            servletResponse.setStatus(401);
            servletResponse.getWriter().print("Unauthorized. Please login first");
            return;
        }

        String appId = servletRequest.getHeader("x-authing-app-id");
        String userPoolId = servletRequest.getHeader("x-authing-userpool-id");
        if (appId == null || userPoolId == null) {
            servletResponse.setStatus(403);
            servletResponse.getWriter().print("client app invalid. please send x-authing-app-id and x-authing-userpool-id");
            return;
        }

        authorization = authorization.replace("Bearer ", "");
        String url = "https://core.authing.cn/api/v2/users/me";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Authorization", authorization)
                .header("x-authing-app-id", appId)
                .header("x-authing-userpool-id", userPoolId)
                .build();
        try {
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            BaseResponse<UserInfo> resp = JSON.parseObject(res.body(), new TypeReference<BaseResponse<UserInfo>>(){});
            if (resp == null || resp.getCode() != 200) {
                servletResponse.setStatus(401);
                servletResponse.getWriter().print("bearer authorization invalid");
            } else {
                request.setAttribute("UserInfo", resp.getData());
                chain.doFilter(request, response);
            }
        } catch (Exception e) {
            servletResponse.setStatus(500);
            servletResponse.getWriter().print("Internal error");
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
