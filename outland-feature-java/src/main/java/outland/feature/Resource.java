package outland.feature;

import com.google.protobuf.Message;
import okhttp3.Response;

interface Resource {

  String GET = "GET";
  String DELETE = "DELETE";
  String HEAD = "HEAD";
  String POST = "POST";
  String PUT = "PUT";

  Response requestThrowing(String method, String url, ResourceOptions options);

  Response requestThrowing(String method, String url, ResourceOptions options, Message body);
}
