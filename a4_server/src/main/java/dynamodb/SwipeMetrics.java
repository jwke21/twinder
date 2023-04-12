package dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import utils.UC;

@DynamoDBTable(tableName = UC.DB_SWIPE_METRICS_TABLE)
public class SwipeMetrics extends UserData {
    private Integer likes = null;
    private Integer dislikes = null;

    // Partition key
    @DynamoDBHashKey(attributeName = UC.DB_PARTITION_KEY)
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    // Number of likes
    @DynamoDBAttribute(attributeName = UC.DB_LIKES_ATTRIBUTE)
    public Integer getLikes() {
        return likes;
    }
    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    // Number of dislikes
    @DynamoDBAttribute(attributeName = UC.DB_DISLIKES_ATTRIBUTE)
    public Integer getDislikes() {
        return dislikes;
    }
    public void setDislikes(Integer dislikes) {
        this.dislikes = dislikes;
    }
}