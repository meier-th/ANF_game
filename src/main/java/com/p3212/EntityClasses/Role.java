package com.p3212.EntityClasses;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="role")
public class Role {
    
    @Id
    @Column(name="role", length=5)
    private String role;

    public Role(){}
    
    public Role(String role) {
        this.role = role;
    }
    
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    @Override
    public String toString() {
        return "{\"role\": \""+role+"\"}";
    }
    
}
