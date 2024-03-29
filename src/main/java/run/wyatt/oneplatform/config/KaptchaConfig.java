package run.wyatt.oneplatform.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;


/**
 * @author Wyatt
 * @date 2023/6/8 16:47
 */
@Configuration
public class KaptchaConfig {
    @Bean
    public DefaultKaptcha kaptchaProducer() {
//        kaptcha.border  是否有边框  默认为true  我们可以自己设置yes，no
//        kaptcha.border.color   边框颜色   默认为Color.BLACK
//        kaptcha.border.thickness  边框粗细度  默认为1
//        kaptcha.producer.impl   验证码生成器  默认为DefaultKaptcha
//        kaptcha.textproducer.impl   验证码文本生成器  默认为DefaultTextCreator
//        kaptcha.textproducer.char.string   验证码文本字符内容范围  默认为abcde2345678gfynmnpwx
//        kaptcha.textproducer.char.length   验证码文本字符长度  默认为5
//        kaptcha.textproducer.char.space  验证码文本字符间距  默认为2
//        kaptcha.textproducer.font.names    验证码文本字体样式  默认为new Font("Arial", 1, fontSize), new Font("Courier", 1, fontSize)
//        kaptcha.textproducer.font.size   验证码文本字符大小  默认为40
//        kaptcha.textproducer.font.color  验证码文本字符颜色  默认为Color.BLACK
//        kaptcha.noise.impl    验证码噪点生成对象  默认为DefaultNoise
//        kaptcha.noise.color   验证码噪点颜色   默认为Color.BLACK
//        kaptcha.obscurificator.impl   验证码样式引擎  默认为WaterRipple
//        kaptcha.word.impl   验证码文本字符渲染   默认为DefaultWordRenderer
//        kaptcha.background.impl   验证码背景生成器   默认为DefaultBackground
//        kaptcha.background.clear.from   验证码背景颜色渐进   默认为Color.LIGHT_GRAY
//        kaptcha.background.clear.to   验证码背景颜色渐进   默认为Color.WHITE
//        kaptcha.image.width   验证码图片宽度  默认为200
//        kaptcha.image.height  验证码图片高度  默认为50
        Properties props = new Properties();
        props.put("kaptcha.border", "no");
        props.put("kaptcha.textproducer.char.string", "ACDEFHKPRSTWX345679");

        Config config = new Config(props);
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
