package si.sunesis.xflex.xflexkafkasample;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import si.sunesis.xflex.xsd.OrderDirectionType;
import si.sunesis.xflex.xsd.OrderTypeType;
import si.sunesis.xflex.xsd.XFLEXOrderISPType;
import si.sunesis.xflex.xsd.XFLEXOrderType;

import javax.xml.bind.JAXB;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * A sample that demonstrates producing an XFLEXOrder to Kafka. KAFKA_PROPERTIES should be adjusted to match the
 * configuration of the Kafka broker that should receive the XFLEXOrder payload.
 *
 * @since 1.0.0
 */
public class XflexKafkaSample {

    private static final Logger LOG = LogManager.getLogger(XflexKafkaSample.class.getName());

    private static final String KAFKA_TOPIC_NAME = "marketflex.localflexibility.input.bids.0";
    private static final Properties KAFKA_PROPERTIES;

    static {
        KAFKA_PROPERTIES = new Properties();

        KAFKA_PROPERTIES.put("bootstrap.servers", "localhost:9092");
        KAFKA_PROPERTIES.put("acks", "all");
        KAFKA_PROPERTIES.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KAFKA_PROPERTIES.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }

    public static void main(String[] args) {
        new XflexKafkaSample().runSample();
    }

    private final Producer<String, String> kafkaProducer;

    public XflexKafkaSample() {
        kafkaProducer = new KafkaProducer<>(KAFKA_PROPERTIES);
    }

    public void runSample() {

        XFLEXOrderType xflexOrder = this.createMockOrder();

        String xml = this.marshalXmlToString(xflexOrder);

        LOG.info("Producing xml to Kafka: \n" + xml);

        try {

            RecordMetadata recordMetadata = this.produceToKafka(xml);
            LOG.info("Produced to partition " + recordMetadata.partition() + " and offset " + recordMetadata.offset());

        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Error when producing to Kafka", e);
        }
    }

    private RecordMetadata produceToKafka(String payload) throws ExecutionException, InterruptedException {

        return this.kafkaProducer.send(new ProducerRecord<>(KAFKA_TOPIC_NAME, payload)).get();
    }

    private String marshalXmlToString(Object jaxbObject) {

        StringWriter buffer = new StringWriter();
        JAXB.marshal(jaxbObject, buffer);

        return buffer.toString();
    }

    private XFLEXOrderType createMockOrder() {

        XFLEXOrderType xflexOrder = new XFLEXOrderType();

        xflexOrder.setFlexOfferMessageID("id-12345");
        xflexOrder.setTimeZone("Europe/Ljubljana");
        xflexOrder.setOrderDirection(OrderDirectionType.UPWARD);
        xflexOrder.setActivationFactor(BigDecimal.ONE);
        xflexOrder.setOrderType(OrderTypeType.REGULAR_LIMIT_ORDER);
        xflexOrder.setPrice(BigDecimal.TEN);
        xflexOrder.setCurrency("EUR");

        XFLEXOrderISPType isp1 = new XFLEXOrderISPType();
        isp1.setPower(BigInteger.TEN);
        isp1.setStart(BigInteger.ONE);
        isp1.setDuration(BigInteger.TEN);
        isp1.setIsDivisible(true);
        xflexOrder.getISP().add(isp1);

        XFLEXOrderISPType isp2 = new XFLEXOrderISPType();
        isp2.setPower(BigInteger.TEN);
        isp2.setStart(BigInteger.ZERO);
        isp2.setDuration(BigInteger.TWO);
        isp2.setIsDivisible(false);
        xflexOrder.getISP().add(isp2);

        return xflexOrder;
    }
}
