package com.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.common.DataResults;
import com.warehouse.common.PageUtils;
import com.warehouse.entity.Products;
import com.warehouse.service.ProductsService;
import com.warehouse.service.TypesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    ProductsService productsService;

    @Autowired
    TypesService typesService;

    /**
     * 01-数据分页(同步)
     *
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @RequestMapping("/page1")
    public String queryPage1(@RequestParam(defaultValue = "1") Long pageIndex,
                            @RequestParam(defaultValue = "10") Long pageSize,
                            @RequestParam(defaultValue = "") String ptitle,Model model) {

        QueryWrapper<Products> wrapper = new QueryWrapper<Products>().eq("del", 0);
        wrapper.like(ptitle!=null,"ptitle",ptitle);//模糊搜索

        //调用MyBatisPlus分页插件完成数据分页
        IPage<Products> page = productsService.page(new Page<Products>(pageIndex, pageSize),wrapper);
        List<Products> records = page.getRecords();

        //为了查询商品大类名称
        for (Products record : records) {
            Integer tid = record.getTid();// 分类ID
            record.setTypeName(typesService.getById(tid).getTname());
        }
        //封装成工具类
        PageUtils pageUtils = new PageUtils(pageIndex, pageSize, page.getTotal(), records);
        System.out.println("后台商品数据分页工具类:" + pageUtils);
        model.addAttribute("pageUtils", pageUtils);
        model.addAttribute("ptitle", ptitle);
        return "product1";
    }

    /**
     * 01-数据分页(同步)
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @RequestMapping("/page2")
    public String queryPage2(@RequestParam(defaultValue = "1") Long pageIndex,
                            @RequestParam(defaultValue = "10") Long pageSize,
                            @RequestParam(defaultValue = "") String ptitle,Model model) {

        QueryWrapper<Products> wrapper = new QueryWrapper<Products>().eq("del", 0);
        wrapper.like(ptitle!=null,"ptitle",ptitle);//模糊搜索

        //调用MyBatisPlus分页插件完成数据分页
        IPage<Products> page = productsService.page(new Page<Products>(pageIndex, pageSize),wrapper);
        List<Products> records = page.getRecords();
        for (Products record : records) {
            Integer tid = record.getTid();// 分类ID
            record.setTypeName(typesService.getById(tid).getTname());
        }
        //封装成工具类
        PageUtils pageUtils = new PageUtils(pageIndex, pageSize, page.getTotal(), records);
        System.out.println("后台商品数据分页工具类:" + pageUtils);
        model.addAttribute("pageUtils", pageUtils);
        model.addAttribute("ptitle", ptitle);
        return "product2";
    }

    @PostMapping("add")
    public void add(Products products, @RequestParam("file") MultipartFile file, HttpServletResponse response) {
        //默认未删除
        products.setDel(0);
        //设置响应编码格式
        response.setContentType("text/html;charset=utf-8");
        try {
            if (file.isEmpty()) {
                response.getWriter().write("<script>alert('上传的图片不能为空!');location.href='/products/page';</script>");
                return;
            }
            //上传的文件不为空,获取图片原始的文件名
            String filename = file.getOriginalFilename();
            //获取文件的后缀  123123.jpg
            String suffixName = filename.substring(filename.lastIndexOf("."));
            //生成一个新的文件名
            filename = UUID.randomUUID() + suffixName;
            System.out.println("要上传服务器的文件名(新的文件名)是:" + filename);

            //获取文件上传的路径
            File path = new File(ResourceUtils.getURL("classpath:").getPath());
            File upload = new File(path.getAbsolutePath(), "/static/upload/" + filename);

            //上传
            file.transferTo(upload);
            System.out.println("文件上传成功:" + upload.getAbsolutePath());

            //设置图片地址
            products.setPimage(filename);
            boolean save = productsService.save(products);
            if (save) {
                response.getWriter().write("<script>alert('新增成功!');location.href='/products/page1';</script>");
            } else {
                response.getWriter().write("<script>alert('新增失败!');location.href='/products/page1';</script>");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转更新页面
     *
     * @param pid
     * @return
     */
    @GetMapping("/goupdate")
    public String goupdate(int pid, Model model) {
        Products products = productsService.getById(pid);
        model.addAttribute("products", products);
        return "editProduct";
    }

    /**
     * 数据更新  废弃
     *
     * @return
     */
    @PutMapping("/update")
    @ResponseBody
    public DataResults update(Products products) {
        productsService.updateById(products);
        return new DataResults(200, "更新成功", null);
    }


    /**
     * 更新商品
     *
     * @param products
     * @param file
     * @param response
     */
    @PostMapping("update")
    public void update(Products products, @RequestParam("file") MultipartFile file, HttpServletResponse response) {
        //设置响应编码格式
        response.setContentType("text/html;charset=utf-8");
        try {
            if (file != null && !file.isEmpty()) {
                //上传的文件不为空,获取图片原始的文件名
                String filename = file.getOriginalFilename();
                //获取文件的后缀  123123.jpg
                String suffixName = filename.substring(filename.lastIndexOf("."));
                //生成一个新的文件名
                filename = UUID.randomUUID() + suffixName;
                System.out.println("要上传服务器的文件名(新的文件名)是:" + filename);

                //获取文件上传的路径
                File path = new File(ResourceUtils.getURL("classpath:").getPath());
                File upload = new File(path.getAbsolutePath(), "/static/upload/" + filename);

                //执行上传
                file.transferTo(upload);
                System.out.println("文件上传成功:" + upload.getAbsolutePath());

                //设置数据库图片字段的地址
                products.setPimage(filename);
            }
            boolean save = productsService.updateById(products);
            if (save) {
                response.getWriter().write("<script>alert('更新成功!');location.href='/products/page1';</script>");
            } else {
                response.getWriter().write("<script>alert('更新失败!');location.href='/products/page1';</script>");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 商品删除
     * @param pid
     * @return
     */
    @DeleteMapping("/delete")
    @ResponseBody
    public DataResults delete(int pid) {
        Products products=new Products();
        products.setPid(pid);
        products.setDel(1);//删除状态  update tb_products set del=1 where id=?
        productsService.updateById(products);
        return new DataResults(200, "删除成功", null);
    }

    /**
     * 根据类型ID查询商品信息集合
     * @return
     */
    @GetMapping("productsByTid")
    @ResponseBody
    public DataResults productsByTid(int tid){
        List<Products> productsList = productsService.list(new QueryWrapper<Products>().eq("del", 0).eq("tid", tid));
        return new DataResults(200,"请求成功",productsList);
    }

}
