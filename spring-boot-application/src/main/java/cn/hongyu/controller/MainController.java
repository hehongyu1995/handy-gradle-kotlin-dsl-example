package cn.hongyu.controller;

import cn.hongyu.service.HelloWorldService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hongyu
 * @date 2020/09/05 12:18 PM
 */
@RestController
public class MainController {

    private final HelloWorldService helloWorldService;

    public MainController(HelloWorldService helloWorldService) {
        this.helloWorldService = helloWorldService;
    }

    @GetMapping("/hello")
    public String helloWorld(){
        return helloWorldService.sayHello();
    }
}
