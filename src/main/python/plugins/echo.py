from de.nerdclubtfg.signalbot import SignalBot

def onMessage(sender, message, group):
    print sender.getNumber()
    print message.getBody()
    if group != None:
        print group.getName()
