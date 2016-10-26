from de.nerdclubtfg.signalbot import SignalInterface as signal
from signalbot.plugin import Plugin

import re

class Echo(Plugin):
    
    pattern = re.compile('^!echo .*')
    
    def onMessage(self, sender, message, group):
        if message.getBody().isPresent():
            signal.sendMessage(sender, message.getBody().get()[6:])
    
    def accepts(self, sender, message, group):
        return message.getBody().isPresent() and self.pattern.match(message.getBody().get())
