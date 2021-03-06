/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local.defaultproperties;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.volatility.local.LocalVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.local.PDEPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class BackwardPDEDefaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENT_NAMES = new String[] {
    ValueRequirementNames.FORWARD_DELTA,
    ValueRequirementNames.DUAL_DELTA,
    ValueRequirementNames.DUAL_GAMMA,
    ValueRequirementNames.FORWARD_GAMMA,
    ValueRequirementNames.FOREX_DOMESTIC_PRICE,
    ValueRequirementNames.FOREX_PV_QUOTES,
    ValueRequirementNames.FORWARD_VEGA,
    ValueRequirementNames.FORWARD_VOMMA,
    ValueRequirementNames.FORWARD_VANNA,
    ValueRequirementNames.IMPLIED_VOLATILITY
  };
  private final String _timeAxis;
  private final String _yAxis;
  private final String _volatilityTransform;
  private final String _timeInterpolator;
  private final String _timeLeftExtrapolator;
  private final String _timeRightExtrapolator;
  private final String _forwardCurveName;
  private final String _forwardCurveCalculationMethod;
  private final String _surfaceName;
  private final String _eps;
  private final String _theta;
  private final String _nTimeSteps;
  private final String _nSpaceSteps;
  private final String _timeStepBunching;
  private final String _spaceStepBunching;
  private final String _maxMoneynessScale;
  private final String _spaceDirectionInterpolator;
  private final String _discountingCurveName;

  public BackwardPDEDefaults(final String timeAxis, final String yAxis, final String volatilityTransform, final String timeInterpolator,
      final String timeLeftExtrapolator, final String timeRightExtrapolator, final String forwardCurveName, final String forwardCurveCalculationMethod, final String surfaceName,
      final String eps, final String theta, final String nTimeSteps, final String nSpaceSteps, final String timeStepBunching, final String spaceStepBunching,
      final String maxMoneynessScale, final String spaceDirectionInterpolator, final String discountingCurveName) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(timeAxis, "time axis");
    ArgumentChecker.notNull(yAxis, "y axis");
    ArgumentChecker.notNull(volatilityTransform, "volatility transform");
    ArgumentChecker.notNull(timeInterpolator, "time interpolator");
    ArgumentChecker.notNull(timeLeftExtrapolator, "time left extrapolator");
    ArgumentChecker.notNull(timeRightExtrapolator, "time right extrapolator");
    ArgumentChecker.notNull(forwardCurveName, "forward curve name");
    ArgumentChecker.notNull(forwardCurveCalculationMethod, "forward curve calculation method");
    ArgumentChecker.notNull(surfaceName, "surface name");
    ArgumentChecker.notNull(eps, "eps");
    ArgumentChecker.notNull(theta, "theta");
    ArgumentChecker.notNull(nTimeSteps, "number of time steps");
    ArgumentChecker.notNull(nSpaceSteps, "number of space steps");
    ArgumentChecker.notNull(timeStepBunching, "time step bunching");
    ArgumentChecker.notNull(spaceStepBunching, "space step bunching");
    ArgumentChecker.notNull(maxMoneynessScale, "max moneyness scale");
    ArgumentChecker.notNull(spaceDirectionInterpolator, "space direction interpolator");
    ArgumentChecker.notNull(discountingCurveName, "discounting curve name");
    _forwardCurveName = forwardCurveName;
    _forwardCurveCalculationMethod = forwardCurveCalculationMethod;
    _surfaceName = surfaceName;
    _timeAxis = timeAxis;
    _yAxis = yAxis;
    _volatilityTransform = volatilityTransform;
    _timeInterpolator = timeInterpolator;
    _timeLeftExtrapolator = timeLeftExtrapolator;
    _timeRightExtrapolator = timeRightExtrapolator;
    _eps = eps;
    _theta = theta;
    _nTimeSteps = nTimeSteps;
    _nSpaceSteps = nSpaceSteps;
    _timeStepBunching = timeStepBunching;
    _spaceStepBunching = spaceStepBunching;
    _maxMoneynessScale = maxMoneynessScale;
    _spaceDirectionInterpolator = spaceDirectionInterpolator;
    _discountingCurveName = discountingCurveName;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENT_NAMES) {
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_AXIS);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_VOLATILITY_TRANSFORM);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_INTERPOLATOR);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_LEFT_EXTRAPOLATOR);
      defaults.addValuePropertyName(valueRequirement, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_RIGHT_EXTRAPOLATOR);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueRequirement, LocalVolatilitySurfacePropertyNamesAndValues.PROPERTY_DERIVATIVE_EPS);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_DISCOUNTING_CURVE_NAME);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_MAX_MONEYNESS);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_NUMBER_SPACE_STEPS);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_NUMBER_TIME_STEPS);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_PDE_DIRECTION);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_SPACE_DIRECTION_INTERPOLATOR);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_SPACE_STEPS_BUNCHING);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_THETA);
      defaults.addValuePropertyName(valueRequirement, PDEPropertyNamesAndValues.PROPERTY_TIME_STEP_BUNCHING);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_AXIS.equals(propertyName)) {
      return Collections.singleton(_timeAxis);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_Y_AXIS.equals(propertyName)) {
      return Collections.singleton(_yAxis);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_VOLATILITY_TRANSFORM.equals(propertyName)) {
      return Collections.singleton(_volatilityTransform);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeInterpolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_LEFT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeLeftExtrapolator);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_TIME_RIGHT_EXTRAPOLATOR.equals(propertyName)) {
      return Collections.singleton(_timeRightExtrapolator);
    }
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(_forwardCurveName);
    }
    if (ValuePropertyNames.CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_forwardCurveCalculationMethod);
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_surfaceName);
    }
    if (LocalVolatilitySurfacePropertyNamesAndValues.PROPERTY_DERIVATIVE_EPS.equals(propertyName)) {
      return Collections.singleton(_eps);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_DISCOUNTING_CURVE_NAME.equals(propertyName)) {
      return Collections.singleton(_discountingCurveName);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_MAX_MONEYNESS.equals(propertyName)) {
      return Collections.singleton(_maxMoneynessScale);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_NUMBER_SPACE_STEPS.equals(propertyName)) {
      return Collections.singleton(_nSpaceSteps);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_NUMBER_TIME_STEPS.equals(propertyName)) {
      return Collections.singleton(_nTimeSteps);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_SPACE_DIRECTION_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_spaceDirectionInterpolator);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_SPACE_STEPS_BUNCHING.equals(propertyName)) {
      return Collections.singleton(_spaceStepBunching);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_THETA.equals(propertyName)) {
      return Collections.singleton(_theta);
    }
    if (PDEPropertyNamesAndValues.PROPERTY_TIME_STEP_BUNCHING.equals(propertyName)) {
      return Collections.singleton(_timeStepBunching);
    }
    return null;
  }

  protected String[] getValueRequirementNames() {
    return VALUE_REQUIREMENT_NAMES;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.PDE_DEFAULTS;
  }

}
