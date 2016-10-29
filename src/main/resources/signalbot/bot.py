from de.nerdclubtfg.signalbot import SignalInterface as signal

plugins = []

def onMessage(sender, message, group):
    for plugin in plugins:
        if plugin.enabled and plugin.accepts(sender, message, group):
            try:
                plugin.onMessage(sender, message, group)
            except Exception as e:
                signal.sendMessage(sender, 'Error: %s' % type(e).__name__)
