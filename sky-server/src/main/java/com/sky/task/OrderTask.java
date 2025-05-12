package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author evan
 * @version 1.0
 */
@Slf4j
@Component
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 处理支付超时订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        log.info("处理支付超时订单：{}", new Date());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        // select * from orders where status = 1 and order_time < 当前时间-15分钟
        List<Orders> ordersList = ordersMapper.getByStatusAndOrdertime(Orders.PENDING_PAYMENT,time);
        if (ordersList != null && ordersList.size() > 0) {
            ordersList.forEach(order -> {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason(MessageConstant.CANCEL_REASON);
                order.setCancelTime(LocalDateTime.now());
                ordersMapper.update(order);
            });
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("处理派送中订单：{}", new Date());
        // select * from orders where status = 4 and order_time < 当前时间-1小时
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> orderList = ordersMapper.getByStatusAndOrdertime(Orders.DELIVERY_IN_PROGRESS, time);
        if (orderList != null && orderList.size() > 0) {
            orderList.forEach(order -> {
                order.setStatus(Orders.COMPLETED);
                ordersMapper.update(order);
            });
        }
    }
}
