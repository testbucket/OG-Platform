/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index.generator;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwap;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.CalendarNoHoliday;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * A list of swap generators that can be used in the tests.
 */
public class GeneratorSwapTestsMaster {

  /**
   * The method unique instance.
   */
  private static final GeneratorSwapTestsMaster INSTANCE = new GeneratorSwapTestsMaster();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static GeneratorSwapTestsMaster getInstance() {
    return INSTANCE;
  }

  /**
   * The map with the list of names and the swap generators.
   */
  private final Map<String, GeneratorSwap> _generatorSwap;

  /**
   * The list of Ibor indexes for test purposes.
   */
  private final IndexIborTestsMaster _iborIndexMaster;

  /**
   * Private constructor.
   */
  private GeneratorSwapTestsMaster() {
    _iborIndexMaster = IndexIborTestsMaster.getInstance();
    Calendar baseCalendar = new CalendarNoHoliday("No Holidays");
    _generatorSwap = new HashMap<String, GeneratorSwap>();
    _generatorSwap.put("USD6MLIBOR3M", new GeneratorSwap(Period.ofMonths(6), DayCountFactory.INSTANCE.getDayCount("30/360"), _iborIndexMaster.getIndex("USDLIBOR3M", baseCalendar)));
    _generatorSwap.put("USD1YLIBOR3M", new GeneratorSwap(Period.ofMonths(12), DayCountFactory.INSTANCE.getDayCount("ACT/360"), _iborIndexMaster.getIndex("USDLIBOR3M", baseCalendar)));
    _generatorSwap.put("EUR1YEURIBOR3M", new GeneratorSwap(Period.ofMonths(12), DayCountFactory.INSTANCE.getDayCount("30/360"), _iborIndexMaster.getIndex("EURIBOR3M", baseCalendar)));
    _generatorSwap.put("EUR1YEURIBOR6M", new GeneratorSwap(Period.ofMonths(12), DayCountFactory.INSTANCE.getDayCount("30/360"), _iborIndexMaster.getIndex("EURIBOR6M", baseCalendar)));
  }

  public GeneratorSwap getGenerator(final String name, final Calendar cal) {
    GeneratorSwap generatorNoCalendar = _generatorSwap.get(name);
    if (generatorNoCalendar == null) {
      throw new OpenGammaRuntimeException("Could not get Ibor index for " + name);
    }
    return new GeneratorSwap(generatorNoCalendar.getFixedLegPeriod(), generatorNoCalendar.getFixedLegDayCount(), _iborIndexMaster.getIndex(generatorNoCalendar.getIborIndex().getName(), cal));
  }

}