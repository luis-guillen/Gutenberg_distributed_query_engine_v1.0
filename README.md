# 🚀 Gutenberg Distributed Query Engine

**Gutenberg Distributed Query Engine** is a distributed query processing system designed to handle large-scale text data efficiently. It integrates Hazelcast for distributed data storage and processing, supports indexing, and enables efficient retrieval of book content from a datalake.

## 📌 Features
- 🔍 **Distributed text processing** using Hazelcast.
- 📚 **Book indexing and retrieval** from a datalake.
- 🗂️ **Data partitioning** to improve query performance.
- 💾 **Data persistence** for both structured and unstructured datasets.
- ⚡ **Parallel execution** to handle large volumes of data efficiently.

## 🔧 Setup and Installation

### 1️⃣ Clone the Repository
git clone https://github.com/your-username/Gutenberg-Distributed-Query-Engine.git
cd Gutenberg-Distributed-Query-Engine

2️⃣ Install Dependencies

If using Maven, install dependencies by running:

mvn clean install

3️⃣ Configure the Application

Modify application.properties and hazelcast.properties in src/main/resources to set up:
	•	Datalake path (datalake.path)
	•	Datamart path (datamart.path)
	•	Hazelcast cluster configuration

4️⃣ Run the Crawler

To download books and store them in the datalake:

java -cp target/QueryEngine.jar org.example.CrawlerMain

5️⃣ Run the Indexer

To process and index books:

java -cp target/QueryEngine.jar org.example.IndexerMain

6️⃣ Start the Query Engine

To start the query engine:

java -cp target/QueryEngine.jar org.example.QueryEngine

7️⃣ Run Hazelcast Server

To start the distributed processing server:

java -cp target/Server.jar Server.ServerMain

⚙️ Configuration Files

application.properties

datalake.path=src/main/resources/datalake_storage
datamart.path=src/main/resources/datamart
hazelcast.cluster.name=dev
hazelcast.members=172.20.10.11:5701,172.20.10.10:5701,172.20.10.12:5701

hazelcast.properties

hazelcast.logging.type=slf4j
hazelcast.network.port=5701
hazelcast.network.port.auto.increment=true
hazelcast.discovery.multicast.enabled=false
hazelcast.discovery.tcpip.enabled=true

🚀 Future Improvements
	•	✅ Support for additional query processing techniques.
	•	✅ Integration with Apache Spark for large-scale data analytics.
	•	✅ Optimized memory management for handling larger datasets.

📜 License

This project is licensed under the MIT License.

🤝 Contributing

Want to contribute? Feel free to fork this repository and submit pull requests!

📬 Contact

For any questions or contributions, contact your-email@example.com or open an issue on GitHub.
