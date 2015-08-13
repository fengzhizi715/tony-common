package cn.salesuite.common.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.salesuite.common.utils.StringUtils;


/**
 * http连接，实现了连接池
 * 
 * @author Tony Shen
 */
public class HttpClient {
	
	private static Logger logger = LoggerFactory.getLogger(HttpClient.class);
	
    /**
     * http请求的警告时间
     */
    private static final long    WARNING_TIME     = 500;

    private static final String DEFAULT_ENCODING = "UTF-8";
    
    private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private DefaultHttpClient   client;

    private int                 maxTotal;

    private int                 maxPerRoute;

    private int                 connectionTimeout;

    private int                 soTimeout;

    private int                 keepAliveTime;

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

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    /**
     * 初始化方法
     */
    public void init() {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        registry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

        // http连接池
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(
                registry);

        // 设置所有最大的连接数为100
        connectionManager.setMaxTotal(maxTotal);
        // 设置每个路由最大连接数为20
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        // 连接超时时间
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);

        client = new DefaultHttpClient(connectionManager, params);
        // 当响应头没有Keep-Alive属性时，指定默认的连接池空闲时间
        final int keepAliveTimeMills = keepAliveTime * 1000;

        client.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                if (response == null) {
                    throw new IllegalArgumentException("HTTP response may not be null");
                }
                HeaderElementIterator it = new BasicHeaderElementIterator(response
                        .headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                            return Long.parseLong(value) * 1000;
                        } catch (NumberFormatException ignore) {
                        	logger.error("httpClient keepAlive is not a number!");
                        }
                    }
                }
                return keepAliveTimeMills;
            }
        });
        
        initHttps();
    }
    
    /**
     * 初始化https连接
     */
    public void initHttps(){
		try {
	    	// 信任所有证书
			TrustManager[] trustManager = new TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
				public X509Certificate[] getAcceptedIssuers() {return null;}
	        }};
			// HOST验证
			X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier() {
				public boolean verify(String arg0, SSLSession arg1) {return true;}
				public void verify(String host, SSLSocket ssl)throws IOException {}
				public void verify(String host, X509Certificate cert)throws SSLException {}
				public void verify(String host, String[] cns,String[] subjectAlts) throws SSLException {}
			};
			SSLContext sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);
			sslContext.init(null, trustManager, null);
			//不校验域名
			SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext, hostnameVerifier);

			Scheme sch = new Scheme("https", 8092, socketFactory);
			client.getConnectionManager().getSchemeRegistry().register(sch);
		} catch (Exception e) {
			logger.error("httpClient initHttps error!");
		}
    }

    /**
     * 构造http get url请求
     * @param uri
     * @param params
     * @return
     */
    public static String buildUrl(String uri, Map<String, String> params) {

        String paramsStr = HttpClient.buildParams(params);
        return paramsStr == null ? uri : (uri + "?" + paramsStr);
    }
    
    /**
     * 构造http get url请求
     * @param uri
     * @param params
     * @return
     */
    public static String buildObjUrl(String uri, Map<String, Object> params) {

        String paramsStr = HttpClient.buildObjParams(params);
        return paramsStr == null ? uri : (uri + "?" + paramsStr);
    }

    /**
     * 构造请求参数字符串
     * @param params
     * @return
     */
    private static String buildParams(Map<String, String> params) {
        if (params == null || params.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Set<Entry<String, String>> entrySet = params.entrySet();
        for (Entry<String, String> entry : entrySet) {
            if (StringUtils.isNotBlank(entry.getKey()) && StringUtils.isNotBlank(entry.getValue())) {
                sb.append(sb.length() == 0 ? entry.getKey() + "=" : "&" + entry.getKey() + "=");
                sb.append(entry.getValue());
            }
        }
        return sb.toString().length() > 0 ? sb.toString() : null;
    }
    
	/**
	 * 构造请求参数字符串
	 * 
	 * @param params
	 * @return
	 */
	public static String buildObjParams(Map<String, Object> params) {
		if (params == null || params.size() == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		Set<Entry<String, Object>> entrySet = params.entrySet();
		for (Entry<String, Object> entry : entrySet) {

			if (StringUtils.isNotBlank(entry.getKey())
					&& entry.getValue() != null) {
				sb.append(sb.length() == 0 ? entry.getKey() + "=" : "&"
						+ entry.getKey() + "=");
				sb.append(entry.getValue().toString());
			}
		}
		return sb.toString().length() > 0 ? sb.toString() : null;
	}

    /**
     * get方法
     * 
     * @param url 带参数
     * @return
     */
    public String get(String url) {
        return this.get(url, DEFAULT_ENCODING);
    }

    /**
     * get方法
     * 
     * @param url 带参数
     * @param encoding 编码方式，默认utf-8
     * @return
     */
    public String get(String url, String encoding) {
    	logger.debug("http request url: " + url);
        return this.execute(new HttpGet(url), encoding);
    }
    
    public void get(String url,HttpResponseHandler callback) throws Exception {
    	logger.debug("http request url: " + url);
    	String body = get(url);
    	callback.onSuccess(body);
    }
    
    public void get(String url, String encoding, HttpResponseHandler callback) throws Exception {
    	logger.debug("http request url: " + url);
    	String body = get(url,encoding);
    	callback.onSuccess(body);
    }

    /**
     * post表单
     * @param url
     * @param params
     * @return
     */
    public String post(String url, String params) {
        return this.post(url, params, null);
    }

    /**
     * post表单
     * 
     * @param url 不带参数
     * @param params 参数内容
     * @param encoding 编码方式，默认utf-8
     * @return
     */
    public String post(String url, String params, String encoding) {
        return post(url, params, encoding, null);
    }
    
    /**
     * post流
     * @param url
     * @param params
     * @param contentType head中的Content-type
     * @return
     */
    public String postStream(String url, String params, String contentType) {
        return this.postStream(url, params, null, contentType);
    }

    /**
     * post 流
     * @param url
     * @param params
     * @param encoding
     * @param contentType head中的Content-type
     * @return
     */
    public String postStream(String url, String params, String encoding, String contentType) {
        return post(url, params, encoding, contentType);
    }
    
    /**
     * post 请求，post body为xml格式
     * @param url
     * @param xmlContent
     * @return
     */
	public String postXml(String url, String xmlContent) {

		HttpPost post = new HttpPost(url);
		post.addHeader("Content-Type", "application/xml; charset=UTF-8");  
		HttpEntity entity;
		String result = null;
		try {
			entity = new ByteArrayEntity(xmlContent.getBytes(DEFAULT_ENCODING));
			post.setEntity(entity);
			HttpResponse response = client.execute(post);
			result = parseResponse(response, DEFAULT_ENCODING);

			logger.debug("http response: " + result);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

    /**
     * get方法,返回HttpResponse对象
     * 
     * @param url 带参数
     * @return
     */
    public HttpResponse getResponse(String url, String encoding) {
        return executeResponse(new HttpGet(url));
    }

    /**
     * post方法， 返回HttpResponse对象
     * @param url
     * @param params
     * @param encoding
     * @return
     */
    public HttpResponse postResponse(String url, String params, String encoding) {
        return postResponse(url, params, encoding , null);
    }

    public HttpResponse postResponse(String url, String params, String encoding, String contentType) {
        HttpPost method = createHttpPost(url, params, encoding, contentType);
        return executeResponse(method);
    }

    public String post(String url, String params, String encoding,String contentType) {
    	logger.debug("http request uri: " + url);
    	logger.debug("http request params: " + params);
        HttpPost method = createHttpPost(url, params, encoding, contentType);
        return execute(method, encoding);
    }
    
    public HttpPost createHttpPost(String url, String params, String encoding, String contentType) {
    	return createHttpPost(url, createNameValuePairs(params), encoding, contentType);
    }
    
    public HttpPost createHttpPost(String url, List<NameValuePair> nameValuePairs, String encoding, String contentType) {
        if (StringUtils.isBlank(encoding)) {
            encoding = DEFAULT_ENCODING;
        }
        if (StringUtils.isBlank(contentType)) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        HttpPost method = new HttpPost(url);
        if (nameValuePairs != null) {
            StringEntity reqEntity = null;
            try {
//				reqEntity = new StringEntity(params, encoding); // 用这个如果参数中包含%特殊字符就会有问题
				reqEntity = new UrlEncodedFormEntity(nameValuePairs, encoding);
			} catch (UnsupportedEncodingException e) {
				logger.error("new StringEntity error for encoding : " + encoding);
            }
            method.addHeader("Content-Type", contentType);
            method.setEntity(reqEntity);
        }
        return method;
    }

    public String execute(HttpRequestBase httpReq, String encoding) {

        HttpResponse response = executeResponse(httpReq);
        String result = parseResponse(response, encoding);

        logger.debug("http response: " + result);
        return result;
    }

    public HttpResponse executeResponse(HttpRequestBase httpReq) {
        if (httpReq == null) {
            return null;
        }

        HttpResponse response = null;
        try {
            response = client.execute(httpReq);
        } catch (Exception e) {
        	logger.error("http请求失败！", e);
        }

        return response;
    }

    public static String parseResponse(HttpResponse response, String encoding) {
        String result = null;
        if (response != null) {
            if (StringUtils.isBlank(encoding)) {
                encoding = DEFAULT_ENCODING;
            }
            try {
                result = EntityUtils.toString(response.getEntity(), encoding);
            } catch (Exception e) {
            	logger.error("http response pase error！", e);
            }
        }
        return result;
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
    }
}
