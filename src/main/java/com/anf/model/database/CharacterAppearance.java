package com.anf.model.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** Represents Features entity Used to operate on in-game characters appearances */
@Entity
@Table(name = "character_appearances")
public class CharacterAppearance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  int id;

  public static enum Gender {
    MALE,
    FEMALE;
  }

  public static enum SkinColour {
    WHITE,
    LATIN,
    DARK,
    BLACK;
  }

  public static enum HairColour {
    YELLOW,
    BROWN,
    BLACK;
  }

  public static enum ClothesColour {
    GREEN,
    RED,
    BLUE;
  }

  /** Gender of character Responsible for model */
  @Enumerated(EnumType.STRING)
  @Column(length = 6, nullable = false)
  private Gender gender;

  /** Skin colour of a character */
  @Column(length = 5, nullable = false)
  @Enumerated(EnumType.STRING)
  private SkinColour skinColour;

  /** Hair colour of a character */
  @Column(length = 6, nullable = false)
  @Enumerated(EnumType.STRING)
  private HairColour hairColour;

  @OneToOne(mappedBy = "appearance", cascade = CascadeType.REMOVE, orphanRemoval = true)
  @JsonIgnore
  private GameCharacter charact;

  /** Clothes colour of a character */
  @Enumerated(EnumType.STRING)
  @Column(length = 5)
  private ClothesColour clothesColour;

  public CharacterAppearance() {}

  /** Getter {@link CharacterAppearance#gender} */
  public Gender getGender() {
    return gender;
  }

  /** Setter {@link CharacterAppearance#gender} */
  public void setGender(Gender gender) {
    this.gender = gender;
  }

  /** Getter {@link CharacterAppearance#skinColour} */
  public SkinColour getSkinColour() {
    return skinColour;
  }

  /** Setter {@link CharacterAppearance#skinColour} */
  public void setSkinColour(SkinColour skinColour) {
    this.skinColour = skinColour;
  }

  /** Getter {@link CharacterAppearance#hairColour} */
  public HairColour getHairColour() {
    return hairColour;
  }

  /** Setter {@link CharacterAppearance#hairColour} */
  public void setHairColour(HairColour hairColour) {
    this.hairColour = hairColour;
  }

  /** Getter {@link CharacterAppearance#clothesColour} */
  public ClothesColour getClothesColour() {
    return clothesColour;
  }

  /** Setter {@link CharacterAppearance#clothesColour} */
  public void setClothesColour(ClothesColour clothesColour) {
    this.clothesColour = clothesColour;
  }

  public int getId() {
    return id;
  }

  public GameCharacter getCharact() {
    return charact;
  }

  public void setCharact(GameCharacter charact) {
    this.charact = charact;
  }

  public void setId(int id) {
    this.id = id;
  }

  public CharacterAppearance(
      Gender gender, SkinColour skinColour, HairColour hairColour, ClothesColour clothesColour) {
    this.gender = gender;
    this.skinColour = skinColour;
    this.hairColour = hairColour;
    this.clothesColour = clothesColour;
  }
}
