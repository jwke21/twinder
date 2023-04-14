package dynamodb;

import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import utils.UC;

import java.util.HashMap;
import java.util.List;


public class DynamoDbConnector {

    private static DynamoDBMapperConfig writeConfig = DynamoDBMapperConfig.builder()
            .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .build();
    private AmazonDynamoDB client;
    private DynamoDB dynamoDB;
    private DynamoDBMapper mapper;
    private HashMap<Integer, UserData> writeBuf;
    private HashMap<Integer, UserData> deleteBuf;

    public DynamoDbConnector() {
        // Instantiate client
        client = AmazonDynamoDBClientBuilder.standard()
                .withRegion("us-west-2")
                .build();
        // Instantiate table
        dynamoDB = new DynamoDB(client);
        // Instantiate mapper
        mapper = new DynamoDBMapper(client);
        // Instantiate the write buffer
        writeBuf = new HashMap<>(UC.DEFAULT_REQUEST_BUFFER_SIZE);
        // Instantiate the delete buffer (Used only to be compatible with batchWrite API)
        deleteBuf = new HashMap<>(UC.DEFAULT_REQUEST_BUFFER_SIZE);
    }

    public void updateItem(UserData updatedItem) {
        // Add update to the buffer
        writeBuf.put(updatedItem.getUserId(), updatedItem);
        // If buffer is full, flush it
        if (writeBuf.size() >= UC.BUFFER_FLUSH_THRESHOLD) {
            flushBuffer();
        }
    }

    public void flushBuffer() {
        List<DynamoDBMapper.FailedBatch> failures =
                mapper.batchWrite(writeBuf.values(), deleteBuf.values(), writeConfig);
        if (failures.size() > 0) {
            System.err.println("Failed to deliver " + failures);
        }
        writeBuf.clear();
    }
}