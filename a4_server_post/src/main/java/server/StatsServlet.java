package server;

import com.google.gson.Gson;
import dynamodb.DynamoDbConnector;
import dynamodb.SwipeMetrics;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/stats/*")
public class StatsServlet extends HttpServlet {
    private DynamoDbConnector dynamoDbConnector;
    private static Gson gson = new Gson();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        dynamoDbConnector = new DynamoDbConnector();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        int userId;
        try {
            userId = Integer.parseInt(request.getPathInfo().substring(1));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid GET path"); // HTTP 404
            return;
        }

        // Get data from db
        SwipeMetrics record;
        record = dynamoDbConnector.getSwipeMetrics(userId);

        // Handle a non-existent user record
        if (record == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found"); // HTTP 404
            return;
        }

        // Build response
        String respBody;
        respBody = gson.toJson(new GetStatsResponseJson(
                record.getLikes(), record.getDislikes()));

        // Send response to client
        response.setStatus(HttpServletResponse.SC_OK); // HTTP 200
        response.getWriter().write(respBody);
    }

    private static class GetStatsResponseJson {
        public int numLikes;
        public int numDislikes;

        public GetStatsResponseJson(int numLikes, int numDislikes) {
            this.numLikes = numLikes;
            this.numDislikes = numDislikes;
        }
    }
}
