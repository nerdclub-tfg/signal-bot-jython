from signalbot.plugin import Plugin

plugins = []

def onMessage(sender, message, group):
    for plugin in plugins:
        if plugin.enabled and plugin.accepts(sender, message, group):
            plugin.onMessage(sender, message, group)
