package org.evosuite.symbolic.dse.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSEExplorationException extends RuntimeException {

  private static final long serialVersionUID = 7112951484696975475L;

  private static final Logger LOGGER = LoggerFactory.getLogger(DSEExplorationException.class);

  public DSEExplorationException(final String msg) {
    super(msg);
  }

  public DSEExplorationException(final Throwable e) {
    super(e);
  }

  public static void check(final boolean expr, final String msg, final Object... msgArgs)
    throws DSEExplorationException {
    if (!expr) {
      final String finalMsg = String.format(msg, msgArgs);
      LOGGER.info(finalMsg);
      throw new DSEExplorationException(finalMsg);
    }
  }


  public static void propagateError(final Throwable t, final String msg, final Object... msgArgs)
    throws DSEExplorationException {
    final String finalMsg = String.format(msg, msgArgs);

    if (t == null) {
      LOGGER.info(finalMsg);
    } else {
      LOGGER.error(finalMsg, t);
    }

    throw new DSEExplorationException(finalMsg);
  }

  public static void propagateError(final String msg, final Object... msgArgs)
    throws DSEExplorationException {
    propagateError(null, msg, msgArgs);
  }
}