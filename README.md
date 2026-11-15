
# UPI Without Internet Mesh – Secure Offline Payment Simulation

A Java Spring Boot backend project that demonstrates **offline payment transmission using a Bluetooth-style mesh network**, hybrid encryption, idempotent transaction processing, and deferred payment settlement.

The system simulates a scenario where users can initiate payments without an active internet connection. Payment packets are encrypted and transferred between nearby virtual devices until an internet-connected bridge device uploads them to the backend for verification and settlement.

The project demonstrates how secure payment processing can be designed using distributed communication concepts, cryptography, and concurrency handling.

> **Note:** This is an educational simulation of mesh-routed deferred settlement, not a production implementation of real-time offline UPI. Bluetooth communication and banking infrastructure are simulated.

---

## 🚀 Key Features

- **Offline Payment Simulation:** Simulates payment transmission between devices without direct internet connectivity.
- **Hybrid Encryption:** Uses RSA-OAEP and AES-256-GCM to protect payment information.
- **Mesh Network Simulation:** Implements a gossip-based packet-sharing mechanism between virtual devices.
- **Bridge Node Communication:** Simulates internet-connected devices forwarding encrypted payment packets to the backend.
- **Idempotent Transaction Processing:** Prevents duplicate payments using SHA-256 hashing and atomic operations.
- **Secure Packet Verification:** Detects tampered ciphertext using AES-GCM authentication.
- **Replay Protection:** Implements timestamp-based freshness validation and unique nonces.
- **Transactional Settlement:** Processes sender debit, receiver credit, and transaction ledger updates within a single database transaction.
- **Concurrency Handling:** Uses ConcurrentHashMap and optimistic locking to handle concurrent payment submissions.
- **Interactive Dashboard:** Provides a dashboard to simulate payments, gossip rounds, bridge uploads, and transaction monitoring.
- **Automated Testing:** Includes tests for encryption, ciphertext tampering, and concurrent duplicate payment processing.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Java 17 | Core programming language |
| Spring Boot 3.3 | Backend application framework |
| Spring MVC | REST API development |
| Spring Data JPA | Database access |
| Hibernate | ORM framework |
| H2 Database | In-memory database |
| Maven | Dependency management |
| RSA-OAEP | AES key encryption |
| AES-256-GCM | Authenticated data encryption |
| SHA-256 | Ciphertext hashing |
| ConcurrentHashMap | Thread-safe idempotency handling |
| JUnit | Unit and concurrency testing |
| HTML / CSS / JavaScript | Interactive dashboard |

## 🏗️ System Architecture

The application follows a layered architecture with separate components for cryptography, business logic, database operations, and HTTP communication.

### Payment Processing Workflow

1. A sender initiates a payment through the simulated application.
2. The backend creates a payment instruction containing sender, receiver, amount, nonce, and timestamp.
3. The payment instruction is encrypted using hybrid encryption.
4. The encrypted packet is injected into the virtual mesh network.
5. Virtual devices exchange packets through gossip rounds.
6. An internet-connected bridge device uploads the packet to the backend.
7. The backend verifies the packet, checks idempotency, decrypts the payload, and validates its freshness.
8. The settlement service processes the debit and credit operations.
9. The transaction ledger is updated after successful settlement.

### Architecture Diagram

```text
       SENDER DEVICE
             |
             v
     Payment Instruction
             |
             v
      Hybrid Encryption
       RSA + AES-GCM
             |
             v
       Encrypted Packet
             |
             v
      VIRTUAL MESH NETWORK
             |
       Gossip Protocol
             |
             v
       BRIDGE DEVICE
        (Internet)
             |
             v
      SPRING BOOT BACKEND
             |
             v
       Bridge Ingestion
             |
             v
       SHA-256 Hashing
             |
             v
      Idempotency Check
             |
             v
       Packet Decryption
             |
             v
       Freshness Check
             |
             v
     Transaction Settlement
             |
       +-----+-----+
       |           |
       v           v
   Debit Sender  Credit Receiver
       |           |
       +-----+-----+
             |
             v
      TRANSACTION LEDGER
```

## 🔐 Core Technical Implementations

### 1. Hybrid Encryption

Implements hybrid cryptography to secure payment packets during transmission.

- Generates a fresh AES-256 key for each packet.
- Encrypts payment data using AES-256-GCM.
- Encrypts the AES key using RSA-OAEP.
- Uses an initialization vector (IV) for authenticated encryption.
- Verifies ciphertext integrity during decryption.

This approach ensures that intermediate devices can forward encrypted packets without accessing the underlying payment information.

### 2. Idempotency Management

Prevents duplicate payment processing when the same packet reaches the backend multiple times.

- Generates a SHA-256 hash of the encrypted packet.
- Uses `ConcurrentHashMap.putIfAbsent()` for atomic packet claiming.
- Rejects duplicate submissions before decryption and settlement.
- Implements TTL-based expiration of cached packet hashes.
- Uses a unique database constraint on the transaction packet hash as an additional safeguard.

### 3. Replay Attack Prevention

Implements two mechanisms to reduce replay attacks:

- **Timestamp Validation:** Rejects payment instructions older than the configured 24-hour freshness window.
- **Unique Nonce:** Assigns a unique UUID to each payment instruction to distinguish separate transactions.

### 4. Transaction Management

Uses Spring's `@Transactional` annotation to ensure that related settlement operations are executed as a single database transaction.

- Debits the sender's account.
- Credits the receiver's account.
- Records the transaction in the ledger.
- Uses optimistic locking with JPA's `@Version` to help prevent conflicting concurrent account updates.

