## Getting Started ##

`git clone --recurse-submodules https://gitlab.com/NuminaGroup/numina-voice-android.git`

then open the resulting directory as project in android studio (which may require updates and such)

you should be able to build and run on a connected device at this point
(to connect with server, you may need to alter the ip/port in the settings activity)

If you just need an android device build, you can probably grab an apk off the releases page instead of using android studio.

to create a new tag/release, you want to update the version in code, then create a tag that matches it.
It will take 5-10 minutes to build the apks and then it will post on the releases page with download links.
This will ensure the version in the about page will match the tag version

## Versioning, Tagging and Releasing ##

**This document covers versioning on Victory voice, how to maintain the values, and create tags to trigger the ci/cd pipeline to create releases.**

_note: SNAPSHOT version indicates an unreleased build_

_the .debug suffix signifies a debug build_

_the apks from release page should be neither unless the build was made incorrectly_

## Testing ##


Testing Methodology for RDS Voice

want to test these features thoroughly + any other functionality:

**1. server interactions**
- ensure the batch picking, manual picking, full case picking work as expected with the sample data
  (this normally should not change per apk version but only if the underlying server code or grammars have changed)
- enhanced functionality such as scan queuing or transaction timing needs to be enabled via server to test
- test wifi connectivity by walking out of range, waiting awhile, and then coming back to ensure reconnects as expected.
- getting text translations from server to update ui (NEED TO TEST)

**2. voice interactions**
- test voice login, settings > enable voice login
- should be able to login via voice in English with 3 or 4 digits + ok (hardcoded onto device at start before any server interaction) (TODO login with keen)
- should 'work' with device without headset though will try to respond to TTS messages as input
- upon connecting or reconnecting a headset should work with headset mic/speakers
- ensure the log off procedure exits the application (please log off, with repeat for confirmation on certain allowed grammars)
- move headset out of bluetooth range and back
- test with background noise
- with keen, can test that out of order phrases are not recognized (picking batch vs batch picking)
- test numerals / alphanumeric input
- test all of this with another language (TODO for keen)
- ensure options persist after changing (server implementation required)

**3. scans**
- ensure scans work as expected
- both with internal scanner (if available) and ring scanner
- disconnect / reconnect scanner
- move scanner out of bluetooth range and back
- test camera scan option


**4. ui**
- ensure UI looks consistent in landscape/portrait on WT6000/WT6300/TC52/TC21/TC53
- ensure options / log / main views all look consistent.

**5. keep track of which zebra versions we have tested**
- currently using for TC21/52 11-26-05.00-RG-U07-STD-HEL-04 (Android 11) and not seeing issues though we saw some on earlier android 11 builds


[FYE_Voice_Prompts_and_Responses_K_2_.docx](uploads/522e545a6b2a0d9fcdb3330a77a06f03/FYE_Voice_Prompts_and_Responses_K_2_.docx)

[fullcase3.pdf](uploads/f38b7206347c7add53b24cab80b7ab7e/fullcase3.pdf)

[Operators.pdf](uploads/8c2d0a812627e95cb3db266c755f63eb/Operators.pdf)

[printers.pdf](uploads/e68b0507f51ed314845201fb465617bf/printers.pdf)

[manual.pdf](uploads/a2e442589407457dde40aa7c787c4df5/manual.pdf)

[lpns.pdf](uploads/3fd0b2755a46a545bd733fa97a8a4948/lpns.pdf)

### **Determining Device Version Information(from device)**

you can determine the version on device in 3 ways:

1. This method you can view the version without opening app (or before first run):

long press (on either settings activity or main activity)

more info

version is at bottom

2. Within Main App on Settings Screen, Version / Voice version should be visible from bottom of pull out settings window when enabled by server.
3. Within Settings App (standalone activity triggered by the setup link on main screen) Version / Voice version should be visible on either portrait/landscape here

### **Determining Device Version Information(from server)** ###

