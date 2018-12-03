# kafka-android-springboot



•	Download kafka:
  - wget http://mirrors.sorengard.com/apache/kafka/2.1.0/kafka_2.12-2.1.0.tgz
  - tar -xvzf kafka_2.12-2.1.0.tgz
	- cd kafka_2.12-2.1.0
•	Start zoo keeper:
  - ./bin/zookeeper-server-start.sh ./config/zookeeper.properties
•	Start Kafka broker Server:
  - ./bin/kafka-server-start.sh config/server.properties
•	Create Topic:
  - ./bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic upload-photos
•	List Topic:
  - ./bin/kafka-topics.sh --list --zookeeper localhost:2181 
•	Console Producer:
  - ./bin/kafka-console-producer.sh --broker-list localhost:9092 --topic upload-photos
•	Console Consumer:
  - ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic upload-photos --from-beginning
