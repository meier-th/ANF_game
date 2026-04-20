package com.anf.domain.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiMessage {
  FIGHT_NOT_FOUND("Fight doesn't exist"),
  NOT_A_PARTICIPANT("Not a participant"),
  CURRENT_TURN_NOT_TIMED_OUT("Current turn has not timed out yet"),
  LOBBY_NOT_FOUND("Lobby doesn't exist"),
  UNSUPPORTED_FIGHT_MODE("Unsupported fight mode"),
  LOBBY_IS_FULL("Lobby is full"),
  COULD_NOT_JOIN_LOBBY("Could not join lobby due to contention"),
  PLAYER_NOT_IN_LOBBY("Player is not in lobby"),
  INVALID_PLAYER_COUNT("Invalid number of players for the selected mode"),
  COULD_NOT_START_FIGHT("Could not start fight"),
  PVE_BOSS_ID_REQUIRED("bossId is required for PvE fights"),
  ANIMAL_NOT_CHOSEN("You haven't chosen your animal"),
  RESERVED_SYSTEM_USERNAME(
      "'SYSTEM' in any case is a reserved word. Users can not use it as their usernames."),
  USER_VALIDATION_FAILED("User object validation failed."),
  USERNAME_OCCUPIED("This username is already occupied"),
  USER_REGISTERED("User successfully registered!"),
  PASSWORD_TOO_SHORT("Password is too short."),
  USER_NOW_STRANGER("You are a stranger now."),
  APPEARANCE_CREATED("Appearance is created"),
  NO_UPGRADE_POINTS("User doesn't have upgrade points."),
  CHARACTER_UPDATED("Character is updated."),
  RESPONSE_OK("ok");

  private final String value;
}
