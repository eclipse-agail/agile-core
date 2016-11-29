package iot.agile.http.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

public class CORSResponseFilter implements ContainerResponseFilter {

//  @Override
  public void filter(ContainerRequestContext request, ContainerResponseContext response) {

      response.getHeaders().add("Powered-By", "Agile");

      String origin = request.getHeaderString("Origin");

      response.getHeaders().add("Access-Control-Allow-Credentials", "true");
      response.getHeaders().add("Access-Control-Allow-Origin", origin);

      response.getHeaders().add("Access-Control-Allow-Headers", "Authorization,Content-Type");
      response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
      response.getHeaders().add("Access-Control-Max-Age", "3600");

  }
}

