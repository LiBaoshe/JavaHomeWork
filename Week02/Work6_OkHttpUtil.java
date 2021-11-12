import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * 6.（必做）写一段代码，使用 HttpClient 或 OkHttp
 * 访问  http://localhost:8801 ，代码提交到 GitHub。
 *
 * okhttp-3.14.9
 */
public class Work6_OkHttpUtil {

    private final OkHttpClient client = new OkHttpClient();

    public String getString(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try ( // try () 括号内定义的 Closeable 实现类会自动关闭
                Response response = client.newCall(request).execute()){
            return response.body().string();
        }
    }

    public static void main(String[] args) throws IOException {
        String url = "http://localhost:8801";
        Work6_OkHttpUtil okHttpUtil = new Work6_OkHttpUtil();
        String resp = okHttpUtil.getString(url);
        System.out.println("resp = " + resp);
    }

}
