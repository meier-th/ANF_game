package EntityClasses;

/**
 * Represents Bidju entity
 * Used to operate on AI bosses data
 */
public class Boss implements Creature {
    
    /**
     * Identifier
     */
    private int id;
    
    /**
     * Name of the boss
     */
    private String name;
    
    /**
     * Number of tails. Responsible for damage, hp
     */
    private int numberOfTails;
    
    /**
     * Maximum mount of chakra (mana)
     */
    private int maxChakraAmount;
    
    /**
     * Getter
     * {@link Boss#id}
     */
    public int getId() {
        return id;
    }
    
    /**
     * Setter
     * {@link Boss#id}
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Getter
     * {@link Boss#name}
     */
    public String getName() {
        return name;
    }
    
    /**
     * Setter
     * {@link Boss#name}
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Getter
     * {@link Boss#numberOfTails}
     */
    public int getNumberOfTails() {
        return numberOfTails;
    }
    
    /**
     * Setter
     * {@link Boss#numberOfTails}
     */
    public void setNumberOfTails(int numberOfTails) {
        this.numberOfTails = numberOfTails;
    }
    
    /**
     * Getter
     * {@link Boss#maxChakraAmount}
     */
    public int getMaxChakraAmount() {
        return maxChakraAmount;
    }

    /**
     * Setter
     * {@link Boss#maxChakraAmount}
     */
    public void setMaxChakraAmount(int maxChakraAmount) {
        this.maxChakraAmount = maxChakraAmount;
    }
    
    
    /**
     * Default constructor for dependency injection
     */
    public Boss(){}
    
    /**
     * To be used when retrieved from database
     * @param id - identifier
     * @param name - name
     * @param tails - number of tails
     * @param chakra - amount of chakra
     */
    public Boss (int id, String name, int tails, int chakra) {
        this.id = id;
        this.name = name;
        this.maxChakraAmount = chakra;
        this.numberOfTails = tails;
    }
}
