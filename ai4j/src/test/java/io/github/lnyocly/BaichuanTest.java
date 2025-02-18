package io.github.lnyocly;

import io.github.lnyocly.ai4j.config.BaichuanConfig;
import io.github.lnyocly.ai4j.interceptor.ErrorInterceptor;
import io.github.lnyocly.ai4j.listener.SseListener;
import io.github.lnyocly.ai4j.platform.openai.chat.entity.ChatCompletion;
import io.github.lnyocly.ai4j.platform.openai.chat.entity.ChatCompletionResponse;
import io.github.lnyocly.ai4j.platform.openai.chat.entity.ChatMessage;
import io.github.lnyocly.ai4j.service.Configuration;
import io.github.lnyocly.ai4j.service.IChatService;
import io.github.lnyocly.ai4j.service.PlatformType;
import io.github.lnyocly.ai4j.service.factor.AiService;
import io.github.lnyocly.ai4j.utils.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * @Author cly
 * @Description 智谱测试类
 * @Date 2024/8/3 18:22
 */
@Slf4j
public class BaichuanTest {

    private IChatService chatService;

    @Before
    public void test_init() throws NoSuchAlgorithmException, KeyManagementException {
        BaichuanConfig baichuanConfig = new BaichuanConfig();
        baichuanConfig.setApiKey("sk-4e5717ac51cacaf5d590cff13630cfce");

        Configuration configuration = new Configuration();
        configuration.setBaichuanConfig(baichuanConfig);


        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new ErrorInterceptor())
                .connectTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .sslSocketFactory(OkHttpUtil.getIgnoreInitedSslContext().getSocketFactory(), OkHttpUtil.IGNORE_SSL_TRUST_MANAGER_X509)
                .hostnameVerifier(OkHttpUtil.getIgnoreSslHostnameVerifier())
                //.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 10809)))
                .build();
        configuration.setOkHttpClient(okHttpClient);

        AiService aiService = new AiService(configuration);

        chatService = aiService.getChatService(PlatformType.BAICHUAN);
    }


    @Test
    public void test_chatCompletions_common() throws Exception {
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model("Baichuan4")
                .message(ChatMessage.withUser("鲁迅为什么打周树人"))
                .build();

        System.out.println("请求参数");
        System.out.println(chatCompletion);

        ChatCompletionResponse chatCompletionResponse = chatService.chatCompletion(chatCompletion);

        System.out.println("请求成功");
        System.out.println(chatCompletionResponse);

    }

    @Test
    public void test_chatCompletions_multimodal() throws Exception {
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model("yi-vision")
                .message(ChatMessage.withUser("这几张图片，分别有什么动物, 并且是什么品种",
                        "https://tse2-mm.cn.bing.net/th/id/OIP-C.SVxZtXIcz3LbcE4ZeS6jEgHaE7?w=231&h=180&c=7&r=0&o=5&dpr=1.3&pid=1.7",
                        "https://ts3.cn.mm.bing.net/th?id=OIP-C.BYyILFgs3ATnTEQ-B5ApFQHaFj&w=288&h=216&c=8&rs=1&qlt=90&o=6&dpr=1.3&pid=3.1&rm=2"))
                .build();

        System.out.println("请求参数");
        System.out.println(chatCompletion);

        ChatCompletionResponse chatCompletionResponse = chatService.chatCompletion(chatCompletion);

        System.out.println("请求成功");
        System.out.println(chatCompletionResponse);
    }


    @Test
    public void test_chatCompletions_stream() throws Exception {
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model("gpt-4o-mini")
                .message(ChatMessage.withUser("鲁迅为什么打周树人"))
                .build();


        System.out.println("请求参数");
        System.out.println(chatCompletion);

        // 构造监听器
        SseListener sseListener = new SseListener() {
            @Override
            protected void send() {
                System.out.println(this.getCurrStr());
            }
        };

        chatService.chatCompletionStream(chatCompletion, sseListener);

        System.out.println("请求成功");
        System.out.println(sseListener.getOutput());
        System.out.println(sseListener.getUsage());

    }

    @Test
    public void test_chatCompletions_function() throws Exception {
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model("gpt-4o-mini")
                .message(ChatMessage.withUser("查询洛阳明天的天气，并告诉我火车是否发车"))
                .functions("queryWeather", "queryTrainInfo")
                .build();

        System.out.println("请求参数");
        System.out.println(chatCompletion);

        ChatCompletionResponse chatCompletionResponse = chatService.chatCompletion(chatCompletion);

        System.out.println("请求成功");
        System.out.println(chatCompletionResponse);

        System.out.println(chatCompletion);

    }

    @Test
    public void test_chatCompletions_stream_function() throws Exception {

        // 构造请求参数
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model("yi-large-fc")
                .message(ChatMessage.withUser("查询洛阳明天的天气"))
                .functions("queryWeather", "queryTrainInfo")
                .build();


        // 构造监听器
        SseListener sseListener = new SseListener() {
            @Override
            protected void send() {
                System.out.println(this.getCurrStr());
            }
        };
        // 显示函数参数，默认不显示
        sseListener.setShowToolArgs(true);

        // 发送SSE请求
        chatService.chatCompletionStream(chatCompletion, sseListener);
        System.out.println("完整内容： ");
        System.out.println(sseListener.getOutput());
        System.out.println("内容花费： ");
        System.out.println(sseListener.getUsage());
    }


}
