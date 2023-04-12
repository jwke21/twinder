package twinder;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import dynamodb.DynamoDbConnector;
import dynamodb.LikedUsers;
import dynamodb.SwipeMetrics;
import dynamodb.UserData;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.UC;

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
    UserData record;
    record = dynamoDbConnector.getLikedUsers(userId);

    // Handle a non-existent user record
    if (record == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found"); // HTTP 404
      return;
    }

    // Build response
    String respBody;
    Set<Integer> allLikedUsers = ((LikedUsers) record).getLikedUsers();
    // We return a set of potential matches that has a max size of 100 according to API spec.
    Set<Integer> potentialMatches = new HashSet<>();
    int i = 0;
    for (Integer likedUser : allLikedUsers) {
      potentialMatches.add(likedUser);
      i++;
      if (i >= 100) {
        break;
      }
    }
    respBody = gson.toJson(new GetMatchesResponseJson(potentialMatches));

    // Send response to client
    response.setStatus(HttpServletResponse.SC_OK); // HTTP 200
    response.getWriter().write(respBody);
  }

  // ------------------------------ GetMatchesResponseJson ------------------------------
  private static class GetMatchesResponseJson {
    public Set<Integer> matchList;

    public GetMatchesResponseJson(Set<Integer> matchList) {
      this.matchList = matchList;
    }
  }
}
