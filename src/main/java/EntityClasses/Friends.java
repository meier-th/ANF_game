package EntityClasses;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Friends")
public class Friends {

    @Id
    @ManyToOne
    @JoinColumn(name="user1")
    @JsonIgnore
    User user1;

    @Id
    @ManyToOne
    @JoinColumn(name="user2")
    @JsonIgnore
    User user2;
}
