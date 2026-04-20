package com.anf.domain.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiField {
  CODE("code"),
  ERROR("error"),
  ANSWER("answer"),
  RESPONSE("response"),
  MSG("msg"),
  TEXT("text"),
  AUTHORIZED("authorized"),
  LOGIN("login"),
  NEXT_ATTACKER("nextAttacker"),
  LOBBY_UUID("lobbyUuid"),
  FIGHT_UUID("fightUuid"),
  FIGHT_MODE("fightMode"),
  LEADER("leader"),
  PLAYERS("players"),
  PARTICIPANTS("participants");

  private final String value;
}
