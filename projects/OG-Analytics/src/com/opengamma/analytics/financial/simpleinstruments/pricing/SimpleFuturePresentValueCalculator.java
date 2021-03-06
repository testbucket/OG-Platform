/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.simpleinstruments.pricing;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFXFuture;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleFuture;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleInstrument;
import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleInstrumentVisitor;
import com.opengamma.util.money.CurrencyAmount;

/**
 * 
 */
public class SimpleFuturePresentValueCalculator implements SimpleInstrumentVisitor<SimpleFutureDataBundle, CurrencyAmount> {

  @Override
  public CurrencyAmount visit(final SimpleInstrument derivative, final SimpleFutureDataBundle data) {
    Validate.notNull(derivative, "derivative");
    Validate.notNull(data, "data");
    return derivative.accept(this, data);
  }

  @Override
  public CurrencyAmount visitSimpleFuture(final SimpleFuture future, final SimpleFutureDataBundle data) {
    final double t = future.getExpiry();    
    return CurrencyAmount.of(future.getCurrency(), future.getUnitAmount() * data.getSpot() * Math.exp(t * (data.getCurve().getInterestRate(t) - data.getCostOfCarry())));
  }

  @Override
  public CurrencyAmount visitSimpleFXFuture(final SimpleFXFuture future, final SimpleFutureDataBundle data) {
    throw new UnsupportedOperationException("Cannot price simple FX future with this calculator");
  }

  @Override
  public CurrencyAmount visit(final SimpleInstrument derivative) {
    throw new UnsupportedOperationException("Cannot price simple future without data");
  }

  @Override
  public CurrencyAmount visitSimpleFuture(final SimpleFuture future) {
    throw new UnsupportedOperationException("Cannot price simple future without data");
  }

  @Override
  public CurrencyAmount visitSimpleFXFuture(final SimpleFXFuture future) {
    throw new UnsupportedOperationException("Cannot price simple future without data");
  }

}
