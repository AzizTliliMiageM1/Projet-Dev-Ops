package student40006741.recommendation.config;

import java.net.http.HttpClient;
import java.time.Duration;

public class RestConfig {

    public HttpClient openAiHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
}
