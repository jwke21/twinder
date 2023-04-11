package dynamodb;

import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;


public class DynamoDbConnector {

    private static DynamoDBMapperConfig readConfig = DynamoDBMapperConfig.builder()
            .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.EVENTUAL)
            .build();
    private AmazonDynamoDB client;
    private DynamoDB dynamoDB;
    private DynamoDBMapper mapper;

    public DynamoDbConnector() {
        // Instantiate client
        client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("us-west-2")
                .build();
        // Instantiate table
        dynamoDB = new DynamoDB(client);
        // Instantiate mapper
        mapper = new DynamoDBMapper(client);
    }

    public SwipeMetrics getSwipeMetrics(Integer userId) {
        return mapper.load(SwipeMetrics.class, userId, readConfig);
    }

    public LikedUsers getLikedUsers(Integer userId) {
        return mapper.load(LikedUsers.class, userId, readConfig);
    }
}