/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO should there be subclasses for portfolio, depgraph, primitives? and associated visitors
 * TODO expanded cells that need full data (matrices, vectors, curves etc)
 */
public class ViewportSpecification {

  private final List<Integer> _rows;
  private final SortedSet<Integer> _columnSet;
  private final List<Integer> _columnList;
  private final boolean _expanded;

  /**
   * @param rows The rows visible in the viewport
   * @param columns The columns visible in the viewport
   * @param expanded Whether the data values should be a single summary value or a full object
   */
  public ViewportSpecification(List<Integer> rows, List<Integer> columns, boolean expanded) {
    ArgumentChecker.notNull(rows, "rows");
    ArgumentChecker.notNull(columns, "columns");
    SortedSet<Integer> sortedColumns = new TreeSet<Integer>(columns);
    List<Integer> sortedRows = new ArrayList<Integer>(rows);
    Collections.sort(sortedRows);
    if (!sortedRows.isEmpty()) {
      int minRow = sortedRows.get(0);
      if (minRow < 0) {
        throw new IllegalArgumentException("All row indices must be non-negative: " + rows);
      }
    }
    if (!sortedColumns.isEmpty()) {
      if (sortedColumns.first() < 0) {
        throw new IllegalArgumentException("All column indices must be non-negative: " + sortedColumns);
      }
    }
    _rows = ImmutableList.copyOf(sortedRows);
    _columnSet = ImmutableSortedSet.copyOf(sortedColumns);
    _columnList = ImmutableList.copyOf(sortedColumns);
    _expanded = expanded;
  }

  /* package */ static ViewportSpecification empty() {
    return new ViewportSpecification(Collections.<Integer>emptyList(), Collections.<Integer>emptyList(), false);
  }

  /* package */ List<Integer> getRows() {
    return _rows;
  }

  /* package */ SortedSet<Integer> getColumns() {
    return _columnSet;
  }

  /* package */ int getGridColumnIndex(int viewportColumnIndex) {
    return _columnList.get(viewportColumnIndex);
  }

  /**
   * Checks whether all cells specified by this object are within the bounds of the grid.
   * @param grid The structure of a grid
   * @return {@code true} if the viewport defined by this object fits within the grid.
   */
  /* package */ boolean isValidFor(GridStructure grid) {
    if (!_rows.isEmpty()) {
      int maxRow = _rows.get(_rows.size() - 1);
      if (maxRow >= grid.getRowCount()) {
        return false;
      }
    }
    if (!_columnSet.isEmpty()) {
      int maxCol = _columnSet.last();
      if (maxCol >= grid.getColumnCount()) {
        return false;
      }
    }
    return true;
  }

  /* package */ boolean isExpanded() {
    return _expanded;
  }

  @Override
  public String toString() {
    return "ViewportSpecification [" +
        "_rows=" + _rows +
        ", _columnSet=" + _columnSet +
        ", _columnList=" + _columnList +
        ", _expanded=" + _expanded +
        "]";
  }
}
