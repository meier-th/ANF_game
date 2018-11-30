package com.p3212.EntityClasses;


import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

/**
 * Represents Features entity
 * Used to operate on in-game characters appearances
 */
@Entity
@Table(name = "Features")
public class Appearance {

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

    /**
     * Gender of character
     * Responsible for model
     */
    @Enumerated(EnumType.STRING)
    @Column(length=6, nullable=false)
    private Gender gender;

    /**
     * Skin colour of a character
     */
    @Column(length=5, nullable=false)
    @Enumerated(EnumType.STRING)
    private SkinColour skinColour;

    /**
     * Hair colour of a character
     */
    @Column(length=6, nullable=false)
    @Enumerated(EnumType.STRING)
    private HairColour hairColour;
    
    @OneToOne(mappedBy="appearance", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private Character charact;
    
    /**
     * Clothes colour of a character
     */
    @Enumerated(EnumType.STRING)
    @Column(length=5)
    private ClothesColour clothesColour;


    /**
     * Getter
     * {@link Appearance#gender}
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Setter
     * {@link Appearance#gender}
     */
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * Getter
     * {@link Appearance#skinColour}
     */
    public SkinColour getSkinColour() {
        return skinColour;
    }

    /**
     * Setter
     * {@link Appearance#skinColour}
     */
    public void setSkinColour(SkinColour skinColour) {
        this.skinColour = skinColour;
    }

    /**
     * Getter
     * {@link Appearance#hairColour}
     */
    public HairColour getHairColour() {
        return hairColour;
    }

    /**
     * Setter
     * {@link Appearance#hairColour}
     */
    public void setHairColour(HairColour hairColour) {
        this.hairColour = hairColour;
    }

    /**
     * Getter
     * {@link Appearance#clothesColour}
     */
    public ClothesColour getClothesColour() {
        return clothesColour;
    }

    /**
     * Setter
     * {@link Appearance#clothesColour}
     */
    public void setClothesColour(ClothesColour clothesColour) {
        this.clothesColour = clothesColour;
    }

    public int getId() {
        return id;
    }

    public Character getCharact() {
        return charact;
    }

    public void setCharact(Character charact) {
        this.charact = charact;
    }

    

}
