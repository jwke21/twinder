package server;

import com.google.gson.Gson;
import dynamodb.DynamoDbConnector;
import dynamodb.LikedUsers;
import dynamodb.UserData;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/matches/*")
public class MatchesServlet extends HttpServlet {

  private DynamoDbConnector dynamoDbConnector;
  // Gson instance that will handle json serialization and de-serialization
  // Ref: https://github.com/google/gson/blob/master/UserGuide.md
  private static Gson gson = new Gson();

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    try {
      // Establish connection with DynamoDB server
      dynamoDbConnector = new DynamoDbConnector();
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("application/json");
    String urlPath = request.getPathInfo();
    // Validate path
    int userId;
    try {
      userId = Integer.parseInt(urlPath.substring(1));
    } catch (NumberFormatException e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid GET path"); // HTTP 404
      return;
    }

    // Get data from db
    LikedUsers record = dynamoDbConnector.getLikedUsers(userId);
    // Build response
    String respBody = record == null ?
            gson.toJson(new GetMatchesResponseJson()) : gson.toJson(new GetMatchesResponseJson(record.getLikedUsers()));
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().write(respBody);
  }

  // ------------------------------ GetMatchesResponseJson ------------------------------
  private static class GetMatchesResponseJson {
    public Set<Integer> matchList;

    public GetMatchesResponseJson() {this.matchList = new HashSet<>(0);}

    public GetMatchesResponseJson(Set<Integer> matchList) {
      this.matchList = matchList;
    }
  }
}
