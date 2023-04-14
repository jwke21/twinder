package consumer;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import dynamodb.DynamoDbConnector;
import rmq.RmqConnectionHandler;
import utils.UC;

import java.nio.charset.StandardCharsets;


public abstract class AbstractTwinderUserDataConsumer {

    protected String exchangeName;
    protected String queueName;
    protected RmqConnectionHandler connectionHandler;
    protected Thread[] threadPool;
    protected Gson gson;


    public AbstractTwinderUserDataConsumer(String exchangeName, String queueName, String rmqConnectionName) {
        this.exchangeName = exchangeName;
        this.queueName = queueName;
        this.connectionHandler = RmqConnectionHandler.createConnectionHandler(UC.NUM_THREADS, UC.RMQ_HOST_NAME,
                                                                                rmqConnectionName);
        connectionHandler.declareQueue(queueName, true);
        connectionHandler.bindQueue(queueName, exchangeName, "");

        // Initialize the thread pool
        this.threadPool = new Thread[UC.NUM_THREADS];
        for (int i = 0; i < UC.NUM_THREADS; i++) {
            threadPool[i] = new Thread(new Consumer(new DynamoDbConnector()));
        }

        // Initialize gson
        this.gson = new Gson();
    }

    public void consume() {
        int numThreads = threadPool.length;
        for (int i = 0; i < numThreads; i++) {
            threadPool[i].start();
        }
    }

    public abstract void updateStats(DynamoDbConnector dbConnection, Integer swiper, Integer swipee, boolean isLike);


    // ------------------------------ Consumer ------------------------------
    /**
     * Implements the logic handling consumption from RMQ and updates to the DB.
     */
    public class Consumer implements Runnable {

        DynamoDbConnector dbConnection;

        public Consumer(DynamoDbConnector dbConnection) {
            this.dbConnection = dbConnection;
        }
        public Consumer() {
            dbConnection = new DynamoDbConnector();
        }

        @Override
        public void run() {
            // Borrow a channel from the exchange
            Channel channel = connectionHandler.borrowChannel();
            try {
                // Set prefetch count for RMQ batch consumption
                int prefetchCount = UC.MAX_RMQ_CHANNEL_BATCH_SIZE;
                channel.basicQos(prefetchCount);
                channel.addShutdownListener(e -> System.err.println(e.getMessage()));
                // Callback lambda used when message delivered
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    // Get the msg json body
                    String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    SwipeMessageJson json = gson.fromJson(msg, SwipeMessageJson.class);

                    // Update the stats
                    updateStats(dbConnection, json.swiper, json.swipee, json.like);
                };

                // Consume any messages that arrive and manually ack them
                boolean autoAck = true;
                channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> { });
            } catch (Exception e) {
                System.err.println("Consumer thread error");
                e.printStackTrace();
            } finally {
                connectionHandler.returnChannel(channel);
            }
        }
    }

    // ------------------------------ SwipeMessageJson ------------------------------
    public static class SwipeMessageJson {
        public Integer swiper;
        public Integer swipee;
        public boolean like;
    }
}
