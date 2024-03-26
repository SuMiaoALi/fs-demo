package com.colawz;

import com.alibaba.fastjson.JSON;
import link.thingscloud.freeswitch.esl.IEslEventListener;
import link.thingscloud.freeswitch.esl.ServerConnectionListener;
import link.thingscloud.freeswitch.esl.constant.Constants;
import link.thingscloud.freeswitch.esl.constant.EventNames;
import link.thingscloud.freeswitch.esl.inbound.NettyInboundClient;
import link.thingscloud.freeswitch.esl.inbound.listener.ServerOptionListener;
import link.thingscloud.freeswitch.esl.inbound.option.ConnectState;
import link.thingscloud.freeswitch.esl.inbound.option.InboundClientOption;
import link.thingscloud.freeswitch.esl.inbound.option.ServerOption;
import link.thingscloud.freeswitch.esl.transport.CommandResponse;
import link.thingscloud.freeswitch.esl.transport.event.EslEvent;
import link.thingscloud.freeswitch.esl.transport.event.EslEventHeaderNames;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cola
 */

@Slf4j
public class InboundApp {

    //    private static final String FS_HOST = "192.168.31.211";
    private static final String FS_HOST = "172.18.70.189";
    private static final String FS_PWD = "ClueCon";
    private static final int FS_PORT = 8021;
    private static final String FS_HOST1 = "172.18.70.103";

    static class MyServerOptionListener implements ServerOptionListener {

        @Override
        public void onAdded(ServerOption serverOption) {
            log.info("ServerOption: onAdded==> serverOption: {} , state: {}", serverOption.addr(), serverOption.state());
        }

        @Override
        public void onRemoved(ServerOption serverOption) {
            log.info("ServerOption: onRemoved==> serverOption: {} , state: {}", serverOption.addr(), serverOption.state());
        }
    }

    private static final MyServerOptionListener MY_SERVER_OPTION_LISTENER = new MyServerOptionListener();

    static class MyServerConnListener implements ServerConnectionListener {
        @Override
        public void onOpened(ServerOption serverOption) {
            log.info("ServerConnection.onOpened==> target serverOption: {} - {}", serverOption.addr(), serverOption.state());
        }

        @Override
        public void onClosed(ServerOption serverOption) {
            log.info("ServerConnection.onClosed==> target serverOption: {} - {}", serverOption.addr(), serverOption.state());
        }
    }

    private static final MyServerConnListener MY_SERVER_CONN_LISTENER = new MyServerConnListener();

    /**
     * 业务消息处理
     */
    static class Processor implements IEslEventListener {

        @Override
        public void eventReceived(String addr, EslEvent event) {
            log.info("MyEventReceived==> addr: {}, event: {}", addr, JSON.toJSONString(event));
            String eventName = event.getEventName();
            log.info("MyEventReceived==> eventName: {}", eventName);
            switch (eventName) {
                case EventNames.CHANNEL_CREATE:
                case EventNames.CHANNEL_ANSWER:
                case EventNames.CHANNEL_HANGUP:
                default:
                    break;
            }
        }

        @Override
        public void backgroundJobResultReceived(String addr, EslEvent event) {
            String jobUuid = event.getEventHeaders().get("Job-UUID");
            log.info("backgroundJobResultReceived==> 异步回调: Job-UUID: {}", jobUuid);
            log.info("backgroundJobResultReceived==> 异步回调: EslEvent: {}", JSON.toJSONString(event));
        }
    }

    private static final Processor processor = new Processor();

