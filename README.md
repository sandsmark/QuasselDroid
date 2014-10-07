QuasselDroid
============

Quassel is a distributed, decentralized IRC client, written using C++ and Qt.
QuasselDroid is a pure-java client for the Quassel core, allowing you to
connect to your Quassel core using your Android (TM) phone.

It requires a recent Android SDK , and the new build system.
- http://developer.android.com/sdk/index.html and
- http://tools.android.com/tech-docs/new-build-system#TOC-Contributing

It uses the following extra projects (though all support libraries are included
for your convenience):
    - Otto: http://square.github.com/otto/
    - Android Support Library: http://developer.android.com/tools/extras/support-library.html
    - Crittercism: https://www.crittercism.com/developers/docs-android
    - ActionBarSherlock: http://actionbarsherlock.com/
NB! Android Support Library requires the corresponding package to be installed in the SDK manager 

Building:
Building is done using gradle. Run "gradlew tasks" to see possible build tasks. Some useful tasks are
assemble and installDebug
NB! I have found some bugs with the build system where i have to run --info and or --stacktrace to not get errors
during build process.


We do support encryption, but no compression support for now (qCompress should,
in theory, be compatible with Deflater if you do some pre-parsing massaging on
it, but I haven't gotten it to work just yet).


Authors (in chronological order of appearance):
Frederik M. J. Vestre (freqmod)
    (Initial qdatastream deserialization attempts)
Martin "Java Sucks" Sandsmark (sandsmark)
    (Protocol implementation, (de)serializers, project (de)moralizer)
Magnus Fjell (magnuf)
    (GUI, Android stuff)
Ken Børge Viktil (Kenji)
    (GUI, Android stuff)


Homepage: http://github.com/sandsmark/QuasselDroid
Beta Builds: https://plus.google.com/communities/104094956084217666662

