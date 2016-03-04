/**
 * 
 */
package cn.salesuite.common.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tony Shen
 *
 */
public class HttpAsyncClient {
	
	private static Logger logger = LoggerFactory.getLogger(HttpAsyncClient.class);
	
    private static final String DEFAULT_ENCODING = "UTF-8";
    
    private CloseableHttpAsyncClient client;
    
    private int                 maxTotal;

    private int                 maxPerRoute;
    
    private int                 connectionTimeout;

    private int                 soTimeout;
    
    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public void setMaxPerRoute(int maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }
    
    /**
     * 初始化方法
     */
    public void init() {
    	RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(soTimeout).setConnectTimeout(connectionTimeout).build();
    	
    	//绕过证书验证，处理https请求  
        SSLContext sslcontext = null;
		try {
			sslcontext = createIgnoreVerifySSL();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		};
    	Registry<SchemeIOSessionStrategy> sessionStrategyRegistry = RegistryBuilder.<SchemeIOSessionStrategy>create()
                .register("http", NoopIOSessionStrategy.INSTANCE)
                .register("https", new SSLIOSessionStrategy(sslcontext))
                .build();
		//配置io线程
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(Runtime.getRuntime().availableProcessors()).build();
		//设置连接池大小
        ConnectingIOReactor ioReactor = null;
		try {
			ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
		} catch (IOReactorException e) {
			
		}
        PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor, null, sessionStrategyRegistry, null);
        
