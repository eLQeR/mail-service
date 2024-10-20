import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

class Sender implements Runnable {
    private final String name;
    private final PostOffice postOffice;
    private static final Logger logger = Logger.getLogger(Sender.class.getName());

    public Sender(String name, PostOffice postOffice) {
        this.name = name;
        this.postOffice = postOffice;
    }

    @Override
    public void run() {
        try {
            // Спробувати відправити посилку
            postOffice.sendMail(name);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, name + " був перерваний під час відправки.", e);
            Thread.currentThread().interrupt();
        }
    }
}

class PostWorker implements Runnable {
    private final PostOffice postOffice;
    private static final Logger logger = Logger.getLogger(PostWorker.class.getName());

    public PostWorker(PostOffice postOffice) {
        this.postOffice = postOffice;
    }

    @Override
    public void run() {
        try {
            postOffice.processMails();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Працівник пошти був перерваний.", e);
            Thread.currentThread().interrupt();
        }
    }
}

class PostOffice {
    private final Semaphore semaphore = new Semaphore(1);
    private final CountDownLatch closingLatch;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean isOpen = true;
    private static final Logger logger = Logger.getLogger(PostOffice.class.getName());

    public PostOffice(int senderCount) {
        this.closingLatch = new CountDownLatch(senderCount); // Синхронізація закриття
    }

    // Метод для відправки листа
    public void sendMail(String senderName) throws InterruptedException {
        if (!isOpen) {
            logger.log(Level.INFO, senderName + " не може відправити листа, оскільки пошта закрита.");
            return;
        }

        // Отримання доступ до семафору
        semaphore.acquire();
        try {
            logger.log(Level.INFO, senderName + " відправляє листа...");
            Thread.sleep(2000);
            logger.log(Level.INFO, senderName + " листа успішно відправлено.");
        } finally {
            semaphore.release(); // Звільнення семафору
            closingLatch.countDown();
        }
    }

    // Метод для обробки пошти
    public void processMails() throws InterruptedException {
        while (isOpen || closingLatch.getCount() > 0) {
            if (lock.tryLock()) {
                try {
                    logger.log(Level.INFO, "Працівник пошти обробляє пошту...");
                    Thread.sleep(3000); // Симуляція часу обробки
                } finally {
                    lock.unlock();
                }
            } else {
                // Очікування наступного клієнта
                Thread.sleep(500);
            }
        }
        logger.log(Level.INFO, "Працівник пошти закінчує роботу.");
    }

    // Метод для закриття пошти
    public void closePostOffice() {
        isOpen = false;
        logger.log(Level.INFO, "Пошта закрита для нових відправлень.");
    }

    public void awaitCompletion() throws InterruptedException {
        closingLatch.await(); // Чекати завершення всіх процесів
    }
}

// Головний клас програми
public class PostalServiceSimulation {
    private static final int SENDER_COUNT = 3;
    private static final ExecutorService executor = Executors.newFixedThreadPool(SENDER_COUNT + 1);
    private static final Logger logger = Logger.getLogger(PostalServiceSimulation.class.getName());

    public static void main(String[] args) {
        PostOffice postOffice = new PostOffice(SENDER_COUNT);

        // Запуск працівника пошти
        executor.submit(new PostWorker(postOffice));

        // Запуск потоків для відправників
        for (int i = 1; i <= SENDER_COUNT; i++) {
            executor.submit(new Sender("Відправник " + i, postOffice));
        }

        try {
            Thread.sleep(5000); // Пошта працює 5 секунд
            postOffice.closePostOffice();
            postOffice.awaitCompletion(); // Чекаємо завершення всіх відправників
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Головний потік був перерваний.", e);
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown(); // Завершення роботи всіх потоків
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Закриття потоків було перерване.", e);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        logger.log(Level.INFO, "Симуляція завершена.");
    }
}
