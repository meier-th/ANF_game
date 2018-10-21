package P3212.ANFBackend.EntityClasses;
/**
     * Represents Stats entity. Used to operate on users' statistic data.
     */
public class Stats {
    
    /**
     * Rating of a user
     */
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
     * Default constructor, to be used for dependency injection
     */
    public Stats(){}
    
    /**
         * To be used when retrieved from database
         * @param r Rating value
         * @param f Number of fights
         * @param w Number of wins
         * @param l Number of losses
         * @param d Number of deaths
         */
    public Stats (int r, int f, int w, int l, int d) {
        this.deaths = d;
        this.fights = f;
        this.losses = l;
        this.rating = r;
        this.wins = w;
    }
    
    /**Setter
     * {@link Stats#rating}
     */
    public void setRating (int rating ) {
        this.rating =rating;
    }
    
    /**Setter
     * {@link Stats#fights}
     */
    public void setFights (int fights) {
        this.fights = fights;
    }
    
     /**Setter
     * {@link Stats#ratingwins}
     */
    public void setWins (int wins) {
        this.wins =wins;
    }
    
    /**Setter
     * {@link Stats#losses}
     */
    public void setLosses (int losses) {
        this.losses =losses;
    }
    
    /**Setter
     * {@link Stats#deaths}
     */
    public void setDeaths (int deaths) {
        this.deaths =deaths;
    }
    
    /**Getter
     * {@link Stats#rating}
     */
    public int getRating () {
        return this.rating;
    }
    
    /**Getter
     * {@link Stats#fights}
     */
    public int getFights () {
        return this.fights;
    }
    
    /**Getter
     * {@link Stats#wins}
     */
    public int getWins () {
        return this.wins;
    }
    
    /**Getter
     * {@link Stats#losses}
     */
    public int getLosses () {
        return this.losses;
    }
    
    /**Getter
     * {@link Stats#deaths}
     */
    public int getDeaths () {
        return this.deaths;
    }
    
    
}
