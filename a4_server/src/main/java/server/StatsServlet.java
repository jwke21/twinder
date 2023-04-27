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
        SwipeMetrics record = dynamoDbConnector.getSwipeMetrics(userId);
        // Build response
        String respBody = record == null ?
                gson.toJson(new GetStatsResponseJson()) : gson.toJson(new GetStatsResponseJson(
                        record.getLikes(), record.getDislikes()
        ));
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(respBody);
    }

    // ------------------------------ GetStatsResponseJson ------------------------------
    private static class GetStatsResponseJson {
        public int numLikes;
        public int numDislikes;

        public GetStatsResponseJson() {}

        public GetStatsResponseJson(int numLikes, int numDislikes) {
            this.numLikes = numLikes;
            this.numDislikes = numDislikes;
        }
    }
}
