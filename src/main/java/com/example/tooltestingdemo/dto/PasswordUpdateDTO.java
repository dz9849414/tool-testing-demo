package com.example.tooltestingdemo.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 密码更新DTO（只需要新密码）
 */
@Data
public class PasswordUpdateDTO {
    
    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}