/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import javax.time.Instant;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ConfigDBForwardSwapCurveDefinitionSource implements ForwardCurveDefinitionSource {
  private static final String SUFFIX = "_FORWARD_SWAP";
  private final ConfigSource _configSource;

  public ConfigDBForwardSwapCurveDefinitionSource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "config source");
    _configSource = configSource;
  }

  @Override
  public ForwardSwapCurveDefinition getDefinition(final String name, final String currency) {
    return _configSource.getLatestByName(ForwardSwapCurveDefinition.class, name + "_" + currency + SUFFIX);
  }

  @Override
  public ForwardSwapCurveDefinition getDefinition(final String name, final String currency, final Instant version) {
    return _configSource.getByName(ForwardSwapCurveDefinition.class, name + "_" + currency + SUFFIX, version);
  }
}
