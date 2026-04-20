package com.anf.domain.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  NOT_FOUND(2),
  CREATION_FAILED(3),
  INVALID_TARGET(6),
  INVALID_REQUEST(8),
  CONFLICT(9),
  FORBIDDEN(10);

  private final int value;
}
