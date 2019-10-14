package com.upgrade.islandrsvsrv.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DateInterval {

  private LocalDate start;
  private LocalDate end;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append(start);
    sb.append(",");
    sb.append(end);
    sb.append(")");
    return sb.toString();
  }
}
