package utils;

public class UC {

    public static final int NUM_THREADS = 100;
    public static final int MAX_SWIPER_ID = 5_000;
    public static final int MAX_LIKED_USERS_STORED = 100; // "a list of 100 users maximum"
    public static final int DEFAULT_REQUEST_BUFFER_SIZE = 45;
    public static final int BUFFER_FLUSH_THRESHOLD = 25;
    public static final int MAX_RMQ_CHANNEL_BATCH_SIZE = 100;

    public static final String RMQ_HOST_NAME = "ip-172-31-26-18.us-west-2.compute.internal"; // Private IPv4
    public static final String RMQ_VHOST = "a3_host";
    public static final String RMQ_USERNAME = "admin";
    public static final String RMQ_PASSWORD = "admin666";
    public static final String RMQ_EXCHANGE_NAME = "swipe_exchange";
    public static final String RMQ_SWIPE_METRICS_QUEUE = "swipe_metrics_queue";
    public static final String RMQ_LIKED_USERS_QUEUE = "liked_users_queue";
    public static final String DB_LIKED_USERS_TABLE = "LikedUsers";
    public static final String DB_SWIPE_METRICS_TABLE = "SwipeMetrics";
    public static final String DB_PARTITION_KEY = "UserId";
    public static final String DB_LIKES_ATTRIBUTE = "Likes";
    public static final String DB_DISLIKES_ATTRIBUTE = "Dislikes";
    public static final String DB_LIKED_USERS_ATTRIBUTE = "LikedUsers";

}
