import link.thingscloud.freeswitch.esl.IEslEventListener;
import link.thingscloud.freeswitch.esl.InboundClient;
import link.thingscloud.freeswitch.esl.InboundClientBootstrap;
import link.thingscloud.freeswitch.esl.constant.Constants;
import link.thingscloud.freeswitch.esl.constant.EventNames;
import link.thingscloud.freeswitch.esl.inbound.NettyInboundClient;
import link.thingscloud.freeswitch.esl.inbound.option.InboundClientOption;
import link.thingscloud.freeswitch.esl.inbound.option.ServerOption;
import link.thingscloud.freeswitch.esl.transport.event.EslEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cola
 */

@Slf4j
public class InboundApp {

    private static final String FS_HOST = "192.168.31.211";
    private static final String FS_PWD = "ClueCon";
    private static final int FS_PORT = 5060;

    /**
     * 业务消息处理
     */
    static class Processor implements IEslEventListener {

        @Override
        public void eventReceived(String addr, EslEvent event) {
            log.info("eventReceived==> ");
            String eventName = event.getEventName();
            switch (eventName) {
                case EventNames.CHANNEL_CREATE:
                case EventNames.CHANNEL_ANSWER:
                case EventNames.CHANNEL_HANGUP:
                default: break;
            }
        }

        @Override
        public void backgroundJobResultReceived(String addr, EslEvent event) {
            String jobUuid = event.getEventHeaders().get("Job-UUID");
                    System.out.println("异步回调:" + jobUuid);
        }
    }

    private static final Processor processor = new Processor();

    public static void main(String[] args) throws InterruptedException {
        ServerOption serverOption = new ServerOption(FS_HOST, FS_PORT);

        InboundClientOption option = new InboundClientOption();
        option.workerGroupThread(8)
                .publicExecutorThread(8)
                .addServerOption(serverOption)
                .defaultPassword(FS_PWD)
                .defaultTimeoutSeconds(10);
        option.addListener(processor);

        NettyInboundClient nettyInboundClient = new NettyInboundClient(option);

        nettyInboundClient.setEventSubscriptions(serverOption.addr(), Constants.PLAIN,"all");
        nettyInboundClient.start();

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
