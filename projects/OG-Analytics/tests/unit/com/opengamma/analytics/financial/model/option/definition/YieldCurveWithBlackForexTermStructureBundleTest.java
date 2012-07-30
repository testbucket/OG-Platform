/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.method.TestsDataSetsForex;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class YieldCurveWithBlackForexTermStructureBundleTest {
  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] NODES = new double[] {0.01, 0.50, 1.00, 2.01, 5.00};
  private static final double[] VOL = new double[] {0.20, 0.25, 0.20, 0.15, 0.20};
  private static final InterpolatedDoublesCurve TERM_STRUCTURE_VOL = InterpolatedDoublesCurve.fromSorted(NODES, VOL, LINEAR_FLAT);
  private static final BlackForexTermStructureParameters VOLS = new BlackForexTermStructureParameters(TERM_STRUCTURE_VOL);
  private static final Pair<Currency, Currency> CCYS = Pair.of(Currency.USD, Currency.EUR);
  private static final YieldCurveWithBlackForexTermStructureBundle FX_DATA = new YieldCurveWithBlackForexTermStructureBundle(CURVES, VOLS, CCYS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves() {
    new YieldCurveWithBlackForexTermStructureBundle(null, VOLS, CCYS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVols() {
    new YieldCurveWithBlackForexTermStructureBundle(CURVES, null, CCYS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencies() {
    new YieldCurveWithBlackForexTermStructureBundle(CURVES, VOLS, null);
  }

}