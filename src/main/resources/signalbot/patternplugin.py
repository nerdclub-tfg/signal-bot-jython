from signalbot.plugin import Plugin
import re

class PatternPlugin(Plugin):
    
    def __init__(self, enabled, pattern):
        Plugin.__init__(self, enabled)
        self.pattern = re.compile(pattern)
    
    def accepts(self, sender, message, group):
        return message.getBody().isPresent() and self.pattern.match(message.getBody().get())
