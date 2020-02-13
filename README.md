# Perfect Time
A proposal for keeping track of time perfectly. That is, you can use this to precisely and consistently pinpoint any moment in time.



## Definition
- When storing and making calculations, **always use strings**. This guarantees no loss in precision, at the cost of being slightly less performant on low-speed systems like embedded systems, but most modern machines should handle this fine.
- This format **does not acknowledge time zones**. That is to say, the 0th second starts in the time zone [UTC±00:00](https://en.wikipedia.org/wiki/UTC±00:00) (where it was at the start of 1970), but after that, it does not change based on any time zone. This format must never be localized. Any given Perfect Time timestamp must represent the same exact moment in time, regardless of where it is used.
- **The unit is one [SI second](https://en.wikipedia.org/wiki/Second#"Atomic"_second)**. Specifically, the duration of 9,192,631,770 periods of the radiation corresponding to the transition between the two hyperfine levels of the ground state of the caesium-133 atom at a temperature of 0 K.
- **Similar to [Unix time](https://en.wikipedia.org/wiki/Unix_time)**. That is, treat `0` as the very beginning of 1970, and each number represents the linear progression of time in seconds.
    - **[Leap Seconds](https://en.wikipedia.org/wiki/Leap_second) are ignored by this format.** That is, in the event of a Leap Second, instead of repeating a timestamp, the format marches on to the next. Although this may cause it to be offset from Unix Time and UTC, it also guarantees that a given timestamp always references the same exact point in time, and always represent a linear progression of time.
    - When archiving, **store it in base 10.** That is, using only the digits (U+0030 to U+0039 `0123456789`), an optional dash (U+002D `-`) to represent dates prior to 1970, and a singular, mandatory full stop for the radix point (U+002E `.`). Other characters are not to be used. **A Perfect Time string must match the regex [`/-?[0-9]+\.[0-9]{20}/`](https://regex101.com/r/tZ0nY9/3).**
    - When archiving, **always use 20 fractional digits** to represent parts of a second as small as one hundredth of one attosecond. For example, `1456020377.012345678911234567890`. This is precise enough to [track light traveling across an atom](https://www.wolframalpha.com/input/?i=lightspeed+*+0.01+attosecond).
        - When the data comes from systems that cannot track time that precisely, **fill untrackable digits with zeroes.** For instance, if a system can only track time as precise as milliseconds: `1456021196.01200000000000000000`.


## A note on coordination
This was designed to be an archival format. Its purpose is to be as accurate and precise as possible. If the system it's in is not properly coordinated, then this value might be offset by that same amount by which the system is offset. To ensure proper accuracy, calibrate your system clock regularly, especially before running software that relies on accurate time.
