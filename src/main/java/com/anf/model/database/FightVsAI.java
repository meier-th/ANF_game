package com.anf.model.database;

import com.anf.model.Fight;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import java.util.List;

/** Used to operate on fight history data */
@Entity
@Table(name = "ai_fights")
public class FightVsAI extends Fight {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  /** Date of a fight */
  @Column(name = "fight_date", nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date fight_date;

  /** Opponent Either a character or an AI boss */
  @ManyToOne
  @JoinColumn(name = "boss", nullable = false)
  private Boss boss;

  @OneToMany(mappedBy = "fight")
  @JsonIgnore
  private List<AiFightParticipation> setFighters;

  public FightVsAI() {
    this.fight_date = new Date();
  }

  public List<AiFightParticipation> getSetFighters() {
    return setFighters;
  }

  public void setSetFighters(List<AiFightParticipation> fighters) {
    this.setFighters = fighters;
  }

  @Override
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Date getFight_date() {
    return fight_date;
  }

  public void setFight_date(Date fight_date) {
    this.fight_date = fight_date;
  }

  public Boss getBoss() {
    return boss;
  }

  public void setBoss(Boss boss) {
    this.boss = boss;
  }

  @Override
  public String toString() {
    return "{\"id\":"
        + id
        + ", \"type\": \"pve\""
        + ", \"timeLeft\":"
        + timeLeft
        + ", \"currentName\":\""
        + currentName
        + "\", \"fighters1\":"
        + fighters
        + ", \"boss\":"
        + boss.toString()
        + ", \"animals1\":"
        + animals1
        + "}";
  }
}
