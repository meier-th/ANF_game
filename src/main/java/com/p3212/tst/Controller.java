package com.p3212.tst;

import com.p3212.EntityClasses.*;
import com.p3212.EntityClasses.Character;
import com.p3212.Services.AppearanceService;
import com.p3212.Services.CharacterService;
import com.p3212.Services.SpellService;
import com.p3212.Services.UserService;
import com.p3212.main.BotListener;

import java.util.*;

import com.p3212.EntityClasses.Character;
import com.p3212.main.BotListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @Autowired
    BotListener bot;

    @Autowired
    private UserService userService;
    @Autowired
    private CharacterService charService;
    @Autowired
    private AppearanceService appService;

    @Autowired
    private SpellService spellService;

    private static HashMap<String, Fight> fights;

    static {
        fights = new HashMap<>();
    }

    @RequestMapping("/testing")
    public String ret() {
        return "It works";
    }

    @RequestMapping("/")
    public String reet() {
        return "start page";
    }

    @RequestMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @RequestMapping("/appearances")
    public List<Appearance> getAllAppearances() {
        Iterator<Appearance> iter = appService.getAllAppearances().iterator();
        List<Appearance> toRet = new ArrayList<Appearance>();
        while (iter.hasNext()) {
            toRet.add(iter.next());
        }
        return toRet;
    }

    @RequestMapping("/persons")
    public List<Character> getAllCharacters() {
        return charService.getAllCharacters();
    }

    @PostMapping("/signUp")
    public String signUp(@RequestParam(name = "login") String login, @RequestParam(name = "password") String password) {
        userService.saveUser(new User(login, password), false); //? "User successfully created" : "User already exists";
        return "It returns void!! We'll decide on it later";
    }

    @PostMapping("/signIn")
    public String signIn(@RequestParam(name = "login") String login, @RequestParam(name = "password") String password) {
        User user = userService.getUser(login);
        if (user == null) return "User doesn't exist";
        if (!user.getPassword().equals(password)) return "Wrong password";
        else return "Authorized";
    }

    @PostMapping("/startpvp")
    public String startPvp(@RequestParam(name = "user1") String user1, @RequestParam(name = "user2") String user2) {
        //TODO("Don't know yet") rating change
        Character char1 = userService.getUser(user1).getCharacter();
        Character char2 = userService.getUser(user2).getCharacter();
        FightPVP fight = new FightPVP();
        fight.setFighters(char1, char2);
        fight.setFighter1HP(char1.getMaxHP());
        fight.setFighter2HP(char2.getMaxHP());
        fight.setFighter1Chakra(char1.getMaxChakraAmount());
        fight.setFighter2Chakra(char2.getMaxChakraAmount());
        String id = String.valueOf(fight.getId());
        fights.put(id, fight);
        return id;
    }

    @PostMapping("/attack")
    public String attack(@RequestParam(name = "user1") String attacker, @RequestParam(name = "user2") String aim, @RequestParam(name = "fight") String fightId, @RequestParam(name = "spell") String spellId) {
        FightPVP fight = (FightPVP) fights.get(fightId);
        Spell spell = spellService.get(Integer.parseInt(spellId));
        Character attackerChar = fight.getPvpId().getFirstFighter();        //TODO attacker may be the second
        Character aimChar = fight.getPvpId().getSecondFighter();
        Attack attack = spell.performAttack(attackerChar.getUser(), aimChar);
        fight.setFighter2HP(fight.getFighter2HP() - attack.getDamage());
        if (fight.getFighter2HP() <= 0) {
            attack.setDeadly(true);
            //TODO stop the fight
        }
        fight.setFighter1Chakra(fight.getFighter1Chakra() - attack.getChakra());
        return attack.toString();
    }
}
