from de.nerdclubtfg.signalbot import SignalInterface as signal

def onMessage(sender, message, group):
    if(message.getBody != None):
        signal.sendMessage(sender, message.getBody().get())
    