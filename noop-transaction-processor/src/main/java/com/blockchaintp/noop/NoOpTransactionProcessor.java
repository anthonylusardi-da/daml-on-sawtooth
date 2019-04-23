package com.blockchaintp.noop;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import sawtooth.sdk.processor.TransactionProcessor;;

/**
 * A transaction processor which blindly accepts or denies transactions without
 * regard to their contents.
 * @author scealiontach
 */
public class NoOpTransactionProcessor {
  private static final int ACCEPT_ALL = 1;
  @SuppressWarnings("unused")
  private static final int INVALID_ALL = 2;
  private static final int ERROR_ALL = 3;

  protected NoOpTransactionProcessor() {

  }

  private static CommandLine parseArgs(final String[] args) throws ParseException {
    Options options = new Options();
    Option validatorOption = Option.builder("c").required(true).longOpt("connect").hasArg().type(String.class)
        .argName("validator-address").desc("Address and port to connect to the validator, e.g. tcp://localhost:4004")
        .build();
    Option familyNameOption = Option.builder("n").required(true).longOpt("family-name").hasArg().type(String.class)
        .argName("family-name").desc("Family Name which this TP will handle").build();
    Option familyVersionOption = Option.builder("f").required().longOpt("family-version").hasArg().type(String.class)
        .argName("family-version").desc("Version of the family this TP will handle").build();
    Option strategyOption = Option.builder("s").required(false).longOpt("strategy").argName("noop-strategy")
        .type(Integer.class)
        .desc("1=Accept All transactions, 2=Reject all transactions, 3=InternalError all transactions").build();
    options.addOption(validatorOption).addOption(familyNameOption).addOption(familyVersionOption)
        .addOption(strategyOption);
    CommandLineParser parser = new DefaultParser();
    return parser.parse(options, args);
  }

  /**
   * The usual main method.
   * @param args execute via the command line for argument syntax
   */
  public static void main(final String[] args) {
    try {
      CommandLine cli = parseArgs(args);
      String validatorAddress = cli.getOptionValue("validator-address");
      TransactionProcessor processor = new TransactionProcessor(validatorAddress);

      String namespace = cli.getOptionValue("family-namespace", "noop");
      String version = cli.getOptionValue("family-version", "1.0");

      Integer strategy = (Integer) cli.getParsedOptionValue("noop-strategy");
      if (strategy > ERROR_ALL || strategy < ACCEPT_ALL) {
        throw new RuntimeException("Strategy must be 1, 2, or 3");
      }

      NoOpTransactionHandler transactionHandler = new NoOpTransactionHandler(namespace, version, strategy);
      processor.addHandler(transactionHandler);
      Thread t = new Thread(processor);
      t.start();

    } catch (ParseException e) {
      e.printStackTrace();
    }

  }

}
