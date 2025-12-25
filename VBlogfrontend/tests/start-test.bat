@echo off
chcp 65001 > nul
echo ╔════════════════════════════════════════╗
echo ║   V-Blog API 测试工具快速启动器       ║
echo ╚════════════════════════════════════════╝
echo.
echo 正在打开测试工具...
echo.
echo 📝 使用提示：
echo 1. 在测试页面中输入你的 JWT Token
echo 2. 点击"运行全部测试"按钮开始测试
echo 3. 查看测试结果和统计数据
echo.

start api-test.html

echo.
echo ✅ 测试工具已打开！
echo.
echo 📚 更多帮助请查看：
echo    - README.md (快速入门)
echo    - TEST_GUIDE.md (详细指南)
echo    - TEST_SUMMARY.md (测试总结)
echo.
pause