    	client = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig)
   			 .setMaxConnPerRoute(maxPerRoute)
   	         .setMaxConnTotal(maxTotal)
   	         .setConnectionManager(connManager)
   			.build();
    }
    
    /** 
     * 绕过验证 
     *   
     * @return 
     * @throws NoSuchAlgorithmException  
     * @throws KeyManagementException  
     */  
    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {  
        SSLContext sc = SSLContext.getInstance("SSLv3");  
  
        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法  
        TrustManager trustManager = new X509TrustManager() {  
            @Override  
            public void checkClientTrusted(  
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,  
                    String paramString) throws CertificateException {  
            }  
  
            @Override  
            public void checkServerTrusted(  
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,  
                    String paramString) throws CertificateException {  
            }  
  
            @Override  
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
                return null;  
            }  
        };  
        sc.init(null, new TrustManager[] { trustManager }, null);  
        return sc;  
    }
    
    /**
     * get方法
     * @param url
     * @param encoding
     * @param callback
     */
	public void get(String url, HttpResponseHandler callback) {
		get(url,null,DEFAULT_ENCODING,callback);
	}
    
    /**
     * get方法
     * @param url
     * @param encoding
     * @param callback
     */
	public void get(String url, String encoding, HttpResponseHandler callback) {
		get(url,null,encoding,callback);
	}
	
    /**
     * get方法
     * @param url
     * @param encoding
     * @param callback
     */
	public void get(String url, Header[] headers ,String encoding, HttpResponseHandler callback) {
		logger.debug("http request url: " + url);
		// 创建请求对象
		HttpRequestBase request = new HttpGet(url);

		// 设置header信息
		if (headers!=null) {
			request.setHeaders(headers);
		}

		// 执行请求
		execute(client, request, encoding, callback);
	}
	
	/**
     * post表单
     * @param url
     * @param params
     * @return
     */
    public void post(String url, String params, HttpResponseHandler callback) {
        post(url, params, DEFAULT_ENCODING,callback);
    }
    
    /**
     * post表单
     * 
     * @param url 不带参数
     * @param params 参数内容
     * @param encoding 编码方式，默认utf-8
     * @return
     */
    public void post(String url, String params, String encoding, HttpResponseHandler callback) {
    	post(url,null,params, encoding,callback);
    }

    /**
     * 
     * @param url 不带参数
     * @param headers
     * @param params 参数内容
     * @param encoding 编码方式，默认utf-8
     * @param callback
     */
    public void post(String url, Header[] headers ,String params, String encoding, HttpResponseHandler callback) {
    	post(url,null,params, encoding,null,callback);
    }
    
    /**
     * 
     * @param url 不带参数
     * @param headers
     * @param params 参数内容
     * @param encoding 编码方式，默认utf-8
     * @param contentType
     * @param callback
     */
    public void post(String url,Header[] headers ,String params, String encoding, String contentType,HttpResponseHandler callback) {
		logger.debug("http request url: " + url);
		logger.debug("http request params: " + params);
		
    	post(url,headers,createNameValuePairs(params), encoding,contentType,callback);
    }
    
    public void post(String url, Header[] headers, List<NameValuePair> createNameValuePairs, String encoding,
			String contentType, HttpResponseHandler callback) {
		// 创建请求对象
    	HttpPost request = new HttpPost(url);

		// 设置header信息
		if (headers!=null) {
			request.setHeaders(headers);
		}
		
		//设置header信息  
        //指定报文头【Content-type】、【User-Agent】  
		request.setHeader("Content-type", "application/json");
		
        if (createNameValuePairs != null) {
            StringEntity reqEntity = null;
            try {
				reqEntity = new UrlEncodedFormEntity(createNameValuePairs, encoding);
			} catch (UnsupportedEncodingException e) {
				logger.error("new StringEntity error for encoding : " + encoding);
            }
            request.addHeader("Content-Type", contentType);
            
          //设置参数到请求对象中
//			((HttpResponse) request).setEntity(reqEntity);
            request.setEntity(reqEntity);
        }
		
		// 执行请求
		execute(client, request, encoding, callback);
	}

	/**
     * 字符串参数转化成NameValuePairs
     * a=1&b=2&c=3
     * 		转化成
     * 	List<NameValuePair>
     * @param params
     * @param encoding
     * @return
     */
    private List<NameValuePair> createNameValuePairs(String params) {
    	List<NameValuePair> paramNVPairs = new ArrayList<NameValuePair>();
    	String[] paramPairs = params.split("&");
    	String key = null;
    	String value = null;
    	for(String paramPair : paramPairs){
    		String[] keyValue = paramPair.split("=");
    		if(keyValue == null || keyValue.length == 0){
    			continue;
    		}
    		key = keyValue[0];
			if(keyValue.length == 1){
				value = "";
			} else {
				value = keyValue[1];
			}
			paramNVPairs.add(new BasicNameValuePair(key, value));
    	}
    	return paramNVPairs;
    }
    
	public void execute(final CloseableHttpAsyncClient client, HttpRequestBase request,final String encoding,
			final HttpResponseHandler callback) {

		// Start the client
		client.start();
		// 异步执行请求操作，通过回调，处理结果
		client.execute(request, new FutureCallback<HttpResponse>() {

			public void failed(Exception e) {
				callback.onFailed(e);
				close(client);
			}

			public void completed(HttpResponse resp) {
				String body = "";
				try {
					HttpEntity entity = resp.getEntity();
					if (entity != null) {
						InputStream instream = entity.getContent();
						try {
							StringBuilder sb = new StringBuilder();
							char[] tmp = new char[1024];
							Reader reader = new InputStreamReader(instream, encoding);
							int l;
							while ((l = reader.read(tmp)) != -1) {
								sb.append(tmp, 0, l);
							}
							body = sb.toString();
						} finally {
							instream.close();
							EntityUtils.consume(entity);
						}
					}
				} catch (ParseException e) {
					logger.error(e.getMessage());
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
				callback.onSuccess(body);
				close(client);
			}

			public void cancelled() {
				callback.onCancelled();
				close(client);
			}
		});

	}
	
	/**
	 * 关闭client对象
	 * 
	 * @param client
	 */
	private static void close(final CloseableHttpAsyncClient client) {
		try {
			client.close();
		} catch (IOException e) {			
			logger.error(e.getMessage());
		}
	}
	
    /**
     * 处理http response的handle
     * @author Tony Shen
     *
     */
    public static interface HttpResponseHandler {

    	/**
    	 * http请求成功后，response转换成content
    	 * @param content
    	 */
    	public void onSuccess(String content);
    	
    	/**
		 * 处理异常时，执行该方法
		 */
    	public void onFailed(Exception e);
    	
    	/**
		 * 处理取消时，执行该方法
		 */
    	public void onCancelled();
    }
}
