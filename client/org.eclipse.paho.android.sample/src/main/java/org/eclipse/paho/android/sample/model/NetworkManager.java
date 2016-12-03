package org.eclipse.paho.android.sample.model;

/**
 * Created by jmsim on 2016. 11. 8..
 */

import android.app.Activity;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;

/**
 * Created by jmsim on 15. 10. 5..
 */
public class NetworkManager {

    //This class is called to execute Post and Get.

    int TIMEOUT = 30000;
    int SO_TIMEOUT = 30000;
    int MAX_CONN = 100;
    public static HttpClient httpClient;
    private static NetworkManager networkManager;
    private final String VERSIONCODE = "0.0.0";

    private Activity mActivity;

    public NetworkManager(Activity activityContext){
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(MAX_CONN));
        ConnManagerParams.setMaxTotalConnections(params, MAX_CONN);
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager conMagr = new ThreadSafeClientConnManager(params, schReg);

        this.mActivity=activityContext;
        this.httpClient = new DefaultHttpClient(conMagr, params);
    }

    public static NetworkManager getInstance(Activity activityContext){
        if(networkManager == null){
            networkManager = new NetworkManager(activityContext);
            return networkManager;
        }
        return networkManager;
    }

    public static HttpResponse execute(HttpPost post) throws IOException {

        HttpResponse response = null;
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        response = networkManager.httpClient.execute(post);
        return response;
    }

    public static HttpResponse execute(HttpGet get) throws IOException {
        return networkManager.httpClient.execute(get);
    }


}
