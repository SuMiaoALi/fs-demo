package com.colawz.inbound;

import link.thingscloud.freeswitch.esl.InboundClient;
import link.thingscloud.freeswitch.esl.inbound.option.InboundClientOption;
import link.thingscloud.freeswitch.esl.inbound.option.ServerOption;
import link.thingscloud.freeswitch.esl.spring.boot.starter.handler.InboundClientOptionHandler;
import link.thingscloud.freeswitch.esl.spring.boot.starter.properties.InboundClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cola
 */

@Slf4j
@RestController
@RequestMapping("/fs")
public class InboundApp implements InitializingBean {

    @Autowired
    private InboundClientProperties inboundClientProperties;
    @Autowired
    private InboundClient inboundClient;
    @Autowired
    private InboundClientOptionHandler inboundClientOptionHandler;
    @Autowired
    private M1ServerOptionListener myServerOptionListener;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("afterPropertiesSet==> servers: {}", inboundClientProperties.getServers());
        InboundClientOption option = inboundClientOptionHandler.getOption();
        option.serverOptionListener(myServerOptionListener);
        log.info("afterPropertiesSet==> serverOptionListener: {}", option.serverOptionListener());
    }

    @GetMapping("/add")
    public void addFs(ServerOption serverOption) {
        InboundClientOption option = inboundClientOptionHandler.getOption();
        option.addServerOption(serverOption);
    }

}
