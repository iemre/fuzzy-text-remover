# Summary

fuzzy-text-remover matches & removes a given string (swear words, private addresses, or other things) from a given source string.
  
It can be used to detect swear words in user comments, or replace personal info from a source string.
 
 
# Example usage:

```
git clone git@github.com:iemre/fuzzy-text-remover.git
cd fuzzy-text-remover
mvn clean install
```

Add the maven dependency to your project

```
<dependency>
  <groupId>org.iemre</groupId>
  <artifactId>fuzzy-text-remover</artifactId>
  <version>0.2.0</version>
</dependency>
```


## Similarity metrics:

Supported similarity metrics:

* The Jaro-Winkler distance 
* Edit distance (Levenshtein distance)
  
 
The following examples make use of the edit distance. However, the user can make use of the Jaro-Winkler distance, by using, 
for example, the method `replaceStringByJwd`, instead of the method `replaceStringByEditDistance` in the examples below.

My personal opinion is that the Jaro-Winkler distance is a much better choice if the user does not know what edit distance value to use. 


## Use case 1:

As a user I would like to remove approximately matching swear words from my product comments:

```
 String result = TextRemoverUtils.replaceStringByEditDistance("This product is apples, "apple", "****", 1) {
```

The `result` is `This product is ****`. The last argument (edit distance) `1` says when the edit distance between any of 
 the words in `This product is apples` matches `apple` with a max edit distance of 1, replace it with `****`.


## Use case 2:

As a user I would like to know if there are any matching swear words in my product comments, but I don't necessarily want to 
remove them.

```
Set<String> matchingStrings = TextRemoverUtils.getMatchingStringsByEditDistance("This product is appl", "apple", 1);
if (matchingStrings.size > 0){ your code }
```

The word `appl` in the source string will match `apple` because the edit distance between them is less than or equal to `1`.  


## Use case 3:

As a user I would like to remove a private address from a source string.

```
String result = TextRemoverUtils.replaceMatchingWindowsByEditDistance("SE65 7AD Cool Street, 90 Hghway, in the middle of nowhere 
                                                        is where cool people hang out"
                                              "Highway 90, Cool Street, 7AD",
                                              "***",
                                              0.7,
                                              1);
```

As a user I am looking for "Highway 90, Cool Street, 7AD" in the source string (the first argument). The above method 
will apply a sliding window approach to the source string and match (without caring about the order of words) 
the segments of the source string that match at least the 70% of the given search string ("Highway 90, Cool Street, 7AD").

The last argument `1` is the max. allowed edit distance at the word level. 



## Acknowledgement and License

Please contact if you have questions/suggestions.

This library is mainly extracted from my other project: https://github.com/iemre/cognition. 

This project uses apache-commons StringUtils for its edit distance and JWD implementation.

License is Apache License 2.0.

