package utils;

// Universal Constants

public class UC {
    public static final int MAX_SWIPER_ID = 5_000; // "swiper - between 1 and 5,000"
    public static final int MAX_SWIPEE_ID = 50_000; // "swipee - between 1 and 50,000"
    public static final int COMMENT_LENGTH = 256;
    public static final int NUM_CHANNELS = 100;
    public static final String RMQ_CONNECTION_NAME = "Twinder Servlet";
    public static final String RMQ_HOST_NAME = "ip-172-31-26-18.us-west-2.compute.internal";
//    public static final String RMQ_HOST_NAME = "35.91.109.227";
    public static final String RMQ_VHOST = "a3_host";
    public static final String RMQ_USERNAME = "admin";
    public static final String RMQ_PASSWORD = "admin666";
    public static final String RMQ_SWIPE_EXCHANGE_NAME = "swipe_exchange";
    public static final String RMQ_SWIPE_EXCHANGE_TYPE = "fanout";
    public static final String URL_MATCHES_TAG = "matches";
    public static final String DB_LIKED_USERS_TABLE = "LikedUsersCopy";
    public static final String DB_SWIPE_METRICS_TABLE = "SwipeMetricsCopy";
    public static final String DB_PARTITION_KEY = "UserId";
    public static final String DB_LIKES_ATTRIBUTE = "Likes";
    public static final String DB_DISLIKES_ATTRIBUTE = "Dislikes";
    public static final String DB_LIKED_USERS_ATTRIBUTE = "LikedUsers";
}
