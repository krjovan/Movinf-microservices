package api.core.trivia;

import java.sql.Date;

public class Trivia {
    private final int movieId;
    private final int triviaId;
    private final Date publishDate;
    private final String content;
	private final boolean spoiler;
    private final String serviceAddress;

    public Trivia() {
    	movieId = 0;
    	triviaId = 0;
    	publishDate = null;
        content = null;
        spoiler = false;
        serviceAddress = null;
    }

    public Trivia(
    	int movieId,
    	int triviaId,
    	Date publishDate,
    	String content,
    	boolean spoiler,
    	String serviceAddress) {
    	
        this.movieId = movieId;
        this.triviaId = triviaId;
        this.publishDate = publishDate;
        this.content = content;
        this.spoiler = spoiler;
        this.serviceAddress = serviceAddress;
    }

    public int getMovieId() {
		return movieId;
	}

	public int getTriviaId() {
		return triviaId;
	}

	public Date getPublishDate() {
		return publishDate;
	}

	public String getContent() {
		return content;
	}

	public boolean isSpoiler() {
		return spoiler;
	}

	public String getServiceAddress() {
        return serviceAddress;
    }
}
