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

### 2️⃣ Install Dependencies

If using Maven, install dependencies by running:
mvn clean install

### 3️⃣ Run the Crawler

To download books and store them in the datalake:
java -cp target/QueryEngine.jar org.example.CrawlerMain

### 4️⃣ Run the Indexer

To process and index books:
java -cp target/QueryEngine.jar org.example.IndexerMain

### 5️⃣ Start the Query Engine

To start the query engine:
java -cp target/QueryEngine.jar org.example.QueryEngine



## 📜 License

This project is licensed under the GNU V3 licence.

## 🤝 Contributing

Want to contribute? Feel free to fork this repository and submit pull requests!

## 📬 Contact

For any questions or contributions, contact `luisgservsp@gmail.com` or open an issue on GitHub.
