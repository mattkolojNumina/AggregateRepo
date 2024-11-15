package victoryApp.gui;

import java.util.Locale;
import java.util.Map;

public class ResourceList {
    //tts
    Locale locale;
    Float volume;
    Float rate;
    Float pitch;

    //scanner
    boolean scanAhead;
    boolean logTimes;

    //voice
    private Map<Integer, Grammar> grammarMap;
    private Map<String, String> numberMap;
    private Map<String, String> alphaMap;
    private String repeatPhrase;
    private String wakeWord;
    private String sleepWord;
    private String terminalPhrase;
    private boolean numericUsesTerminalPhrase;
    private boolean alphanumericUsesTerminalPhrase;

    //keen settings
    private final Float KASRVadTimeoutEndSilenceForGoodMatch;
    private final Float KASRVadTimeoutEndSilenceForAnyMatch;
    private final Float KASRVadTimeoutMaxDuration;
    private final Float KASRVadTimeoutForNoSpeech;

    // //string translations
    // private final Map<String, String> translationMap;

    //we use this class for setup of session
    //including voice, tts, and onscreen translations
    //we consume this file upon connection and set up session state, creating necessary files from this data
    public ResourceList(
      Map<Integer, Grammar> grammarMap, 
      Map<String, String> numberMap, 
      Map<String, String> alphaMap, 
      Locale locale, 
      Float volume, 
      Float rate, 
      Float pitch, 
      String repeatPhrase, 
      String wakeWord, 
      String sleepWord, 
      String terminalPhrase,
      boolean numericUsesTerminalPhrase,
      boolean alphanumericUsesTerminalPhrase,
      Float KASRVadTimeoutEndSilenceForGoodMatch,
      Float KASRVadTimeoutEndSilenceForAnyMatch,
      Float KASRVadTimeoutMaxDuration,
      Float KASRVadTimeoutForNoSpeech
    ) {
        this.grammarMap = grammarMap;
        this.numberMap = numberMap;
        this.alphaMap = alphaMap;
        this.locale = locale;
        this.volume = volume;
        this.rate = rate;
        this.pitch = pitch;
        this.repeatPhrase = repeatPhrase;
        this.wakeWord = wakeWord;
        this.sleepWord = sleepWord;
        this.terminalPhrase = terminalPhrase;
        this.numericUsesTerminalPhrase = numericUsesTerminalPhrase;
        this.alphanumericUsesTerminalPhrase = alphanumericUsesTerminalPhrase;
        this.KASRVadTimeoutEndSilenceForGoodMatch = KASRVadTimeoutEndSilenceForGoodMatch;
        this.KASRVadTimeoutEndSilenceForAnyMatch = KASRVadTimeoutEndSilenceForAnyMatch;
        this.KASRVadTimeoutMaxDuration = KASRVadTimeoutMaxDuration;
        this.KASRVadTimeoutForNoSpeech = KASRVadTimeoutForNoSpeech;
    }

    public Map<Integer, Grammar> getGrammarMap() {
        return grammarMap;
    }

    public Locale getLocale() {
        return locale;
    }

    public Float getVolume() {
        return volume;
    }

    public Float getRate() {
        return rate;
    }

    public Float getPitch() {
        return pitch;
    }

    // public Map<String, String> getTranslationMap() {
    //     return translationMap;
    // }

    // public boolean isScanAhead() {
    //     return scanAhead;
    // }

    // public boolean isLogTimes() {
    //     return logTimes;
    // }

    public Map<String, String> getNumberMap() {
        return numberMap;
    }

    public Map<String, String> getAlphaMap() {
        return alphaMap;
    }

    public String getRepeatPhrase() {
        return repeatPhrase;
    }

    public String getWakeWord() {
        return wakeWord;
    }

    public String getSleepWord() {
        return sleepWord;
    }

    public String getTerminalPhrase() {
        return terminalPhrase;
    }

    public boolean isNumericUsesTerminalPhrase() {
        return numericUsesTerminalPhrase;
    }

    public boolean isAlphanumericUsesTerminalPhrase() {
        return alphanumericUsesTerminalPhrase;
    }

    public void setNumberMap(Map<String, String> numberMap) {
        this.numberMap = numberMap;
    }

    public void setAlphaMap(Map<String, String> alphaMap) {
        this.alphaMap = alphaMap;
    }

    public void setRepeatPhrase(String repeatPhrase) {
        this.repeatPhrase = repeatPhrase;
    }

    public void setWakeWord(String wakeWord) {
        this.wakeWord = wakeWord;
    }

    public void setSleepWord(String sleepWord) {
        this.sleepWord = sleepWord;
    }

    public void setTerminalPhrase(String terminalPhrase) {
        this.terminalPhrase = terminalPhrase;
    }

    public void setNumericUsesTerminalPhrase(boolean numericUsesTerminalPhrase) {
        this.numericUsesTerminalPhrase = numericUsesTerminalPhrase;
    }

    public void setAlphanumericUsesTerminalPhrase(boolean alphanumericUsesTerminalPhrase) {
        this.alphanumericUsesTerminalPhrase = alphanumericUsesTerminalPhrase;
    }

    public Float getKASRVadTimeoutEndSilenceForGoodMatch() {
        return KASRVadTimeoutEndSilenceForGoodMatch;
    }

    public Float getKASRVadTimeoutEndSilenceForAnyMatch() {
        return KASRVadTimeoutEndSilenceForAnyMatch;
    }

    public Float getKASRVadTimeoutMaxDuration() {
        return KASRVadTimeoutMaxDuration;
    }

    public Float getKASRVadTimeoutForNoSpeech() {
        return KASRVadTimeoutForNoSpeech;
    }
}
