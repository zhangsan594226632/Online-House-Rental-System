package com.warehouse.controller;

import com.warehouse.entity.Products;
import com.warehouse.service.ProductsService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

@Controller
public class ProductsExportController {

    @Autowired
    private ProductsService productsService; // 注入商品服务

    /**
     * 导出商品数据到Excel
     */
    @GetMapping("/products/export")
    public void exportProducts(HttpServletResponse response) throws Exception {
        // 1. 查询所有商品数据（根据业务需求调整，比如过滤del=0的有效数据）
        List<Products> productsList = productsService.list(); 

        // 2. 创建Excel工作簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("商品数据");

        // 3. 创建表头行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"商品ID", "商品图片", "商品标题", "商品备注", "分类ID", "分类名称", "删除标识", "库存预警值", "价格", "库存数量"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            // 表头样式（可选）
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        // 4. 填充商品数据
        for (int i = 0; i < productsList.size(); i++) {
            Products product = productsList.get(i);
            Row dataRow = sheet.createRow(i + 1);
            // 按顺序填充字段
            dataRow.createCell(0).setCellValue(product.getPid() == null ? "" : product.getPid().toString());
            dataRow.createCell(1).setCellValue(product.getPimage() == null ? "" : product.getPimage());
            dataRow.createCell(2).setCellValue(product.getPtitle() == null ? "" : product.getPtitle());
            dataRow.createCell(3).setCellValue(product.getPremark() == null ? "" : product.getPremark());
            dataRow.createCell(4).setCellValue(product.getTid() == null ? "" : product.getTid().toString());
            dataRow.createCell(5).setCellValue(product.getTypeName() == null ? "" : product.getTypeName());
            dataRow.createCell(6).setCellValue(product.getDel() == null ? "" : product.getDel().toString());
            dataRow.createCell(7).setCellValue(product.getPwarn() == null ? "" : product.getPwarn().toString());
            dataRow.createCell(8).setCellValue(product.getPrice() == null ? "" : product.getPrice().toString());
            dataRow.createCell(9).setCellValue(product.getPcount() == null ? "" : product.getPcount().toString());
        }

        // 5. 设置响应头（解决下载文件名中文乱码）
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("商品数据导出", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        // 6. 输出Excel文件
        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);

        // 7. 关闭资源
        outputStream.flush();
        outputStream.close();
        workbook.close();
    }
}