### 5. Mesh Network Simulation

Implements a software-based mesh network using virtual devices.

- Simulates multiple devices participating in the network.
- Uses gossip rounds to distribute encrypted packets.
- Decrements packet TTL during transmission.
- Identifies bridge devices with internet connectivity.
- Simulates packet forwarding from bridge devices to backend APIs.

## 📂 Project Structure

```text
upi-offline-mesh/
│
├── src/
│   ├── main/
│   │   ├── java/com/demo/upimesh/
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── Account.java
│   │   │   │   ├── AccountRepository.java
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   ├── MeshPacket.java
│   │   │   │   └── PaymentInstruction.java
│   │   │   │
│   │   │   ├── crypto/
│   │   │   │   ├── ServerKeyHolder.java
│   │   │   │   └── HybridCryptoService.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── DemoService.java
│   │   │   │   ├── VirtualDevice.java
│   │   │   │   ├── MeshSimulatorService.java
│   │   │   │   ├── IdempotencyService.java
│   │   │   │   ├── SettlementService.java
│   │   │   │   └── BridgeIngestionService.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── ApiController.java
│   │   │   │   └── DashboardController.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   └── AppConfig.java
│   │   │   │
│   │   │   └── UpiMeshApplication.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   │           └── dashboard.html
│   │
│   └── test/
│       └── java/com/demo/upimesh/
│           └── IdempotencyConcurrencyTest.java
│
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## ⚙️ Getting Started

Follow these instructions to run the project locally.

### Prerequisites

Make sure you have installed:

- Java JDK 17 or later
- Git
- IntelliJ IDEA / Eclipse / VS Code

**Note:** The project uses an H2 in-memory database, so a separate database installation is not required.

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR-USERNAME/UPI_Without_Internet.git
```

### 2. Navigate to the Project Directory

```bash
cd UPI_Without_Internet
```

### 3. Run the Application

**Windows:**

```bash
mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

### 4. Access the Dashboard

Once the application starts successfully, open:

```text
http://localhost:8080
```

You can now interact with the dashboard and simulate the complete offline payment workflow.

## 🔗 REST API Reference

The application exposes REST endpoints for payment processing, mesh simulation, and transaction monitoring.

| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | Displays the interactive dashboard |
| GET | `/api/server-key` | Retrieves the server's RSA public key |
| GET | `/api/accounts` | Retrieves account details and balances |
| GET | `/api/transactions` | Retrieves recent transactions |
| GET | `/api/mesh/state` | Retrieves virtual device states |
| POST | `/api/demo/send` | Simulates payment creation and packet injection |
| POST | `/api/mesh/gossip` | Executes a mesh gossip round |
| POST | `/api/mesh/flush` | Uploads packets through bridge devices |
| POST | `/api/mesh/reset` | Resets the mesh and idempotency cache |
| POST | `/api/bridge/ingest` | Processes incoming encrypted payment packets |

## 🧪 Testing

Run the test suite using Maven:

**Windows:**

```bash
mvnw.cmd test
```

**Linux / macOS:**

```bash
./mvnw test
```

### Test Cases

The project includes tests covering important payment-processing scenarios:

| Test Case | Description |
|---|---|
| `encryptDecryptRoundTrip` | Verifies successful encryption and decryption |
| `tamperedCiphertextIsRejected` | Verifies rejection of modified encrypted packets |
| `singlePacketDeliveredByThreeBridgesSettlesExactlyOnce` | Verifies that concurrent duplicate submissions result in only one successful settlement |

### Run the Concurrency Test Individually

```bash
mvnw.cmd test -Dtest=IdempotencyConcurrencyTest#singlePacketDeliveredByThreeBridgesSettlesExactlyOnce
```

## ⚠️ Current Limitations

This project is an educational prototype and is not intended for real financial transactions.

Some components are simulated and would require additional infrastructure for production deployment.

| Current Implementation | Potential Production Approach |
|---|---|
| H2 in-memory database | PostgreSQL / MySQL |
| ConcurrentHashMap idempotency cache | Redis with atomic SET NX EX |
| Runtime-generated RSA key pair | Secure key management using HSM or KMS |
| Virtual mesh devices | Bluetooth Low Energy or Wi-Fi Direct |
| Simulated payment sender | Dedicated Android application |
| Simulated account balances | Integration with authorized banking infrastructure |
| No bridge authentication | Mutual TLS or signed bridge credentials |
| Console logging | Centralized structured logging and monitoring |

**Important considerations:**

- Offline payment initiation does not guarantee that the sender has sufficient funds at settlement time.
- Multiple offline payments may create double-spending risks.
- Real Bluetooth communication requires additional mobile-device integration.
- Production deployment would require proper authentication, authorization, key management, fraud prevention, and regulatory compliance.

## 🎯 Learning Outcomes

This project provides practical exposure to several backend engineering concepts:

- Building RESTful applications using Spring Boot.
- Implementing cryptographic operations in Java.
- Understanding RSA and AES encryption.
- Working with SHA-256 hashing.
- Implementing thread-safe operations using ConcurrentHashMap.
- Understanding idempotency in distributed systems.
- Handling concurrent transaction requests.
- Managing database transactions using Spring Data JPA.
- Implementing optimistic locking.
- Designing service-oriented and layered architectures.
- Writing unit and concurrency tests.

## 👨‍💻 Author

**Md Arman Ansari**

- GitHub: https://github.com/arman23-dev
- LinkedIn: https://www.linkedin.com/in/armaanansari23

---

If you find this project interesting, consider giving the repository a ⭐.

**Built with Java and Spring Boot.**
