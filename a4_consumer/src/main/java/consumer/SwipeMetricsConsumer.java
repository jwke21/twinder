package consumer;

import dynamodb.DynamoDbConnector;
import dynamodb.SwipeMetrics;
import utils.UC;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Consumer for the queue that records swipe directions for an individual user (i.e. like vs. dislike counts).
 */
public class SwipeMetricsConsumer extends AbstractTwinderUserDataConsumer {

    private ConcurrentHashMap<Integer, SwipeMetrics> bySwiperId;
    private static final String RMQ_CONNECTION_NAME = "Swipe Metrics Consumer";

    public SwipeMetricsConsumer() {
        super(UC.RMQ_EXCHANGE_NAME, UC.RMQ_SWIPE_METRICS_QUEUE, RMQ_CONNECTION_NAME);

        // Initialize the map
        bySwiperId = new ConcurrentHashMap<>(UC.MAX_SWIPER_ID);
        for (int i = 0; i < UC.MAX_SWIPER_ID; i++) {
            SwipeMetrics record = new SwipeMetrics();
            record.setUserId(i);
            record.setLikes(0);
            record.setDislikes(0);
            bySwiperId.put(i, record);
        }

        System.out.println("Consumer for queue '" + UC.RMQ_SWIPE_METRICS_QUEUE + "' successfully launched. " +
                "Press CTRL+C to shut down.");
    }

    @Override
    public void updateStats(DynamoDbConnector dbConnecton, Integer swiperId, Integer swipeeId, boolean isLike) {
        // Atomically update the like/dislike count
        bySwiperId.computeIfPresent(swiperId, (k, v) -> {
            if (isLike) {
                v.setLikes(v.getLikes() + 1);
            } else {
                v.setDislikes(v.getDislikes() + 1);
            }
            dbConnecton.updateItem(v);
            return v;
        });
    }

    public static void main(String[] argv) {
        try {
            SwipeMetricsConsumer consumer = new SwipeMetricsConsumer();
            consumer.consume();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}