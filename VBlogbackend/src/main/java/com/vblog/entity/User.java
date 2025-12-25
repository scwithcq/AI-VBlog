package com.vblog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User Entity
 * 
 * @author V-Blog Team
 */
@Data
@TableName("tb_user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    /**
     * User ID (primary key)
     */
    private Long userId;

    /**
     * Username
     */
    private String username;

    /**
     * Password (encrypted)
     */
    private String password;

    /**
     * Nickname
     */
    private String nickname;

    /**
     * Gender (0: unknown/other, 1: male, 2: female)
     */
    private Integer gender;

    /**
     * Phone number
     */
    private String phone;

    /**
     * Email address
     */
    private String email;

    /**
     * Location
     */
    private String location;

    /**
     * Personal website URL
     */
    private String website;

    /**
     * Avatar URL
     */
    private String avatar;

    /**
     * Background image URL
     */
    @TableField(exist = false)
    private String background;

    /**
     * User bio/introduction
     */
    private String bio;
}
