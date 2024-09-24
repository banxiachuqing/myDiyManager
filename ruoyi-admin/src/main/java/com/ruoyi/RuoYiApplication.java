package com.ruoyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class RuoYiApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(RuoYiApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  若依启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                "  __  __                                   \n" +
                " |  \\/  | __ _ _ __   __ _  __ _  ___ _ __ \n" +
                " | |\\/| |/ _` | '_ \\ / _` |/ _` |/ _ \\ '__|\n" +
                " | |  | | (_| | | | | (_| | (_| |  __/ |   \n" +
                " |_|  |_|\\__,_|_| |_|\\__,_|\\__, |\\___|_|   \n" +
                "                           |___/           ");
    }
}
