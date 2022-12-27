# Evolduo Live!

Evolduo live is a version of [evolduo](https://evolduo.cons.gr/) which can run as a standalone software and send the generated music into two
midi channels in real time.

## Requirements

### Software

- `JDK11+`. Grab the current latest from [here](https://www.oracle.com/java/technologies/javase/jdk19-archive-downloads.html)
- `PureData`. Grab the vanilla distrubution from [here](https://puredata.info/downloads/pure-data)
- A Virtual Instrument. For example [Dexed](https://asb2m10.github.io/dexed/) and [Vital](https://vital.audio/)
  are both free and great.
- `Evolduo`. Grab the most recent release from [here](https://github.com/kongeor/evolduo-app/releases)

### Midi Ports

For this experiment you need two midi inputs. This configuration is OS dependent.

For linux users, all you need to do is:

```sh
sudo modprobe -r snd-seq-dummy
sudo modprobe snd-seq-dummy ports=2
```

For OSX, you need to configure the `IAC Driver` as described [here](https://puredata.info/docs/faq/midiinput).

For windows, install [loopMidi](http://www.tobias-erichsen.de/loopMIDI.html) and create
two virtual midi ports. This was tested on Windows 11 and it was working fine.


## Configuration

Launch `PureData`, download the [evolduo.pd](https://raw.githubusercontent.com/kongeor/evolduo-app/main/resources/pd/evolduo.pd) and open.

Make sure nothing is running on ports 3000 and 3001. If you don't know what that means, you are
probably fine. If not, you will get an error when loading the `evolduo.pd` file.

Under `File -> Preferences -> Midi...` select `Use Multiple Devices`. In `Output Devices` set 1 and 2
to `Midi Through Port-0` and `Midi Through Port-1` respectively. On OSX and Windows you will get
slightly different options but you need to do the same thing.

Set your Virtual Instrument to accept data through Midi Port 0. E.g. in `Dexed` it's under
`Options -> Audio/Midi Settings`. You can launch a second instance, or a different instrument,
and make it accept data on Port 1.

Start a command line, I know I know ...

Verify your `Java` version by running

```sh
java -version
```

You should get something like:

```
openjdk version "11.0.17" 2022-10-18
OpenJDK Runtime Environment (build 11.0.17+1)
OpenJDK 64-Bit Server VM (build 11.0.17+1, mixed mode)
```

This is `Java` 11 which means we are good to go.

Launch `Evolduo` by running:

``` 
java -jar evolduo-<version>-standalone.jar live
```

where `<version>` set the version you have downloaded e.g. `0.1.125`.

You should see an ugly, reminiscent of early 2000s UI with a bunch of controls. Try not too look
at it for a long time, just smash the `Start` button and you should experience live music generation.


## What now?

If you know Clojure you can alter the code as the program is running.

If you know PureData you know what to do. If not, you can check [this amazing free book](http://pd-tutorial.com/).


## Know issues

You can experience crashes, errors or other inconsistencies. This is a proof
of concept software so all of that is expected. 

You may have an idea, suggestion or question.

In both cases, don't hesitate to [fill in a ticket](https://github.com/kongeor/evolduo-app/issues).