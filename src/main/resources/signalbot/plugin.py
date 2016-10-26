class Plugin:

    def __init__(self, enabled):
        self.enabled = enabled
    
    def onMessage(self, sender, message, group):
        pass
    
    def accepts(self, sender, message, group):
        return True
    
    def setEnabled(self, enabled):
        self.enabled = enabled
