package api.composite.movie;

public class TriviaSummary {

    private final int triviaId;
    private final String content;
    private final boolean spoiler;

    public TriviaSummary(int triviaId, String content, boolean spoiler) {
        this.triviaId = triviaId;
        this.content = content;
        this.spoiler = spoiler;
    }

    public int getTriviaId() {
        return triviaId;
    }

    public String getContent() {
        return content;
    }

    public boolean isSpoiler() {
        return spoiler;
    }
}
