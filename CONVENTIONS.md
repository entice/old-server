#### General

_**Note** that these coding conventions are guidelines and can be broken with, if necessary, at an time in the code, especially if there are special constructs that are better of being aligned/written in a different way to make for easy reading and understanding._

#### Order in Typedefinitions etc.

_Especially for these rules: Please use common sense before using them._

1. Imports
2. Abstract types
3. Internal classes/traits/objects etc. that are public API (not for cake traits)
3. Public variables, functions etc. as desired
4. Private stuff

#### For the cake pattern

- ```extends``` describe a "I am a ..." relationship
- ```self: ??? =>``` describe a "I need to be used with a ..." relationship
- Structure roughly:
  1. Imports if any
  2. Abstract types etc.
  3. Needed references
  4. Provided references, functions etc.
  5. Public Stuff :P
  6. Private Stuff
