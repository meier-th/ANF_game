package com.p3212.EntityClasses;

import java.security.Principal;

public class StompPrincipal implements Principal {
    
    private String name;
    
    public StompPrincipal(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
}
