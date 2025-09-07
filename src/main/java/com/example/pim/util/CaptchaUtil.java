package com.example.pim.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CaptchaUtil {
    // 验证码宽度
    private static final int WIDTH = 120;
    // 验证码高度
    private static final int HEIGHT = 40;
    // 验证码字符数
    private static final int CODE_COUNT = 4;
    // 干扰线数量
    private static final int LINE_COUNT = 10;
    // 字体大小
    private static final int FONT_SIZE = 20;
    // 验证码字符范围
    private static final String CODE_STR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final Random random = new Random();

    /**
     * 生成随机颜色
     */
    private static Color getRandomColor(int min, int max) {
        min = Math.min(min, 255);
        max = Math.min(max, 255);
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }

    /**
     * 生成随机验证码
     */
    public static String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_COUNT; i++) {
            int index = random.nextInt(CODE_STR.length());
            sb.append(CODE_STR.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 生成验证码图片
     */
    public static BufferedImage generateImage(String code) {
        // 创建图像
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        // 获取图形上下文
        Graphics g = image.getGraphics();
        // 设置背景色
        g.setColor(getRandomColor(200, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, FONT_SIZE));
        // 绘制干扰线
        for (int i = 0; i < LINE_COUNT; i++) {
            g.setColor(getRandomColor(160, 200));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
        // 绘制验证码字符
        for (int i = 0; i < code.length(); i++) {
            g.setColor(getRandomColor(10, 150));
            g.drawString(String.valueOf(code.charAt(i)), (i * (WIDTH / CODE_COUNT)) + 5, 28);
        }
        // 释放图形上下文
        g.dispose();
        return image;
    }

    /**
     * 将BufferedImage转换为Base64字符串
     */
    public static String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        byte[] imageData = baos.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageData);
    }

    /**
     * 生成验证码和对应的Base64图片
     */
    public static CaptchaResult generateCaptcha() throws IOException {
        String code = generateCode();
        BufferedImage image = generateImage(code);
        String base64Image = imageToBase64(image);
        return new CaptchaResult(code, base64Image);
    }

    /**
     * 验证码结果类
     */
    public static class CaptchaResult {
        private String code;
        private String imageBase64;

        public CaptchaResult(String code, String imageBase64) {
            this.code = code;
            this.imageBase64 = imageBase64;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getImageBase64() {
            return imageBase64;
        }

        public void setImageBase64(String imageBase64) {
            this.imageBase64 = imageBase64;
        }
    }
}