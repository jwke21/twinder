package dynamodb;

/**
 * API reference: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.html.
 */
public abstract class UserData {

    protected Integer userId = null;


    public abstract Integer getUserId();
    public abstract void setUserId(Integer userId);
}
