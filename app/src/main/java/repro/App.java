package repro;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import jdk.net.ExtendedSocketOptions;

public class App {
    public static void main(String[] args) throws Exception {
        // wait for rmq to start up
        Thread.sleep(5000);

        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("rabbitmq");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");

        // configure various options that should provide more trust in good socket behavior, but doesn't help in this case
        connectionFactory.setConnectionTimeout(5000);
        connectionFactory.setRequestedHeartbeat(5);
        connectionFactory.setAutomaticRecoveryEnabled(false);
        connectionFactory.setSocketConfigurator(connectionFactory.getSocketConfigurator().andThen(socket -> {
            socket.setKeepAlive(true);
            socket.setSoTimeout(5000);
            socket.setSoLinger(true, 10);
            socket.setOption(ExtendedSocketOptions.TCP_KEEPCOUNT, 2);
            socket.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, 10);
            socket.setOption(ExtendedSocketOptions.TCP_KEEPINTERVAL, 3);
        }));

        try (final Connection connection = connectionFactory.newConnection()) {
            try (final Channel channel = connection.openChannel().orElseThrow()) {
                System.out.println("Channel is open, starting publishing");
                final AMQP.BasicProperties basicProperties = new AMQP.BasicProperties();
                for (int i = 0; ; i++) {
                    final long startTime = System.currentTimeMillis();
                    try {
                        channel.basicPublish("", "", basicProperties, new byte[]{});
                    } finally {
                        final long endTime = System.currentTimeMillis();
                        final long duration = endTime - startTime;
                        if (duration > 5000) {
                            System.out.println("Time exceeded: " + duration);
                        }
                    }
                }
            }
        }
    }
}
