# Perfect Time
A library for keeping track of time perfectly. That is, you can use this to precisely pinpoint an insanely small moment in time.

## Basic premises
- **Always use strings to store time**, to guarantee no loss in precision. This makes it slightly less performant on low-speed systems like digital watches and flip phones, but all modern systems should handle this fine.
- **Only store in GMT** (offset +00.00).
- **The [speed of time is tracked at 1G](https://en.wikipedia.org/wiki/Gravitational_time_dilation).**
- **Use [Unix time](https://en.wikipedia.org/wiki/Unix_time)** to represent time. That is, treat `0` as the very beginning of 1970, and each number represents the linear progression of time in seconds.
	- Except: **[Leap Seconds](https://en.wikipedia.org/wiki/Leap_second) are added to the time stamp.** That is, in the event of a Leap Second, instead of repeating a second, the timestamp marches on to the next. Although this may cause it to be offset from UTC, it also guarantees that a given timestamp always references the same exact point in time, and always represent a linear progression of time.
	- **Store it in base 10.** That is, using only the digits (U+0030 to U+0039 `0123456789`), an optional dash (U+002D `-`) to represent dates prior to 1970, and a singular, mandatory full stop for the radix point (U+002E `.`). Other characters are not to be used. **A Perfect Time string must match the regex [`/-?[0-9]+\.[0-9]{18}/`](https://regex101.com/r/tZ0nY9/1).**
	- **Use 18 fractional digits** to represent parts of a second as small as an attosecond. For example, `1456020377.0123456789112345678`. This is precise enough to [track light traveling across atoms](http://www.wolframalpha.com/input/?i=distance+light+travels+in+1+attosecond).
		- When the data comes from systems that cannot track time that precisely, **fill untrackable digits with zeroes.** For instance, if a system can only track time as precise as milliseconds: `1456021196.012000000000000000`.
    
    
## A note on coordination
This is meant to be an archival format. It's designed to be accurate and precise in the reference frame of the system which records it. If the system it's in is not properly coordinated with UTC, then this value will be offset by the same amount by which the system is offset. To ensure proper accuracy, calibrate your system clock regularly, especially before running software that relies on accurate time.