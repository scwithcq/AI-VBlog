package com.vblog.utils;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * XSS过滤工具类
 * 从用户输入中过滤XSS脚本
 * 
 * @author V-Blog团队
 */
@Component
public class XssUtil {

    /**
     * XSS模式：脚本标签和常见XSS模式
     */
    private static final Pattern[] XSS_PATTERNS = {
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onerror\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onclick\\s*=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload\\s*=", Pattern.CASE_INSENSITIVE)
    };

    /**
     * 从输入中过滤XSS脚本
     * 
     * @param input 用户输入字符串
     * @return 过滤后的字符串
     */
    public String filter(String input) {
        if (StrUtil.isBlank(input)) {
            return input;
        }

        String filtered = input;
        for (Pattern pattern : XSS_PATTERNS) {
            filtered = pattern.matcher(filtered).replaceAll("");
        }

        // 转义HTML特殊字符
        filtered = filtered.replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");

        return filtered;
    }

    /**
     * 检查输入是否包含XSS脚本
     * 
     * @param input 用户输入字符串
     * @return 如果包含XSS返回true，否则返回false
     */
    public boolean containsXss(String input) {
        if (StrUtil.isBlank(input)) {
            return false;
        }

        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true;
            }
        }

        return false;
    }
}
