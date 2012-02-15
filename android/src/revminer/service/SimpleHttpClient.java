/**
 * Some code borrowed from http://thinkandroid.wordpress.com/2009/12/30/getting-response-body-of-httpresponse/
 */
package revminer.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

public abstract class SimpleHttpClient {
  public static String get(String url) {
    HttpClient httpClient = new DefaultHttpClient();
    HttpGet httpGet = new HttpGet(url);
    HttpResponse response;
    try {
      response = httpClient.execute(httpGet);
      return getResponseBody(response.getEntity());
    } catch (ClientProtocolException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }

  private static String getResponseBody(final HttpEntity entity)
      throws IOException, ParseException {
    if (entity == null) {
      throw new IllegalArgumentException("HTTP entity may not be null");
    }
    
    InputStream instream = entity.getContent();
    if (instream == null) {
      return "";
    }

    if (entity.getContentLength() > Integer.MAX_VALUE) {
      throw new IllegalArgumentException(
          "HTTP entity too large to be buffered in memory");
    }

    String charset = getContentCharSet(entity);

    if (charset == null) {
      charset = HTTP.DEFAULT_CONTENT_CHARSET;
    }

    Reader reader = new InputStreamReader(instream, charset);
    StringBuilder buffer = new StringBuilder();
    
    try {
        char[] tmp = new char[1024];
        int l;

        while ((l = reader.read(tmp)) != -1) {
          buffer.append(tmp, 0, l);
        }
    } finally {
      reader.close();
    }

    return buffer.toString();
 }

  public static String getContentCharSet(final HttpEntity entity) throws ParseException {
    if (entity == null) {
      throw new IllegalArgumentException("HTTP entity may not be null");
    }
  
    String charset = null;
  
    if (entity.getContentType() != null) {
      HeaderElement values[] = entity.getContentType().getElements();
  
      if (values.length > 0) {
        NameValuePair param = values[0].getParameterByName("charset");
        if (param != null) {
          charset = param.getValue();
        }
      }
    }
    return charset;
  }
}
