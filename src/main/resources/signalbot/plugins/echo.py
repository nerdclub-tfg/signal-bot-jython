from de.nerdclubtfg.signalbot import SignalInterface as signal
from signalbot.patternplugin import PatternPlugin

class Echo(PatternPlugin):
    
    def __init__(self, enabled):
        PatternPlugin.__init__(self, enabled, '^!echo .*')
    
    def onMessage(self, sender, message, group):
        signal.sendMessage(sender, group, message.getBody().get()[6:])
