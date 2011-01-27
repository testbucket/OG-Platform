/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import java.util.Set;

import javax.time.calendar.LocalDate;

import com.google.common.collect.Sets;
import com.opengamma.core.common.Currency;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.bond.BondDefinition;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 */
public class BondMarketYieldFunction extends BondFunction {

  public BondMarketYieldFunction() {
    super(MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, "YLD_CNV_MID");
  }

  @Override
  protected Set<ComputedValue> getComputedValues(FunctionExecutionContext context, Currency currency, final Position position, final BondDefinition definition, final Object value, 
      final LocalDate now, final String yieldCurveName) {
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.MARKET_YTM, position), getUniqueId());
    return Sets.newHashSet(new ComputedValue(specification, value));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.MARKET_YTM, target.getPosition()), getUniqueId()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "BondMarketYieldFunction";
  }

}
