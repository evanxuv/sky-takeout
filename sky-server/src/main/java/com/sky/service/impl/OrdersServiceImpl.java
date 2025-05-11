package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrdersService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author evan
 * @version 1.0
 */
@Service
@Slf4j
public class OrdersServiceImpl implements OrdersService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 用户下单--最终目的，将订单数据存入表中（orders,order_detail）
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional //开启事务
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // 查询地址表，获取收货人信息
        //异常情况的处理（收货地址为空、购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 查询用户表，获取下单人信息
        Long userId = BaseContext.getCurrentId();
        User user =  userMapper.selectById(userId);
        if (user == null) {
            throw new OrderBusinessException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 查询购物车列表数据---只查询自己名下的购物车数据
        List<ShoppingCart> cartList = shoppingCartMapper.list(userId);
        if (cartList == null || cartList.size() == 0) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //1.构造订单数据存入orders表
        Orders orders = new Orders();
        // 拷贝属性值
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        // 补充缺失的属性值
        orders.setNumber(System.currentTimeMillis()+"");// 订单编号
        orders.setStatus(Orders.PENDING_PAYMENT);//订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);//支付状态 0未支付 1已支付 2退款
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee()); // 收货人
        orders.setUserName(user.getName());//下单人
        ordersMapper.insert(orders);
        log.info("订单id:{}", orders.getId());

        //2.构造订单明细数据，存入order_detail表中
        List<OrderDetail> orderDetailList = new  ArrayList<>();
        // 循环遍历购物车列表数据，构造订单明细
        cartList.forEach(cart->{
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail,"id");
            // 关联订单id
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        });
        // 批量插入订单明细数据
        orderDetailMapper.insertBatch(orderDetailList);
        //3.清空购物车--删除自己名下的购物车
        shoppingCartMapper.delete(userId);
        //4.构造OrderSubmitVO并返回
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        // Long userId = BaseContext.getCurrentId();
        // User user = userMapper.selectById(userId);
        //
        // //调用微信支付接口，生成预支付交易单
        // JSONObject jsonObject = weChatPayUtil.pay(
        //         ordersPaymentDTO.getOrderNumber(), //商户订单号
        //         new BigDecimal(0.01), //支付金额，单位 元
        //         "苍穹外卖订单", //商品描述
        //         user.getOpenid() //微信用户的openid
        // );
        //
        // if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
        //     throw new OrderBusinessException("该订单已支付");
        // }
        //
        // OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        // vo.setPackageStr(jsonObject.getString("package"));
        //
        // return vo;
        //////////////////////////////////////////////////////////////////////////
        // 模拟支付成功---修改订单状态
        //业务处理，修改订单状态、来单提醒
        paySuccess(ordersPaymentDTO.getOrderNumber());
        // 返回一个空对象即可
        return new OrderPaymentVO();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = ordersMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.update(orders);
    }

    /**
     * 历史订单查询
     * 用户端订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQueryUser(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 设置分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        // 设置当前用户ID
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.getStatus();
        log.info("用户id:{}", ordersPageQueryDTO.getUserId());
        log.info("状态：{}", ordersPageQueryDTO.getStatus());
        // 分页条件查询
        Page<Orders> page = ordersMapper.pageQueryUser(ordersPageQueryDTO);
        // 查询出订单明细，并封装入OrderVO进行响应
        // 创建一个空的列表，用于存放转换后的订单视图对象（OrderVO）。
        ArrayList<OrderVO> list = new ArrayList<>();
        // 判断分页结果是否为空且有数据，如果有订单数据才进行后续处理。
        if (page!=null && page.getTotal()>0) {
            //遍历分页查询到的每一个订单（Orders）。
            for (Orders orders :page) {
                // 获取当前订单的ID。
                Long ordersId = orders.getId();
                // 根据订单ID查询该订单的所有明细（比如每个菜品、数量等）。
                List<OrderDetail> orderDetails = orderDetailMapper.getByUserId(ordersId);
                // 创建一个订单视图对象（OrderVO），用于封装订单和明细信息。
                OrderVO orderVO = new OrderVO();
                // 把订单（Orders）对象的属性值复制到订单视图对象（OrderVO）中。
                BeanUtils.copyProperties(orders, orderVO);
                // 把刚才查到的订单明细列表orderDetails设置到订单视图OrderVO对象中。
                orderVO.setOrderDetailList(orderDetails);
                // 把封装好的订单视图orderVO对象加入到结果列表list中。
                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);
    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        // 根据id查询订单
        Orders orders = ordersMapper.getById(id);
        // 查询该订单对应的菜品/套餐明细
        List<OrderDetail> orderDetailList = orderDetailMapper.getByUserId(orders.getId());
        // 将该订单及其详情封装到OrderVO并返回
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }


}
