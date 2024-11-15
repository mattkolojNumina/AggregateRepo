package victoryApp.gui;

import java.util.Map;
import java.util.Objects;
import java.util.List;

public class Grammar {

    private String filename;
    private int minAlphaNumeric = 0;
    private int maxAlphaNumeric = 0;
    private int minNumeric = 0;
    private int maxNumeric = 0;
    private String key;//a unique identifier for this  grammar, if it is changed this will indicate it needs to be recreated
    private Map<String, String> phraseMap;  //<phrase, codetoReturn>, Map of phrases used by 'standard' type grammars
    private List<String> phrases; //List of phrases used by 'concat' type grammars
    private String type; //Grammar type, can either be 'standard', or 'concat'

    public Grammar(String filename, int minAlphaNumeric, int maxAlphaNumeric, int minNumeric, int maxNumeric, Map<String, String> phraseMap, String key, List<String> phrases, String type) {
        this.filename = filename;
        this.minAlphaNumeric = minAlphaNumeric;
        this.maxAlphaNumeric = maxAlphaNumeric;
        this.minNumeric = minNumeric;
        this.maxNumeric = maxNumeric;
        this.phraseMap = phraseMap;
        this.key = key;
        this.phrases = phrases;
        this.type = type;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getMinAlphaNumeric() {
        return minAlphaNumeric;
    }

    public void setMinAlphaNumeric(int minAlphaNumeric) {
        this.minAlphaNumeric = minAlphaNumeric;
    }

    public int getMaxAlphaNumeric() {
        return maxAlphaNumeric;
    }

    public void setMaxAlphaNumeric(int maxAlphaNumeric) {
        this.maxAlphaNumeric = maxAlphaNumeric;
    }

    public int getMinNumeric() {
        return minNumeric;
    }

    public void setMinNumeric(int minNumeric) {
        this.minNumeric = minNumeric;
    }

    public int getMaxNumeric() {
        return maxNumeric;
    }

    public void setMaxNumeric(int maxNumeric) {
        this.maxNumeric = maxNumeric;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Map<String, String> getPhraseMap() {
        return phraseMap;
    }

    public void setPhraseMap(Map<String, String> phraseMap) {
        this.phraseMap = phraseMap;
    }

    public List<String> getPhrases() {
        return phrases;
    }

    public void setPhrases(List<String> phrases) {
        this.phrases = phrases;
    }

    public String getType() {
        return type;
    }

    public void setPhraseMap(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grammar grammar = (Grammar) o;
        return minAlphaNumeric == grammar.minAlphaNumeric && maxAlphaNumeric == grammar.maxAlphaNumeric && minNumeric == grammar.minNumeric && maxNumeric == grammar.maxNumeric && Objects.equals(phraseMap, grammar.phraseMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minAlphaNumeric, maxAlphaNumeric, minNumeric, maxNumeric, phraseMap);
    }

}
