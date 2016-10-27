# signal-bot

A bot for the Signal messenger. The bot features a python plugin system using Jython.

## Usage

### Install
```shell
git clone git@github.com:nerdclub-tfg/signal-bot.git
cd signal-bot
./gradlew fatJar
java -jar build/libs/signal-bot-all.jar
```
And follow the instructions for registration.
#### Staging vs Production
If possible you should use the staging server for all testing of new plugins and especially for testing of changes to the way
the bot interacts with Signal to prevent problems with your existing installation. However the bot can be run on both servers 
(or your own server, if you got it working).
#### Development Environment
I'm using Eclipse with the gradle plugin preinstalled. For testing you can register the bot as a secondary device to your android client or install it using a different number.

### Configuration
The bot loads a default configuration at startup and copies it to `config.json`. You can edit this file if the bot is not running.

## Adding plugins
The plugins are saved in `src/main/resources/plugins` as python scripts. To add a new plugin
create a new file with the name of your plugin and add the plugin to the default config in 
`de.nerdclub-tfg.signalbot.defaultConfig.json`.

## Contributing
Please create pull request for plugins or changes to the java source code. If you find an issue please use 
the github issue tracker.
