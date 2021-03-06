/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.function;

import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.FLAT_EXTRAPOLATOR;
import static com.opengamma.analytics.math.interpolation.Interpolator1DFactory.LINEAR;
import static com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction.PAR_RATE_STRING;
import static com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;

import java.io.OutputStreamWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFormatter;
import org.fudgemsg.wire.FudgeMsgWriter;
import org.fudgemsg.wire.xml.FudgeXMLSettings;
import org.fudgemsg.wire.xml.FudgeXMLStreamWriter;

import com.opengamma.analytics.financial.equity.future.pricing.EquityFuturePricerFactory;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.WeightingFunctionFactory;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionFactory;
import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;
import com.opengamma.analytics.financial.schedule.TimeSeriesSamplingFunctionFactory;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.aggregation.BottomPositionValues;
import com.opengamma.financial.aggregation.SortedPositionValues;
import com.opengamma.financial.aggregation.TopPositionValues;
import com.opengamma.financial.analytics.FilteringSummingFunction;
import com.opengamma.financial.analytics.LastHistoricalValueFunction;
import com.opengamma.financial.analytics.PositionScalingFunction;
import com.opengamma.financial.analytics.PositionTradeScalingFunction;
import com.opengamma.financial.analytics.SummingFunction;
import com.opengamma.financial.analytics.UnitPositionScalingFunction;
import com.opengamma.financial.analytics.UnitPositionTradeScalingFunction;
import com.opengamma.financial.analytics.equity.SecurityMarketPriceFunction;
import com.opengamma.financial.analytics.ircurve.DefaultYieldCurveMarketDataShiftFunction;
import com.opengamma.financial.analytics.ircurve.DefaultYieldCurveShiftFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveMarketDataShiftFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveShiftFunction;
import com.opengamma.financial.analytics.model.bond.BondCleanPriceFromCurvesFunction;
import com.opengamma.financial.analytics.model.bond.BondCleanPriceFromYieldFunction;
import com.opengamma.financial.analytics.model.bond.BondCouponPaymentDiaryFunction;
import com.opengamma.financial.analytics.model.bond.BondDefaultCurveNamesFunction;
import com.opengamma.financial.analytics.model.bond.BondDirtyPriceFromCurvesFunction;
import com.opengamma.financial.analytics.model.bond.BondDirtyPriceFromYieldFunction;
import com.opengamma.financial.analytics.model.bond.BondMacaulayDurationFromCurvesFunction;
import com.opengamma.financial.analytics.model.bond.BondMacaulayDurationFromYieldFunction;
import com.opengamma.financial.analytics.model.bond.BondMarketCleanPriceFunction;
import com.opengamma.financial.analytics.model.bond.BondMarketDirtyPriceFunction;
import com.opengamma.financial.analytics.model.bond.BondMarketYieldFunction;
import com.opengamma.financial.analytics.model.bond.BondModifiedDurationFromCurvesFunction;
import com.opengamma.financial.analytics.model.bond.BondModifiedDurationFromYieldFunction;
import com.opengamma.financial.analytics.model.bond.BondTenorFunction;
import com.opengamma.financial.analytics.model.bond.BondYieldFromCurvesFunction;
import com.opengamma.financial.analytics.model.bond.BondZSpreadFromCurveCleanPriceFunction;
import com.opengamma.financial.analytics.model.bond.BondZSpreadFromMarketCleanPriceFunction;
import com.opengamma.financial.analytics.model.bond.BondZSpreadPresentValueSensitivityFromCurveCleanPriceFunction;
import com.opengamma.financial.analytics.model.bond.BondZSpreadPresentValueSensitivityFromMarketCleanPriceFunction;
import com.opengamma.financial.analytics.model.bond.NelsonSiegelSvenssonBondCurveFunction;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.equity.futures.EquityFutureYieldCurveNodeSensitivityFunction;
import com.opengamma.financial.analytics.model.equity.futures.EquityFuturesFunction;
import com.opengamma.financial.analytics.model.equity.futures.EquityIndexDividendFutureYieldCurveNodeSensitivityFunction;
import com.opengamma.financial.analytics.model.equity.futures.EquityIndexDividendFuturesFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMBetaDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMBetaDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMBetaModelPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMBetaModelPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMFromRegressionDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMFromRegressionDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMFromRegressionModelPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.CAPMFromRegressionModelPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.JensenAlphaDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.JensenAlphaDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.JensenAlphaPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.JensenAlphaPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.SharpeRatioDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.SharpeRatioDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.SharpeRatioPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.SharpeRatioPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.StandardEquityModelFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TotalRiskAlphaDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TotalRiskAlphaDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TotalRiskAlphaPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TotalRiskAlphaPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TreynorRatioDefaultPropertiesPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TreynorRatioDefaultPropertiesPositionFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TreynorRatioPortfolioNodeFunction;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.TreynorRatioPositionFunction;
import com.opengamma.financial.analytics.model.equity.variance.EquityForwardFromSpotAndYieldCurveFunction;
import com.opengamma.financial.analytics.model.equity.variance.EquityVarianceSwapPresentValueFunction;
import com.opengamma.financial.analytics.model.equity.variance.EquityVarianceSwapVegaFunction;
import com.opengamma.financial.analytics.model.equity.variance.EquityVarianceSwapYieldCurveNodeSensitivityFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentPV01Function;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentParRateCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentParRateFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentParRateParallelCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentPresentValueFunction;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.analytics.model.fixedincome.deprecated.InterestRateInstrumentDefaultCurveNameFunctionDeprecated;
import com.opengamma.financial.analytics.model.fixedincome.deprecated.InterestRateInstrumentPV01FunctionDeprecated;
import com.opengamma.financial.analytics.model.fixedincome.deprecated.InterestRateInstrumentParRateCurveSensitivityFunctionDeprecated;
import com.opengamma.financial.analytics.model.fixedincome.deprecated.InterestRateInstrumentParRateFunctionDeprecated;
import com.opengamma.financial.analytics.model.fixedincome.deprecated.InterestRateInstrumentParRateParallelCurveSensitivityFunctionDeprecated;
import com.opengamma.financial.analytics.model.fixedincome.deprecated.InterestRateInstrumentPresentValueFunctionDeprecated;
import com.opengamma.financial.analytics.model.fixedincome.deprecated.InterestRateInstrumentYieldCurveNodeSensitivitiesFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXForwardDefaultsDeprecated;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXOptionBlackDefaultsDeprecated;
import com.opengamma.financial.analytics.model.forex.forward.deprecated.FXForwardCurrencyExposureFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.forward.deprecated.FXForwardPresentValueCurveSensitivityFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.forward.deprecated.FXForwardPresentValueFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.forward.deprecated.FXForwardYCNSFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackCurrencyExposureFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackPresentValueCurveSensitivityFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackPresentValueFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackVegaFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackVegaMatrixFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackVegaQuoteMatrixFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.option.black.deprecated.FXOptionBlackYCNSFunctionDeprecated;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEDualDeltaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEDualGammaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEForwardDeltaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEForwardGammaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEForwardVannaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEForwardVegaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEForwardVommaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEGridDualDeltaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEGridDualGammaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEGridForwardDeltaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEGridForwardGammaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEGridForwardVannaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEGridForwardVegaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEGridForwardVommaFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEGridImpliedVolatilityFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEGridPipsPresentValueFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEImpliedVolatilityFunction;
import com.opengamma.financial.analytics.model.forex.option.localvol.ForexLocalVolatilityForwardPDEPipsPresentValueFunction;
import com.opengamma.financial.analytics.model.future.BondFutureGrossBasisFromCurvesFunction;
import com.opengamma.financial.analytics.model.future.BondFutureNetBasisFromCurvesFunction;
import com.opengamma.financial.analytics.model.future.InterestRateFutureDefaultValuesFunctionDeprecated;
import com.opengamma.financial.analytics.model.future.InterestRateFuturePV01FunctionDeprecated;
import com.opengamma.financial.analytics.model.future.InterestRateFuturePresentValueFunctionDeprecated;
import com.opengamma.financial.analytics.model.future.InterestRateFutureYieldCurveNodeSensitivitiesFunctionDeprecated;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionDefaultValuesFunctionDeprecated;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionSABRPresentValueFunction;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionSABRSensitivitiesFunction;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionSABRVegaFunction;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionSABRYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.option.BlackScholesMertonModelFunction;
import com.opengamma.financial.analytics.model.option.BlackScholesModelCostOfCarryFunction;
import com.opengamma.financial.analytics.model.pnl.EquityPnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.EquityPnLFunction;
import com.opengamma.financial.analytics.model.pnl.ExternallyProvidedSensitivityPnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.ExternallyProvidedSensitivityPnLFunction;
import com.opengamma.financial.analytics.model.pnl.PortfolioExchangeTradedDailyPnLFunction;
import com.opengamma.financial.analytics.model.pnl.PortfolioExchangeTradedPnLFunction;
import com.opengamma.financial.analytics.model.pnl.PositionExchangeTradedDailyPnLFunction;
import com.opengamma.financial.analytics.model.pnl.PositionExchangeTradedPnLFunction;
import com.opengamma.financial.analytics.model.pnl.SecurityPriceSeriesDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.SecurityPriceSeriesFunction;
import com.opengamma.financial.analytics.model.pnl.SimpleFXFuturePnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.SimpleFXFuturePnLFunction;
import com.opengamma.financial.analytics.model.pnl.SimpleFuturePnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.SimpleFuturePnLFunction;
import com.opengamma.financial.analytics.model.pnl.TradeExchangeTradedDailyPnLFunction;
import com.opengamma.financial.analytics.model.pnl.TradeExchangeTradedPnLFunction;
import com.opengamma.financial.analytics.model.pnl.ValueGreekSensitivityPnLDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.pnl.ValueGreekSensitivityPnLFunction;
import com.opengamma.financial.analytics.model.pnl.YieldCurveNodePnLFunctionDeprecated;
import com.opengamma.financial.analytics.model.pnl.YieldCurveNodeSensitivityPnLDefaultsDeprecated;
import com.opengamma.financial.analytics.model.riskfactor.option.OptionGreekToValueGreekConverterFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadNoExtrapolationPVCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadNoExtrapolationPVCurveSensitivityFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadNoExtrapolationPresentValueFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadNoExtrapolationPresentValueFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadNoExtrapolationVegaFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadNoExtrapolationVegaFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadNoExtrapolationYCNSFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadNoExtrapolationYCNSFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadRightExtrapolationPVCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadRightExtrapolationPVSABRNodeSensitivityFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadRightExtrapolationPVSABRSensitivityFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadRightExtrapolationPresentValueFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRCMSSpreadRightExtrapolationYCNSFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRNoExtrapolationPVCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRNoExtrapolationPVCurveSensitivityFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRNoExtrapolationPVSABRSensitivityFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRNoExtrapolationPVSABRSensitivityFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRNoExtrapolationPresentValueFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRNoExtrapolationPresentValueFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRNoExtrapolationVegaFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRNoExtrapolationVegaFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRNoExtrapolationYCNSFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRNoExtrapolationYCNSFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationPVCurveSensitivityFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationPVCurveSensitivityFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationPVSABRNodeSensitivityFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationPVSABRSensitivityFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationPVSABRSensitivityFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationPresentValueFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationPresentValueFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationVegaFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationVegaFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationYCNSFunction;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationYCNSFunctionDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationVegaDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRNoExtrapolationVegaDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationVegaDefaults;
import com.opengamma.financial.analytics.model.sabrcube.defaultproperties.SABRRightExtrapolationVegaDefaultsDeprecated;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSecurityMarkFunction;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesCreditFactorsFunction;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesNonYieldCurveFunction;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesYieldCurveCS01Function;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesYieldCurveDV01Function;
import com.opengamma.financial.analytics.model.sensitivities.ExternallyProvidedSensitivitiesYieldCurveNodeSensitivitiesFunction;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleFXFuturePresentValueFunction;
import com.opengamma.financial.analytics.model.simpleinstrument.SimpleFuturePresentValueFunction;
import com.opengamma.financial.analytics.model.var.NormalPortfolioHistoricalVaRDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.var.NormalPortfolioHistoricalVaRFunction;
import com.opengamma.financial.analytics.model.var.NormalPositionHistoricalVaRDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.var.NormalPositionHistoricalVaRFunction;
import com.opengamma.financial.analytics.model.volatility.VolatilityDataFittingDefaults;
import com.opengamma.financial.analytics.model.volatility.cube.SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults;
import com.opengamma.financial.analytics.model.volatility.cube.SABRNonLinearLeastSquaresSwaptionCubeFittingFunction;
import com.opengamma.financial.analytics.model.volatility.local.ForexDupireLocalVolatilitySurfaceFunction;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.BackwardPDEMixedLogNormalDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.BackwardPDESABRDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.BackwardPDESplineDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.ForwardPDEMixedLogNormalDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.ForwardPDESABRDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.ForwardPDESplineDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.LocalVolatilitySurfaceMixedLogNormalDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.LocalVolatilitySurfaceSABRDefaults;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.LocalVolatilitySurfaceSplineDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.BlackScholesMertonImpliedVolatilitySurfaceFunction;
import com.opengamma.financial.analytics.model.volatility.surface.SABRNonLinearLeastSquaresIRFutureOptionSurfaceFittingFunction;
import com.opengamma.financial.analytics.model.volatility.surface.SABRNonLinearLeastSquaresIRFutureSurfaceDefaultValuesFunction;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfaceMixedLogNormalInterpolatorFunction;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfaceSABRInterpolatorFunction;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfaceSplineInterpolatorFunction;
import com.opengamma.financial.analytics.model.volatility.surface.black.ForexBlackVolatilitySurfaceFunction;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceMixedLogNormalDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceMixedLogNormalInterpolatorDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceSABRDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceSABRInterpolatorDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceSplineDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.BlackVolatilitySurfaceSplineInterpolatorDefaults;
import com.opengamma.financial.analytics.timeseries.DefaultHistoricalTimeSeriesShiftFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesLatestSecurityValueFunction;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesLatestValueFunction;
import com.opengamma.financial.analytics.timeseries.YieldCurveHistoricalTimeSeriesFunction;
import com.opengamma.financial.analytics.timeseries.YieldCurveInstrumentConversionHistoricalTimeSeriesFunction;
import com.opengamma.financial.analytics.timeseries.YieldCurveInstrumentConversionHistoricalTimeSeriesFunctionDeprecated;
import com.opengamma.financial.analytics.timeseries.YieldCurveInstrumentConversionHistoricalTimeSeriesShiftFunctionDeprecated;
import com.opengamma.financial.analytics.volatility.surface.DefaultVolatilitySurfaceShiftFunction;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceShiftFunction;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixSourcingFunction;
import com.opengamma.financial.currency.FixedIncomeInstrumentPnLSeriesCurrencyConversionFunction;
import com.opengamma.financial.currency.PortfolioNodeCurrencyConversionFunction;
import com.opengamma.financial.currency.PortfolioNodeDefaultCurrencyFunction;
import com.opengamma.financial.currency.PositionCurrencyConversionFunction;
import com.opengamma.financial.currency.PositionDefaultCurrencyFunction;
import com.opengamma.financial.currency.SecurityCurrencyConversionFunction;
import com.opengamma.financial.currency.SecurityDefaultCurrencyFunction;
import com.opengamma.financial.property.AggregationDefaultPropertyFunction;
import com.opengamma.financial.property.DefaultPropertyFunction.PriorityClass;
import com.opengamma.financial.property.PortfolioNodeCalcConfigDefaultPropertyFunction;
import com.opengamma.financial.property.PositionCalcConfigDefaultPropertyFunction;
import com.opengamma.financial.property.PositionDefaultPropertyFunction;
import com.opengamma.financial.property.PrimitiveCalcConfigDefaultPropertyFunction;
import com.opengamma.financial.property.SecurityCalcConfigDefaultPropertyFunction;
import com.opengamma.financial.property.TradeCalcConfigDefaultPropertyFunction;
import com.opengamma.financial.property.TradeDefaultPropertyFunction;
import com.opengamma.financial.value.PositionValueFunction;
import com.opengamma.financial.value.SecurityValueFunction;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;

