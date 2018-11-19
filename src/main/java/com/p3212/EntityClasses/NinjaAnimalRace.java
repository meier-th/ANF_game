package com.p3212.EntityClasses;

import javax.persistence.Column;
import javax.persistence.Id;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * Represents NinjaAnimalRaces entity
 * Used to operate on connection between characters and animals
 */
@Entity
@Table(name = "Ninja_animal_races")
public class NinjaAnimalRace {
    static enum races {
    Snake,
    VILKOY,
    GOVNO;
    }
    @Id
    @Column(length=20)
    @Enumerated(EnumType.STRING)
    private races raceName;
}
