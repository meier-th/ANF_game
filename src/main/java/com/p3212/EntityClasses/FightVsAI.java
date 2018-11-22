package com.p3212.EntityClasses;

import java.util.Date;
import java.util.List;
import javax.persistence.*;

/**
 * Used to operate on fight history data
 */
@Entity
@Table(name = "ai_fights")
public class FightVsAI extends Fight {

    @Id
    private int id;

    /**
     * Date of a fight
     */
    @Column(name = "fight_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fight_date;

    /**
     * Opponent
     * Either a character or an AI boss
     */

    @ManyToOne
    @JoinColumn(name = "boss")
    private Boss boss;

    @OneToMany(mappedBy = "fight")
    private List<UserAIFight> setFighters;

    public List<UserAIFight> getSetFighters() {
        return setFighters;
    }

    public void setSetFighters(List<UserAIFight> fighters) {
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
}
