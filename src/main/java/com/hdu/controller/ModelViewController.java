package com.hdu.controller;

import com.hdu.model.SysUser;
import com.hdu.service.TrainNumberDetailService;
import com.hdu.service.TrainNumberService;
import com.hdu.service.TrainSeatService;
import com.hdu.service.TrainStationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ModelViewController {

    /* 登录页面和主页面 */
    @RequestMapping("/")
    public void index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 考虑是否登录状态
        Object object = request.getSession().getAttribute("user");
        if (object == null) {
            response.sendRedirect("/login.page");
        } else {
            response.sendRedirect("/admin/index.page");
        }
    }

    // 进入登录页面
    @GetMapping("/login.page")
    public ModelAndView login() {
        return new ModelAndView("/pages/login");
    }

    // 退出登录
    @RequestMapping("/logout.page")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.getSession().invalidate();
        String path = "/login.page";
        response.sendRedirect(path);
    }

    // 在登录页面执行登录操作（之后也要跳转页面）
    @RequestMapping("/mockLogin.page")
    public void mockLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        SysUser sysUser = SysUser.builder().id(1).username("admin").build();
        request.getSession().setAttribute("user", sysUser);
        response.sendRedirect("/admin/index.page");
    }

    // 登录成功后进入管理员首页
    @RequestMapping("/admin/index.page")
    public ModelAndView index() {
        return new ModelAndView("/pages/admin");
    }

    // 管理员首页默认加载的页面
    @GetMapping("/welcome.page")
    public ModelAndView welcome() {
        return new ModelAndView("/pages/welcome");
    }

    /* 城市管理页面 */
    @RequestMapping("/admin/train/city/list.page")
    public ModelAndView trainCityPage() {
        return new ModelAndView("/pages/trainCity");
    }

    /* 站点管理页面 */
    @RequestMapping("/admin/train/station/list.page")
    public ModelAndView trainStationPage() {
        return new ModelAndView("/pages/trainStation");
    }

    /* 车次管理页面 */
    @RequestMapping("/admin/train/number/list.page")
    public ModelAndView trainNumberPage() {
        return new ModelAndView("/pages/trainNumber");
    }

    /* 车次详情管理页面 */
    @RequestMapping("/admin/train/numberDetail/list.page")
    public ModelAndView trainNumberDetailPage() {
        return new ModelAndView("/pages/trainNumberDetail");
    }


    /* 座位管理页面 */
    @RequestMapping("/admin/train/seat/list.page")
    public ModelAndView trainSeatPage() {
        return new ModelAndView("/pages/trainSeat");
    }

}



















