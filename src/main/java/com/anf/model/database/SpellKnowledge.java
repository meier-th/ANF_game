package com.anf.model.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;

@Entity
@Table(name = "spell_knowledge")
public class SpellKnowledge {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int spellKnowledgeId;

  /** Character */
  @ManyToOne
  @JoinColumn(name = "character", nullable = false)
  @JsonIgnore
  private GameCharacter characterHandler;

  /** Spell */
  @ManyToOne
  @JoinColumn(name = "spell", nullable = false)
  private Spell spellUse;

  /** Level of the spell */
  @Column(nullable = false)
  private int spellLevel;

  /** Contains all information about characters' spells knowledge */
  public static ArrayList<SpellKnowledge> infoAboutSpells;

  /** Getter {@link SpellKnowledge#spellLevel} */
  public int getSpellLevel() {
    return spellLevel;
  }

  /** Setter {@link SpellKnowledge#spellLevel} */
  public void setSpellLevel(int spellLevel) {
    this.spellLevel = spellLevel;
  }

  public SpellKnowledge() {}

  public SpellKnowledge(int level, Spell spell, GameCharacter user) {
    this.spellLevel = level;
    this.spellUse = spell;
    this.characterHandler = user;
  }

  public int getSpellKnowledgeId() {
    return spellKnowledgeId;
  }

  public void setSpellKnowledgeId(int spellKnowledgeId) {
    this.spellKnowledgeId = spellKnowledgeId;
  }

  public GameCharacter getCharacterHandler() {
    return characterHandler;
  }

  public void setCharacterHandler(GameCharacter characterHandler) {
    this.characterHandler = characterHandler;
  }

  public Spell getSpellUse() {
    return spellUse;
  }

  public void setSpellUse(Spell spellUse) {
    this.spellUse = spellUse;
  }

  public static ArrayList<SpellKnowledge> getInfoAboutSpells() {
    return infoAboutSpells;
  }

  public static void setInfoAboutSpells(ArrayList<SpellKnowledge> infoAboutSpells) {
    SpellKnowledge.infoAboutSpells = infoAboutSpells;
  }
}
