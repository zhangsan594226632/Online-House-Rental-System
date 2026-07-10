package com.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.entity.Pie;
import com.warehouse.entity.Products;
import com.warehouse.entity.Types;
import com.warehouse.service.ProductsService;
import com.warehouse.service.TypesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class PieController {

    @Autowired
    TypesService typesService;

    @Autowired
    ProductsService productsService;

    /**
     * 01-跳转饼状图页面
     * @return
     */
    @GetMapping("/pie-doughnut")
    public String toPie(Model model){
        try {
            ObjectMapper objectMapper=new ObjectMapper();
            //1.查询所有的商品类别
            //list查询全部
            List<Types> typesList = typesService.list(new QueryWrapper<Types>().eq("del", 0));
            List<String> typesNames=new ArrayList<String>();
            List<Pie> pieList=new ArrayList<Pie>();
            //2.循环商品类别
            for (Types types : typesList) {
                //查询类别下商品的个数
                int count = productsService.count(new QueryWrapper<Products>().eq("del", 0).eq("tid", types.getTid()));
                //存储所有的商品类别名字
                typesNames.add(types.getTname());
                //存储商品类型对应的商品数量
                pieList.add(new Pie(count,types.getTname()));
            }
            //调用JackSon工具类把data1 、data2 转换为json格式
            String data1 = objectMapper.writeValueAsString(typesNames);
            String data2 = objectMapper.writeValueAsString(pieList);
            System.out.println(data1);
            System.out.println(data2);

            //把data1 、data2 转换为json的字符串格式后，页面上渲染
            model.addAttribute("data1",data1);
            model.addAttribute("data2",data2);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "pie-doughnut";

    }
}
