package com.lance.order;

import com.alibaba.fastjson.JSON;
import com.lance.BaseResponse;
import com.lance.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    OrderMapper mapper;

    @ResponseBody
    @RequestMapping("/create")
    public String create(HttpServletRequest request, HttpServletResponse response,
                         @RequestBody Order order) throws IOException {
        UserInfo userInfo = (UserInfo) request.getAttribute("UserInfo");
        order.setUser_id(userInfo.getId());
        mapper.insert(order);
        return JSON.toJSONString(new BaseResponse<>());
    }

    @GetMapping("/list")
    public String list(HttpServletRequest request, HttpServletResponse response) {
        UserInfo userInfo = (UserInfo) request.getAttribute("UserInfo");
        List<Order> orders = mapper.getAll(userInfo.getId());
        BaseResponse<List<Order>> res = new BaseResponse<>();
        res.setData(orders);
        return JSON.toJSONString(res);
    }

    @DeleteMapping("/delete")
    public String delete(HttpServletRequest request, HttpServletResponse response, @RequestParam("id")int orderId) {
        UserInfo userInfo = (UserInfo) request.getAttribute("UserInfo");
        List<Order> orders = mapper.getAll(userInfo.getId());
        for (Order order : orders) {
            if (order.getIdorder() == orderId) {
                mapper.delete(orderId);
                return JSON.toJSONString(new BaseResponse<>());
            }
        }

        response.setStatus(403);
        return "Order id is invalid";
    }
}