    public static void main(String[] args) throws InterruptedException {
        List<ServerOption> serverOptionList = new ArrayList<>();
        ServerOption serverOption = new ServerOption(FS_HOST, FS_PORT).password(FS_PWD).timeoutSeconds(10);
        ServerOption serverOption1 = new ServerOption(FS_HOST1, FS_PORT).password(FS_PWD).timeoutSeconds(10);
        serverOptionList.add(serverOption);
        serverOptionList.add(serverOption1);

        String serverAddr = serverOption.addr();
        String serverAddr1 = serverOption1.addr();

        InboundClientOption option = new InboundClientOption();
        option.workerGroupThread(8)
                .publicExecutorThread(8)
                .privateExecutorThread(8)
                .serverConnectionListener(MY_SERVER_CONN_LISTENER)
                .serverOptionListener(MY_SERVER_OPTION_LISTENER)
                .addServerOption(serverOption)
                .addServerOption(serverOption1)
                .addListener(processor);


        NettyInboundClient nettyInboundClient = new NettyInboundClient(option);
        nettyInboundClient.start();

        for (ServerOption serverOption2 : serverOptionList) {
            nettyInboundClient.addEventFilter(serverOption2.addr(), EslEventHeaderNames.EVENT_NAME, EventNames.CHANNEL_HANGUP_COMPLETE);
            nettyInboundClient.addEventFilter(serverOption2.addr(), EslEventHeaderNames.EVENT_NAME, EventNames.CHANNEL_HANGUP_COMPLETE);
        }

        int i = 0;
        boolean allConnected = true;

        do {
            List<ServerOption> notAutherList = option.serverOptions().stream()
                    .filter(serverOp -> serverOp.state() != ConnectState.AUTHED)
                    .collect(Collectors.toList());
            if (notAutherList.isEmpty()) {
                log.info("main==> 所有连接都已鉴权完毕");
                allConnected = true;
            } else {
                allConnected = false;
                log.info("main==> 等待socket连接成功... {}", i);
                i++;
                Thread.sleep(1000);
            }
        } while (!allConnected);

        CommandResponse commandResponse = nettyInboundClient.setEventSubscriptions(serverAddr, Constants.PLAIN, EventNames.ALL);
        log.info("main==> s1.event plain all res: {}", JSON.toJSONString(commandResponse));

        commandResponse = nettyInboundClient.setEventSubscriptions(serverAddr1, Constants.PLAIN, EventNames.ALL);
        log.info("main==> s2.event plain all res: {}", JSON.toJSONString(commandResponse));

        nettyInboundClient.sendAsyncApiCommand(serverAddr, "originate", "user/1000" + " &echo", (jobUUID) -> {
            log.info("main==> originate s1. jobId: {}", jobUUID);
        });
        nettyInboundClient.sendAsyncApiCommand(serverAddr1, "originate", "user/1012" + " &echo", (jobUUID) -> {
            log.info("main==> originate s2. jobId: {}", jobUUID);
        });

//
//        Client client = new Client();
//        try {
//            //连接freeswitch
//            client.connect("localhost", 8021, "ClueCon", 10);
//
//            client.addEventListener(new IEslEventListener() {
//
//                @Override
//                public void eventReceived(EslEvent event) {
//                    String eventName = event.getEventName();
//                    //这里仅演示了CHANNEL_开头的几个常用事件
//                    if (eventName.startsWith("CHANNEL_")) {
//                        String calleeNumber = event.getEventHeaders().get("Caller-Callee-ID-Number");
//                        String callerNumber = event.getEventHeaders().get("Caller-Caller-ID-Number");
//                        switch (eventName) {
//                            case "CHANNEL_CREATE":
//                                System.out.println("发起呼叫, 主叫：" + callerNumber + " , 被叫：" + calleeNumber);
//                                break;
//                            case "CHANNEL_BRIDGE":
//                                System.out.println("用户转接, 主叫：" + callerNumber + " , 被叫：" + calleeNumber);
//                                break;
//                            case "CHANNEL_ANSWER":
//                                System.out.println("用户应答, 主叫：" + callerNumber + " , 被叫：" + calleeNumber);
//                                break;
//                            case "CHANNEL_HANGUP":
//                                String response = event.getEventHeaders().get("variable_current_application_response");
//                                String hangupCause = event.getEventHeaders().get("Hangup-Cause");
//                                System.out.println("用户挂断, 主叫：" + callerNumber + " , 被叫：" + calleeNumber + " , response:" + response + " ,hangup cause:" + hangupCause);
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//                }
//
//                @Override
//                public void backgroundJobResultReceived(EslEvent event) {
//                    String jobUuid = event.getEventHeaders().get("Job-UUID");
//                    System.out.println("异步回调:" + jobUuid);
//                }
//            });
//
//            client.setEventSubscriptions("plain", "all");
//
//            //这里必须检查，防止网络抖动时，连接断开
//            if (client.canSend()) {
//                System.out.println("连接成功，准备发起呼叫...");
//                //（异步）向1000用户发起呼叫，用户接通后，播放音乐/tmp/demo1.wav
//                String callResult = client.sendAsyncApiCommand("originate", "user/1000 &playback(/tmp/demo.wav)");
//                System.out.println("api uuid:" + callResult);
//            }
//
//        } catch (InboundConnectionFailure inboundConnectionFailure) {
//            System.out.println("连接失败！");
//            inboundConnectionFailure.printStackTrace();
//        }

    }

}