During connection, many device metrics are sent including APK, Android Major version, specific OS Build, etc. See [here](https://gitlab.com/NuminaGroup/numina-shared-library/-/blob/main/app/src/main/java/com/numinagroup/sharedlibrary/serviceObjects/ConnectionResponse.java?ref_type=heads)

### **Setting Device Version Information(from codebase)** ###

From within the codebase, the version should be set in app level [build.gradle ](https://gitlab.com/NuminaGroup/rds-voice-android-gui/-/blob/master/app/build.gradle?ref_type=heads)(search versionName), the snapshot should be removed when creating a versioned release (and thus version finalized). After release and development again begins, the version should be incremented and snapshot added to signify something beyond the last released version.

**Note**: _This version corresponds exactly to the version in the above steps to identify version from device or server._

_It is a convention to match the TAG version, it is easy to trigger the CI/CD on tag, and makes it easier to keep track of releases (can search tag / release matches tag etc)_

_But this isn't required, so if you don't maintain this alignment it may get confusing (IE you could TAG "crazyTAGMessage" and it would create a matching release despite not matching the versionName in build.gradle which is what will be reported by device and server - not your TAG)._

**For Victory / Pico I am currently using version 0.x.x**

**For RDS Voice / Keen Non Concat I 2.0.x (unmaintained)**

**For RDS Voice / Keen Concat I 1.0.x (unmaintained)**

Or feel free to alter that design as needed of course, that is just the pattern I have been doing. Initially tried to follow semantic versioning but with the multiple releases of picovoice/keen this seemed better than long version names (was complained about)

version names are arbitrary though, so change as needed

### **Triggering Builds** ###

To create a new release, commit your finalized version # to build gradle as described above. **Create a tag** (via the tags page) Enter your tag message and choose your branch you committed to. **This triggers the CI/CD pipeline**, will run assemble release with the signing key (creates a production build for Picovoice / play store standards),

### **Editing the CI/CD pipeline** ###

gitlab \> build \> pipeline editor

there is a script there if you need to adjust it. There are a few stages, defined by yml, uses a docker image with android / git commands to generate apk and create release with tag message

if there is an issue with the pipeline, it will email a warning message and the logs are under pipelines

### **Branch Info** ###

Victory - Pico is master / shared library (main)

Victory - Keen Non Concat is keen/ shared library is (keenVictory)

Victory - Keen Concat is keen2/ shared library is (keenVictory)

I only recently realized you could have multiple tags per commit, so may start tagging numina shared library with like "picoVictory1.x.x" and could also be tagged "rdsVoice0.x.x" on same commit but have a record beyond commit hash (which works, but harder to differentiate). Even though there is no ci/cd or need for a release here it may help organizing things.

[Documentation Wiki](https://gitlab.com/NuminaGroup/numina-voice-android/-/wikis/RDS-Voice-Android-Documentation)

## **List of server commands**

<table>
<tr>
<th>command</th>
<th>data</th>
<th>description</th>
</tr>
<tr>
<td>voiceRequest</td>
<td>

[object](https://gitlab.com/NuminaGroup/numina-shared-library/-/blob/main/app/src/main/java/com/numinagroup/sharedlibrary/serviceObjects/VoiceRequest.java) containing single Integer 'grammar'
</td>
<td>send int of grammar, start listening at that index</td>
</tr>
<tr>
<td>listenRequest</td>
<td>object containing single Boolean 'listenEnabled'</td>
<td>send boolean to pause or continue listening</td> 
</tr>
<tr>
<td>scanRequest</td>
<td>object containing single Boolean 'scanEnabled'</td>
<td>send boolean to pause or continue scanning</td>
</tr>
<tr>
<td>speechRequest</td>
<td>object containing single List 'phrases'</td>
<td>send list of phrases to speak immediately</td>
</tr>
<tr>
<td>

[resourceList](https://gitlab.com/NuminaGroup/numina-voice-android/-/wikis/Resource-List)
</td>
<td>

[object](https://gitlab.com/NuminaGroup/numina-voice-android/-/wikis/Resource-List) containing resource configurations
</td>
<td>

send [object](https://gitlab.com/NuminaGroup/numina-voice-android/-/wikis/Resource-List) containing base 64 list of grammars to initialize pico voice
</td>
</tr>
<tr>
<td>error</td>
<td>object containing single String 'text'</td>
<td>send string, right now this just triggers toast event</td>
</tr>
<tr>
<td>vibrate</td>
<td>object containing single int 'ms'</td>
<td>send int, vibrate for int ms</td>
</tr>
<tr>
<td>quit</td>
<td>no data required</td>
<td>exit program immediately</td>
</tr>
<tr>
<td>dumpLogs</td>
<td>no data required</td>
<td>dump and send logs to server</td>
</tr>
<tr>
<td>serverMessage</td>
<td>

[object](https://gitlab.com/NuminaGroup/numina-voice-android/-/blob/master/app/src/main/java/com/numinagroup/androidtcpclient/serviceobjects/ServerMessageServiceObject.java) containing 3 strings, the server message text, the text color, and the background color.

The text color and background color will be in hex code format, in the order of argb. They also must match the following constraints:

\-Exact length of 8 characters

\-Contains only valid hex code characters 0-9 and/or A-F
</td>
<td>sends a server message to be displayed to the user</td>
</tr>
</table>

**Messages From Client:**

<table>
<tr>
<th>command</th>
<th>data</th>
<th>description</th>
</tr>
<tr>
<td>errorMessage</td>
<td>string</td>
<td>message string of error</td>
</tr>
<tr>
<td>connect</td>
<td>

[Connection Response](https://gitlab.com/NuminaGroup/numina-voice-android/-/blob/master/app/src/main/java/com/numinagroup/androidtcpclient/models/ConnectionResponse.java?ref_type=heads)
</td>
<td>

is sent immediately upon connection, has the following format:

```plaintext
{
  "androidVersion": "11",
  "buildModel": "TC21",
  "deviceID": "c84b1c5dc7ea",
  "operatorID": "606",
  "deviceOSBuild": "11-26-05.00-RG-U07-STD-HEL-04",
  "pairedDeviceList": "RS5100 S20319523021103 , Zebra HS3100",
  "releaseMode": "Debug",
  "version": "0.3.7.debug",
  "voiceVersion": "Rhino: 2.2.0 Porcupine: 2.1.0"
}
```
</td>
</tr>
<tr>
<td>voiceReady</td>
<td>

[Connection Response](https://gitlab.com/NuminaGroup/numina-voice-android/-/blob/master/app/src/main/java/com/numinagroup/androidtcpclient/models/ConnectionResponse.java?ref_type=heads)
</td>
<td>is sent after processing resourcelist, should have duplicate data as connect but indicates we are ready for voice commands</td>
</tr>
<tr>
<td>setUserVolume</td>
<td>float</td>
<td>sets the user volume (TTS)</td>
</tr>
<tr>
<td>setUserPitch</td>
<td>float</td>
<td>sets user pitch (TTS)</td>
</tr>
<tr>
<td>setUserRate</td>
<td>float</td>
<td>sets the user speed (TTS)</td>
</tr>
<tr>
<td>setUserSensitivity</td>
<td>float</td>
<td>sets the user sensitivity for rhino (only for picovoice versions)</td>
</tr>
<tr>
<td>guiResponse</td>
<td>

[GUIResponse](https://gitlab.com/NuminaGroup/rds-voice-android-gui/-/blob/master/app/src/main/java/com/numinagroup/rdsVoiceGUI/GUI/models/GUIResponse.java)
</td>
<td>message from various input source</td>
</tr>
<tr>
<td>logMessage</td>
<td>string</td>
<td>message string of log (enabled if detailLogging resourceList was enabled)</td>
</tr>
</table>

## APK Configuration File ##

create a file config.txt

populate it with the desired configuration:

_ipAddress default = 192.168.1.240_\
_port default is 10200_\
_cameraScan default = false_\
_useSpeaker default = false_\
_usePickList default = true_\
_detailedLogging default = false_\
_forgetBluetooth default = true_\
_heartbeatInterval default = 1000_

_sensitivity default = 75 (For Keen remove this line)_

we just scan line by line here so needs to match up exactly (ie can't just populate the last value and expect it to decipher), example:

```plaintext
1.1.1.1
1111
false
false
false
true
1000
75
```

file should be placed inside the app folder (ie after the app has been installed). There should be a newline at each end of line

**/storage/emulated/0**/Android/data/com.numinagroup.androidtcpclient/files/config.txt

this bolded location MAY change per device

to test this, you can connect a device via adb and manually copy the file

adb push config.txt /storage/emulated/0/Android/data/com.numinagroup.androidtcpclient/files/config.txt

here is a snippet about that, some other possible values:

<details>
<summary>

1. **/storage/emulated/0/**: The standard path for the internal storage of the primary user.
2. **/storage/self/primary**: A symlink or path that typically points to the primary user's internal storage.
3. **/sdcard/**: A symlink to the internal storage (common in older Android versions).
4. **/storage/{UUID}/**: For devices with multiple users or storage options, they might be accessed via a unique UUID.
5. **/mnt/sdcard/**: Another legacy path that might be used for internal storage.

</summary></details>

Note that we can only access the inner folder here at runtime

**com.numinagroup.androidtcpclient/files/**

So, this file should be placed here via the file step in stagenow, similar to copying the apk to device before installing.

the order should be thus:

1. copy apk to device
2. install apk
3. copy customized config.txt to /storage/emulated/0/Android/data/com.numinagroup.androidtcpclient/files/config.txt
4. run either settings or main program
5. on THE VERY FIRST RUN of either, we will look at this file and use the defaults from there instead of program defaults
6. if the device storage is cleared, it will again trigger

## Datawedge Configuration

With our scanner class, we create a 'numina' profile upon app start, send to datawedge, creating a profile on device 'Numina' with all the scanner settings.

on subsequent runs, if this profile exists we will use it instead of reverting to default settings.

You can delete the profile on device, and it will rebuild the profile with said default settings next run.

This allows unintentional setting by operator, but was designed like that to allow changing settings.

**Note: The pickrate setting is set upon this profile being created**, so to change it you must recreate the profile.

## Resource List ##

This [file](https://gitlab.com/NuminaGroup/numina-shared-library/-/blob/main/app/src/main/java/com/numinagroup/sharedlibrary/serviceObjects/ResourceList.java) passed in by server on start will initiatlize the settings, the grammar, and wake word for the session.

class member names:

- List **resources** - list of the files (grammar named grammarXX.rhn when XX are 0-9, numina.ppn for the wake word, model files for extra languages)
- Locale **locale** - initializes Text To Speech (ensure to have the correct language pack installed)
- Float **volume** - 0 to 1.0 user volume
- Float **rate** - 0.8 to 3.0 user speed
- Float **pitch** -0.8 to 1.8 user pitch
- Float **sensitivity** 0 to 1.0 user picovoice sensitivity
- boolean **scanAhead** scan queue enabled
- boolean **logTimes** log scan/voice times via console on device tab
- String **modelFilename** (default rhino_params.pv (included), need to override for other languages rhino_params_es.pv download [here](https://github.com/Picovoice/rhino/tree/master/lib/common)
- Map<String, String> **translationMap** map of translated strings to ui elements see translation page [here](https://gitlab.com/NuminaGroup/numina-voice-android/-/wikis/Translating-for-other-Languages)

## Keen Resource List ##

This [file](https://gitlab.com/NuminaGroup/numina-shared-library/-/blob/keenVoiceDemo2/app/src/main/java/com/numinagroup/sharedlibrary/serviceObjects/ResourceList.java) passed in by server on start will initiatlize the settings, and recognizers for the session.

[sample json](https://gitlab.com/NuminaGroup/numina-voice-android/-/blob/keenVoiceDemo/app/src/main/assets/sample.json)

ResourceList\
class member names:

* List **resources** - list of the files (grammar named grammarXX.rhn when XX are 0-9, numina.ppn for the wake word, model files for extra languages)
* Locale **locale** - initializes Text To Speech (ensure to have the correct language pack installed)
* Float **volume** - 0 to 1.0 user volume
* Float **rate** - 0.8 to 3.0 user speed
* Float **pitch** -0.8 to 1.8 user pitch
* Float **sensitivity** 0 to 1.0 user picovoice sensitivity
* boolean **scanAhead** scan queue enabled
* boolean **logTimes** log scan/voice times via console on device tab
* String **repeatPhrase** default is "REPEAT". phrase to recognize as local command repeat (may need to change for alternative languages or try something else)
* String **wakeWord** default is "VICTORIA START" <span dir="">phrase to recognize as wake word (may need to change for alternative languages or try something else)</span>
* String **sleepWord** <span dir="">default is "VICTORIA STOP" phrase to recognize as wake word (may need to change for alternative languages or try something else)</span>
* String **terminalPhrase** default is "OK" phrase to recognize end of numeric/alphanumeric input, can be disabled via the booleans:
* boolean **loginUsesTerminalPhrase** <span dir="">default true</span> optionally disable use of terminal phrase on login screen
* boolean **numericUsesTerminalPhrase** <span dir="">default true</span> <span dir="">optionally disable use of terminal phrase on </span>numeric input
* boolean **alphanumericUsesTerminalPhrase** default true <span dir="">optionally disable use of terminal phrase on alphanumeric input</span>
* int **loginGrammarMinDigits** default 3 lower range of login <span dir="">numeric</span> recognizer
* int **loginGrammarMaxDigits** default 4 upper range of login numeric recognizer
* map\<String, Grammar\> **grammarMap** defines the specific grammars (see Grammar description below)
* map\<String, String\> **alphaMap** default nato phonetic defines the alphabet recognizers for alphanumeric input
* map\<String, String\> **numberMap** default english numbers defines the recognizers for numeric input
* Map\<String, String\> **translationMap** map of translated strings to ui elements see translation page [here](https://gitlab.com/NuminaGroup/numina-voice-android/-/wikis/Translating-for-other-Languages)


* float **KASRVadTimeoutEndSilenceForGoodMatch** default 0.5f these sped two sped it up a bit
* float **KASRVadTimeoutEndSilenceForAnyMatch** default 0.5f
* float **KASRVadTimeoutMaxDuration** default 30f haven't used these two but may be of use
* float **KASRVadTimeoutForNoSpeech** default 5f

[Grammar](https://gitlab.com/NuminaGroup/numina-shared-library/-/blob/keenVoiceDemo2/app/src/main/java/com/numinagroup/sharedlibrary/serviceObjects/Grammar.java)

class member names:

* int **minAlphaNumeric** <span dir="">default 0 lower range of alphanumeric recognizer</span>
* int **maxAlphaNumeric** <span dir="">default 0</span> <span dir="">upper range of alphanumeric recognizer</span>
* <span dir="">int **minNumeric** default 0 lower range of login numeric recognizer</span>
* <span dir="">int **maxNumeric** default 0 upper range of login numeric recognizer</span>
* String **key** <span dir="">a unique identifier for this grammar, if it is changed this will indicate it needs to be recreated</span>.
* map\<String, String\> **phraseMap** defines the phrases to recognize \<key, value\> = \<<span dir="">phrase to recognize, code to Return</span>\\>

## Translating for Other Languages

Three things can be configured to support additional languages

**1. TTS Engine configuration**
- in the [ResourceList](https://gitlab.com/NuminaGroup/numina-shared-library/-/blob/main/app/src/main/java/com/numinagroup/sharedlibrary/serviceObjects/ResourceList.java) sent during configuration there is a locale object sent in.
  This should be configured for the TTS engine to play speech in another language.
  eg: Locale.US for English, new Locale("es", "US") for Spanish

**2. Picovoice recognizer model**
for additional languages, send the appropriate filename in ResourceList & file in server directory to be transferred to device.
Here is a [download link](https://github.com/Picovoice/rhino/tree/master/lib/common) for all 8 languages they support,
include in ResourceList 'modelFilename':

- for english, specify the text "rhino_params.pv", this is included by default and does not need to be added to server.  Also is the default filename so no need to specify this text at all.

- for spanish, specify the text "rhino_params_es.pv" and also include this associated file with the spanish recognizers. 
 
 **- need BOTH the new modelfilename in resourcelist and the actual file (when not english)**
- additionally, this needs to be same version as library and grammars created via console (2.1 at this time)

Additionally, you will need create translated grammars and wake word for new language, include in the same format grammar01, grammar02, etc. and numina.ppn wake word

**3. Internal String customization:**
- Also in ResourceList, you can input a dictionary/map of strings to replace:

 ```
    connecting = Connecting…
    info_tab_name = Info
    log_tab_name = Log
    option_tab_name = Option
    camera_scan_button_label = Scan
    dump_logs_button_label = Dump Logs
    last_command_description = Last:
    options_colon = Options:
    pitch_colon = Pitch:
    speed_colon = Speed:
    volume_colon = Volume:
    current_instructions_colon = Current Instructions:
    speech_sensitivity_colon = Speech Sensitivity:
    off = Off
    on = On
    version_colon = Version:

    versionWithNumber = Version: %1$s (just the word Version here, rest is read internally)
```

## PicoVoice Sensitivity

https://picovoice.ai/docs/faq/rhino/

### What’s sensitivity value? What should I set the sensitivity value to? ###

You should pick a [sensitivity](https://picovoice.ai/docs/glossary/#sensitivity) parameter that suits your application's requirements. A higher sensitivity value gives a lower miss rate at the expense of a higher false alarm rate.

In RDS Voice, default is 75

can change in settings activity, but will be trumped by server user settings

upon changing while logged into server, will notify server of new value to save

_Q&A:_

**so 'false alarm' would be a incorrect recognition**

**ie a noise triggering a recognizer for a single syllable word or similar scenario**

**and 'miss rate' would be not understanding something that should have been understood.**

**ie saying an expected word wasn't recognized**

**is this correct?**

_You're correct with the definitions:_

_"False alarm" is an incorrect recognition, ie. a noise accidentally triggering a response or returning a similar but incorrect response._

_"Miss rate" is not understanding something that should have been understood, ie. saying an expected utterance wasn't recognized._

**Also just noticed/remembered that the sensitivity is float.  we have been using integer 0-100 translated to 0.0 - 1.0.   So user has choice to set from 0-100.  Is this too much freedom? (should we restrict to 10 values for .1, .2, etc instead of 100 possible values?** 

_With regards to the sensitivity, 0.0 and 1.0 are inclusive and okay values to use in and as of themselves. Between using 100 or 10 values, you won't find much difference in sensitivity at steps of 100, so 10 might be the better option. It won't cause any issues of course, but noticeable granularity is somewhere around steps of 0.05._