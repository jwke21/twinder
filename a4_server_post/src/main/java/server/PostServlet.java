package server;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import rmq.RmqConnectionHandler;
import utils.UC;

@WebServlet(name = "PostServlet", value = "/Twinder/swipe/*")
public class PostServlet extends HttpServlet {

  private RmqConnectionHandler rmqConnectionHandler;
  // Gson instance that will handle json serialization and de-serialization
  // Ref: https://github.com/google/gson/blob/master/UserGuide.md
  private static Gson gson = new Gson();
  private static final Pattern validPostPaths[] = {
      // path = "/swipe/{leftorright}/"
      // leftorright - Like or dislike user. String
      Pattern.compile("/swipe/(left|right)"),
  };

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    try {
      // Establish connection with RabbitMQ server
      rmqConnectionHandler = RmqConnectionHandler.createConnectionHandler(UC.NUM_CHANNELS, UC.RMQ_HOST_NAME,
          UC.RMQ_CONNECTION_NAME);
      rmqConnectionHandler.declareExchange(UC.RMQ_SWIPE_EXCHANGE_NAME, UC.RMQ_SWIPE_EXCHANGE_TYPE, true);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("application/json");
    String urlPath = request.getPathInfo();
    // Validate path
    if (!isValidPostPath(urlPath)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid POST path"); // HTTP 404
      return;
    }
    // Borrow a channel from the channel pool
    Channel channel = rmqConnectionHandler.borrowChannel();
    try {
      String requestBody = readRequestBody(request);
      // Parse request's JSON into a PostRequestJson Object
      PostRequestJson jsonPayload = gson.fromJson(requestBody, PostServlet.PostRequestJson.class);
      // Validate post request JSON
      if (!isValidPostRequestJson(jsonPayload)) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid inputs"); // HTTP 400
        return;
      }
      // Build the message to be sent to the RMQ broker
      boolean liked = urlPath.contains("right") ? true : false;
      String msg = "{swiper:" + jsonPayload.swiper + ",swipee:" + jsonPayload.swipee + ",like:" + liked + "}";
      // Publish the JSON message to the fanout exchange
      channel.basicPublish(UC.RMQ_SWIPE_EXCHANGE_NAME,
          "", // routingKey
          null, // Message properties
          msg.getBytes(StandardCharsets.UTF_8));
      // Send response to client
      response.setStatus(HttpServletResponse.SC_CREATED); // HTTP 201
      response.getWriter().write("Write successful");
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Issue updating user data"); // HTTP 404
      e.printStackTrace();
    } finally {
      // Return the channel to the channel pool
      rmqConnectionHandler.returnChannel(channel);
    }
  }

  private boolean isValidPostPath(String urlPath) {
    // null check
    if (urlPath == null || urlPath.isEmpty()) {
      return false;
    }
    // Check if given urlPath matches any valid POST path patterns
    for (int i=0; i < validPostPaths.length; i++) {
      if (validPostPaths[i].matcher(urlPath).matches()) { return true; }
    }
    return false;
  }

  private String readRequestBody(HttpServletRequest request) throws IOException {
    // Read JSON payload into BufferedReader
    BufferedReader requestBody = request.getReader();
    StringBuilder body = new StringBuilder();
    String line;
    while ((line = requestBody.readLine()) != null) {
      body.append(line);
    }
    return body.toString();
  }

  private boolean isValidPostRequestJson(PostRequestJson json) {
    return json.swiper >= 1 && json.swiper <= UC.MAX_SWIPER_ID &&
        json.swipee >= 1 && json.swipee <= UC.MAX_SWIPEE_ID &&
        json.comment != null && json.comment.length() == UC.COMMENT_LENGTH; // "comment - random string of 256 characters"
  }


  // ------------------------------ PostRequestJson ------------------------------
  private static class PostRequestJson {
    public int swiper;
    public int swipee;
    public String comment;
  }
}
