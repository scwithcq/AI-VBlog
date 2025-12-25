package com.vblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * V-Blog Application Main Class
 * 
 * @author V-Blog Team
 */
@SpringBootApplication
@MapperScan("com.vblog.mapper")
public class VBlogApplication {

  public static void main(String[] args) {
    SpringApplication.run(VBlogApplication.class, args);
  }
}