/**
 * Constructs a standard function repository.
 * <p>
 * This should be replaced by something that loads the functions from the configuration database
 */
public class ExampleStandardFunctionConfiguration extends SingletonFactoryBean<RepositoryConfigurationSource> {

  private static final String USD = Currency.USD.getCode();
  private static final String SECONDARY = "SECONDARY";
  private static final String COST_OF_CARRY_FIELD = "COST_OF_CARRY";
  private static final String HTS_PRICE_FIELD = "CLOSE";
  private static final boolean OUTPUT_REPO_CONFIGURATION = false;

  public static <F extends FunctionDefinition> FunctionConfiguration functionConfiguration(final Class<F> clazz, final String... args) {
    if (Modifier.isAbstract(clazz.getModifiers())) {
      throw new IllegalStateException("Attempting to register an abstract class - " + clazz);
    }
    if (args.length == 0) {
      return new StaticFunctionConfiguration(clazz.getName());
    } else {
      return new ParameterizedFunctionConfiguration(clazz.getName(), Arrays.asList(args));
    }
  }

  protected static void addValueFunctions(final List<FunctionConfiguration> functionConfigs) {
    addSummingFunction(functionConfigs, ValueRequirementNames.VALUE);
    functionConfigs.add(functionConfiguration(PositionValueFunction.class));
    functionConfigs.add(functionConfiguration(SecurityValueFunction.class));
  }

