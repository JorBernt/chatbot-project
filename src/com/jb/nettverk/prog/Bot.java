package com.jb.nettverk.prog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.*;

enum Mood {
    GOOD,
    BAD,
    INDIFFERENT
}

class Verb {
    String present;
    String infinitive;

    public Verb(String infinitive, String present) {
        this.infinitive = infinitive;
        this.present = present;
    }
}

public class Bot {
    protected final Mood mood;
    protected final String name;
    protected Random random = new Random();
    protected Map<Mood, List<String>> adjectives = new HashMap<>();
    protected Map<Mood, List<Verb>> verbs = new HashMap<>();
    protected Map<Mood, List<String>> sentences = new HashMap<>();

    public static void main(String[] args) {
        Bot_John bj = new Bot_John();
        System.out.println(bj.getResponse("How are your wine?"));
    }

    protected Bot(String name, Mood mood) {
        this.name = name;
        this.mood = mood;
        init();
    }

    static Bot getBot(String name) {
        return switch (name) {
            case "Liam" -> new Bot_LIAM();
            case "Hannah" -> new Bot_Hannah();
            case "John" -> new Bot_John();
            case "Sara" -> new Bot_Sara();
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

        List<String> indiff_adj = new ArrayList<>();
        indiff_adj.add("Okay");
        indiff_adj.add("Fine");
        indiff_adj.add("Whatever");
        indiff_adj.add("Ok");
        adjectives.put(Mood.INDIFFERENT, indiff_adj);

        List<Verb> good_verb = new ArrayList<>();
        good_verb.add(new Verb("Write", "Writing"));
        good_verb.add(new Verb("Work", "Working"));
        good_verb.add(new Verb("Swim", "Swimming"));
        good_verb.add(new Verb("Run", "Running"));
        good_verb.add(new Verb("Code", "Coding"));

        verbs.put(Mood.GOOD, good_verb);

        List<Verb> bad_verb = new ArrayList<>();
        bad_verb.add(new Verb("Steal", "Stealing"));
        bad_verb.add(new Verb("Murder", "Mudering"));
        bad_verb.add(new Verb("Cheat", "Cheating"));
        bad_verb.add(new Verb("Fight", "Fighting"));
        bad_verb.add(new Verb("Bully", "Bullying"));
        verbs.put(Mood.BAD, bad_verb);

        List<Verb> indiff_verb = new ArrayList<>();
        indiff_verb.add(new Verb("Steal", "Stealing"));
        indiff_verb.add(new Verb("Murder", "Mudering"));
        indiff_verb.add(new Verb("Cheat", "Cheating"));
        indiff_verb.add(new Verb("Fight", "Fighting"));
        indiff_verb.add(new Verb("Bully", "Bullying"));
        indiff_verb.add(new Verb("Write", "Writing"));
        indiff_verb.add(new Verb("Work", "Working"));
        indiff_verb.add(new Verb("Swim", "Swimming"));
        indiff_verb.add(new Verb("Run", "Running"));
        indiff_verb.add(new Verb("Code", "Coding"));
        verbs.put(Mood.INDIFFERENT, indiff_verb);


        List<String> good_sentences = new ArrayList<>();
        good_sentences.add("That's {_a}! I like {_}!");
        good_sentences.add("{a}! We can {} together. I also like {_vp}.");
        good_sentences.add("Yes, I love {}! This is going to be {_a}.");
        sentences.put(Mood.GOOD, good_sentences);

        List<String> bad_sentences = new ArrayList<>();
        bad_sentences.add("That's {_a}! I don't like {}!");
        bad_sentences.add("Isn't that just {_a}! I'd rather be {_vp}.");
        bad_sentences.add("No, I think {} is {_a}!");
        sentences.put(Mood.BAD, bad_sentences);

        List<String> indiff_sentences = new ArrayList<>();
        indiff_sentences.add("Well, that's {_a}. So {_a}.");
        indiff_sentences.add("{a}. {vp}, {_vp}, {_vp} or {}, its {_a}.");
        indiff_sentences.add("No, I think {} is {_a}.");
        sentences.put(Mood.INDIFFERENT, indiff_sentences);
    }

    private String getRandom(Map<Mood, List<String>> list, Mood mood) {
        return list.get(mood).get(random.nextInt(list.get(mood).size()));
    }

    private String getRandom(Map<Mood, List<Verb>> list, Mood mood, boolean present) {
        Verb verb = list.get(mood).get(random.nextInt(list.get(mood).size()));
        return present ? verb.present : verb.infinitive;
    }

    public String getResponse(String input) {
        if (input == null || input.equals("")) return null;
        String[] sentence = input.split(" ");
        String respons = getRandom(sentences, mood);
        while (true) {
            if(respons.contains("{}")) {
                respons = respons.replaceFirst("(\\{})", sentence[sentence.length - 1].replaceAll("\\W", ""));
                continue;
            }
            if(respons.contains("{_}")) {
                respons = respons.replaceFirst("(\\{_})", sentence[sentence.length - 1].replaceAll("\\W", "").toLowerCase());
                continue;
            }
            if(respons.contains("{a}")) {
                respons = respons.replaceFirst("(\\{a})", getRandom(adjectives, mood));
                continue;
            }
            if(respons.contains("{_a}")) {
                respons = respons.replaceFirst("(\\{_a})", getRandom(adjectives, mood).toLowerCase());
                continue;
            }
            if(respons.contains("{v}")) {
                respons = input.replaceFirst("(\\{v})", getRandom(verbs, mood, false));
                continue;
            }
            if(respons.contains("{_v}")) {
                respons = respons.replaceFirst("(\\{_v})", getRandom(verbs, mood,false).toLowerCase());
                continue;
            }
            if(respons.contains("{vp}")) {
                respons = respons.replaceFirst("(\\{vp})", getRandom(verbs, mood, true));
                continue;
            }
            if(respons.contains("{_vp}")) {
                respons = respons.replaceFirst("(\\{_vp})", getRandom(verbs, mood,true).toLowerCase());
                continue;
            }
            return respons;
        }
    }

    public String getName() {
        return name;
    }
}

class Bot_LIAM extends Bot {

