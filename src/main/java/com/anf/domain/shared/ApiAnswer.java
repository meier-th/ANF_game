package com.anf.domain.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiAnswer {
  OK("OK"),
  ALREADY_IN_LOBBY("ALREADY_IN_LOBBY"),
  LEFT("LEFT"),
  LEFT_AND_LOBBY_CLOSED("LEFT_AND_LOBBY_CLOSED"),
  ALREADY_PROCESSED("ALREADY_PROCESSED"),
  TIMED_OUT("TIMED_OUT");

  private final String value;
}
