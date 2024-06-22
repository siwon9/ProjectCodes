package org.choongang.global.router;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.choongang.global.config.containers.BeanContainer;

import java.io.IOException;

@WebServlet("/") //모든 요청을 이 서블릿이 처리하게 된다.
public class DispatcherServlet extends HttpServlet  {

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        BeanContainer bc = BeanContainer.getInstance();
        bc.addBean(HttpServletRequest.class.getName(), request); // 요청 class 키값과, 요청 저장
        bc.addBean(HttpServletResponse.class.getName(), response);

        bc.loadBeans();  // 파일조회

        RouterService service = bc.getBean(RouterService.class);
        service.route(request, response);
    }
}