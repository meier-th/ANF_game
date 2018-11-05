package EntityClasses;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import javax.persistence.GeneratedValue;

@Entity
@Table(name = "friends")
public class Friends {

    @Id
    @GeneratedValue
    private int id;
    
    @ManyToOne
    @JoinColumn(name="user1")
    //@JsonIgnore
    User user1;

    
    @ManyToOne
    @JoinColumn(name="user2")
    //@JsonIgnore
    User user2;
}
