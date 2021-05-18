import javax.jms.*;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.*;

/**
 * A minimal and simple application for Point-to-point messaging.
 * <p>
 * Application makes use of fixed literals, any customisations will require
 * re-compilation of this source file. Application assumes that the named queue
 * is empty prior to a run.
 * <p>
 * Notes:
 * <p>
 * API type: JMS API (v2.0, simplified domain)
 * <p>
 * Messaging domain: Point-to-point
 * <p>
 * Provider type: IBM MQ
 * <p>
 * Connection mode: Client connection
 * <p>
 * JNDI in use: No
 */
public class Client {
    private static String host = "localhost"; // Host name or IP address
    private static int port = 1414; // Listener port for your queue manager
    private static String channel = "DEV.APP.SVRCONN"; // Channel name
    private static String login = "app"; // User name that application uses to connect to MQ
    private static String password = "passw0rd"; // Password that the application uses to connect to MQ
    private static String dataQueueName;// = "DEV.QUEUE.1"; // Queue that the application uses to put and get messages
    private static String notifyQueueName;// = "DEV.QUEUE.2";
    private static String queueManager;// = "QM1"; // Queue manager name
    private static String systemCode;
    private static String sourceSys;
    private static String targetSys;

    // System exit status value (assume unset value to be 1)
    private static int status = 1;

    // Create variables for the connection to MQ
//    private static String HOST = "_YOUR_HOSTNAME_"; // Host name or IP address
//    private static int PORT = 1414; // Listener port for your queue manager
//    private static String CHANNEL = "DEV.APP.SVRCONN"; // Channel name
//    private static String QMGR = "QM1"; // Queue manager name
//    private static String APP_USER = "app"; // User name that application uses to connect to MQ
//    private static String APP_PASSWORD = "_APP_PASSWORD_"; // Password that the application uses to connect to MQ
//    private static String QUEUE_NAME = "DEV.QUEUE.1"; // Queue that the application uses to put and get messages to and from

    // and from


    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args) {

        host = args[0];
        port = Integer.parseInt(args[1]);
        channel = args[2];
        login = args[3];
        password = args[4];
        dataQueueName = args[5];
        notifyQueueName = args[6];
        queueManager = args[7];
        systemCode = args[8];
        sourceSys = args[9];
        targetSys = args[10];

        boolean mqcsp = true;
        if (password.equals("")) {
            mqcsp = false;
        }

//        ////////////////////////////////////////////////////////

//        // Variables
        //JMSContext context = null;
        Destination destination = null;
        MessageProducer producer = null;
        JMSConsumer consumer = null;
        Session session = null;
        Connection connection = null;


        try {
            // Create a connection factory
            JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
            JmsConnectionFactory cf = ff.createConnectionFactory();

            // Set the properties
            cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
            cf.setIntProperty(WMQConstants.WMQ_PORT, port);
            cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
            cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
            cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, queueManager);
            cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsPutGet (JMS)");
            cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, mqcsp);
            cf.setStringProperty(WMQConstants.USERID, login);
            if(mqcsp) {
                cf.setStringProperty(WMQConstants.PASSWORD, password);
            }

            // Create JMS objects
            connection = cf.createConnection();
            session = connection.createSession();
//            destination = session.createQueue("queue:///" + dataQueueName);
//            producer = session.createProducer(destination);

            for (int i = 11; i < args.length; i += 5) {
                try {
                    DataMessage dataMessage = new DataMessage(systemCode, sourceSys, targetSys, args[i + 1], args[i + 2]
                            , args[i + 3], fileToBytes(new File(args[i])));
                    sendObject(dataMessage, session, dataQueueName);
                    DataNotification dataNotification = new DataNotification(systemCode, sourceSys, targetSys,
                            args[i + 1], args[i + 2], args[i + 4], args[i + 3]);
                    sendObject(dataNotification, session, notifyQueueName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

//            consumer = context.createConsumer(destination); // autoclosable
//            String receivedMessage = consumer.receiveBody(String.class, 15000); // in ms or 15 seconds
//
//            System.out.println("\nReceived message:\n" + receivedMessage);

            recordSuccess();
        } catch (JMSException jmsex) {
            recordFailure(jmsex);
        }

        System.exit(status);

    } // end main()

    /**
     * Record this run as successful.
     */
    private static void recordSuccess() {
        System.out.println("SUCCESS");
        status = 0;
        return;
    }

    /**
     * Record this run as failure.
     *
     * @param ex
     */
    private static void recordFailure(Exception ex) {
        if (ex != null) {
            if (ex instanceof JMSException) {
                processJMSException((JMSException) ex);
            } else {
                System.out.println(ex);
            }
        }
        System.out.println("FAILURE");
        status = -1;
        return;
    }

    /**
     * Process a JMSException and any associated inner exceptions.
     *
     * @param jmsex
     */
    private static void processJMSException(JMSException jmsex) {
        System.out.println(jmsex);
        Throwable innerException = jmsex.getLinkedException();
        if (innerException != null) {
            System.out.println("Inner exception(s):");
        }
        while (innerException != null) {
            System.out.println(innerException);
            innerException = innerException.getCause();
        }
        return;
    }

    private static void filesToMessages(List<File> files, Session session, MessageProducer producer) {

        byte[] fileContent;
        BytesMessage message;
        for (File file : files) {
            try {
                message = session.createBytesMessage();
                fileContent = fileToBytes(file);
                message.writeBytes(fileContent);
                producer.send(message);
                System.out.println("");
            } catch (JMSException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    private static byte[] fileToBytes(File file) throws IOException {
        byte[] fileContent = new byte[0];
        fileContent = Files.readAllBytes(file.toPath());

        return fileContent;
    }


    private static void sendObject(Serializable object, Session session, String queueName) {
        Destination destination = null;
        MessageProducer producer = null;
        try {
            destination = session.createQueue("queue:///" + queueName);
            producer = session.createProducer(destination);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        ObjectMessage message = null;
        try {
            message = session.createObjectMessage(object);
            producer.send(message);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}