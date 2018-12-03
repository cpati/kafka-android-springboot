package com.plinz.photos;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
public class TopicSendReceive {
	private final static String TOPIC="upload-photos";
	private final static String BOOTSTRAP_SERVERS="localhost:9092";
	
	@Autowired
	SimpMessageSendingOperations simpMessageSendingOperations;
	
	public void startProducer(String message) {
		// Use Kafka producer to send the temperature data coming from Android device to Kafka server
		 System.out.printf("startProducer() called.\n");
		
		 // initialize Kafka producer
		 Properties props = new Properties();
		 props.put("bootstrap.servers", "localhost:9092"); // producer port 9092
		 props.put("acks", "all");
		 props.put("retries", 0);
		 props.put("batch.size", 16384);
		 props.put("linger.ms", 1);
		 props.put("buffer.memory", 33554432);
		 props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		 props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		 // send data to Kafka server
		 Producer<String, String> producer = new KafkaProducer<String,String>(props);
		 producer.send(new ProducerRecord<String, String>(TOPIC, message)); // send to topic "temperature"
		 producer.close();
	}
	
	private static Consumer<Long, String> createConsumer() {
	      final Properties props = new Properties();
	      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
	                                  BOOTSTRAP_SERVERS);
	      props.put(ConsumerConfig.GROUP_ID_CONFIG,
	                                  "KafkaExampleConsumer");
	      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
	              LongDeserializer.class.getName());
	      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
	              StringDeserializer.class.getName());

	      // Create the consumer using props.
	      final Consumer<Long, String> consumer =
	                                  new KafkaConsumer<Long,String>(props);

	      // Subscribe to the topic.
	      consumer.subscribe(Collections.singletonList(TOPIC));
	      return consumer;
	  }
	
	@EventListener(ApplicationReadyEvent.class)
	 void runConsumer() throws InterruptedException {
        final Consumer<Long, String> consumer = createConsumer();

        final int giveUp = 100;   int noRecordsCount = 0;

        while (true) {
            final ConsumerRecords<Long, String> consumerRecords =
                    consumer.poll(1000);

            if (consumerRecords.count()==0) {
                noRecordsCount++;
                if (noRecordsCount > giveUp) break;
                else continue;
            }
            
            for(ConsumerRecord<Long, String> record:consumerRecords) {
            	System.out.printf("Consumer Record:(%d, %s, %d, %d)\n",
                        record.key(), record.value(),
                        record.partition(), record.offset());
            	try {
					//photosController.sendGreeting(record.value());
					 simpMessageSendingOperations.convertAndSend("/topic/upload-photos", record.value());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }

            consumer.commitAsync();
        }
        consumer.close();
        System.out.println("DONE");
    }
}
