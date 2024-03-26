package com.colawz.inbound;

import link.thingscloud.freeswitch.esl.InboundClient;
import link.thingscloud.freeswitch.esl.inbound.listener.ServerOptionListener;
import link.thingscloud.freeswitch.esl.inbound.option.ServerOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author cola
 */

@Slf4j
@Component
public class M1ServerOptionListener implements ServerOptionListener {

    @Autowired
    private InboundClient inboundClient;

    @Override
    public void onAdded(ServerOption serverOption) {
        log.info("onAdded==> 新增了服务：{}", serverOption.addr());
        inboundClient.start();
    }

    @Override
    public void onRemoved(ServerOption serverOption) {

    }
}