    public Bot_LIAM() {
        super("Liam", Mood.GOOD);

    }
}

class Bot_Hannah extends Bot {

    public Bot_Hannah() {
        super("Hannah",Mood.BAD);

    }
}

class Bot_Sara extends Bot {

    public Bot_Sara() {
        super("Sara",Mood.INDIFFERENT);

    }
}

class Bot_John extends Bot {
    private final Map<String, Node> graph;

    public Bot_John() {
        super("John", null);
        graph = new HashMap<>();
        init();
    }

    private void init() {
        try {
            buildTree();
        }
        catch (Exception e) {
            System.out.println("Could not read file");
        }
    }

    private void buildTree() throws FileNotFoundException {
        File file = new File("C:\\Users\\bernt\\IdeaProjects\\chatbot-project\\botData\\quotes_db.txt");
        Scanner in = new Scanner(new FileInputStream(file), StandardCharsets.UTF_8);
        while (in.hasNextLine()) {
            String input = in.nextLine().toLowerCase();
            String[] inputWords = input.split(" ");

            Node prevWord = null;

            for(String s : inputWords) {
                boolean hasPunctuation = s.matches("[\\w]+[\\W]+");
                char punctuation = hasPunctuation ? s.charAt(s.length() - 1) : 0;
                s = s.replaceAll("[-.,?!:;()]", "");

                Node currentWord = graph.getOrDefault(s, new Node(s));
                currentWord.occurrence++;

                if(hasPunctuation)
                    currentWord.getPunctuation(punctuation).increase();

                if(prevWord != null)
                    prevWord.edges.add(currentWord);

                graph.put(s, currentWord);
                prevWord = currentWord;
            }
        }
    }

    private Node getNode(String word) {
        Node node = graph.get(word);
        if(node == null) {
            int n = random.nextInt(graph.size());
            for(var nn : graph.values())
                if(n-- == 0) {
                    node = nn;
                    break;
                }
        }
        return node;
    }

    @Override
    public String getResponse(String input) {
        input = input.toLowerCase();
        input = input.replaceAll("[-.,:;?!]","");

        List<String> pickedWords = new ArrayList<>();
        String[] inputWords = input.split(" ");
        String lastWord = inputWords[inputWords.length - 1];

        int responseLength = random.nextInt(15)+5;
        int timesSinceLastComma = 0;
        boolean done = false;

        Node prevWord = getNode(lastWord);

        while (!done) {
            Node current = prevWord.getNext();
            String picked = current.word;

            if(pickedWords.isEmpty() && picked.length() > 0)
                picked = Character.toUpperCase(picked.charAt(0)) + picked.substring(1);

            if(pickedWords.size() >= responseLength) {
                if(current.suitableEnd() && current.punctuations.size() > 0) {
                    char punctuation = current.punctuations.peek().character;
                    picked += punctuation == ',' ? '.' : punctuation;
                    done = true;
                }
                if(pickedWords.size() > responseLength + 20) {
                    picked += ".";
                    done = true;
                }
            }

            if(!done && current.punctuations.size() > 0 && current.punctuations.peek().character == ',' && timesSinceLastComma > 5)
                if(random.nextBoolean()) {
                    picked += ',';
                    timesSinceLastComma = 0;
                }

            prevWord = current;
            timesSinceLastComma++;
            pickedWords.add(picked);
        }
        return String.join(" ", pickedWords);
    }

    static class Node {
        private final String word;
        private final Set<Node> edges = new HashSet<>();
        private int occurrence = 0;
        private final PriorityQueue<Punctuation> punctuations = new PriorityQueue<>(Comparator.comparingInt(a -> -a.score));
        private final Random random = new Random();

        public Node(String word) {
            this.word = word;
        }

        public Punctuation getPunctuation(char c) {
            for(var p : punctuations) {
                if(p.character == c) return p;
            }
            Punctuation punctuation = new Punctuation(c);
            punctuations.add(punctuation);
            return punctuation;
        }

        public boolean suitableEnd() {
            return (double) punctuations.size() / occurrence >= 0.3 && punctuations.size() > 0 && punctuations.peek().character != ',';
        }

        public Node getNext() {
            int m = random.nextInt(edges.size());
            for(Node n : edges) {
                if(m-- == 0) return  n;
            }
            return null;
        }
    }

    static class Punctuation {
        char character;
        int score;

        public Punctuation(char character) {
            this.character = character;
            this.score = 0;
        }

        public void increase() {
            score++;
        }
    }
}

