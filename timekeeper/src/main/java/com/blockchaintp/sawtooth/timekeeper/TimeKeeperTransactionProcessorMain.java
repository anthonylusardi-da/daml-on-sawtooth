package com.blockchaintp.sawtooth.timekeeper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.blockchaintp.sawtooth.timekeeper.processor.TimeKeeperTransactionHandler;
import com.blockchaintp.utils.InMemoryKeyManager;
import com.blockchaintp.utils.KeyManager;

import sawtooth.sdk.messaging.Stream;
import sawtooth.sdk.messaging.ZmqStream;
import sawtooth.sdk.processor.TransactionHandler;
import sawtooth.sdk.processor.TransactionProcessor;

/**
 * A basic Main class for TimeKeeperTransactionProcessor.
 * @author scealiontach
 */
public final class TimeKeeperTransactionProcessorMain {

  private static final Logger LOGGER = Logger.getLogger(TimeKeeperTransactionProcessorMain.class.getName());

  /**
   * A basic main method for this transaction processor.
   * @param args at this time only one argument the address of the validator
   *             component endpoint, e.g. tcp://localhost:4004
   */
  public static void main(final String[] args) {
    ScheduledExecutorService clockExecutor = Executors.newSingleThreadScheduledExecutor();

    Stream stream = new ZmqStream(args[0]);
    KeyManager kmgr = InMemoryKeyManager.createSECP256k1();
    final long period = 1;
    final TimeUnit periodUnit = TimeUnit.MINUTES;
    clockExecutor.scheduleWithFixedDelay(new TimeKeeperRunnable(kmgr, stream), period, period, periodUnit);

    TransactionProcessor transactionProcessor = new TransactionProcessor(args[0]);
    TransactionHandler handler = new TimeKeeperTransactionHandler();
    transactionProcessor.addHandler(handler);

    Thread thread = new Thread(transactionProcessor);
    thread.start();
    try {
      thread.join();
      clockExecutor.shutdownNow();
    } catch (InterruptedException exc) {
      LOGGER.warning("TransactionProcessor was interrupted");
    }
  }

  private TimeKeeperTransactionProcessorMain() {
    // private constructor for utility class
  }

}