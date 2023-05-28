package run.wyatt.oneplatform.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Wyatt
 * @date 2023/5/27 17:26
 */
@RestController
@RequestMapping("/api")
public class LoginController {
    @GetMapping("/login")
//    public Map<String, Object> login(Map<String, Object> query) {
    public Map<String, Object> login() {
//        String username = (String) query.get("username");
//        String password = (String) query.get("password");

        Map<String, Object> data = new HashMap<>();
        data.put("token", "d8c6ed7a-3fd4-46e4-a477-b20d1ce9cda0");
        data.put("permission", null);

        Map<String, Object> model = new HashMap<>();
        model.put("succ", true);
        model.put("code", 1000101);
        model.put("mesg", "认证成功");
        model.put("data", data);

        return model;
    }
}
