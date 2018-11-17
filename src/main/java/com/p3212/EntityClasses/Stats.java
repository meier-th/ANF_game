package com.p3212.EntityClasses;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

/**
 * Represents Stats entity. Used to operate on users' statistic data.
 */
@Entity
@Table(name = "Statistics")
public class Stats {

    /**
     * Rating of a user
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(mappedBy = "stats")
    @JsonIgnore
    private User user;

    public String getLogin() {
        return user.getLogin();
    }

    private int rating;
    /**
     * Number of fights user took part in
     */
    private int fights;
    /**
     * Number of fights user won
     */
    private int wins;
    /**
     * Number of fights user lost
     */
    private int losses;
    /**
     * Number of fights during which user died
     * Death occurs when a character died in battle against AI, but his team won
     */
    private int deaths;

    /**
     * Level
     */
    @Column(name = "lvl")
    private int level;

    /**
     * Experience
     */
    private int experience;

    /**
     * Number of available upgrade points
     */
    private int upgradePoints;

    /**
     * Default constructor, to be used for dependency injection
     */
    public Stats() {
    }

    /**
     * To be used when retrieved from database
     *
     * @param r Rating value
     * @param f Number of fights
     * @param w Number of wins
     * @param l Number of losses
     * @param d Number of deaths
     */
    public Stats(int r, int f, int w, int l, int d) {
        this.deaths = d;
        this.fights = f;
        this.losses = l;
        this.rating = r;
        this.wins = w;
    }

    public Stats(int rating, int fights, int wins, int losses, int deaths, int experience, int level, int points) {
        this.deaths = deaths;
        this.fights = fights;
        this.losses = losses;
        this.rating = rating;
        this.wins = wins;
        this.experience = experience;
        this.level = level;
        this.upgradePoints = points;
    }

    /**
     * Setter
     * {@link Stats#rating}
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Setter
     * {@link Stats#fights}
     */
    public void setFights(int fights) {
        this.fights = fights;
    }

    /**
     * Setter
     * {@link Stats#wins}
     */
    public void setWins(int wins) {
        this.wins = wins;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getUpgradePoints() {
        return upgradePoints;
    }

    public void setUpgradePoints(int upgradePoints) {
        this.upgradePoints = upgradePoints;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    /**
     * Setter
     * {@link Stats#losses}
     */
    public void setLosses(int losses) {
        this.losses = losses;
    }

    /**
     * Setter
     * {@link Stats#deaths}
     */
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    /**
     * Getter
     * {@link Stats#rating}
     */
    public int getRating() {
        return this.rating;
    }

    /**
     * Getter
     * {@link Stats#fights}
     */
    public int getFights() {
        return this.fights;
    }

    /**
     * Getter
     * {@link Stats#wins}
     */
    public int getWins() {
        return this.wins;
    }

    /**
     * Getter
     * {@link Stats#losses}
     */
    public int getLosses() {
        return this.losses;
    }

    /**
     * Getter
     * {@link Stats#deaths}
     */
    public int getDeaths() {
        return this.deaths;
    }

    public void changeRating(int change) {
        rating += change;
    }

    @Override
    public String toString() {
        return "Username: " + user.getLogin() +
                ",\n  rating=" + rating +
                ",\n  fights=" + fights +
                ",\n  wins=" + wins +
                ",\n  losses=" + losses +
                ",\n  deaths=" + deaths;
    }
}
