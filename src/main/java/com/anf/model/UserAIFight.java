package com.anf.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_aifight")
public class UserAIFight {

  public static enum Result {
    WON,
    LOST,
    DIED;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int fIdentity;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "fight_id")
  private FightVsAI fight;

  @ManyToOne
  @JsonIgnore
  @JoinColumn(name = "id", referencedColumnName = "id", nullable = false)
  private GameCharacter fighter;

  /** Result - won, lost, died */
  @Column(name = "fresult", length = 4, nullable = false)
  @Enumerated(EnumType.STRING)
  private Result result;

  @Column(name = "experience_gain", nullable = false)
  private int experience;

  public UserAIFight() {}

  public UserAIFight(Result result, FightVsAI fight, GameCharacter fighter, int exp) {
    this.experience = exp;
    this.fight = fight;
    this.fighter = fighter;
    this.result = result;
  }

  public int getId() {
    return fIdentity;
  }

  public FightVsAI getFight() {
    return fight;
  }

  public GameCharacter getFighter() {
    return fighter;
  }

  /** Getter {@link FightVsAI#result} */
  public Result getResult() {
    return result;
  }

  /** Setter {@link FightVsAI#result} */
  public void setResult(Result result) {
    this.result = result;
  }

  public int getExperience() {
    return experience;
  }

  public void setExperience(int experience) {
    this.experience = experience;
  }

  public int getfIdentity() {
    return fIdentity;
  }

  public void setfIdentity(int fIdentity) {
    this.fIdentity = fIdentity;
  }

  public void setFight(FightVsAI fight) {
    this.fight = fight;
  }

  public void setFighter(GameCharacter fighter) {
    this.fighter = fighter;
  }
}
