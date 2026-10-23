# SE-314 Lab 07: Test First Programming II (Social Network Analysis)

Shahzaib Ahmad, CMS 505849, SE15A

Problem 3 of MIT 6.005 Problem Set 1 ("Tweet Tweet"), solved test first.

- `src/twitter/SocialNetwork.java`: `guessFollowsGraph()` infers that Ernie follows Bert if Ernie @-mentions Bert; `influencers()` sorts users by follower count, most followed first.
- `test/twitter/SocialNetworkTest.java`: the testing strategy and 10 test cases, written before the implementation.
- `src/twitter/Main.java`: prints the top 10 influencers. The MIT sample server no longer serves tweets, so it falls back to `sample-tweets.json`.

Extract and Filter are carried over from Lab 05.

## Running

Compile `src` and `test` with `lib/javax.json-1.0.jar` and JUnit 4 on the classpath, copy `sample-tweets.json` next to the compiled classes, and run with assertions enabled:

```
java -ea -cp <classpath> org.junit.runner.JUnitCore twitter.SocialNetworkTest
java -ea -cp <classpath> twitter.Main
```