  public static void addScalingFunction(final List<FunctionConfiguration> functionConfigs, final String requirementName) {
    functionConfigs.add(functionConfiguration(PositionScalingFunction.class, requirementName));
    functionConfigs.add(functionConfiguration(PositionTradeScalingFunction.class, requirementName));
  }

  public static void addUnitScalingFunction(final List<FunctionConfiguration> functionConfigs, final String requirementName) {
    functionConfigs.add(functionConfiguration(UnitPositionScalingFunction.class, requirementName));
    functionConfigs.add(functionConfiguration(UnitPositionTradeScalingFunction.class, requirementName));
  }

  /**
   * Adds a summing function for the value.
   *
   * @param functionConfigs the configuration block to add the definition to
   * @param requirementName the requirement to sum at portfolio node levels
   */
  public static void addSummingFunction(final List<FunctionConfiguration> functionConfigs, final String requirementName) {
    functionConfigs.add(functionConfiguration(FilteringSummingFunction.class, requirementName));
    functionConfigs.add(functionConfiguration(SummingFunction.class, requirementName));
    functionConfigs.add(functionConfiguration(AggregationDefaultPropertyFunction.class, requirementName, SummingFunction.AGGREGATION_STYLE_FULL, FilteringSummingFunction.AGGREGATION_STYLE_FILTERED));
  }

  protected static void addValueGreekAndSummingFunction(final List<FunctionConfiguration> functionConfigs, final String requirementName) {
    functionConfigs.add(functionConfiguration(OptionGreekToValueGreekConverterFunction.class, requirementName));
    addSummingFunction(functionConfigs, requirementName);
  }

