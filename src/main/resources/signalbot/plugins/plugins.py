from de.nerdclubtfg.signalbot import SignalInterface as signal
from signalbot.patternplugin import PatternPlugin
from signalbot import bot

class Plugins(PatternPlugin):
    
    def __init__(self, enabled):
        PatternPlugin.__init__(self, enabled, '^!plugins.*')
    
    def onMessage(self, sender, message, group):
        body = message.getBody().get()[9:]
        names = [type(plugin).__name__.lower() for plugin in bot.plugins]
        if body.startswith('enable'):
            if signal.isSudo(sender):
                name = body[7:]
                if name in names:
                    signal.setEnabled(name, True)
                    signal.sendMessage(sender, group, 'Enabled')
                else:
                    signal.sendMessage(sender, group, 'Plugin not installed!')
            else:
                signal.sendMessage(sender, group, 'This command requires sudo!')
        elif body.startswith('disable'):
            if signal.isSudo(sender):
                name = body[8:]
                if name in names:
                    signal.setEnabled(name, False)
                    signal.sendMessage(sender, group, 'Disabled')
                else:
                    signal.sendMessage(sender, group, 'Plugin not installed!')
            else:
                signal.sendMessage(sender, group, 'This command requires sudo!')
        elif len(body) > 0:
            signal.sendMessage(sender, group, 'Unknown command!')
        else:
            s = 'Plugins:\n'
            for name in names:
                s += name + ' '
                if next(v for v in bot.plugins if type(v).__name__.lower() == name).enabled:
                    s += u'\u2714' # check mark
                else:
                    s += u'\u274C' # cross
                s += '\n'
            signal.sendMessage(sender, group, s)

        