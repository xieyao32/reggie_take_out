package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.dto.DishDto;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;
import com.reggie.entity.DishFlavor;
import com.reggie.service.CategoryService;
import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;


    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        Page<Dish> pageInfo = new Page(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.hasText(name),Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = new ArrayList<>();

        for (Dish dish : records){
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(dish,dishDto);

            Long categoryId = dish.getCategoryId();

            //根据id获取菜品分类信息
            Category category = categoryService.getById(categoryId);

            if (category != null){
                dishDto.setCategoryName(category.getName());
            }

            list.add(dishDto);
        }

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 菜品添加
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("添加菜成功");
    }


    /**
     * 根据id获取菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getDishById(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return R.success("修改成功");
    }

    /**
     * 菜品状态修改
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status")Integer status,Long[] ids){

        log.info(status.toString());
        log.info(ids.toString());

        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Dish::getStatus,status).in(Dish::getId,ids);

        dishService.update(updateWrapper);

        return R.success("更改成功");
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam Long[] ids){

        dishService.removeWithFlavor(ids);

        return R.success("删除成功");
    }


    /**
     * 根据菜品类型id获取菜品
     * @param categoryId
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Long categoryId){
//
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Dish::getCategoryId,categoryId);
//        List<Dish> dishList = dishService.list(queryWrapper);
//
//        return R.success(dishList);
//    }

    /**
     * 根据菜品类型id获取菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(queryWrapper);

        List<DishDto> list = new ArrayList<>();

        for (Dish d : dishList){
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(d,dishDto);

            LambdaQueryWrapper<DishFlavor> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,d.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlavors);
            list.add(dishDto);
        }

        return R.success(list);
    }

}
