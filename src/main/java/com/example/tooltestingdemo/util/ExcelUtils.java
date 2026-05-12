package com.example.tooltestingdemo.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel工具类
 */
public class ExcelUtils {
    
    /**
     * 导出权限数据到Excel
     *
     * @param title       标题
     * @param permissions 权限列表（Map形式，包含id, name, code, description, module）
     * @return Excel文件字节数组
     */
    public static byte[] exportPermissionsToExcel(String title, List<Map<String, Object>> permissions) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(title);
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建数据行样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            String[] headers = {"权限ID", "权限名称", "权限编码", "权限描述", "所属模块"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }
            
            // 填充数据
            int rowNum = 1;
            for (Map<String, Object> permission : permissions) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(getStringValue(permission.get("id")));
                cell0.setCellStyle(dataStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(getStringValue(permission.get("name")));
                cell1.setCellStyle(dataStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(getStringValue(permission.get("code")));
                cell2.setCellStyle(dataStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(getStringValue(permission.get("description")));
                cell3.setCellStyle(dataStyle);
                
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(getStringValue(permission.get("module")));
                cell4.setCellStyle(dataStyle);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }
    
    /**
     * 从Excel导入角色权限数据（角色权限专用）
     * 
     * Excel文件格式要求：
     * - 第1-3行：角色信息（会被跳过）
     * - 第4行：空行（会被跳过）
     * - 第5行：权限表标题行（会被跳过）
     * - 第6行及以后：权限数据，权限编码在第三列（索引2）
     *
     * @param inputStream Excel文件输入流
     * @return 权限编码列表
     */
    public static List<String> importPermissionsFromExcel(InputStream inputStream) {
        List<String> permissionCodes = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 跳过前5行（角色信息+空行+标题行），从第6行（索引5）开始读取权限数据
            // 如果表格格式不同，也支持从包含"权限编码"的标题行之后开始读取
            int startRow = 5; // 默认从第6行开始
            
            // 查找"权限编码"标题所在行，从该行之后开始读取
            for (int i = 0; i <= Math.min(10, sheet.getLastRowNum()); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(2);
                    if (cell != null) {
                        String value = getCellStringValue(cell);
                        if (value != null && value.contains("权限编码")) {
                            startRow = i + 1; // 从标题行的下一行开始
                            break;
                        }
                    }
                }
            }
            
            // 从正确的起始行开始读取权限数据
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                // 读取权限编码（第三列，索引为2）
                Cell cell = row.getCell(2);
                if (cell != null) {
                    String code = getCellStringValue(cell);
                    if (code != null && !code.trim().isEmpty()) {
                        permissionCodes.add(code.trim());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("导入角色权限Excel失败", e);
        }
        
        return permissionCodes;
    }
    
    /**
     * 从Excel导入用户权限数据（用户权限专用）
     * 
     * Excel文件格式要求：
     * - 第1行：用户信息（标题）
     * - 第2行：用户ID
     * - 第3行：用户名
     * - 第4行：真实姓名
     * - 第5行：空行（会被跳过）
     * - 第6行：模块、权限编码（标题行）
     * - 第7行及以后：权限数据，模块在第一列（索引0），权限编码在第二列（索引1）
     *
     * @param inputStream Excel文件输入流
     * @return 权限编码列表
     */
    public static List<String> importUserPermissionsFromExcel(InputStream inputStream) {
        List<String> permissionCodes = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 默认从第7行（索引6）开始读取权限数据
            int startRow = 6;
            
            // 查找"权限编码"标题所在行，从该行之后开始读取
            for (int i = 0; i <= Math.min(10, sheet.getLastRowNum()); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(1); // 用户权限格式中权限编码标题在第二列
                    if (cell != null) {
                        String value = getCellStringValue(cell);
                        if (value != null && value.contains("权限编码")) {
                            startRow = i + 1; // 从标题行的下一行开始
                            break;
                        }
                    }
                }
            }
            
            // 从正确的起始行开始读取权限数据
            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                // 读取权限编码（第二列，索引为1）
                Cell cell = row.getCell(1);
                if (cell != null) {
                    String code = getCellStringValue(cell);
                    if (code != null && !code.trim().isEmpty()) {
                        permissionCodes.add(code.trim());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("导入用户权限Excel失败", e);
        }
        
        return permissionCodes;
    }
    
    /**
     * 获取单元格字符串值
     */
    private static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // 处理数字类型，避免科学计数法
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }
    
    /**
     * 获取对象的字符串值
     */
    private static String getStringValue(Object obj) {
        return obj == null ? "" : obj.toString();
    }
    
    /**
     * 导出用户权限数据到Excel（按模块分组格式）
     *
     * @param title      标题
     * @param userId     用户ID
     * @param username   用户名
     * @param realName   真实姓名
     * @param permissions 权限列表（Map形式，key为模块名，value为权限编码列表）
     * @return Excel文件字节数组
     */
    public static byte[] exportUserPermissionsToExcel(String title, String userId, String username, 
                                                       String realName, Map<String, List<String>> permissions) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(title);
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建数据行样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            // 创建用户信息标题行
            Row infoHeaderRow = sheet.createRow(0);
            Cell infoHeaderCell = infoHeaderRow.createCell(0);
            infoHeaderCell.setCellValue("用户信息");
            infoHeaderCell.setCellStyle(headerStyle);
            
            // 用户信息数据行
            int rowNum = 1;
            
            Row userIdRow = sheet.createRow(rowNum++);
            userIdRow.createCell(0).setCellValue("用户ID");
            userIdRow.createCell(1).setCellValue(userId);
            
            Row usernameRow = sheet.createRow(rowNum++);
            usernameRow.createCell(0).setCellValue("用户名");
            usernameRow.createCell(1).setCellValue(username);
            
            Row realNameRow = sheet.createRow(rowNum++);
            realNameRow.createCell(0).setCellValue("真实姓名");
            realNameRow.createCell(1).setCellValue(realName);
            
            // 空行
            rowNum++;
            
            // 权限列表标题行
            Row permissionHeaderRow = sheet.createRow(rowNum++);
            String[] headers = {"模块", "权限编码"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = permissionHeaderRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 6000);
            }
            
