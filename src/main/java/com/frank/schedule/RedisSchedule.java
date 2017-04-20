package com.frank.schedule;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by frank on 17/4/20.
 */
@Component
public class RedisSchedule {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");

//    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Logger log = Logger.getLogger(this.getClass());

    @Scheduled(fixedRate = 60000)
    public void reportCurrentTime() {
        log.info("现在时间：" + dateFormat.format(new Date()));
    }
}
