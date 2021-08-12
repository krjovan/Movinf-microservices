package api.composite.movie;

public class ReviewSummary {

    private final int reviewId;
    private final String title;
    private final String content;
    private final int rating;

    public ReviewSummary(int reviewId, String title, String content, int rating) {
        this.reviewId = reviewId;
        this.title = title;
        this.content = content;
        this.rating = rating;
    }

    public int getReviewId() {
        return reviewId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
    
    public int getRating() {
        return rating;
    }
}
