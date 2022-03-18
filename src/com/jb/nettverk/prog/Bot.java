package com.jb.nettverk.prog;

import org.w3c.dom.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
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
    private Mood mood;

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
            e.printStackTrace();
        }
    }

    private void buildTree() throws FileNotFoundException {
        File file = new File("C:\\Users\\bernt\\IdeaProjects\\chatbot-project\\botData\\quotes_db.txt");
        Scanner in = new Scanner(new FileInputStream(file), StandardCharsets.UTF_8);
        Node prevWord = null;
        while (in.hasNextLine()) {
            String input = in.nextLine().toLowerCase();
            String[] inputWords = input.split(" ");
            for(String s : inputWords) {
                boolean hasPunctuation = s.matches("[\\w]+[\\W]+");
                char punctuation = 'x';
                if(hasPunctuation) {
                     punctuation = s.charAt(s.length() - 1);
                }
                s = s.replaceAll("[-.,?!:;()]", "");
                Node currentWord = graph.getOrDefault(s, new Node(s));
                currentWord.occurrence++;
                if(hasPunctuation) {
                    currentWord.getPunctuation(punctuation).increase();
                }
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
            if(pickedWords.isEmpty()) {
                picked = Character.toUpperCase(picked.charAt(0)) + picked.substring(1);
            }
            if(pickedWords.size() >= responseLength) {
                if(current.suitableEnd()) {
                    char punctuation = current.punctuations.peek().character;
                    picked += punctuation == ',' ? '.' : punctuation;
                    done = true;
                }
                if(pickedWords.size() > responseLength + 20) {
                    picked += ".";
                    done = true;
                }
            }
            if(!done && current.punctuations.size() > 0 && current.punctuations.peek().character == ',' && timesSinceLastComma > 3) {
                if(random.nextBoolean()) {
                    picked += ',';
                    timesSinceLastComma = 0;
                }
            }
            prevWord = current;
            timesSinceLastComma++;
            pickedWords.add(picked);
        }
        return String.join(" ", pickedWords);
    }

    static class Node {
        private String word;
        private Set<Node> edges;
        private int fittingEndOfSentence;
        private int occurrence;
        private PriorityQueue<Punctuation> punctuations;
        private Random random;

        public Node(String word) {
            this.word = word;
            this.edges = new HashSet<>();
            fittingEndOfSentence = 0;
            occurrence = 0;
            punctuations = new PriorityQueue<>(Comparator.comparingInt(a -> -a.score));
            random = new Random();
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
            return (double) punctuations.size() / occurrence >= 0.3 && punctuations.peek().character != ',';
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