            // 填充权限数据
            for (Map.Entry<String, List<String>> entry : permissions.entrySet()) {
                String module = entry.getKey();
                List<String> perms = entry.getValue();
                
                for (int i = 0; i < perms.size(); i++) {
                    Row row = sheet.createRow(rowNum++);
                    
                    Cell cell0 = row.createCell(0);
                    // 只在第一个权限时显示模块名
                    cell0.setCellValue(i == 0 ? module : "");
                    cell0.setCellStyle(dataStyle);
                    
                    Cell cell1 = row.createCell(1);
                    cell1.setCellValue(perms.get(i));
                    cell1.setCellStyle(dataStyle);
                }
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }
    
    /**
     * 导出角色权限数据到Excel
     *
     * @param title      标题
     * @param roleId     角色ID
     * @param roleName   角色名称
     * @param permissions 权限列表（Map形式，包含id, name, code, description, module）
     * @return Excel文件字节数组
     */
    public static byte[] exportRolePermissionsToExcel(String title, String roleId, String roleName,
                                                       List<Map<String, Object>> permissions) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(title);
            
            // 创建标题行样式
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 创建数据行样式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            // 创建角色信息标题行
            Row infoHeaderRow = sheet.createRow(0);
            Cell infoHeaderCell = infoHeaderRow.createCell(0);
            infoHeaderCell.setCellValue("角色信息");
            infoHeaderCell.setCellStyle(headerStyle);
            
            // 角色信息数据行
            int rowNum = 1;
            
            Row roleIdRow = sheet.createRow(rowNum++);
            roleIdRow.createCell(0).setCellValue("角色ID");
            roleIdRow.createCell(1).setCellValue(roleId);
            
            Row roleNameRow = sheet.createRow(rowNum++);
            roleNameRow.createCell(0).setCellValue("角色名称");
            roleNameRow.createCell(1).setCellValue(roleName);
            
            // 空行
            rowNum++;
            
            // 权限列表标题行
            Row permissionHeaderRow = sheet.createRow(rowNum++);
            String[] headers = {"权限ID", "权限名称", "权限编码", "权限描述", "所属模块"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = permissionHeaderRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }
            
            // 填充权限数据
            for (Map<String, Object> permission : permissions) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(getStringValue(permission.get("id")));
                cell0.setCellStyle(dataStyle);
                
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(getStringValue(permission.get("name")));
                cell1.setCellStyle(dataStyle);
                
                Cell cell2 = row.createCell(2);
                cell2.setCellValue(getStringValue(permission.get("code")));
                cell2.setCellStyle(dataStyle);
                
                Cell cell3 = row.createCell(3);
                cell3.setCellValue(getStringValue(permission.get("description")));
                cell3.setCellStyle(dataStyle);
                
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(getStringValue(permission.get("module")));
                cell4.setCellStyle(dataStyle);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }
}