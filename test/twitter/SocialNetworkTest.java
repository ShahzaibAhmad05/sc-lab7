/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * Testing strategy for guessFollowsGraph(tweets):
     *
     * Partition the input as follows:
     *   number of tweets: 0, 1, > 1
     *   mentions per tweet: 0, 1, > 1
     *   tweets per author: 1, > 1
     *   case of usernames: same case as the author, different case
     *
     * The postcondition is underdetermined: an implementation may add extra
     * edges from other kinds of evidence. So these tests only check that the
     * required @-mention edges are present, never that the graph has exactly
     * those edges. The one exception is a graph with a single author and no
     * mentions, where no edge is possible because users can't follow
     * themselves. Usernames are compared case-insensitively.
     *
     * Testing strategy for influencers(followsGraph):
     *
     * Partition the input as follows:
     *   number of users: 0, 1, > 1
     *   follower count of the top user: 0, 1, > 1
     *   ties in follower count: none, some users tied
     *
     * The result must hold every distinct user exactly once, in descending
     * order of follower count. Tied users may appear in either order.
     *
     * Each test below says which parts it covers.
     */

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");

    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    // covers 0 tweets
    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(new ArrayList<>());

        assertTrue("expected empty graph", followsGraph.isEmpty());
    }

    // covers 1 tweet, 0 mentions
    @Test
    public void testGuessFollowsGraphNoMentions() {
        Tweet tweet = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet));

        for (Set<String> follows : followsGraph.values()) {
            assertTrue("expected no one followed", follows.isEmpty());
        }
    }

    // covers 1 tweet, 1 mention, mention in a different case from the stored name
    @Test
    public void testGuessFollowsGraphSingleMention() {
        Tweet tweet = new Tweet(1, "ernie", "hey @Bert, want to get rubber duckies?", d1);
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet));

        assertTrue("expected ernie to follow bert", follows(followsGraph, "ernie", "bert"));
    }

    // covers 1 tweet, > 1 mentions
    @Test
    public void testGuessFollowsGraphMultipleMentions() {
        Tweet tweet = new Tweet(1, "alyssa", "lunch with @bbitdiddle and @Eva_Lu today", d1);
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet));

        assertTrue("expected alyssa to follow bbitdiddle", follows(followsGraph, "alyssa", "bbitdiddle"));
        assertTrue("expected alyssa to follow eva_lu", follows(followsGraph, "alyssa", "eva_lu"));
    }

    // covers > 1 tweets, > 1 tweets per author, author name in a different case
    @Test
    public void testGuessFollowsGraphMultipleTweetsOneUser() {
        Tweet tweet1 = new Tweet(1, "ernie", "@bert where is my rubber duck?", d1);
        Tweet tweet2 = new Tweet(2, "ERNIE", "@bert found it! also hi @elmo", d2);
        Tweet tweet3 = new Tweet(3, "ernie", "@Bert never mind", d3);
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(
                Arrays.asList(tweet1, tweet2, tweet3));

        assertTrue("expected ernie to follow bert", follows(followsGraph, "ernie", "bert"));
        assertTrue("expected ernie to follow elmo", follows(followsGraph, "ernie", "elmo"));
        assertEquals("expected ernie to be one key", 1, countKeys(followsGraph, "ernie"));
    }

    // covers 0 users
    @Test
    public void testInfluencersEmpty() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertTrue("expected empty list", influencers.isEmpty());
    }

    // covers 1 user, top count 0
    @Test
    public void testInfluencersSingleUserNoFollowers() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("alyssa", new HashSet<>());
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        // the spec still lists every user, so alyssa appears with zero followers
        assertEquals("expected only alyssa", 1, influencers.size());
        assertEquals("alyssa", influencers.get(0).toLowerCase());
    }

    // covers > 1 users, top count 1
    @Test
    public void testInfluencersSingleInfluencer() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("ernie", new HashSet<>(Arrays.asList("bert")));
        List<String> influencers = SocialNetwork.influencers(followsGraph);

        assertEquals("expected ernie and bert", 2, influencers.size());
        assertEquals("expected bert first", "bert", influencers.get(0).toLowerCase());
        assertEquals("ernie", influencers.get(1).toLowerCase());
    }

    // covers > 1 users, top count > 1, no ties
    @Test
    public void testInfluencersMultipleInfluencers() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("a", new HashSet<>(Arrays.asList("c", "b")));
        followsGraph.put("b", new HashSet<>(Arrays.asList("c")));
        followsGraph.put("d", new HashSet<>(Arrays.asList("c", "b")));
        followsGraph.put("e", new HashSet<>(Arrays.asList("c")));
        // followers: c = 4, b = 2, a = 0, d = 0, e = 0
        List<String> influencers = toLowerCase(SocialNetwork.influencers(followsGraph));

        assertEquals("expected every user once", 5, influencers.size());
        assertEquals(new HashSet<>(Arrays.asList("a", "b", "c", "d", "e")), new HashSet<>(influencers));
        assertEquals("expected c first", "c", influencers.get(0));
        assertEquals("expected b second", "b", influencers.get(1));
    }

    // covers > 1 users, some users tied
    @Test
    public void testInfluencersTiedInfluence() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        followsGraph.put("a", new HashSet<>(Arrays.asList("x", "y")));
        followsGraph.put("b", new HashSet<>(Arrays.asList("x", "y")));
        followsGraph.put("c", new HashSet<>(Arrays.asList("z")));
        // followers: x = 2, y = 2, z = 1, a = b = c = 0
        List<String> influencers = toLowerCase(SocialNetwork.influencers(followsGraph));

        assertEquals("expected every user once", 6, influencers.size());
        assertEquals("expected x and y on top, in either order",
                new HashSet<>(Arrays.asList("x", "y")), new HashSet<>(influencers.subList(0, 2)));
        assertEquals("expected z third", "z", influencers.get(2));
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * SocialNetwork class that follows the spec. It will be run against several
     * staff implementations of SocialNetwork, which will be done by overwriting
     * (temporarily) your version of SocialNetwork with the staff's version.
     * DO NOT strengthen the spec of SocialNetwork or its methods.
     *
     * In particular, your test cases must not call helper methods of your own
     * that you have put in SocialNetwork, because that means you're testing a
     * stronger spec than SocialNetwork says. If you need such helper methods,
     * define them in a different class. If you only need them in this test
     * class, then keep them in this test class.
     */

    /** @return true if follower follows followee in graph, ignoring username case */
    private static boolean follows(Map<String, Set<String>> graph, String follower, String followee) {
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(follower)) {
                for (String user : entry.getValue()) {
                    if (user.equalsIgnoreCase(followee)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** @return number of keys in graph equal to username, ignoring case */
    private static int countKeys(Map<String, Set<String>> graph, String username) {
        int count = 0;
        for (String key : graph.keySet()) {
            if (key.equalsIgnoreCase(username)) {
                count++;
            }
        }
        return count;
    }

    private static List<String> toLowerCase(List<String> usernames) {
        List<String> result = new ArrayList<>();
        for (String username : usernames) {
            result.add(username.toLowerCase());
        }
        return result;
    }

}