  protected static void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs, final String requirementName) {
    functionConfigs.add(functionConfiguration(PortfolioNodeCurrencyConversionFunction.class, requirementName));
    functionConfigs.add(functionConfiguration(PositionCurrencyConversionFunction.class, requirementName));
    functionConfigs.add(functionConfiguration(SecurityCurrencyConversionFunction.class, requirementName));
    functionConfigs.add(functionConfiguration(PortfolioNodeDefaultCurrencyFunction.Permissive.class, requirementName));
    functionConfigs.add(functionConfiguration(PositionDefaultCurrencyFunction.Permissive.class, requirementName));
    functionConfigs.add(functionConfiguration(SecurityDefaultCurrencyFunction.Permissive.class, requirementName));
  }

  protected static void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.FAIR_VALUE);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.PV01);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.PRESENT_VALUE);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.DAILY_PNL);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.VALUE_DELTA);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.VALUE_GAMMA);
    addCurrencyConversionFunctions(functionConfigs, ValueRequirementNames.VALUE_SPEED);
    functionConfigs.add(functionConfiguration(SecurityCurrencyConversionFunction.class, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES));
    functionConfigs.add(functionConfiguration(PortfolioNodeDefaultCurrencyFunction.Permissive.class, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES));
    functionConfigs.add(functionConfiguration(CurrencyMatrixSourcingFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
    functionConfigs.add(functionConfiguration(FixedIncomeInstrumentPnLSeriesCurrencyConversionFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  protected static void addLateAggregationFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(BottomPositionValues.class));
    functionConfigs.add(functionConfiguration(SortedPositionValues.class));
    functionConfigs.add(functionConfiguration(TopPositionValues.class));
  }

  protected static void addDataShiftingFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(VolatilitySurfaceShiftFunction.class));
    functionConfigs.add(functionConfiguration(DefaultVolatilitySurfaceShiftFunction.class));
    functionConfigs.add(functionConfiguration(YieldCurveShiftFunction.class));
    functionConfigs.add(functionConfiguration(DefaultYieldCurveShiftFunction.class));
    functionConfigs.add(functionConfiguration(YieldCurveMarketDataShiftFunction.class));
    functionConfigs.add(functionConfiguration(DefaultYieldCurveMarketDataShiftFunction.class));
  }

  protected static void addDefaultPropertyFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(PortfolioNodeCalcConfigDefaultPropertyFunction.Generic.class));
    functionConfigs.add(functionConfiguration(PortfolioNodeCalcConfigDefaultPropertyFunction.Specific.class));
    functionConfigs.add(functionConfiguration(PositionCalcConfigDefaultPropertyFunction.Generic.class));
    functionConfigs.add(functionConfiguration(PositionCalcConfigDefaultPropertyFunction.Specific.class));
    functionConfigs.add(functionConfiguration(PrimitiveCalcConfigDefaultPropertyFunction.Generic.class));
    functionConfigs.add(functionConfiguration(PrimitiveCalcConfigDefaultPropertyFunction.Specific.class));
    functionConfigs.add(functionConfiguration(SecurityCalcConfigDefaultPropertyFunction.Generic.class));
    functionConfigs.add(functionConfiguration(SecurityCalcConfigDefaultPropertyFunction.Specific.class));
    functionConfigs.add(functionConfiguration(TradeCalcConfigDefaultPropertyFunction.Generic.class));
    functionConfigs.add(functionConfiguration(TradeCalcConfigDefaultPropertyFunction.Specific.class));
    functionConfigs.add(functionConfiguration(PositionDefaultPropertyFunction.class));
    functionConfigs.add(functionConfiguration(TradeDefaultPropertyFunction.class));
  }

  protected static void addHistoricalDataFunctions(final List<FunctionConfiguration> functionConfigs, final String requirementName) {
    addUnitScalingFunction(functionConfigs, requirementName);
    functionConfigs.add(functionConfiguration(LastHistoricalValueFunction.class, requirementName));
//    functionConfigs.add(functionConfiguration(HistoricalTimeSeriesFunction.class));
//    functionConfigs.add(functionConfiguration(HistoricalTimeSeriesLatestValueFunction.class));
//    functionConfigs.add(functionConfiguration(YieldCurveHistoricalTimeSeriesFunction.class));
//    functionConfigs.add(functionConfiguration(YieldCurveInstrumentConversionHistoricalTimeSeriesFunction.class));
//    functionConfigs.add(functionConfiguration(YieldCurveInstrumentConversionHistoricalTimeSeriesFunctionDeprecated.class));
  }

  protected static void addHistoricalDataFunctions(final List<FunctionConfiguration> functionConfigs) {
    addHistoricalDataFunctions(functionConfigs, ValueRequirementNames.DAILY_VOLUME);
    addHistoricalDataFunctions(functionConfigs, ValueRequirementNames.DAILY_MARKET_CAP);
    addHistoricalDataFunctions(functionConfigs, ValueRequirementNames.DAILY_APPLIED_BETA);
    addHistoricalDataFunctions(functionConfigs, ValueRequirementNames.DAILY_PRICE);
    functionConfigs.add(functionConfiguration(HistoricalTimeSeriesFunction.class));
    functionConfigs.add(functionConfiguration(HistoricalTimeSeriesLatestValueFunction.class));
    functionConfigs.add(functionConfiguration(HistoricalTimeSeriesLatestSecurityValueFunction.class));
    functionConfigs.add(functionConfiguration(YieldCurveHistoricalTimeSeriesFunction.class));
    functionConfigs.add(functionConfiguration(YieldCurveInstrumentConversionHistoricalTimeSeriesFunction.class));
    functionConfigs.add(functionConfiguration(YieldCurveInstrumentConversionHistoricalTimeSeriesFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(YieldCurveInstrumentConversionHistoricalTimeSeriesShiftFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(DefaultHistoricalTimeSeriesShiftFunction.class));
  }

  public static RepositoryConfiguration constructRepositoryConfiguration() {
    final List<FunctionConfiguration> functionConfigs = new ArrayList<FunctionConfiguration>();

    addValueFunctions(functionConfigs);

    functionConfigs.add(functionConfiguration(SecurityMarketPriceFunction.class));
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY);

    // options
    functionConfigs.add(functionConfiguration(BlackScholesMertonModelFunction.class));
    functionConfigs.add(functionConfiguration(BlackScholesMertonImpliedVolatilitySurfaceFunction.class));
    functionConfigs.add(functionConfiguration(BlackScholesModelCostOfCarryFunction.class));

    // equity and portfolio
    functionConfigs.add(functionConfiguration(PositionExchangeTradedPnLFunction.class));
    functionConfigs.add(functionConfiguration(PortfolioExchangeTradedPnLFunction.class));
    functionConfigs.add(functionConfiguration(PortfolioExchangeTradedDailyPnLFunction.Impl.class));
    functionConfigs.add(functionConfiguration(AggregationDefaultPropertyFunction.class, ValueRequirementNames.DAILY_PNL, PortfolioExchangeTradedDailyPnLFunction.Impl.AGGREGATION_STYLE_FULL));

    addPnLCalculators(functionConfigs);
    addVaRCalculators(functionConfigs);
    addPortfolioAnalysisCalculators(functionConfigs);
    addFixedIncomeInstrumentCalculators(functionConfigs);
    addDeprecatedFixedIncomeInstrumentCalculators(functionConfigs);

    functionConfigs.add(functionConfiguration(StandardEquityModelFunction.class));
    functionConfigs.add(functionConfiguration(SimpleFuturePresentValueFunction.class, SECONDARY));
    functionConfigs.add(functionConfiguration(SimpleFXFuturePresentValueFunction.class, SECONDARY, SECONDARY));
    addBondCalculators(functionConfigs);
    addBondFutureCalculators(functionConfigs);
    addSABRCalculators(functionConfigs);
    addDeprecatedSABRCalculators(functionConfigs);
    addForexOptionCalculators(functionConfigs);
    addForexForwardCalculators(functionConfigs);
    addInterestRateFutureCalculators(functionConfigs);
    addInterestRateFutureOptionCalculators(functionConfigs);
    addEquityDerivativesCalculators(functionConfigs);
    addLocalVolatilityCalculators(functionConfigs);
    addExternallyProvidedSensitivitiesFunctions(functionConfigs);

    addScalingFunction(functionConfigs, ValueRequirementNames.FAIR_VALUE);

    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DELTA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DELTA_BLEED);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.STRIKE_DELTA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DRIFTLESS_THETA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GAMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GAMMA_P);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.STRIKE_GAMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GAMMA_BLEED);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GAMMA_P_BLEED);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VEGA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VEGA_P);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VARIANCE_VEGA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VEGA_BLEED);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.THETA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.RHO);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.CARRY_RHO);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.ZETA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.ZETA_BLEED);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DZETA_DVOL);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.ELASTICITY);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.PHI);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.ZOMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.ZOMMA_P);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.ULTIMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VARIANCE_ULTIMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.SPEED);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.SPEED_P);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VANNA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VARIANCE_VANNA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DVANNA_DVOL);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VOMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VOMMA_P);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.VARIANCE_VOMMA);

    addUnitScalingFunction(functionConfigs, ValueRequirementNames.FORWARD_DELTA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.FORWARD_GAMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DUAL_DELTA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DUAL_GAMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.FORWARD_VEGA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.FORWARD_VANNA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.FORWARD_VOMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.IMPLIED_VOLATILITY);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GRID_FORWARD_DELTA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GRID_FORWARD_GAMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GRID_DUAL_DELTA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GRID_DUAL_GAMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GRID_FORWARD_VEGA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GRID_FORWARD_VANNA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GRID_FORWARD_VOMMA);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GRID_IMPLIED_VOLATILITY);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GRID_PRESENT_VALUE);

    addUnitScalingFunction(functionConfigs, ValueRequirementNames.BOND_TENOR);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MARKET_DIRTY_PRICE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MARKET_CLEAN_PRICE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MARKET_YTM);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.CLEAN_PRICE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.DIRTY_PRICE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.YTM);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MODIFIED_DURATION);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.Z_SPREAD);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_Z_SPREAD_SENSITIVITY);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.CONVEXITY);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.MACAULAY_DURATION);

    addUnitScalingFunction(functionConfigs, ValueRequirementNames.GROSS_BASIS);
    addSummingFunction(functionConfigs, ValueRequirementNames.GROSS_BASIS);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.NET_BASIS);
    addSummingFunction(functionConfigs, ValueRequirementNames.NET_BASIS);

    addScalingFunction(functionConfigs, ValueRequirementNames.PV01);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_NODE_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_NODE_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_NU_NODE_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.PAR_RATE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.PAR_RATE_PARALLEL_CURVE_SHIFT);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.PAR_RATE_CURVE_SENSITIVITY);

    addSummingFunction(functionConfigs, ValueRequirementNames.FAIR_VALUE);
    addSummingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE);
    addSummingFunction(functionConfigs, ValueRequirementNames.PV01);
    addSummingFunction(functionConfigs, ValueRequirementNames.DV01);
    addSummingFunction(functionConfigs, ValueRequirementNames.CS01);
    addSummingFunction(functionConfigs, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    addSummingFunction(functionConfigs, ValueRequirementNames.FX_PRESENT_VALUE);
    addSummingFunction(functionConfigs, ValueRequirementNames.VEGA_MATRIX);
    addSummingFunction(functionConfigs, ValueRequirementNames.VEGA_QUOTE_MATRIX);
    addSummingFunction(functionConfigs, ValueRequirementNames.VEGA_QUOTE_CUBE);
    addSummingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY);
    addSummingFunction(functionConfigs, ValueRequirementNames.PRICE_SERIES);
    addSummingFunction(functionConfigs, ValueRequirementNames.PNL_SERIES);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY);
    addScalingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY);
    addSummingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_NODE_SENSITIVITY);
    addSummingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_NODE_SENSITIVITY);
    addSummingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_SABR_NU_NODE_SENSITIVITY);
    addSummingFunction(functionConfigs, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
    addSummingFunction(functionConfigs, ValueRequirementNames.EXTERNAL_SENSITIVITIES);
    addSummingFunction(functionConfigs, ValueRequirementNames.CREDIT_SENSITIVITIES);
    addSummingFunction(functionConfigs, ValueRequirementNames.WEIGHT);

    addSummingFunction(functionConfigs, ValueRequirementNames.PRESENT_VALUE_Z_SPREAD_SENSITIVITY);
    addSummingFunction(functionConfigs, ValueRequirementNames.BOND_COUPON_PAYMENT_TIMES);
    addScalingFunction(functionConfigs, ValueRequirementNames.BOND_COUPON_PAYMENT_TIMES);

    addScalingFunction(functionConfigs, ValueRequirementNames.FX_PRESENT_VALUE);
    addScalingFunction(functionConfigs, ValueRequirementNames.FX_CURRENCY_EXPOSURE);
    addUnitScalingFunction(functionConfigs, ValueRequirementNames.FX_CURVE_SENSITIVITIES);

    addScalingFunction(functionConfigs, ValueRequirementNames.VEGA_MATRIX);
    addScalingFunction(functionConfigs, ValueRequirementNames.VEGA_QUOTE_MATRIX);
    addScalingFunction(functionConfigs, ValueRequirementNames.VEGA_QUOTE_CUBE);
    addScalingFunction(functionConfigs, ValueRequirementNames.VALUE_VEGA);
    addSummingFunction(functionConfigs, ValueRequirementNames.VALUE_VEGA);

    addScalingFunction(functionConfigs, ValueRequirementNames.VALUE_DELTA);
    addScalingFunction(functionConfigs, ValueRequirementNames.VALUE_RHO);
    addSummingFunction(functionConfigs, ValueRequirementNames.VALUE_RHO);

    addUnitScalingFunction(functionConfigs, ValueRequirementNames.FORWARD);
    addSummingFunction(functionConfigs, ValueRequirementNames.FORWARD);
    addValueGreekAndSummingFunction(functionConfigs, ValueRequirementNames.VALUE_DELTA);
    addValueGreekAndSummingFunction(functionConfigs, ValueRequirementNames.VALUE_GAMMA);
    addValueGreekAndSummingFunction(functionConfigs, ValueRequirementNames.VALUE_SPEED);

    addCurrencyConversionFunctions(functionConfigs);
    addLateAggregationFunctions(functionConfigs);
    addDataShiftingFunctions(functionConfigs);
    addDefaultPropertyFunctions(functionConfigs);
    addHistoricalDataFunctions(functionConfigs);
    functionConfigs.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, SECONDARY));

    final RepositoryConfiguration repoConfig = new RepositoryConfiguration(functionConfigs);

    if (OUTPUT_REPO_CONFIGURATION) {
      final FudgeMsg msg = OpenGammaFudgeContext.getInstance().toFudgeMsg(repoConfig).getMessage();
      FudgeMsgFormatter.outputToSystemOut(msg);
      try {
        final FudgeXMLSettings xmlSettings = new FudgeXMLSettings();
        xmlSettings.setEnvelopeElementName(null);
        final FudgeMsgWriter msgWriter = new FudgeMsgWriter(new FudgeXMLStreamWriter(FudgeContext.GLOBAL_DEFAULT, new OutputStreamWriter(System.out), xmlSettings));
        msgWriter.setDefaultMessageProcessingDirectives(0);
        msgWriter.setDefaultMessageVersion(0);
        msgWriter.setDefaultTaxonomyId(0);
        msgWriter.writeMessage(msg);
        msgWriter.flush();
      } catch (final Exception e) {
        // Just swallow it.
      }
    }
    return repoConfig;
  }

  private static void addPnLCalculators(final List<FunctionConfiguration> functionConfigs) {
    final String defaultCurveCalculationMethod = PRESENT_VALUE_STRING;
    final String defaultReturnCalculatorName = TimeSeriesReturnCalculatorFactory.SIMPLE_NET_LENIENT;
    final String defaultSamplingPeriodName = "P2Y";
    final String defaultScheduleName = ScheduleCalculatorFactory.DAILY;
    final String defaultSamplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    functionConfigs.add(functionConfiguration(TradeExchangeTradedPnLFunction.class, DEFAULT_CONFIG_NAME, HTS_PRICE_FIELD, COST_OF_CARRY_FIELD));
    functionConfigs.add(functionConfiguration(TradeExchangeTradedDailyPnLFunction.class, DEFAULT_CONFIG_NAME, HTS_PRICE_FIELD, COST_OF_CARRY_FIELD));
    functionConfigs.add(functionConfiguration(PositionExchangeTradedDailyPnLFunction.class, DEFAULT_CONFIG_NAME, HTS_PRICE_FIELD, COST_OF_CARRY_FIELD));
    functionConfigs.add(functionConfiguration(SecurityPriceSeriesFunction.class, DEFAULT_CONFIG_NAME, MarketDataRequirementNames.MARKET_VALUE));
    functionConfigs.add(functionConfiguration(SecurityPriceSeriesDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName));
    functionConfigs.add(functionConfiguration(EquityPnLFunction.class));
    functionConfigs.add(functionConfiguration(EquityPnLDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName, defaultReturnCalculatorName));
    functionConfigs.add(functionConfiguration(SimpleFuturePnLFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(SimpleFuturePnLDefaultPropertiesFunction.class, SECONDARY, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName));
    functionConfigs.add(functionConfiguration(SimpleFXFuturePnLFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(SimpleFXFuturePnLDefaultPropertiesFunction.class, SECONDARY, SECONDARY,
        defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName));
    functionConfigs.add(functionConfiguration(YieldCurveNodePnLFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(YieldCurveNodeSensitivityPnLDefaultsDeprecated.class, SECONDARY, SECONDARY, defaultCurveCalculationMethod, defaultSamplingPeriodName,
        defaultScheduleName, defaultSamplingCalculatorName, "AUD", USD, "CAD", "DKK", "EUR", "GBP", "JPY", "NZD", "CHF"));
    functionConfigs.add(functionConfiguration(ValueGreekSensitivityPnLFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(ValueGreekSensitivityPnLDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName,
        defaultReturnCalculatorName));
  }

  private static void addVaRCalculators(final List<FunctionConfiguration> functionConfigs) {
    final String defaultSamplingPeriodName = "P2Y";
    final String defaultScheduleName = ScheduleCalculatorFactory.DAILY;
    final String defaultSamplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    final String defaultMeanCalculatorName = StatisticsCalculatorFactory.MEAN;
    final String defaultStdDevCalculatorName = StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION;
    final String defaultConfidenceLevelName = "0.99";
    final String defaultHorizonName = "1";
    functionConfigs.add(functionConfiguration(NormalPositionHistoricalVaRFunction.class));
    functionConfigs.add(functionConfiguration(NormalPortfolioHistoricalVaRFunction.class));
    functionConfigs.add(functionConfiguration(NormalPositionHistoricalVaRDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName,
        defaultMeanCalculatorName, defaultStdDevCalculatorName, defaultConfidenceLevelName, defaultHorizonName, PriorityClass.ABOVE_NORMAL.name()));
    functionConfigs.add(functionConfiguration(NormalPortfolioHistoricalVaRDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName,
        defaultMeanCalculatorName, defaultStdDevCalculatorName, defaultConfidenceLevelName, defaultHorizonName, PriorityClass.ABOVE_NORMAL.name()));
  }

  private static void addEquityDerivativesCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(),
        Arrays.asList(ValueRequirementNames.PRESENT_VALUE, EquityFuturePricerFactory.MARK_TO_MARKET, SECONDARY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(),
        Arrays.asList(ValueRequirementNames.PRESENT_VALUE, EquityFuturePricerFactory.DIVIDEND_YIELD, SECONDARY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityIndexDividendFuturesFunction.class.getName(),
        Arrays.asList(ValueRequirementNames.PRESENT_VALUE, EquityFuturePricerFactory.MARK_TO_MARKET, SECONDARY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(),
        Arrays.asList(ValueRequirementNames.PRESENT_VALUE, EquityFuturePricerFactory.DIVIDEND_YIELD, SECONDARY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(),
        Arrays.asList(ValueRequirementNames.PV01, EquityFuturePricerFactory.MARK_TO_MARKET, SECONDARY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(),
        Arrays.asList(ValueRequirementNames.PV01, EquityFuturePricerFactory.DIVIDEND_YIELD, SECONDARY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(),
        Arrays.asList(ValueRequirementNames.VALUE_RHO, EquityFuturePricerFactory.MARK_TO_MARKET, SECONDARY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(),
        Arrays.asList(ValueRequirementNames.VALUE_RHO, EquityFuturePricerFactory.DIVIDEND_YIELD, SECONDARY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(),
        Arrays.asList(ValueRequirementNames.VALUE_DELTA, EquityFuturePricerFactory.MARK_TO_MARKET, SECONDARY)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(EquityFuturesFunction.class.getName(),
        Arrays.asList(ValueRequirementNames.VALUE_DELTA, EquityFuturePricerFactory.DIVIDEND_YIELD, SECONDARY)));
    functionConfigs.add(functionConfiguration(EquityFutureYieldCurveNodeSensitivityFunction.class, SECONDARY));
    functionConfigs.add(functionConfiguration(EquityIndexDividendFutureYieldCurveNodeSensitivityFunction.class, SECONDARY));
    functionConfigs.add(functionConfiguration(EquityForwardFromSpotAndYieldCurveFunction.class, SECONDARY));
    functionConfigs.add(functionConfiguration(EquityVarianceSwapPresentValueFunction.class, SECONDARY, SECONDARY, EquityForwardFromSpotAndYieldCurveFunction.FORWARD_FROM_SPOT_AND_YIELD_CURVE));
    functionConfigs.add(functionConfiguration(EquityVarianceSwapYieldCurveNodeSensitivityFunction.class, SECONDARY, SECONDARY,
        EquityForwardFromSpotAndYieldCurveFunction.FORWARD_FROM_SPOT_AND_YIELD_CURVE));
    functionConfigs.add(functionConfiguration(EquityVarianceSwapVegaFunction.class, SECONDARY, SECONDARY,
        EquityForwardFromSpotAndYieldCurveFunction.FORWARD_FROM_SPOT_AND_YIELD_CURVE));
  }

  private static void addBondFutureCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(BondFutureGrossBasisFromCurvesFunction.class, USD, SECONDARY, SECONDARY));
    functionConfigs.add(functionConfiguration(BondFutureNetBasisFromCurvesFunction.class, USD, SECONDARY, SECONDARY));
  }

  private static void addBondCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(BondCouponPaymentDiaryFunction.class));
    functionConfigs.add(functionConfiguration(BondTenorFunction.class));
    functionConfigs.add(functionConfiguration(BondMarketCleanPriceFunction.class));
    functionConfigs.add(functionConfiguration(BondMarketDirtyPriceFunction.class));
    functionConfigs.add(functionConfiguration(BondMarketYieldFunction.class));
    functionConfigs.add(functionConfiguration(BondYieldFromCurvesFunction.class));
    functionConfigs.add(functionConfiguration(BondCleanPriceFromCurvesFunction.class));
    functionConfigs.add(functionConfiguration(BondDirtyPriceFromCurvesFunction.class));
    functionConfigs.add(functionConfiguration(BondMacaulayDurationFromCurvesFunction.class));
    functionConfigs.add(functionConfiguration(BondModifiedDurationFromCurvesFunction.class));
    functionConfigs.add(functionConfiguration(BondCleanPriceFromYieldFunction.class));
    functionConfigs.add(functionConfiguration(BondDirtyPriceFromYieldFunction.class));
    functionConfigs.add(functionConfiguration(BondMacaulayDurationFromYieldFunction.class));
    functionConfigs.add(functionConfiguration(BondModifiedDurationFromYieldFunction.class));
    functionConfigs.add(functionConfiguration(BondZSpreadFromCurveCleanPriceFunction.class));
    functionConfigs.add(functionConfiguration(BondZSpreadFromMarketCleanPriceFunction.class));
    functionConfigs.add(functionConfiguration(BondZSpreadPresentValueSensitivityFromCurveCleanPriceFunction.class));
    functionConfigs.add(functionConfiguration(BondZSpreadPresentValueSensitivityFromMarketCleanPriceFunction.class));
    functionConfigs.add(functionConfiguration(NelsonSiegelSvenssonBondCurveFunction.class));
    functionConfigs.add(functionConfiguration(BondDefaultCurveNamesFunction.class, SECONDARY, SECONDARY, ValueRequirementNames.CLEAN_PRICE,
        ValueRequirementNames.DIRTY_PRICE, ValueRequirementNames.MACAULAY_DURATION, ValueRequirementNames.MODIFIED_DURATION, ValueRequirementNames.YTM,
        ValueRequirementNames.Z_SPREAD, ValueRequirementNames.PRESENT_VALUE_Z_SPREAD_SENSITIVITY));
  }

  private static void addForexOptionCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(ExampleForexSpotRateMarketDataFunction.class));
    functionConfigs.add(functionConfiguration(FXOptionBlackPresentValueFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(FXOptionBlackCurrencyExposureFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(FXOptionBlackVegaFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(FXOptionBlackVegaMatrixFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(FXOptionBlackVegaQuoteMatrixFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(FXOptionBlackPresentValueCurveSensitivityFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(FXOptionBlackYCNSFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(FXOptionBlackDefaultsDeprecated.class, SECONDARY, SECONDARY, PAR_RATE_STRING, SECONDARY,
        SECONDARY, PAR_RATE_STRING, SECONDARY, "DoubleQuadratic", "LinearExtrapolator", "LinearExtrapolator", "USD", "EUR"));
    functionConfigs.add(functionConfiguration(FXOptionBlackDefaultsDeprecated.class, SECONDARY, SECONDARY, PAR_RATE_STRING, SECONDARY,
        SECONDARY, PAR_RATE_STRING, SECONDARY, "DoubleQuadratic", "LinearExtrapolator", "LinearExtrapolator", "EUR", "USD"));
  }

  private static void addForexForwardCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(FXForwardPresentValueFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(FXForwardCurrencyExposureFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(FXForwardYCNSFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(FXForwardPresentValueCurveSensitivityFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "USD", "EUR"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "EUR", "USD"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "USD", "GBP"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "GBP", "USD"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "USD", "JPY"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "JPY", "USD"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "USD", "CHF"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "CHF", "USD"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "EUR", "GBP"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "GBP", "EUR"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "EUR", "JPY"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "JPY", "EUR"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "EUR", "CHF"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "GBP", "CHF"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "JPY", "CHF"));
    functionConfigs.add(functionConfiguration(FXForwardDefaultsDeprecated.class, "SECONDARY", "SECONDARY", PAR_RATE_STRING, "SECONDARY", "SECONDARY",
        PAR_RATE_STRING, "CHF", "JPY"));
  }

  private static void addInterestRateFutureCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateFuturePresentValueFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(InterestRateFuturePV01FunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(InterestRateFutureYieldCurveNodeSensitivitiesFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(InterestRateFutureDefaultValuesFunctionDeprecated.class, SECONDARY, SECONDARY, PAR_RATE_STRING, USD, "EUR"));
  }

  private static void addInterestRateFutureOptionCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateFutureOptionSABRPresentValueFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateFutureOptionSABRSensitivitiesFunction.class, ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY));
    functionConfigs.add(functionConfiguration(InterestRateFutureOptionSABRSensitivitiesFunction.class, ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY));
    functionConfigs.add(functionConfiguration(InterestRateFutureOptionSABRSensitivitiesFunction.class, ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY));
    functionConfigs.add(functionConfiguration(InterestRateFutureOptionSABRVegaFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateFutureOptionSABRYieldCurveNodeSensitivitiesFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateFutureOptionDefaultValuesFunctionDeprecated.class, SECONDARY, SECONDARY, "DEFAULT", PAR_RATE_STRING, USD, "EUR"));
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresIRFutureOptionSurfaceFittingFunction.class));
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresIRFutureSurfaceDefaultValuesFunction.class, "DEFAULT"));
  }

  private static void addLocalVolatilityCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new StaticFunctionConfiguration(BlackVolatilitySurfaceMixedLogNormalInterpolatorFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BlackVolatilitySurfaceSABRInterpolatorFunction.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BlackVolatilitySurfaceSplineInterpolatorFunction.Exception.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(BlackVolatilitySurfaceSplineInterpolatorFunction.Quiet.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(ForexBlackVolatilitySurfaceFunction.MixedLogNormal.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(ForexBlackVolatilitySurfaceFunction.SABR.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(ForexBlackVolatilitySurfaceFunction.Spline.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(ForexDupireLocalVolatilitySurfaceFunction.MixedLogNormal.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(ForexDupireLocalVolatilitySurfaceFunction.SABR.class.getName()));
    functionConfigs.add(new StaticFunctionConfiguration(ForexDupireLocalVolatilitySurfaceFunction.Spline.class.getName()));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEPipsPresentValueFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEPipsPresentValueFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEPipsPresentValueFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEDualDeltaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEDualDeltaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEDualDeltaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEDualGammaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEDualGammaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEDualGammaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardDeltaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardDeltaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardDeltaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardGammaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardGammaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardGammaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardVegaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardVegaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardVegaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardVannaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardVannaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardVannaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardVommaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardVommaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEForwardVommaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEImpliedVolatilityFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEImpliedVolatilityFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEImpliedVolatilityFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    addLocalVolatilityGridFunctions(functionConfigs);
    addLocalVolatilityDefaultProperties(functionConfigs);
  }

  private static void addLocalVolatilityGridFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridDualDeltaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridDualDeltaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridDualDeltaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridDualGammaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridDualGammaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridDualGammaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardDeltaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardDeltaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardDeltaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardGammaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardGammaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardGammaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardVegaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardVegaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardVegaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardVannaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardVannaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardVannaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardVommaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardVommaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridForwardVommaFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridImpliedVolatilityFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridImpliedVolatilityFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridImpliedVolatilityFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridPipsPresentValueFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SPLINE)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridPipsPresentValueFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.SABR)));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForexLocalVolatilityForwardPDEGridPipsPresentValueFunction.class.getName(),
        Arrays.asList(BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL)));
  }

  private static void addLocalVolatilityDefaultProperties(final List<FunctionConfiguration> functionConfigs) {
    final List<String> commonBlackSurfaceInterpolatorProperties = Arrays.asList(
        BlackVolatilitySurfacePropertyNamesAndValues.LOG_TIME,
        BlackVolatilitySurfacePropertyNamesAndValues.LOG_Y,
        BlackVolatilitySurfacePropertyNamesAndValues.INTEGRATED_VARIANCE,
        Interpolator1DFactory.DOUBLE_QUADRATIC,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    final List<String> mixedLogNormalProperties = new ArrayList<String>(commonBlackSurfaceInterpolatorProperties);
    mixedLogNormalProperties.add(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME);
    final List<String> sabrProperties = new ArrayList<String>(commonBlackSurfaceInterpolatorProperties);
    sabrProperties.add(VolatilityFunctionFactory.HAGAN);
    sabrProperties.add(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME);
    sabrProperties.add("false");
    sabrProperties.add("0.5");
    final List<String> splineProperties = new ArrayList<String>(commonBlackSurfaceInterpolatorProperties);
    splineProperties.add(Interpolator1DFactory.DOUBLE_QUADRATIC);
    splineProperties.add(Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    splineProperties.add(Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    // Equity default is: Quiet - if ShiftedLogNormalTailExtrapolationFitter fails on boundary, try on next strike in interior of domain
    splineProperties.add(BlackVolatilitySurfacePropertyNamesAndValues.QUIET_SPLINE_EXTRAPOLATOR_FAILURE);

    final List<String> commonForexBlackSurfaceProperties = new ArrayList<String>(commonBlackSurfaceInterpolatorProperties);
    commonForexBlackSurfaceProperties.add("SECONDARY-SECONDARY");
    commonForexBlackSurfaceProperties.add(ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    commonForexBlackSurfaceProperties.add("SECONDARY");
    final List<String> forexBlackSurfaceMixedLogNormalProperties = new ArrayList<String>(commonForexBlackSurfaceProperties);
    forexBlackSurfaceMixedLogNormalProperties.add(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME);
    final List<String> forexBlackSurfaceSABRProperties = new ArrayList<String>(commonForexBlackSurfaceProperties);
    forexBlackSurfaceSABRProperties.add(VolatilityFunctionFactory.HAGAN);
    forexBlackSurfaceSABRProperties.add(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME);
    forexBlackSurfaceSABRProperties.add("false");
    forexBlackSurfaceSABRProperties.add("0.5");
    final List<String> forexBlackSurfaceSplineProperties = new ArrayList<String>(commonForexBlackSurfaceProperties);
    forexBlackSurfaceSplineProperties.add(Interpolator1DFactory.DOUBLE_QUADRATIC);
    forexBlackSurfaceSplineProperties.add(Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    forexBlackSurfaceSplineProperties.add(Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    forexBlackSurfaceSplineProperties.add(BlackVolatilitySurfacePropertyNamesAndValues.QUIET_SPLINE_EXTRAPOLATOR_FAILURE);
    final List<String> commonForexLocalSurfaceProperties = new ArrayList<String>(commonForexBlackSurfaceProperties);
    commonForexLocalSurfaceProperties.add("1e-3");
    final List<String> forexLocalSurfaceMixedLogNormalProperties = new ArrayList<String>(commonForexLocalSurfaceProperties);
    forexLocalSurfaceMixedLogNormalProperties.add(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME);
    final List<String> forexLocalSurfaceSABRProperties = new ArrayList<String>(commonForexLocalSurfaceProperties);
    forexLocalSurfaceSABRProperties.add(VolatilityFunctionFactory.HAGAN);
    forexLocalSurfaceSABRProperties.add(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME);
    forexLocalSurfaceSABRProperties.add("false");
    forexLocalSurfaceSABRProperties.add("0.5");
    final List<String> forexLocalSurfaceSplineProperties = new ArrayList<String>(commonForexLocalSurfaceProperties);
    forexLocalSurfaceSplineProperties.add(Interpolator1DFactory.DOUBLE_QUADRATIC);
    forexLocalSurfaceSplineProperties.add(Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    forexLocalSurfaceSplineProperties.add(Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    forexLocalSurfaceSplineProperties.add(BlackVolatilitySurfacePropertyNamesAndValues.QUIET_SPLINE_EXTRAPOLATOR_FAILURE);
    final List<String> commonForexBackwardPDEProperties = new ArrayList<String>(commonForexLocalSurfaceProperties);
    commonForexBackwardPDEProperties.add("0.5");
    commonForexBackwardPDEProperties.add("100");
    commonForexBackwardPDEProperties.add("100");
    commonForexBackwardPDEProperties.add("5.0");
    commonForexBackwardPDEProperties.add("0.05");
    commonForexBackwardPDEProperties.add("3.5");
    commonForexBackwardPDEProperties.add(Interpolator1DFactory.DOUBLE_QUADRATIC);
    commonForexBackwardPDEProperties.add("SECONDARY");
    final List<String> forexBackwardPDEMixedLogNormalProperties = new ArrayList<String>(commonForexBackwardPDEProperties);
    forexBackwardPDEMixedLogNormalProperties.add(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME);
    final List<String> forexBackwardPDESABRProperties = new ArrayList<String>(commonForexBackwardPDEProperties);
    forexBackwardPDESABRProperties.add(VolatilityFunctionFactory.HAGAN);
    forexBackwardPDESABRProperties.add(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME);
    forexBackwardPDESABRProperties.add("false");
    forexBackwardPDESABRProperties.add("0.5");
    final List<String> forexBackwardPDESplineProperties = new ArrayList<String>(commonForexBackwardPDEProperties);
    forexBackwardPDESplineProperties.add(Interpolator1DFactory.DOUBLE_QUADRATIC);
    forexBackwardPDESplineProperties.add(Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    forexBackwardPDESplineProperties.add(Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    forexBackwardPDESplineProperties.add(BlackVolatilitySurfacePropertyNamesAndValues.QUIET_SPLINE_EXTRAPOLATOR_FAILURE);
    final List<String> commonForexForwardPDEProperties = new ArrayList<String>(commonForexLocalSurfaceProperties);
    commonForexForwardPDEProperties.add("0.5");
    commonForexForwardPDEProperties.add("100");
    commonForexForwardPDEProperties.add("100");
    commonForexForwardPDEProperties.add("5.0");
    commonForexForwardPDEProperties.add("0.05");
    commonForexForwardPDEProperties.add("1.5");
    commonForexForwardPDEProperties.add("1.0");
    commonForexForwardPDEProperties.add(Interpolator1DFactory.DOUBLE_QUADRATIC);
    commonForexForwardPDEProperties.add("SECONDARY");
    final List<String> forexForwardPDEMixedLogNormalProperties = new ArrayList<String>(commonForexForwardPDEProperties);
    forexForwardPDEMixedLogNormalProperties.add(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME);
    final List<String> forexForwardPDESABRProperties = new ArrayList<String>(commonForexForwardPDEProperties);
    forexForwardPDESABRProperties.add(VolatilityFunctionFactory.HAGAN);
    forexForwardPDESABRProperties.add(WeightingFunctionFactory.SINE_WEIGHTING_FUNCTION_NAME);
    forexForwardPDESABRProperties.add("false");
    forexForwardPDESABRProperties.add("0.5");
    final List<String> forexForwardPDESplineProperties = new ArrayList<String>(commonForexForwardPDEProperties);
    forexForwardPDESplineProperties.add(Interpolator1DFactory.DOUBLE_QUADRATIC);
    forexForwardPDESplineProperties.add(Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    forexForwardPDESplineProperties.add(Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    forexForwardPDESplineProperties.add(BlackVolatilitySurfacePropertyNamesAndValues.QUIET_SPLINE_EXTRAPOLATOR_FAILURE);
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceMixedLogNormalInterpolatorDefaults.class.getName(), mixedLogNormalProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceSABRInterpolatorDefaults.class.getName(), sabrProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceSplineInterpolatorDefaults.class.getName(), splineProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceMixedLogNormalDefaults.class.getName(), forexBlackSurfaceMixedLogNormalProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceSABRDefaults.class.getName(), forexBlackSurfaceSABRProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BlackVolatilitySurfaceSplineDefaults.class.getName(), forexBlackSurfaceSplineProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(LocalVolatilitySurfaceMixedLogNormalDefaults.class.getName(), forexLocalSurfaceMixedLogNormalProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(LocalVolatilitySurfaceSABRDefaults.class.getName(), forexLocalSurfaceSABRProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(LocalVolatilitySurfaceSplineDefaults.class.getName(), forexLocalSurfaceSplineProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BackwardPDEMixedLogNormalDefaults.class.getName(), forexBackwardPDEMixedLogNormalProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BackwardPDESABRDefaults.class.getName(), forexBackwardPDESABRProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(BackwardPDESplineDefaults.class.getName(), forexBackwardPDESplineProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForwardPDEMixedLogNormalDefaults.class.getName(), forexForwardPDEMixedLogNormalProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForwardPDESABRDefaults.class.getName(), forexForwardPDESABRProperties));
    functionConfigs.add(new ParameterizedFunctionConfiguration(ForwardPDESplineDefaults.class.getName(), forexForwardPDESplineProperties));
  }

  private static void addDeprecatedSABRCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingFunction.class));
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, "USD", "BLOOMBERG"));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVCurveSensitivityFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPresentValueFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunctionDeprecated.Alpha.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunctionDeprecated.Nu.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunctionDeprecated.Rho.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationVegaFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationYCNSFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationPVCurveSensitivityFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationPresentValueFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationPVSABRSensitivityFunctionDeprecated.Alpha.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationPVSABRSensitivityFunctionDeprecated.Nu.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationPVSABRSensitivityFunctionDeprecated.Rho.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationVegaFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationYCNSFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPVCurveSensitivityFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPresentValueFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPVSABRSensitivityFunctionDeprecated.Alpha.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPVSABRSensitivityFunctionDeprecated.Nu.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPVSABRSensitivityFunctionDeprecated.Rho.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationVegaFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationYCNSFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationDefaultsDeprecated.class, SECONDARY, SECONDARY, SECONDARY, "NonLinearLeastSquares", PAR_RATE_STRING, "USD"));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationDefaultsDeprecated.class, SECONDARY, SECONDARY, SECONDARY, "NonLinearLeastSquares", PAR_RATE_STRING, "0.07", "10.0", "USD"));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationVegaDefaultsDeprecated.class, SECONDARY, SECONDARY, SECONDARY, "NonLinearLeastSquares", PAR_RATE_STRING,
        "Linear", "FlatExtrapolator", "FlatExtrapolator", "Linear", "FlatExtrapolator", "FlatExtrapolator", USD));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationVegaDefaultsDeprecated.class, SECONDARY, SECONDARY, SECONDARY, "NonLinearLeastSquares", PAR_RATE_STRING,
        "0.07", "10.0", "Linear", "FlatExtrapolator", "FlatExtrapolator", "Linear", "FlatExtrapolator", "FlatExtrapolator", USD));
  }

  private static void addSABRCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingFunction.class));
    functionConfigs.add(functionConfiguration(SABRNonLinearLeastSquaresSwaptionCubeFittingDefaults.class, USD, SECONDARY));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVCurveSensitivityFunction.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPresentValueFunction.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunction.Alpha.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunction.Nu.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationPVSABRSensitivityFunction.Rho.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPresentValueFunction.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVCurveSensitivityFunction.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVSABRNodeSensitivityFunction.Alpha.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVSABRNodeSensitivityFunction.Nu.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVSABRNodeSensitivityFunction.Rho.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVSABRSensitivityFunction.Alpha.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVSABRSensitivityFunction.Nu.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadRightExtrapolationPVSABRSensitivityFunction.Rho.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationVegaFunction.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadNoExtrapolationYCNSFunction.class));
    functionConfigs.add(functionConfiguration(SABRCMSSpreadRightExtrapolationYCNSFunction.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationPVCurveSensitivityFunction.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationPresentValueFunction.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationPVSABRSensitivityFunction.Alpha.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationPVSABRSensitivityFunction.Nu.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationPVSABRSensitivityFunction.Rho.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationVegaFunction.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationYCNSFunction.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPVCurveSensitivityFunction.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPresentValueFunction.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPVSABRSensitivityFunction.Alpha.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPVSABRSensitivityFunction.Nu.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPVSABRSensitivityFunction.Rho.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPVSABRNodeSensitivityFunction.Alpha.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPVSABRNodeSensitivityFunction.Nu.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationPVSABRNodeSensitivityFunction.Rho.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationVegaFunction.class));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationYCNSFunction.class));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationDefaults.class, PriorityClass.ABOVE_NORMAL.name(), VolatilityDataFittingDefaults.NON_LINEAR_LEAST_SQUARES,
        USD, "DefaultTwoCurveUSDConfig", SECONDARY));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationDefaults.class, PriorityClass.ABOVE_NORMAL.name(), VolatilityDataFittingDefaults.NON_LINEAR_LEAST_SQUARES,
        "0.07", "10.0",
        USD, "DefaultTwoCurveUSDConfig", SECONDARY));
    functionConfigs.add(functionConfiguration(SABRNoExtrapolationVegaDefaults.class, PriorityClass.ABOVE_NORMAL.name(), VolatilityDataFittingDefaults.NON_LINEAR_LEAST_SQUARES,
        LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR,
        USD, "DefaultTwoCurveUSDConfig", SECONDARY));
    functionConfigs.add(functionConfiguration(SABRRightExtrapolationVegaDefaults.class, PriorityClass.ABOVE_NORMAL.name(), VolatilityDataFittingDefaults.NON_LINEAR_LEAST_SQUARES,
        "0.07", "10.0", LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR, LINEAR, FLAT_EXTRAPOLATOR, FLAT_EXTRAPOLATOR,
        USD, "DefaultTwoCurveUSDConfig", SECONDARY));

  }

  private static void addDeprecatedFixedIncomeInstrumentCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateInstrumentParRateFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentPresentValueFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentParRateCurveSensitivityFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentParRateParallelCurveSensitivityFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentPV01FunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentYieldCurveNodeSensitivitiesFunctionDeprecated.class));
    functionConfigs.add(functionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class, PAR_RATE_STRING));
    functionConfigs.add(functionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class, PRESENT_VALUE_STRING));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentDefaultCurveNameFunctionDeprecated.class, "ParRate", SECONDARY, SECONDARY, "AUD", "CAD", "CHF", "DKK", "EUR",
        "GBP", "JPY", "NZD", USD));
  }

  private static void addFixedIncomeInstrumentCalculators(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(InterestRateInstrumentParRateCurveSensitivityFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentParRateFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentParRateParallelCurveSensitivityFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentPresentValueFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentPV01Function.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentYieldCurveNodeSensitivitiesFunction.class));
    functionConfigs.add(functionConfiguration(InterestRateInstrumentDefaultPropertiesFunction.class, PriorityClass.ABOVE_NORMAL.name(), "false",
        "USD", "DefaultTwoCurveUSDConfig",
        "GBP", "DefaultTwoCurveGBPConfig",
        "EUR", "DefaultTwoCurveEURConfig",
        "JPY", "DefaultTwoCurveJPYConfig",
        "CHF", "DefaultTwoCurveCHFConfig"));
  }

  private static void addPortfolioAnalysisCalculators(final List<FunctionConfiguration> functionConfigs) {
    final String returnCalculator = TimeSeriesReturnCalculatorFactory.SIMPLE_NET_STRICT;
    final String samplingPeriod = "P2Y";
    final String schedule = ScheduleCalculatorFactory.DAILY;
    final String samplingFunction = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    final String stdDevCalculator = StatisticsCalculatorFactory.SAMPLE_STANDARD_DEVIATION;
    final String covarianceCalculator = StatisticsCalculatorFactory.SAMPLE_COVARIANCE;
    final String varianceCalculator = StatisticsCalculatorFactory.SAMPLE_VARIANCE;
    final String excessReturnCalculator = StatisticsCalculatorFactory.MEAN;

    functionConfigs.add(functionConfiguration(CAPMBetaDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(CAPMBetaDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(CAPMBetaDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(CAPMBetaDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(CAPMBetaModelPositionFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(CAPMBetaModelPortfolioNodeFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(CAPMFromRegressionDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator));
    functionConfigs.add(functionConfiguration(CAPMFromRegressionDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator));
    functionConfigs.add(functionConfiguration(CAPMFromRegressionModelPositionFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(CAPMFromRegressionModelPortfolioNodeFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(SharpeRatioDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator));
    functionConfigs.add(functionConfiguration(SharpeRatioDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator));
    functionConfigs.add(functionConfiguration(SharpeRatioPositionFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(SharpeRatioPortfolioNodeFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(TreynorRatioDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(TreynorRatioDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(TreynorRatioPositionFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(TreynorRatioPortfolioNodeFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(JensenAlphaDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(JensenAlphaDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator, covarianceCalculator, varianceCalculator));
    functionConfigs.add(functionConfiguration(JensenAlphaPositionFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(JensenAlphaPortfolioNodeFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(TotalRiskAlphaDefaultPropertiesPortfolioNodeFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator));
    functionConfigs.add(functionConfiguration(TotalRiskAlphaDefaultPropertiesPositionFunction.class, samplingPeriod, schedule, samplingFunction,
        returnCalculator, stdDevCalculator, excessReturnCalculator));
    functionConfigs.add(functionConfiguration(TotalRiskAlphaPositionFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(TotalRiskAlphaPortfolioNodeFunction.class, DEFAULT_CONFIG_NAME));
  }

  private static void addExternallyProvidedSensitivitiesFunctions(final List<FunctionConfiguration> functionConfigs) {
    final String defaultSamplingPeriodName = "P2Y";
    final String defaultScheduleName = ScheduleCalculatorFactory.DAILY;
    final String defaultSamplingCalculatorName = TimeSeriesSamplingFunctionFactory.PREVIOUS_AND_FIRST_VALUE_PADDING;
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesYieldCurveNodeSensitivitiesFunction.class));
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesNonYieldCurveFunction.class));

    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesCreditFactorsFunction.class));
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivityPnLFunction.class, DEFAULT_CONFIG_NAME));
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivityPnLDefaultPropertiesFunction.class, defaultSamplingPeriodName, defaultScheduleName, defaultSamplingCalculatorName));
    functionConfigs.add(functionConfiguration(ExternallyProvidedSecurityMarkFunction.class));
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesYieldCurveDV01Function.class));
    functionConfigs.add(functionConfiguration(ExternallyProvidedSensitivitiesYieldCurveCS01Function.class));
  }

  //-------------------------------------------------------------------------
  public static RepositoryConfigurationSource constructRepositoryConfigurationSource() {
    return new SimpleRepositoryConfigurationSource(constructRepositoryConfiguration());
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return constructRepositoryConfigurationSource();
  }

}
