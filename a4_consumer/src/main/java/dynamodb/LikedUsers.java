package dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import utils.UC;

import java.util.Set;

@DynamoDBTable(tableName = UC.DB_LIKED_USERS_TABLE)
public class LikedUsers extends UserData {

    private Set<Integer> likedUsers = null;

    // Partition key
    @DynamoDBHashKey(attributeName = UC.DB_PARTITION_KEY)
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    // Liked users
    @DynamoDBAttribute(attributeName = UC.DB_LIKED_USERS_ATTRIBUTE)
    public Set<Integer> getLikedUsers() {
        return likedUsers;
    }
    public void setLikedUsers(Set<Integer> likedUsers) {
        this.likedUsers = likedUsers;
    }
}
