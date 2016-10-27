from de.nerdclubtfg.signalbot import SignalInterface as signal
from signalbot.patternplugin import PatternPlugin

class Plugins(PatternPlugin):
    
    def __init__(self, enabled):
        PatternPlugin.__init__(self, enabled, '^!plugins .*')
    
    def onMessage(self, sender, message, group):
        