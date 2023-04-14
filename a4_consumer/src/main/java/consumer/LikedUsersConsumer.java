package consumer;

import dynamodb.DynamoDbConnector;
import dynamodb.LikedUsers;
import utils.UC;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consumer for the queue that records the users liked by an individual user.
 */
public class LikedUsersConsumer extends AbstractTwinderUserDataConsumer {

    private ConcurrentHashMap<Integer, LikedUsers> bySwiperId;
    private static final String RMQ_CONNECTION_NAME = "Liked Users Consumer";

    public LikedUsersConsumer() {
        super(UC.RMQ_EXCHANGE_NAME, UC.RMQ_LIKED_USERS_QUEUE, RMQ_CONNECTION_NAME);

        // Initialize the map
        bySwiperId = new ConcurrentHashMap<>(UC.MAX_SWIPER_ID);
        for (int i = 0; i < UC.MAX_SWIPER_ID; i++) {
            LikedUsers record = new LikedUsers();
            record.setUserId(i);
            record.setLikedUsers(new HashSet<>(175));
            bySwiperId.put(i, record);
        }

        System.out.println("Consumer for queue '" + UC.RMQ_LIKED_USERS_QUEUE + "' successfully launched. " +
                "Press CTRL+C to shut down.");
    }

    @Override
    public void updateStats(DynamoDbConnector dbConnection, Integer swiperId, Integer swipeeId, boolean isLike) {
        // Atomically update the set of liked users
        bySwiperId.computeIfPresent(swiperId, (k, v) -> {
            // If it was not a like, do nothing
            if (!isLike) {
                return v;
            }
            // Add liked user to local store if less than capacity
            if (v.getLikedUsers().size() < UC.MAX_LIKED_USERS_STORED) {
                v.getLikedUsers().add(swipeeId);
                // Send update to db
                dbConnection.updateItem(v);
            }
            return v;
        });
    }

    public static void main(String[] argv) {
        try {
            LikedUsersConsumer consumer = new LikedUsersConsumer();
            consumer.consume();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
