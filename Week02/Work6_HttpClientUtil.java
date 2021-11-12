import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * 6.（必做）写一段代码，使用 HttpClient 或 OkHttp
 * 访问  http://localhost:8801 ，代码提交到 GitHub。
 *
 * httpclient-4.5.13
 */
public class Work6_HttpClientUtil {

    public static String getString(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        try ( // try () 括号内定义的 Closeable 实现类会自动关闭
                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(httpGet)
        ){
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        }
    }

    public static void main(String[] args) throws IOException {
        String url = "http://localhost:8801";
        String resp = Work6_HttpClientUtil.getString(url);
        System.out.println("resp = " + resp);
    }
}
