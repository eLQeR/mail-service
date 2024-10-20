# Postal Service Simulation

This project simulates the operations of a postal service where multiple senders send letters or packages, and a single postal worker processes them. The simulation utilizes multithreading, concurrency tools such as `Semaphore`, `CountDownLatch`, `ExecutorService`, and `ReentrantLock` to handle the interactions between senders and the postal worker.

## Features

- Multiple senders (threads) can send mail at the same time.
- A single postal worker processes the mail in parallel with the senders.
- The post office has working hours. Once it is closed, no new mail can be accepted.
- Synchronized access to resources is handled via a semaphore and locks.
- Graceful shutdown of all threads after the post office is closed.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 8 or higher**: [Download and install Java](https://www.oracle.com/java/technologies/javase-downloads.html)
- **Maven** (optional for dependency management and build): [Install Maven](https://maven.apache.org/install.html)

## How to Run

1. **Clone the repository** (if applicable):
   ```bash
   git clone https://github.com/eLQeR/mail-service.git
   cd mail-service
   ```
Compile the project: You can compile the project using the javac command:

```bash
javac -d out -sourcepath src src/PostalServiceSimulation.java
```
Run the simulation: Run the compiled Java program from the out directory:

```bash
java -cp out PostalServiceSimulation
```
You should see output similar to the following:

```yaml
Жов 20, 2024 12:34:56 PM PostWorker run
INFO: Працівник пошти обробляє пошту...
Жов 20, 2024 12:34:56 PM Sender run
INFO: Відправник
```