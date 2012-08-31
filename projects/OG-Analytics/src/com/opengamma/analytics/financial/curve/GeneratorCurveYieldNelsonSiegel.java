/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.DoublesCurveNelsonSiegel;
import com.opengamma.util.ArgumentChecker;

/**
 * Store the details and generate the required curve. The yield (continuously compounded) is generated by a Nelson-Siegel function.
 * <p> Reference: Nelson, C.R., Siegel, A.F. (1987). Parsimonious modeling of yield curves, Journal of Business, 60(4):473-489.
 */
public class GeneratorCurveYieldNelsonSiegel extends GeneratorCurve {

  /**
   * The number of parameters of the curve.
   */
  private static final int NB_PARAMETERS = 4;

  @Override
  public int getNumberOfParameter() {
    return NB_PARAMETERS;
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, double[] parameters) {
    ArgumentChecker.isTrue(parameters.length == NB_PARAMETERS, "Nelson-Siegel should have 4 parameters");
    return new YieldCurve(name, new DoublesCurveNelsonSiegel(name, parameters));
  }

  @Override
  public YieldAndDiscountCurve generateCurve(String name, YieldCurveBundle bundle, double[] parameters) {
    return generateCurve(name, parameters);
  }

}
