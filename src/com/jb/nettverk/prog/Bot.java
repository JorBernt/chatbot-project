package com.jb.nettverk.prog;

import java.util.*;

enum Mood {
    GOOD,
    BAD,
}

class Verb {
    String present;
    String infinitiv;

    public Verb(String infinitiv, String present) {
        this.infinitiv = infinitiv;
        this.present = present;
    }
}

public class Bot {
    protected final String name;
    protected Map<Mood, List<String>> adjectives = new HashMap<>();
    protected Map<Mood, List<Verb>> verbs = new HashMap<>();
    protected Random random = new Random();
    protected Map<Mood, List<String>> sentences = new HashMap<>();

    protected Bot(String name) {
        this.name = name;
        init();
    }

    static Bot getBot(String name) {
        return switch (name) {
            case "Liam" -> new Bot_LIAM();
            default -> null;
        };
    }

    private void init() {
        List<String> good_adj = new ArrayList<>();
        good_adj.add("Nice");
        good_adj.add("Awesome");
        good_adj.add("Cool");
        good_adj.add("Terrific");
        good_adj.add("Great");
        good_adj.add("Toit");
        good_adj.add("Coolbeans");
        adjectives.put(Mood.GOOD, good_adj);

        List<String> bad_adj = new ArrayList<>();
        bad_adj.add("Stupid");
        bad_adj.add("Awful");
        bad_adj.add("Bad");
        bad_adj.add("Dreadful");
        bad_adj.add("Crappy");
        bad_adj.add("Abominable");
        bad_adj.add("Gross");
        adjectives.put(Mood.BAD, bad_adj);

        List<Verb> good_verb = new ArrayList<>();
        good_verb.add(new Verb("Write", "Writing"));
        good_verb.add(new Verb("Work", "Working"));
        good_verb.add(new Verb("Swim", "Swimming"));
        good_verb.add(new Verb("Run", "Running"));
        good_verb.add(new Verb("Code", "Coding"));

        verbs.put(Mood.GOOD, good_verb);

        List<Verb> bad_verb = new ArrayList<>();
        bad_verb.add(new Verb("Steal", "Stealing"));
        bad_verb.add(new Verb("Work", "Working"));
        bad_verb.add(new Verb("Swim", "Swimming"));
        bad_verb.add(new Verb("Run", "Running"));
        bad_verb.add(new Verb("Code", "Coding"));
        verbs.put(Mood.BAD, bad_verb);

        List<String> good_sentences = new ArrayList<>();
        good_sentences.add("That's {_a}! I like {}!");
        good_sentences.add("{a}! We can {} together. I also like {_vp}.");
        good_sentences.add("Yes, I love {}! This is going to be {_a}.");
        sentences.put(Mood.GOOD, good_sentences);

        List<String> bad_sentences = new ArrayList<>();
        bad_sentences.add("That's {_a}! I don't like {}!");
        bad_sentences.add("Isn't that just {_a}! I'd rather be {_vp}.");
        bad_sentences.add("No, I think {} is {_a}!");
        sentences.put(Mood.BAD, bad_sentences);
    }

    private String getRandom(Map<Mood, List<String>> list, Mood mood) {
        return list.get(mood).get(random.nextInt(list.get(mood).size()));

    }

    private String getRandom(Map<Mood, List<Verb>> list, Mood mood, boolean present) {
        Verb verb = list.get(mood).get(random.nextInt(list.get(mood).size()));
        return present ? verb.present : verb.infinitiv;

    }

    public String getResponse(String input) {
        if (input == null || input.equals("")) return null;
        String[] sentence = input.split(" ");
        Mood mood = random.nextBoolean() ? Mood.GOOD : Mood.BAD;
        String respons = getRandom(sentences, mood);
        while (true) {
            if(respons.contains("{}")) {
                respons = respons.replace("{}", sentence[sentence.length - 1].replaceAll("\\W", ""));
                continue;
            }
            if(respons.contains("{a}")) {
                respons = respons.replace("{a}", getRandom(adjectives, mood));
                continue;
            }
            if(respons.contains("{_a}")) {
                respons = respons.replace("{_a}", getRandom(adjectives, mood).toLowerCase());
                continue;
            }
            if(respons.contains("{v}")) {
                respons = input.replace("{v}", getRandom(verbs, mood, false));
                continue;
            }
            if(respons.contains("{_v}")) {
                respons = respons.replace("{_v}", getRandom(verbs, mood,false).toLowerCase());
                continue;
            }
            if(respons.contains("{vp}")) {
                respons = respons.replace("{vp}", getRandom(verbs, mood, true));
                continue;
            }
            if(respons.contains("{_vp}")) {
                respons = respons.replace("{_vp}", getRandom(verbs, mood,true).toLowerCase());
                continue;
            }
            break;
        }
        return respons;
    }


    public String getName() {
        return name;
    }
}

class Bot_LIAM extends Bot {

    public Bot_LIAM() {
        super("Liam");

    }


}
