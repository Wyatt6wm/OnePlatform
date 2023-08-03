package run.wyatt.oneplatform.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Wyatt
 * @date 2023/6/22 8:42
 */
@Slf4j
@Component
public class LogFilter extends OncePerRequestFilter implements Filter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            String rid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
            MDC.put("RID", rid);
            // 只筛选业务请求打印日志
            String url = request.getRequestURL().toString();
            if (url.matches("^.*/api/.*$")) {
                log.info(">>>>> 开始处理请求: {} {}", request.getMethod(), request.getRequestURL());
            }
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
