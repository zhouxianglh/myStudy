package com.henglu.weixin.servlet;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.henglu.summer.control.IControl;

/**
 * Servlet
 * @author zhouxianglh@gmail.com
 * @version 1.0  2014-1-20 下午10:40:14
 */
public class StartServlet extends HttpServlet {
    private static final long serialVersionUID = -8128980502013463031L;
    private static Logger logger = Logger.getLogger(StartServlet.class);
    private WebApplicationContext applicationContext;

    public StartServlet() {
        super();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("接到新的消息请求......");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        IControl control = (IControl) applicationContext.getBean("validateInterceptor");
        try {
            control.execute(request, response);
        } catch (Exception e) {
            logger.error("", e);
        }
        logger.info("执行完毕......");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext servletContext = this.getServletContext();
        applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    }
}