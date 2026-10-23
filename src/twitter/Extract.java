package twitter;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Extract {

    /**
     * Get the time period spanned by tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return a minimum-length time interval that contains the timestamp of
     *         every tweet in the list.
     */
    public static Timespan getTimespan(List<Tweet> tweets) {
        if (tweets.isEmpty()) {
            throw new IllegalArgumentException("Tweet list cannot be empty");
        }
        Instant minTime = tweets.get(0).getTimestamp();
        Instant maxTime = minTime;
        
        for (Tweet tweet : tweets) {
            Instant t = tweet.getTimestamp();
            if (t.isBefore(minTime)) {
                minTime = t;
            }
            if (t.isAfter(maxTime)) {
                maxTime = t;
            }
        }
        return new Timespan(minTime, maxTime);
    }

    /**
     * Get usernames mentioned in a list of tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return the set of usernames who are mentioned in the text of the tweets.
     *         A username-mention is "@" followed by a Twitter username (as
     *         defined by Tweet.getAuthor()'s spec).
     *         The username-mention cannot be immediately preceded or followed by any
     *         character valid in a Twitter username.
     *         For this reason, an email address like bitdiddle@mit.edu does NOT 
     *         contain a mention of the username mit.
     *         Twitter usernames are case-insensitive, and the returned set may
     *         include a username at most once.
     */
    public static Set<String> getMentionedUsers(List<Tweet> tweets) {
        Set<String> mentionedUsers = new HashSet<>();
        // Regex to match '@' preceded/followed by non-username characters and capturing valid username chars [A-Za-z0-9_]
        Pattern pattern = Pattern.compile("(?<![A-Za-z0-9_])@([A-Za-z0-9_]+)(?![A-Za-z0-9_])");
        
        for (Tweet tweet : tweets) {
            Matcher matcher = pattern.matcher(tweet.getText());
            while (matcher.find()) {
                mentionedUsers.add(matcher.group(1).toLowerCase());
            }
        }
        return mentionedUsers;
    }
}