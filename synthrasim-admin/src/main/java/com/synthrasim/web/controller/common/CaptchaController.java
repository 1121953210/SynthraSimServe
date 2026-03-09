package com.synthrasim.web.controller.common;

import com.google.code.kaptcha.Producer;
import com.synthrasim.common.constant.Constants;
import com.synthrasim.common.core.domain.AjaxResult;
import com.synthrasim.common.core.redis.RedisCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码控制器
 * 
 * 生成图形验证码，流程：
 * 1. 生成随机验证码文本和对应图片
 * 2. 将验证码文本存入Redis（key=captcha_codes:{uuid}，有效期2分钟）
 * 3. 将验证码图片转为Base64返回给前端
 * 4. 前端登录时将用户输入的验证码和uuid一起提交给后端校验
 */
@Api(tags = "验证码")
@RestController
public class CaptchaController {

    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Autowired
    private RedisCache redisCache;

    @ApiOperation("获取验证码")
    @GetMapping("/captchaImage")
    public AjaxResult getCode() throws Exception {
        // 1. 生成UUID作为验证码标识
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;

        // 2. 生成验证码文本
        String capText = captchaProducer.createText();

        // 3. 存入Redis，有效期2分钟
        redisCache.setCacheObject(verifyKey, capText, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);

        // 4. 生成验证码图片并转Base64
        BufferedImage image = captchaProducer.createImage(capText);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
        String base64Img = Base64.getEncoder().encodeToString(outputStream.toByteArray());

        return AjaxResult.success()
                .put("uuid", uuid)
                .put("img", base64Img);
    }
}
