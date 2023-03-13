package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import com.reggie.entity.ShoppingCart;
import com.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 添加到购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info(shoppingCart.toString());
        //1.设置userId的值
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        //创建查询条件
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);

        //2.判断是菜品还是套餐
        if (shoppingCart.getDishId() != null) {
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        //3.判断是否已经加入购物车，如果已经加入，则将number+1，否则直接添加
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
        if (cart != null) {  //购物车已经存在,将number+1
            cart.setNumber(cart.getNumber()+1);
            shoppingCartService.updateById(cart);

        } else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cart = shoppingCart;
        }
        return R.success(cart);
    }


    /**
     * 显示购物车信息
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){

        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);


        return R.success(list);
    }

    /**
     * 菜品数量减少
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){

        //1.设置userId的值
        Long userId = BaseContext.getCurrentId();

        //创建查询条件
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);

        //2.判断是菜品还是套餐
        if (shoppingCart.getDishId() != null){
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //3.根据userId和菜品（套餐）id获取该项购物车
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);

        ShoppingCart shoppingCart1 = new ShoppingCart();
        shoppingCart1.setNumber(0);
        if (cart != null){
            //4.如果number>1 则将数量减一，否则删除
            if (cart.getNumber() > 1){
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartService.updateById(cart);
                return R.success(cart);
            }else {
                shoppingCartService.removeById(cart.getId());

                return R.success(shoppingCart1);
            }
        }
        return R.success(shoppingCart1);
    }

    @DeleteMapping("/clean")
    public R<String> delete(){

        //根据userId进行购物车的删除
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);

        shoppingCartService.remove(queryWrapper);

        return R.success("删除成功");
    }

}
