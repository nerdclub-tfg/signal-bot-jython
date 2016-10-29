from de.nerdclubtfg.signalbot import SignalInterface as signal
from datetime import datetime

plugins = []

def onMessage(sender, message, group):
    called = []
    for plugin in plugins:
        try:
            if plugin.enabled and plugin.accepts(sender, message, group):
                called.append(type(plugin).__name__)
                plugin.onMessage(sender, message, group)
        except Exception as e:
            signal.sendMessage(sender, 'Error: %s' % type(e).__name__)
    
    print('[%s] %s%s: \"%s\" forwarded to %s' % (
                        timestampToString(message.getTimestamp()),
                        sender.getNumber(), 
                        (' in group %s' % group.getName()) if group != None else '', 
                        message.getBody().get() if message.getBody().isPresent() else 'no body', 
                        called))

def timestampToString(timestamp):
    return datetime.fromtimestamp(timestamp / 1000).strftime('%Y-%m-%d %H:%M:%S')
