package com.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.获取本次请求的路径
        String requestURI = request.getRequestURI();
        log.info("拦截到的请求：{}",request.getRequestURL());

        log.info("当前线程id："+Thread.currentThread().getId());

        //列出不需要拦截的路径
        String[] urls = new String[]{
            "/backend/**",
            "/front/**",
            "/employee/login",
            "/employee/logout",
            "/user/sendMsg",
            "/user/login"
        };

        //2.判断是否是所要拦截的路径
        boolean b = checkURL(requestURI, urls);
        //3.如果不需要处理，则放行
        if (b){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }
        //4.判断登录状态，如果已经登录，则直接放行
        if (request.getSession().getAttribute("employee") != null){
            log.info("用户已登录");

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);

            return;
        }

        //4.判断登录状态，如果已经登录，则直接放行
        if (request.getSession().getAttribute("user") != null){
            log.info("用户已登录");

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);

            return;
        }

        //5.如果没有登录则放回未登录结果，通过输出流方式向客户端页面相应数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }

    public boolean checkURL(String requestURL,String[] urls){
        for (String url : urls){
            boolean match = PATH_MATCHER.match(url, requestURL);
            if (match){
                return true;
            }
        }
        return false;
    }
}
